# Contributing to xarch

Thank you for your interest in contributing to **xarch** — AI-Enabled
Enterprise Backend Framework. This guide covers everything you need to
get a pull request merged: setup, conventions, testing, and release
processes.

> By participating in this project you agree to abide by our
> [Code of Conduct](CODE_OF_CONDUCT.md).

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Help](#getting-help)
3. [Reporting Bugs](#reporting-bugs)
4. [Suggesting Features](#suggesting-features)
5. [Development Setup](#development-setup)
6. [Building the Project](#building-the-project)
7. [Testing](#testing)
8. [Coding Standards](#coding-standards)
9. [Commit Message Convention](#commit-message-convention)
10. [Branch Strategy](#branch-strategy)
11. [Pull Request Process](#pull-request-process)
12. [Release Process](#release-process)

---

## Code of Conduct

This project follows the [Contributor Covenant 2.1](CODE_OF_CONDUCT.md).
Be respectful, assume good faith, and focus on the technical merit of
contributions. Unacceptable behavior can be reported to
`conduct@xarch.example`.

---

## Getting Help

- **Questions / how-to**: Open a GitHub Discussion in the
  *Q&A* category. Do **not** file an issue for usage questions.
- **Bugs / defects**: Use the *Bug Report* issue template.
- **Security issues**: Follow [SECURITY.md](SECURITY.md) — **do not**
  open a public issue.
- **Chat / community**: see the project README for current channels.

---

## Reporting Bugs

Use the bug report template and include:

- **Environment**: OS, JDK version (`java -version`), Node version
  (`node --version`), Python version (`python --version`).
- **Reproducible steps** — exact commands, configuration, and inputs.
- **Expected vs actual behavior**.
- **Logs / stack traces** — wrap long output in triple-backtick blocks.
- **Minimal reproducer** if possible (a small project that fails).

Before filing, please:

1. Check the issue tracker for duplicates.
2. Confirm the bug exists on `main` (not just an old release).
3. Try the latest patch release.

---

## Suggesting Features

Open a GitHub Discussion in the *Ideas* category first. Large changes
should be preceded by an **RFC** discussion that captures motivation,
alternatives considered, and a sketch of the API. The maintainers will
signal acceptance by moving the discussion into the project roadmap.

---

## Development Setup

### Required Toolchain

| Tool | Version | Notes |
|------|---------|-------|
| JDK | **25** (LTS) | Records, sealed types, virtual threads |
| Node.js | **20.x** LTS | Frontend and Node MCP servers |
| Bun | **1.x** | Optional, recommended for Node MCP servers |
| Python | **3.10+** | Python MCP servers |
| Docker | **24+** | Local stack (Postgres, Redis, Nacos) |
| Docker Compose | **v2** | Local orchestration |
| Gradle | shipped wrapper | `./gradlew` (no global install) |
| kustomize | **5.x** | Optional, for K8s overlays |

### Installing JDK 25

**Using SDKMAN (recommended):**

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25-tem
sdk use java 25-tem
```

**Direct install (Windows):**

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
# or download from https://adoptium.net/
```

Verify with `java -version` — output should start with `openjdk "25`.

### Installing Node.js 20

```bash
# Using nvm (recommended)
nvm install 20
nvm use 20

# Or download from https://nodejs.org/
```

### Installing Python 3.10+

```bash
# Windows
winget install Python.Python.3.12

# macOS
brew install python@3.12

# Linux
sudo apt install python3.12 python3.12-venv python3-pip
```

### Installing Bun (Optional)

```bash
curl -fsSL https://bun.sh/install | bash
```

### IDE Setup

**IntelliJ IDEA (recommended for backend):**

- Install *Lombok* and *MapStruct Support* plugins.
- Enable annotation processing: *Settings > Build > Compiler >
  Annotation Processors > Enable*.
- Set Project SDK to **25** and language level to **25**.
- Install the *Kotlin* plugin (used by Gradle DSL).

**VS Code (recommended for frontend / Node MCP):**

- Install extensions: *Vue Language Features (Volar)*, *TypeScript*,
  *ESLint*, *Prettier*, *Java Extension Pack*.
- Workspace settings: enable *Format On Save*.

---

## Building the Project

### Backend (Gradle)

```bash
cd backend
./gradlew clean build
```

To skip tests:

```bash
./gradlew build -x test
```

To run the example app:

```bash
cd backend/xarch-example
./gradlew bootRun
```

### Frontend (Vue 3 Admin)

```bash
cd vue3-admin
npm install        # or: pnpm install / bun install
npm run build      # produces dist/
npm run dev        # dev server with HMR
```

### Python MCP Servers

Each package is installable in editable mode:

```bash
cd py-mcp-servers/database_mcp
pip install -e ".[dev]"

cd ../knowledge_mcp
pip install -e ".[dev]"

cd ../filesystem_mcp
pip install -e ".[dev]"
```

The standalone `python/vector_mcp` package follows the same pattern.

### Node MCP Servers

```bash
cd node-mcp-servers/database-mcp
npm install
npm run build
npm start          # node dist/index.js
# or: bun run start:bun
```

Repeat for `knowledge-mcp`, `filesystem-mcp`, `vector-mcp`.

---

## Testing

### Backend

```bash
cd backend
./gradlew test                         # unit tests
./gradlew integrationTest              # integration tests
./gradlew verify                       # full gate (test + check + jacoco)
```

### Frontend

```bash
cd vue3-admin
npm run test          # vitest unit tests
npm run test:e2e      # playwright end-to-end
npm run lint          # eslint
```

### Python

```bash
cd py-mcp-servers/database_mcp
pytest -q
ruff check .
mypy --strict src
```

### Node MCP Servers

```bash
cd node-mcp-servers/database-mcp
npm test              # vitest
npm run lint          # eslint
npm run typecheck     # tsc --noEmit
```

All CI checks must pass before a PR can be merged.

---

## Coding Standards

### Java

- Follow **Google Java Style** with 2-space indentation.
- Prefer **`record`** types for immutable data carriers.
- Prefer **sealed types** for closed hierarchies (e.g. `ResultCode`).
- Use **Lombok** sparingly; records are preferred where they apply.
- All public methods on `*Service` interfaces documented with Javadoc.
- No `@Autowired` field injection — use constructor injection.

### TypeScript

- ESLint (`@typescript-eslint/recommended`) + Prettier 3.x.
- Strict mode enabled (`"strict": true` in `tsconfig.json`).
- Prefer `readonly` and exhaustive `switch` on discriminated unions.
- Use `as const` for literal types instead of string enums.

### Python

- Formatter: **ruff format**.
- Linter: **ruff check** (E/F/W/I/B/UP rules).
- Type-checker: **mypy --strict**.
- Public APIs documented with docstrings (PEP 257).

### SQL / Migrations

- One migration per change; never edit an applied migration.
- Use the database's snake_case convention (Postgres default).
- Indexes for every foreign key; EXPLAIN-check every slow query.

### Commit Messages

We use [Conventional Commits](https://www.conventionalcommits.org/):

```text
<type>(<scope>): <subject>

<body>

<footer>
```

| Type | Use for |
|------|---------|
| `feat` | New user-visible feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | No behavior change |
| `test` | Add or fix tests |
| `chore` | Build, deps, tooling |
| `perf` | Performance improvement |
| `security` | Security fix |
| `revert` | Revert a previous commit |

Examples:

```text
feat(mcp-database): add MongoDB schema introspection tool
fix(gateway): avoid dropping in-flight requests on route reload
docs(readme): clarify Bun runtime install steps
```

---

## Branch Strategy

We practice **trunk-based development**:

- `main` is always deployable; protected branch.
- Feature branches: `feat/<short-topic>` or `fix/<short-topic>`.
- Hotfix branches: `hotfix/<version>` cut from a release tag.
- Branch lifetime should be **< 3 days**; split large features into
  stacked PRs.

---

## Pull Request Process

1. **Branch from `main`** and push commits in logical units.
2. **Run the full gate locally**: `./gradlew verify`,
   `npm run lint && npm run test`, `ruff check && mypy --strict`.
3. **Open the PR** using the template — fill in the description,
   linked issue, and testing notes.
4. **Pass CI** — lint, unit tests, integration tests, build, and
   security scan (`./gradlew dependencyCheck`). Failures block merge.
5. **One reviewer approval** is required; two for changes touching
   `xarch-core` or `xarch-mcp-*`.
6. **Squash merge** with a Conventional Commits-formatted title.
7. The PR description is preserved as the merge commit body.

### Reviewer Checklist

- Tests cover new code paths.
- No regressions to public API contracts.
- Migration guide updated if schema changes.
- CHANGELOG `Unreleased` updated for user-visible changes.
- Security implications considered.

---

## Release Process

1. Cut a release branch `release/vX.Y.Z` from `main` when the milestone
   is complete.
2. Bump versions in the Gradle version catalog (`gradle.properties`),
   `package.json` files, and `pyproject.toml` files.
3. Update `CHANGELOG.md` — move items from `Unreleased` to the
   versioned section, set the release date.
4. Tag `vX.Y.Z` (signed) and push: `git tag -s vX.Y.Z -m "vX.Y.Z"`.
5. CI publishes artifacts (Maven local artifacts, Docker images,
   Python wheels) and creates a GitHub release with notes pulled from
   the changelog.
6. Post-release: merge `CHANGELOG.md` updates back to `main`.

For patch releases, cherry-pick the fix commits into the active
release branch, re-tag, and publish.

---

Welcome to the project, and thanks for contributing!