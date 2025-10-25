package cmd

import (
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"
)

// TranscribeCmd represents the transcribe command
var TranscribeCmd = &cobra.Command{
	Use:   "transcribe",
	Short: "Transcribe audio files using Whisper",
	Long: `Transcribe audio files using OpenAI's Whisper model.

Requires whisper to be installed:
  pip install openai-whisper

Example:
  vkm transcribe --input data/videos --output data/transcripts --model base`,
	RunE: runTranscribe,
}

var (
	inputDir    string
	transcriptOutputDir string
	whisperModel string
	language    string
	device      string
)

func init() {
	TranscribeCmd.Flags().StringVar(&inputDir, "input", "data/videos", "Input directory with audio files")
	TranscribeCmd.Flags().StringVar(&transcriptOutputDir, "output", "data/transcripts", "Output directory for transcripts")
	TranscribeCmd.Flags().StringVar(&whisperModel, "model", "base", "Whisper model size (tiny, base, small, medium, large)")
	TranscribeCmd.Flags().StringVar(&language, "language", "en", "Language code (default: en)")
	TranscribeCmd.Flags().StringVar(&device, "device", "cpu", "Device to use (cpu or cuda)")
}

type TranscriptSegment struct {
	Timestamp float64 `json:"timestamp"`
	Text      string  `json:"text"`
	Duration  float64 `json:"duration"`
}

type Transcript struct {
	VideoID    string               `json:"video_id"`
	Title      string               `json:"title"`
	PublishedAt string              `json:"published_at"`
	Transcript  []TranscriptSegment `json:"transcript"`
}

func runTranscribe(cmd *cobra.Command, args []string) error {
	// Create output directory
	if err := os.MkdirAll(transcriptOutputDir, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	// Check if whisper is installed
	if err := checkWhisperInstalled(); err != nil {
		return err
	}

	fmt.Printf("Transcribing files from: %s\n", inputDir)
	fmt.Printf("Output directory: %s\n", transcriptOutputDir)
	fmt.Printf("Whisper model: %s\n", whisperModel)

	// Find all audio files
	files, err := findAudioFiles(inputDir)
	if err != nil {
		return fmt.Errorf("failed to find audio files: %w", err)
	}

	fmt.Printf("Found %d audio files\n\n", len(files))

	// Transcribe each file
	for i, file := range files {
		fmt.Printf("[%d/%d] Transcribing: %s\n", i+1, len(files), filepath.Base(file))

		if err := transcribeFile(file, transcriptOutputDir); err != nil {
			fmt.Fprintf(os.Stderr, "Warning: Failed to transcribe %s: %v\n", file, err)
			continue
		}

		fmt.Printf("✓ Completed\n\n")
	}

	fmt.Println("Transcription complete!")
	return nil
}

func checkWhisperInstalled() error {
	cmd := exec.Command("whisper", "--help")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("whisper not found - please install with: pip install openai-whisper")
	}
	return nil
}

func findAudioFiles(dir string) ([]string, error) {
	var files []string

	err := filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		if !info.IsDir() {
			ext := strings.ToLower(filepath.Ext(path))
			if ext == ".mp3" || ext == ".wav" || ext == ".m4a" || ext == ".mp4" {
				files = append(files, path)
			}
		}

		return nil
	})

	return files, err
}

func transcribeFile(audioPath string, outputDir string) error {
	// Get base name without extension
	baseName := strings.TrimSuffix(filepath.Base(audioPath), filepath.Ext(audioPath))

	// Output paths
	tempOutputDir := filepath.Join(outputDir, "temp")
	os.MkdirAll(tempOutputDir, 0755)

	// Run whisper
	args := []string{
		audioPath,
		"--model", whisperModel,
		"--language", language,
		"--output_format", "json",
		"--output_dir", tempOutputDir,
		"--device", device,
	}

	cmd := exec.Command("whisper", args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("whisper command failed: %w", err)
	}

	// Parse whisper output
	whisperOutputPath := filepath.Join(tempOutputDir, baseName+".json")
	whisperOutput, err := os.ReadFile(whisperOutputPath)
	if err != nil {
		return fmt.Errorf("failed to read whisper output: %w", err)
	}

	// Parse JSON
	var whisperData struct {
		Text     string `json:"text"`
		Segments []struct {
			Start float64 `json:"start"`
			End   float64 `json:"end"`
			Text  string  `json:"text"`
		} `json:"segments"`
	}

	if err := json.Unmarshal(whisperOutput, &whisperData); err != nil {
		return fmt.Errorf("failed to parse whisper output: %w", err)
	}

	// Convert to our transcript format
	transcript := Transcript{
		VideoID: baseName,
		Title:   baseName,
		Transcript: make([]TranscriptSegment, len(whisperData.Segments)),
	}

	for i, seg := range whisperData.Segments {
		transcript.Transcript[i] = TranscriptSegment{
			Timestamp: seg.Start,
			Text:      strings.TrimSpace(seg.Text),
			Duration:  seg.End - seg.Start,
		}
	}

	// Save our transcript format
	outputPath := filepath.Join(outputDir, baseName+".json")
	data, err := json.MarshalIndent(transcript, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal transcript: %w", err)
	}

	if err := os.WriteFile(outputPath, data, 0644); err != nil {
		return fmt.Errorf("failed to write transcript: %w", err)
	}

	// Clean up temp file
	os.Remove(whisperOutputPath)

	return nil
}
