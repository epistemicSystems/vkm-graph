package cmd

import (
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"time"

	"github.com/kkdai/youtube/v2"
	"github.com/schollz/progressbar/v3"
	"github.com/spf13/cobra"
)

// DownloadCmd represents the download command
var DownloadCmd = &cobra.Command{
	Use:   "download",
	Short: "Download videos from a YouTube channel",
	Long: `Download videos from a YouTube channel for processing.

Videos are downloaded as audio-only (MP3) to minimize storage and
processing time, since we only need audio for transcription.

Example:
  vkm download --channel UCxxx --output data/videos --max-videos 50`,
	RunE: runDownload,
}

var (
	channelID  string
	outputDir  string
	maxVideos  int
	dateFrom   string
	dateTo     string
	audioOnly  bool
)

func init() {
	DownloadCmd.Flags().StringVar(&channelID, "channel", "", "YouTube channel ID (required)")
	DownloadCmd.Flags().StringVar(&outputDir, "output", "data/videos", "Output directory for downloaded videos")
	DownloadCmd.Flags().IntVar(&maxVideos, "max-videos", 50, "Maximum number of videos to download")
	DownloadCmd.Flags().StringVar(&dateFrom, "date-from", "", "Download videos from this date (YYYY-MM-DD)")
	DownloadCmd.Flags().StringVar(&dateTo, "date-to", "", "Download videos until this date (YYYY-MM-DD)")
	DownloadCmd.Flags().BoolVar(&audioOnly, "audio-only", true, "Download audio only (default: true)")

	DownloadCmd.MarkFlagRequired("channel")
}

type VideoMetadata struct {
	VideoID     string    `json:"video_id"`
	Title       string    `json:"title"`
	ChannelID   string    `json:"channel_id"`
	PublishedAt time.Time `json:"published_at"`
	Duration    int       `json:"duration"`
	FilePath    string    `json:"file_path"`
}

func runDownload(cmd *cobra.Command, args []string) error {
	// Create output directory
	if err := os.MkdirAll(outputDir, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	fmt.Printf("Downloading videos from channel: %s\n", channelID)
	fmt.Printf("Output directory: %s\n", outputDir)
	fmt.Printf("Max videos: %d\n", maxVideos)

	// Initialize YouTube client
	client := youtube.Client{}

	// For this example, we'll show how to download a single video
	// In production, you would:
	// 1. Use YouTube Data API to list channel videos
	// 2. Filter by date range
	// 3. Download each video

	// NOTE: This is a simplified example.
	// Full implementation would require YouTube Data API v3 integration
	// to list channel videos, which requires API credentials.

	fmt.Println("\nNOTE: Full channel download requires YouTube Data API v3 credentials.")
	fmt.Println("For now, this is a template showing the download structure.")
	fmt.Println("\nTo implement full channel download:")
	fmt.Println("1. Get YouTube Data API key from Google Cloud Console")
	fmt.Println("2. Use google.golang.org/api/youtube/v3 to list channel videos")
	fmt.Println("3. Filter by date range and download each video")

	// Example: Download a single video if video ID is provided
	if len(args) > 0 {
		videoID := args[0]
		if err := downloadVideo(&client, videoID, outputDir); err != nil {
			return fmt.Errorf("failed to download video %s: %w", videoID, err)
		}
	}

	return nil
}

func downloadVideo(client *youtube.Client, videoID string, outputDir string) error {
	fmt.Printf("\nDownloading video: %s\n", videoID)

	// Get video metadata
	video, err := client.GetVideo(videoID)
	if err != nil {
		return fmt.Errorf("failed to get video metadata: %w", err)
	}

	fmt.Printf("Title: %s\n", video.Title)
	fmt.Printf("Author: %s\n", video.Author)
	fmt.Printf("Duration: %s\n", video.Duration)

	// Get audio stream
	formats := video.Formats.WithAudioChannels()
	if len(formats) == 0 {
		return fmt.Errorf("no audio formats available")
	}

	// Choose format (prefer smallest audio for efficiency)
	format := formats[0]
	for _, f := range formats {
		if f.Bitrate < format.Bitrate {
			format = f
		}
	}

	fmt.Printf("Format: %s (bitrate: %d)\n", format.MimeType, format.Bitrate)

	// Prepare output file
	outputPath := filepath.Join(outputDir, fmt.Sprintf("%s.mp3", videoID))
	file, err := os.Create(outputPath)
	if err != nil {
		return fmt.Errorf("failed to create output file: %w", err)
	}
	defer file.Close()

	// Download stream with progress bar
	stream, size, err := client.GetStream(video, &format)
	if err != nil {
		return fmt.Errorf("failed to get stream: %w", err)
	}
	defer stream.Close()

	bar := progressbar.DefaultBytes(
		size,
		"downloading",
	)

	// Copy stream to file with progress
	_, err = io.Copy(io.MultiWriter(file, bar), stream)
	if err != nil {
		return fmt.Errorf("failed to download stream: %w", err)
	}

	fmt.Printf("\nDownloaded to: %s\n", outputPath)

	// Save metadata
	metadata := VideoMetadata{
		VideoID:     videoID,
		Title:       video.Title,
		ChannelID:   video.Author,
		PublishedAt: video.PublishDate,
		Duration:    int(video.Duration.Seconds()),
		FilePath:    outputPath,
	}

	metadataPath := filepath.Join(outputDir, fmt.Sprintf("%s.json", videoID))
	if err := saveMetadata(metadata, metadataPath); err != nil {
		return fmt.Errorf("failed to save metadata: %w", err)
	}

	return nil
}

func saveMetadata(metadata VideoMetadata, path string) error {
	data, err := json.MarshalIndent(metadata, "", "  ")
	if err != nil {
		return err
	}

	return os.WriteFile(path, data, 0644)
}

// Helper function to download channel videos (template)
func downloadChannelVideos(channelID string, maxVideos int, outputDir string) error {
	// This would require YouTube Data API v3
	// Steps:
	// 1. Initialize YouTube Data API client with credentials
	// 2. List channel uploads playlist
	// 3. Get video IDs from playlist
	// 4. Filter by date range if specified
	// 5. Download each video using downloadVideo()

	return fmt.Errorf("channel download requires YouTube Data API v3 - not implemented in this template")
}
