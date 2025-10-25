package main

import (
	"fmt"
	"os"

	"github.com/epistemicSystems/vkm-graph/cli/cmd"
	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "vkm",
	Short: "VKM Graph CLI - Knowledge Graph Evolution System",
	Long: `VKM Graph CLI is a tool for downloading, transcribing, and processing
knowledge sources (YouTube videos) for the Knowledge Graph Evolution System.

The system treats knowledge patches as points in a moduli stack, with commits
as morphisms that trace understanding evolution over time.`,
	Version: "0.1.0",
}

func init() {
	// Add subcommands
	rootCmd.AddCommand(cmd.DownloadCmd)
	rootCmd.AddCommand(cmd.TranscribeCmd)
	rootCmd.AddCommand(cmd.ProcessCmd)
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}
}
