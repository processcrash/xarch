# ===============================================
# go-ai-agent-base Makefile
# ===============================================
# Common targets: build, test, lint, run, clean, docker
# Use `make help` to list available targets.

GO ?= go
BIN_DIR ?= bin
LDFLAGS ?= -s -w -X main.version=$(shell git describe --tags --always --dirty 2>/dev/null || echo dev)

# Use the first value of GOOS/GOARCH when cross-building.
PLATFORMS ?= linux/amd64 linux/arm64 darwin/amd64 darwin/arm64 windows/amd64

.PHONY: help
help: ## Show this help message
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*?##/ {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.PHONY: deps
deps: ## Download Go module dependencies
	$(GO) mod download
	$(GO) mod tidy

.PHONY: build
build: ## Build all binaries into bin/
	mkdir -p $(BIN_DIR)
	$(GO) build -ldflags="$(LDFLAGS)" -o $(BIN_DIR)/agent ./cmd/agent
	$(GO) build -ldflags="$(LDFLAGS)" -o $(BIN_DIR)/server ./cmd/server

.PHONY: build-all
build-all: ## Cross-compile for all supported platforms
	@for platform in $(PLATFORMS); do \
		os=$$(echo $$platform | cut -d/ -f1); arch=$$(echo $$platform | cut -d/ -f2); \
		output=$(BIN_DIR)/agent-$$os-$$arch; \
		echo "Building $$platform -> $$output"; \
		GOOS=$$os GOARCH=$$arch $(GO) build -ldflags="$(LDFLAGS)" -o $$output ./cmd/agent; \
		GOOS=$$os GOARCH=$$arch $(GO) build -ldflags="$(LDFLAGS)" -o $(BIN_DIR)/server-$$os-$$arch ./cmd/server; \
	done

.PHONY: test
test: ## Run unit tests
	$(GO) test -race -count=1 ./...

.PHONY: test-integration
test-integration: ## Run integration tests (requires Docker)
	$(GO) test -race -count=1 -tags=integration ./tests/integration/...

.PHONY: coverage
coverage: ## Run tests with coverage report
	$(GO) test -race -coverprofile=coverage.out -count=1 ./...
	$(GO) tool cover -html=coverage.out -o coverage.html
	@echo "Coverage report: coverage.html"

.PHONY: lint
lint: ## Run golangci-lint
	golangci-lint run ./...

.PHONY: fmt
fmt: ## Run gofmt + goimports
	gofmt -s -w .
	goimports -w .

.PHONY: vet
vet: ## Run go vet
	$(GO) vet ./...

.PHONY: run-agent
run-agent: build ## Run the CLI agent
	./$(BIN_DIR)/agent chat

.PHONY: run-server
run-server: build ## Run the HTTP server
	./$(BIN_DIR)/server

.PHONY: docker-build
docker-build: ## Build Docker image
	docker build -t go-ai-agent-base:latest -f deployments/docker/Dockerfile .

.PHONY: docker-run
docker-run: docker-build ## Build and run Docker image
	docker run --rm -p 8080:8080 go-ai-agent-base:latest

.PHONY: compose-up
compose-up: ## Start docker-compose dev stack
	docker compose -f deployments/docker/docker-compose.yml up -d

.PHONY: compose-down
compose-down: ## Stop docker-compose dev stack
	docker compose -f deployments/docker/docker-compose.yml down

.PHONY: clean
clean: ## Remove build artifacts
	rm -rf $(BIN_DIR) coverage.out coverage.html

.PHONY: tidy
tidy: ## Run go mod tidy
	$(GO) mod tidy

.PHONY: install-tools
install-tools: ## Install dev tools
	$(GO) install github.com/golangci/golangci-lint/cmd/golangci-lint@latest
	$(GO) install golang.org/x/tools/cmd/goimports@latest

.DEFAULT_GOAL := help