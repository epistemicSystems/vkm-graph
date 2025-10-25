package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"
)

var (
	pipelineOutputDir string
	pipelineBackendURL string
	pipelineKeepFiles bool
)

// PipelineCmd runs the complete end-to-end pipeline
var PipelineCmd = &cobra.Command{
	Use:   "pipeline [youtube-url...]",
	Short: "Run complete pipeline: download → transcribe → extract → visualize",
	Long: `Run the complete end-to-end pipeline for YouTube videos or playlists.

Steps:
1. Download video(s) using yt-dlp
2. Transcribe audio using OpenAI Whisper
3. Extract facts using Claude API (via backend)
4. Store patches in Datomic (via backend)
5. Ready for visualization

Requires:
  - yt-dlp installed
  - OPENAI_API_KEY for transcription
  - Backend server running (default: http://localhost:3000)
  - Backend configured with CLAUDE_API_KEY

Examples:
  vkm-cli pipeline "https://youtube.com/watch?v=..."
  vkm-cli pipeline "https://youtube.com/playlist?list=..." --keep-files
  vkm-cli pipeline <url> --backend http://my-server:3000`,
	Args: cobra.MinimumNArgs(1),
	RunE: runPipeline,
}

func init() {
	PipelineCmd.Flags().StringVarP(&pipelineOutputDir, "output", "o", "data/pipeline", "Working directory for pipeline files")
	PipelineCmd.Flags().StringVarP(&pipelineBackendURL, "backend", "b", "http://localhost:3000", "Backend API URL")
	PipelineCmd.Flags().BoolVarP(&pipelineKeepFiles, "keep-files", "k", false, "Keep downloaded videos and transcripts after processing")
}

func runPipeline(cmd *cobra.Command, args []string) error {
	// Check prerequisites
	if err := checkPipelinePrerequisites(); err != nil {
		return err
	}

	// Create working directories
	videoDir := filepath.Join(pipelineOutputDir, "videos")
	transcriptDir := filepath.Join(pipelineOutputDir, "transcripts")

	for _, dir := range []string{videoDir, transcriptDir} {
		if err := os.MkdirAll(dir, 0755); err != nil {
			return fmt.Errorf("failed to create directory %s: %w", dir, err)
		}
	}

	fmt.Println("=== VKM Graph Pipeline ===")
	fmt.Printf("Backend: %s\n", pipelineBackendURL)
	fmt.Printf("Working directory: %s\n\n", pipelineOutputDir)

	totalProcessed := 0

	for _, url := range args {
		fmt.Printf("Processing: %s\n", url)

		// Step 1: Download
		fmt.Println("  [1/4] Downloading...")
		if err := downloadVideoForPipeline(url, videoDir); err != nil {
			fmt.Fprintf(os.Stderr, "  ✗ Download failed: %v\n", err)
			continue
		}

		// Find downloaded file
		videoFiles, err := filepath.Glob(filepath.Join(videoDir, "*"))
		if err != nil || len(videoFiles) == 0 {
			fmt.Fprintf(os.Stderr, "  ✗ No video file found\n")
			continue
		}
		videoFile := videoFiles[len(videoFiles)-1] // Get latest
		fmt.Printf("  ✓ Downloaded: %s\n", filepath.Base(videoFile))

		// Step 2: Transcribe
		fmt.Println("  [2/4] Transcribing with Whisper...")
		transcript, err := transcribeForPipeline(videoFile)
		if err != nil {
			fmt.Fprintf(os.Stderr, "  ✗ Transcription failed: %v\n", err)
			if !pipelineKeepFiles {
				os.Remove(videoFile)
			}
			continue
		}

		// Save transcript
		baseName := strings.TrimSuffix(filepath.Base(videoFile), filepath.Ext(videoFile))
		transcriptFile := filepath.Join(transcriptDir, baseName+".txt")
		if err := os.WriteFile(transcriptFile, []byte(transcript), 0644); err != nil {
			fmt.Fprintf(os.Stderr, "  ✗ Failed to save transcript: %v\n", err)
			continue
		}
		fmt.Printf("  ✓ Transcribed: %d characters\n", len(transcript))

		// Step 3: Extract facts via backend
		fmt.Println("  [3/4] Extracting facts with Claude...")
		patchID, factsCount, err := uploadToBackend(transcript, baseName)
		if err != nil {
			fmt.Fprintf(os.Stderr, "  ✗ Fact extraction failed: %v\n", err)
			if !pipelineKeepFiles {
				os.Remove(videoFile)
				os.Remove(transcriptFile)
			}
			continue
		}
		fmt.Printf("  ✓ Extracted: %d facts\n", factsCount)

		// Step 4: Complete
		fmt.Printf("  [4/4] Complete!\n")
		fmt.Printf("  → Patch ID: %s\n", patchID)
		fmt.Printf("  → View at: http://localhost:5173 (switch to 'Backend Data')\n\n")

		// Cleanup if not keeping files
		if !pipelineKeepFiles {
			os.Remove(videoFile)
			os.Remove(transcriptFile)
		}

		totalProcessed++
	}

	fmt.Printf("=== Pipeline Complete ===\n")
	fmt.Printf("Successfully processed: %d/%d\n", totalProcessed, len(args))

	if pipelineKeepFiles {
		fmt.Printf("Files saved to: %s\n", pipelineOutputDir)
	}

	return nil
}

func checkPipelinePrerequisites() error {
	// Check yt-dlp
	if !commandExists("yt-dlp") {
		return fmt.Errorf("yt-dlp not found. Install with: pip install yt-dlp")
	}

	// Check OpenAI API key
	if os.Getenv("OPENAI_API_KEY") == "" {
		return fmt.Errorf("OPENAI_API_KEY environment variable not set")
	}

	// Check backend health
	resp, err := http.Get(pipelineBackendURL + "/health")
	if err != nil {
		return fmt.Errorf("backend not reachable at %s: %w", pipelineBackendURL, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("backend health check failed (status %d)", resp.StatusCode)
	}

	return nil
}

func downloadVideoForPipeline(url, outputDir string) error {
	return downloadVideoWithYtDlp(url, outputDir)
}

func transcribeForPipeline(videoFile string) (string, error) {
	apiKey := os.Getenv("OPENAI_API_KEY")
	return transcribeWithWhisper(videoFile, apiKey)
}

func uploadToBackend(content, filename string) (patchID string, factsCount int, err error) {
	reqBody, err := json.Marshal(map[string]string{
		"content":  content,
		"filename": filename,
	})
	if err != nil {
		return "", 0, fmt.Errorf("failed to marshal request: %w", err)
	}

	resp, err := http.Post(
		pipelineBackendURL+"/api/upload",
		"application/json",
		bytes.NewReader(reqBody),
	)
	if err != nil {
		return "", 0, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", 0, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return "", 0, fmt.Errorf("backend error (status %d): %s", resp.StatusCode, string(body))
	}

	var result struct {
		PatchID    string `json:"patch-id"`
		FactsCount int    `json:"facts-count"`
		Message    string `json:"message"`
	}

	if err := json.Unmarshal(body, &result); err != nil {
		return "", 0, fmt.Errorf("failed to parse response: %w", err)
	}

	return result.PatchID, result.FactsCount, nil
}
