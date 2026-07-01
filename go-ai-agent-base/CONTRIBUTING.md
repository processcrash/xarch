# Contributing to go-ai-agent-base

> "Discuss before code" — let's agree on the approach before writing PRs.

## Quick links

- 💡 [Discussions](https://github.com/xarch/go-ai-agent-base/discussions)
- 🐛 [Issues](https://github.com/xarch/go-ai-agent-base/issues)
- 📜 [Code of Conduct](CODE_OF_CONDUCT.md)

## Workflow

1. 🔍 Search existing issues / discussions before opening a new one
2. 💬 Open an issue or discussion describing the change
3. ✅ Get a `ready-for-pr` label from a maintainer
4. 🚀 Open a PR linking the issue
5. 🔁 Iterate on review
6. 🎉 Merge!

## Development setup

Prerequisites:
- Go 1.23+
- Docker (for integration tests)
- `golangci-lint` (`go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest`)

```bash
git clone https://github.com/xarch/go-ai-agent-base
cd go-ai-agent-base
go mod download
make test
```

## Code conventions

- All exported functions have GoDoc comments
- Use `errors.Is` / `errors.As` for error checks
- Wrap errors with `fmt.Errorf("...: %w", err)` — never swallow them
- Use `context.Context` as the first parameter of all long-running calls
- Prefer stdlib `log/slog` over third-party loggers in library code (Zap is fine in `cmd/`)
- Tests use stretchr/testify; integration tests use testcontainers-go
- Lint: `golangci-lint run` (config: `.golangci.yml`)
- Format: `gofmt -s -w .` and `goimports -w .`

## Commit messages

Conventional Commits:
- `feat: add Anthropic provider`
- `fix(session): handle Redis disconnect`
- `docs: add MCP integration guide`
- `chore: bump adk-go to v0.3.0`
- `test: add e2e for streaming endpoint`

## Release process

1. Tag: `git tag v0.x.0`
2. Push: `git push origin v0.x.0`
3. CI builds cross-platform binaries
4. GitHub Release published automatically with changelog

## Triaging (maintainers)

- 7-day first response SLA
- Label with `area:*`, `priority:*`, `status:*`, `type:*`
- See xarch's ISSUE_WORKFLOW.md for the full playbook

---

Thanks for contributing! 🚀