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

// DownloadSimpleCmd downloads videos using yt-dlp
var DownloadSimpleCmd = &cobra.Command{
	Use:   "download-simple [video-urls...]",
	Short: "Download videos using yt-dlp",
	Long: `Download YouTube videos using yt-dlp (must be installed).

This is a simplified download that works with direct video URLs.
For bulk channel downloads, use download --channel.

Requirements:
  - yt-dlp installed: pip install yt-dlp
  - ffmpeg installed: brew install ffmpeg (or apt install ffmpeg)

Examples:
  # Single video
  vkm download-simple https://youtube.com/watch?v=abc123

  # Multiple videos
  vkm download-simple https://youtube.com/watch?v=abc123 https://youtube.com/watch?v=def456

  # With custom output directory
  vkm download-simple --output ./my-videos https://youtube.com/watch?v=abc123`,
	RunE: runDownloadSimple,
}

var (
	simpleOutputDir string
	audioFormat     string
)

func init() {
	DownloadSimpleCmd.Flags().StringVarP(&simpleOutputDir, "output", "o", "data/videos", "Output directory")
	DownloadSimpleCmd.Flags().StringVar(&audioFormat, "format", "mp3", "Audio format (mp3, wav, m4a)")
}

func runDownloadSimple(cmd *cobra.Command, args []string) error {
	if len(args) == 0 {
		return fmt.Errorf("no video URLs provided")
	}

	// Check if yt-dlp is installed
	if err := checkYtDlpInstalled(); err != nil {
		return err
	}

	// Create output directory
	if err := os.MkdirAll(simpleOutputDir, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	fmt.Printf("Downloading %d video(s) to %s\n\n", len(args), simpleOutputDir)

	for i, url := range args {
		fmt.Printf("[%d/%d] Downloading: %s\n", i+1, len(args), url)

		if err := downloadVideoWithYtDlp(url, simpleOutputDir); err != nil {
			fmt.Fprintf(os.Stderr, "Warning: Failed to download %s: %v\n", url, err)
			continue
		}

		fmt.Printf("✓ Downloaded successfully\n\n")
	}

	fmt.Println("Download complete!")
	fmt.Printf("Videos saved to: %s\n", simpleOutputDir)
	fmt.Println("\nNext step: Transcribe the videos")
	fmt.Printf("  vkm transcribe --input %s --output data/transcripts\n", simpleOutputDir)

	return nil
}

func checkYtDlpInstalled() error {
	cmd := exec.Command("yt-dlp", "--version")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("yt-dlp not found. Install with: pip install yt-dlp")
	}
	return nil
}

func downloadVideoWithYtDlp(url string, outputDir string) error {
	// Download audio only in specified format
	outputTemplate := filepath.Join(outputDir, "%(id)s.%(ext)s")

	args := []string{
		"--extract-audio",
		"--audio-format", audioFormat,
		"--output", outputTemplate,
		"--write-info-json", // Save metadata
		"--no-playlist",     // Don't download playlists
		"--quiet",           // Suppress most output
		"--progress",        // Show progress
		url,
	}

	cmd := exec.Command("yt-dlp", args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	return cmd.Run()
}

// DownloadPlaylistCmd downloads a full playlist
var DownloadPlaylistCmd = &cobra.Command{
	Use:   "download-playlist [playlist-url]",
	Short: "Download full YouTube playlist",
	Long: `Download all videos from a YouTube playlist.

Requirements: yt-dlp installed

Example:
  vkm download-playlist https://youtube.com/playlist?list=PLxxx`,
	RunE: runDownloadPlaylist,
}

var (
	playlistOutputDir string
	playlistMaxVideos int
)

func init() {
	DownloadPlaylistCmd.Flags().StringVarP(&playlistOutputDir, "output", "o", "data/videos", "Output directory")
	DownloadPlaylistCmd.Flags().IntVar(&playlistMaxVideos, "max-videos", 50, "Maximum videos to download")
}

func runDownloadPlaylist(cmd *cobra.Command, args []string) error {
	if len(args) == 0 {
		return fmt.Errorf("no playlist URL provided")
	}

	playlistURL := args[0]

	// Check if yt-dlp is installed
	if err := checkYtDlpInstalled(); err != nil {
		return err
	}

	// Create output directory
	if err := os.MkdirAll(playlistOutputDir, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	fmt.Printf("Downloading playlist: %s\n", playlistURL)
	fmt.Printf("Output directory: %s\n", playlistOutputDir)
	fmt.Printf("Max videos: %d\n\n", playlistMaxVideos)

	outputTemplate := filepath.Join(playlistOutputDir, "%(playlist_index)s-%(id)s.%(ext)s")

	args = []string{
		"--extract-audio",
		"--audio-format", audioFormat,
		"--output", outputTemplate,
		"--write-info-json",
		"--max-downloads", fmt.Sprintf("%d", playlistMaxVideos),
		"--yes-playlist",
		playlistURL,
	}

	dlCmd := exec.Command("yt-dlp", args...)
	dlCmd.Stdout = os.Stdout
	dlCmd.Stderr = os.Stderr

	if err := dlCmd.Run(); err != nil {
		return fmt.Errorf("download failed: %w", err)
	}

	fmt.Println("\nPlaylist download complete!")
	fmt.Printf("Videos saved to: %s\n", playlistOutputDir)

	return nil
}

// Helper to extract video metadata from info.json
func loadVideoMetadata(infoJsonPath string) (map[string]interface{}, error) {
	data, err := os.ReadFile(infoJsonPath)
	if err != nil {
		return nil, err
	}

	var metadata map[string]interface{}
	if err := json.Unmarshal(data, &metadata); err != nil {
		return nil, err
	}

	return metadata, nil
}

// GetVideoInfo extracts useful metadata
func GetVideoInfo(videoID string, videosDir string) (map[string]interface{}, error) {
	// Find the info.json file
	infoPath := filepath.Join(videosDir, videoID+".info.json")

	if _, err := os.Stat(infoPath); os.IsNotExist(err) {
		// Try to find it by globbing
		pattern := filepath.Join(videosDir, "*"+videoID+"*.info.json")
		matches, _ := filepath.Glob(pattern)
		if len(matches) > 0 {
			infoPath = matches[0]
		} else {
			return nil, fmt.Errorf("metadata not found for video %s", videoID)
		}
	}

	return loadVideoMetadata(infoPath)
}

// ListDownloadedVideos lists all downloaded videos in a directory
func ListDownloadedVideos(dir string) ([]string, error) {
	var videos []string

	// Find all .mp3 files (or other audio formats)
	patterns := []string{"*.mp3", "*.wav", "*.m4a"}

	for _, pattern := range patterns {
		matches, err := filepath.Glob(filepath.Join(dir, pattern))
		if err != nil {
			continue
		}
		videos = append(videos, matches...)
	}

	return videos, nil
}

// CleanFilename removes problematic characters from filenames
func CleanFilename(name string) string {
	// Replace problematic characters
	name = strings.ReplaceAll(name, "/", "-")
	name = strings.ReplaceAll(name, "\\", "-")
	name = strings.ReplaceAll(name, ":", "-")
	name = strings.ReplaceAll(name, "*", "-")
	name = strings.ReplaceAll(name, "?", "-")
	name = strings.ReplaceAll(name, "\"", "-")
	name = strings.ReplaceAll(name, "<", "-")
	name = strings.ReplaceAll(name, ">", "-")
	name = strings.ReplaceAll(name, "|", "-")

	return name
}
