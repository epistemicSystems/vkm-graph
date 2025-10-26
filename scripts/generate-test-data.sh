#!/bin/bash
# Generate Test Data for VKM Graph
# Creates sample documents that can be uploaded to test the system

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DATA_DIR="$PROJECT_ROOT/data/test-documents"

# Create data directory
mkdir -p "$DATA_DIR"

echo "📝 Generating test documents..."
echo

# Document 1: AI Scaling
cat > "$DATA_DIR/01-ai-scaling.txt" << 'EOF'
Large language models demonstrate emergent capabilities at scale.
Training with more compute improves model performance significantly.
Transformer architectures enable efficient parallel processing.
Scaling laws predict performance across different model sizes.
Larger models require exponentially more training compute.
Model performance correlates with training dataset size.
EOF

echo "✓ Created 01-ai-scaling.txt"

# Document 2: Deep Learning Architectures
cat > "$DATA_DIR/02-architectures.txt" << 'EOF'
Convolutional neural networks excel at image recognition tasks.
Recurrent neural networks process sequential data effectively.
Transformers have become the dominant architecture for language models.
Attention mechanisms allow models to focus on relevant information.
Residual connections help train very deep neural networks.
Batch normalization stabilizes training of deep networks.
EOF

echo "✓ Created 02-architectures.txt"

# Document 3: Training Techniques
cat > "$DATA_DIR/03-training.txt" << 'EOF'
Adam optimizer provides adaptive learning rates for each parameter.
Learning rate schedules improve model convergence.
Gradient clipping prevents exploding gradients during training.
Data augmentation improves model generalization.
Dropout regularization reduces overfitting on training data.
Early stopping prevents overfitting by monitoring validation loss.
Mixed precision training accelerates model training.
EOF

echo "✓ Created 03-training.txt"

# Document 4: Model Evaluation
cat > "$DATA_DIR/04-evaluation.txt" << 'EOF'
Cross-validation provides reliable estimates of model performance.
Test sets must be separate from training data to avoid leakage.
Accuracy is insufficient for imbalanced classification tasks.
F1 score balances precision and recall metrics.
ROC curves visualize trade-offs between true and false positive rates.
Confidence intervals quantify uncertainty in performance estimates.
EOF

echo "✓ Created 04-evaluation.txt"

# Document 5: Deployment Challenges
cat > "$DATA_DIR/05-deployment.txt" << 'EOF'
Model inference latency affects user experience in production.
Quantization reduces model size and inference cost.
Model serving infrastructure must handle variable load.
Monitoring detects model performance degradation over time.
A/B testing validates model improvements in production.
Shadow deployment allows safe testing of new models.
Model versioning enables rollback to previous versions.
EOF

echo "✓ Created 05-deployment.txt"

# Document 6: Emerging Trends
cat > "$DATA_DIR/06-trends.txt" << 'EOF'
Few-shot learning enables models to learn from minimal examples.
Transfer learning leverages pre-trained models for new tasks.
Multi-modal models process text, images, and audio jointly.
Retrieval-augmented generation combines language models with knowledge bases.
Constitutional AI aims to align language models with human values.
Interpretability research explains model decision-making processes.
Efficient architectures reduce computational requirements.
EOF

echo "✓ Created 06-trends.txt"

echo
echo "📊 Generated 6 test documents in: $DATA_DIR"
echo
echo "🚀 Next steps:"
echo "1. Start the backend:  cd core && clj -M:server"
echo "2. Start the frontend: cd viz && npm run dev"
echo "3. Open http://localhost:5173"
echo "4. Drag and drop files from $DATA_DIR"
echo "5. Watch knowledge graph evolution!"
echo
