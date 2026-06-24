# Pull Request

## Description

<!-- Provide a clear and concise description of what this PR does and why. -->
<!-- Link any related issues: Fixes #123, Relates to #456 -->

---

## Type of Change

<!-- Mark the relevant option(s) with an "x". -->

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Refactor (no functional change, code improvement)
- [ ] Documentation update
- [ ] Build / CI / tooling change
- [ ] Performance improvement
- [ ] Dependency upgrade
- [ ] Other (please describe):

---

## Affected Components

<!-- Check all that apply. -->

- [ ] Java backend (`backend/`)
- [ ] Vue 3 admin frontend (`vue3-admin/`)
- [ ] Python MCP servers (`py-mcp-servers/`, `python/`)
- [ ] Node.js MCP servers (`node-mcp-servers/`)
- [ ] Spring Cloud / Gateway
- [ ] xarch example app
- [ ] Build configuration (`build.gradle`, `settings.gradle.kts`)
- [ ] Docker / Kubernetes manifests
- [ ] CI/CD workflows (`.github/workflows/`)
- [ ] Documentation

---

## Testing Checklist

<!-- Mark the items you have verified. -->

- [ ] Unit tests added / updated
- [ ] Integration tests added / updated
- [ ] Manual testing performed
- [ ] All CI checks pass locally
- [ ] Code builds successfully (`./gradlew build` / `npm run build`)
- [ ] Lint passes (`npm run lint`, `ruff check`, etc.)
- [ ] Test coverage maintained or improved

### Test Plan

<!-- Describe how you tested this change. -->

```
# Example:
# 1. Run unit tests
cd backend && ./gradlew test

# 2. Run frontend build
cd vue3-admin && npm run build

# 3. Manual verification steps...
```

---

## Security Checklist

<!-- Confirm each item. -->

- [ ] No secrets, credentials, or API keys committed
- [ ] No new dependencies with known vulnerabilities (`npm audit`, `pip-audit`)
- [ ] Input validation added for any user-supplied data
- [ ] SQL queries use parameter binding (no string concatenation)
- [ ] Authentication / authorization re-checked for affected endpoints
- [ ] No new external network calls without justification
- [ ] Dockerfile best practices followed (non-root user, multi-stage, pinned base images)
- [ ] OWASP top 10 considered
- [ ] Sensitive logs sanitized (no PII, passwords, tokens)

---

## Documentation

- [ ] README.md updated
- [ ] API documentation updated
- [ ] CHANGELOG entry added
- [ ] Inline code comments added where helpful
- [ ] No documentation needed

---

## Breaking Changes

<!-- If this PR introduces breaking changes, describe the migration path. -->

**Migration Steps:**

```
# Example:
# 1. Update configuration file format (see docs/migration.md)
# 2. Run migration script: ./gradlew migrate
```

---

## Deployment Notes

<!-- Anything operators need to know when rolling this out. -->

- [ ] Requires database migration
- [ ] Requires environment variable changes
- [ ] Requires configuration update
- [ ] Requires Kubernetes manifest update
- [ ] Requires dependency upgrade (Java, Node, Python versions)
- [ ] Requires service restart
- [ ] Backward compatible (no special steps)

**Rollback Plan:**

<!-- Describe how to safely revert this change if needed. -->

---

## Screenshots / Recordings

<!-- If the change is UI-related, include before/after screenshots. -->

| Before | After |
| ------ | ----- |
|        |       |

---

## Reviewer Notes

<!-- Anything specific you want reviewers to focus on. -->

---

## Checklist Before Merge

<!-- Final confirmations. -->

- [ ] All conversations on this PR are resolved
- [ ] CI pipeline is green
- [ ] Security scan passed (no CRITICAL/HIGH findings)
- [ ] At least one approval from a code owner
- [ ] Branch is up to date with `main`
- [ ] Squash-merged or rebased appropriately