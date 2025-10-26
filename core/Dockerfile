# VKM Graph Backend - Clojure API Server
FROM clojure:temurin-21-tools-deps-alpine

# Set working directory
WORKDIR /app

# Copy project files
COPY deps.edn .
COPY src/ ./src/
COPY resources/ ./resources/

# Download dependencies
RUN clj -P -M:server

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

# Start server
CMD ["clj", "-M:server"]
