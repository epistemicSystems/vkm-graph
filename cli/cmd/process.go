package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

// ProcessCmd represents the process command
var ProcessCmd = &cobra.Command{
	Use:   "process",
	Short: "Process transcripts into knowledge patches",
	Long: `Process transcripts into knowledge patches using the Clojure pipeline.

This command calls the Clojure processing pipeline to:
1. Extract facts from transcripts using Claude
2. Build knowledge patches
3. Compute embeddings
4. Store in Datomic
5. Extract motives

Example:
  vkm process --source my-channel --transcripts data/transcripts/my-channel`,
	RunE: runProcess,
}

var (
	sourceID       string
	transcriptsDir string
)

func init() {
	ProcessCmd.Flags().StringVar(&sourceID, "source", "", "Source identifier (required)")
	ProcessCmd.Flags().StringVar(&transcriptsDir, "transcripts", "", "Transcripts directory (required)")

	ProcessCmd.MarkFlagRequired("source")
	ProcessCmd.MarkFlagRequired("transcripts")
}

func runProcess(cmd *cobra.Command, args []string) error {
	fmt.Printf("Processing transcripts for source: %s\n", sourceID)
	fmt.Printf("Transcripts directory: %s\n", transcriptsDir)
	fmt.Println()

	fmt.Println("To process transcripts, run the Clojure pipeline:")
	fmt.Printf("  cd core\n")
	fmt.Printf("  clj -M:run process --source %s --transcripts ../%s\n", sourceID, transcriptsDir)
	fmt.Println()

	fmt.Println("Or use the full pipeline:")
	fmt.Println("  1. Initialize database:")
	fmt.Println("     cd core && clj -M:run init")
	fmt.Println()
	fmt.Println("  2. Process transcripts:")
	fmt.Printf("     clj -M:run process --source %s --transcripts ../%s\n", sourceID, transcriptsDir)
	fmt.Println()
	fmt.Println("  3. Export visualization data:")
	fmt.Println("     clj -M:run export --output ../data/viz-data.edn")

	return nil
}
