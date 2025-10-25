package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/spf13/cobra"
)

var (
	transcribeOutputDir string
	whisperModel        string
	whisperLanguage     string
)

// TranscribeWhisperCmd transcribes audio/video files using OpenAI Whisper API
var TranscribeWhisperCmd = &cobra.Command{
	Use:   "transcribe-whisper [file...]",
	Short: "Transcribe audio/video files using OpenAI Whisper API",
	Long: `Transcribe audio or video files using OpenAI's Whisper API.

Requires OPENAI_API_KEY environment variable to be set.

Supported formats: mp3, mp4, mpeg, mpga, m4a, wav, webm

Examples:
  vkm-cli transcribe-whisper video.mp4
  vkm-cli transcribe-whisper *.mp3 --output transcripts/
  vkm-cli transcribe-whisper audio.mp3 --model whisper-1 --language en`,
	Args: cobra.MinimumNArgs(1),
	RunE: runTranscribeWhisper,
}

func init() {
	TranscribeWhisperCmd.Flags().StringVarP(&transcribeOutputDir, "output", "o", "data/transcripts", "Output directory for transcripts")
	TranscribeWhisperCmd.Flags().StringVarP(&whisperModel, "model", "m", "whisper-1", "Whisper model to use")
	TranscribeWhisperCmd.Flags().StringVarP(&whisperLanguage, "language", "l", "", "Audio language (optional, auto-detected if not specified)")
}

type WhisperResponse struct {
	Text string `json:"text"`
}

func runTranscribeWhisper(cmd *cobra.Command, args []string) error {
	apiKey := os.Getenv("OPENAI_API_KEY")
	if apiKey == "" {
		return fmt.Errorf("OPENAI_API_KEY environment variable not set")
	}

	// Create output directory
	if err := os.MkdirAll(transcribeOutputDir, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	fmt.Printf("Transcribing %d file(s)...\n", len(args))

	successCount := 0
	for i, filePath := range args {
		fmt.Printf("[%d/%d] Transcribing: %s\n", i+1, len(args), filePath)

		transcript, err := transcribeWithWhisper(filePath, apiKey)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error transcribing %s: %v\n", filePath, err)
			continue
		}

		// Save transcript
		baseName := filepath.Base(filePath)
		outputName := strings.TrimSuffix(baseName, filepath.Ext(baseName)) + ".txt"
		outputPath := filepath.Join(transcribeOutputDir, outputName)

		if err := os.WriteFile(outputPath, []byte(transcript), 0644); err != nil {
			fmt.Fprintf(os.Stderr, "Error saving transcript %s: %v\n", outputPath, err)
			continue
		}

		fmt.Printf("  ✓ Saved to: %s\n", outputPath)
		successCount++
	}

	fmt.Printf("\nCompleted: %d/%d transcriptions successful\n", successCount, len(args))

	return nil
}

func transcribeWithWhisper(filePath, apiKey string) (string, error) {
	// Open the file
	file, err := os.Open(filePath)
	if err != nil {
		return "", fmt.Errorf("failed to open file: %w", err)
	}
	defer file.Close()

	// Check file size (Whisper has 25MB limit)
	fileInfo, err := file.Stat()
	if err != nil {
		return "", fmt.Errorf("failed to stat file: %w", err)
	}
	const maxSize = 25 * 1024 * 1024 // 25MB
	if fileInfo.Size() > maxSize {
		return "", fmt.Errorf("file size %d bytes exceeds Whisper API limit of 25MB", fileInfo.Size())
	}

	// Create multipart form
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	// Add file
	part, err := writer.CreateFormFile("file", filepath.Base(filePath))
	if err != nil {
		return "", fmt.Errorf("failed to create form file: %w", err)
	}
	if _, err := io.Copy(part, file); err != nil {
		return "", fmt.Errorf("failed to copy file: %w", err)
	}

	// Add model
	if err := writer.WriteField("model", whisperModel); err != nil {
		return "", fmt.Errorf("failed to write model field: %w", err)
	}

	// Add language if specified
	if whisperLanguage != "" {
		if err := writer.WriteField("language", whisperLanguage); err != nil {
			return "", fmt.Errorf("failed to write language field: %w", err)
		}
	}

	// Add response format
	if err := writer.WriteField("response_format", "json"); err != nil {
		return "", fmt.Errorf("failed to write response_format field: %w", err)
	}

	if err := writer.Close(); err != nil {
		return "", fmt.Errorf("failed to close writer: %w", err)
	}

	// Create HTTP request
	req, err := http.NewRequest("POST", "https://api.openai.com/v1/audio/transcriptions", &body)
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+apiKey)
	req.Header.Set("Content-Type", writer.FormDataContentType())

	// Send request with timeout
	client := &http.Client{
		Timeout: 5 * time.Minute, // Whisper can take a while
	}

	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	// Read response
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("API error (status %d): %s", resp.StatusCode, string(respBody))
	}

	// Parse response
	var whisperResp WhisperResponse
	if err := json.Unmarshal(respBody, &whisperResp); err != nil {
		return "", fmt.Errorf("failed to parse response: %w", err)
	}

	return whisperResp.Text, nil
}
