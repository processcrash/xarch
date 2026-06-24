# Security Policy

xarch takes the security of its users seriously. This document
describes how to report vulnerabilities, the security features
shipped with the framework, and the process for issuing security
updates.

---

## Supported Versions

Only the **latest major release** receives security updates. Bug
fixes for older majors are not back-ported unless explicitly
announced.

| Version | Supported |
|---------|-----------|
| 1.x     | Yes       |
| < 1.0   | No        |

We strongly recommend staying on the latest minor version of the
current major to receive all security patches promptly.

---

## Reporting a Vulnerability

**Please do NOT open a public GitHub issue for security problems.**

### Contact

- **Email**: `security@xarch.example` (PGP-encrypted preferred)
- **GitHub**: Use *Security Advisories* via the *Report a vulnerability*
  button on the repository's *Security* tab.

### What to include

- A clear description of the issue and its impact.
- Reproduction steps or a minimal proof-of-concept.
- Affected component (starter name, MCP server, version).
- Your name / handle if you'd like to be credited in the advisory.

### Disclosure Policy

We follow a **90-day coordinated disclosure** policy:

1. **Day 0** — Report received, an internal incident is opened.
2. **Day 1-7** — Maintainers triage and confirm the report.
3. **Day 8-60** — Develop and validate a fix in a private fork.
4. **Day 60-90** — Coordinate the release date with the reporter.
5. **Day 90 (or earlier)** — Public advisory and patched release.

After 90 days the report may be disclosed publicly regardless of fix
status to encourage remediation. Extensions are granted on request
when a downstream ecosystem needs more time.

### PGP Key

```
Placeholder — actual key will be published here before v1.0 GA.
Fingerprint: 0000 0000 0000 0000 0000  0000 0000 0000 0000 0000
```

---

## Security Features Built-In

xarch ships with sensible defaults so that applications start secure
out of the box.

### Authentication & Authorization

- **Sa-Token** is the primary auth framework, issuing **JWT** tokens
  with configurable TTL and refresh.
- **BCrypt** (strength 12) is used for password hashing.
- Method-level `@SaCheckPermission("user:add")` annotations for
  fine-grained authorization.
- Session fixation and CSRF protections enabled by default.

### Input Safety

- **SQL injection prevention** — all database access goes through
  `MyBatis-Flex` with parameterized queries; the framework
  convention forbids string concatenation into SQL.
- **XSS prevention** — a request wrapper applies context-aware HTML
  escaping for `text/html` and `application/json` responses.
- **Bean Validation (Jakarta Validation)** annotations applied at the
  DTO boundary; invalid input is rejected before reaching services.

### Transport & Web

- **CSRF protection** via double-submit token for state-changing
  browser requests (Sa-Token integration).
- **Rate limiting** — `xarch-cache-spring-boot-starter` ships a
  token-bucket limiter that can be attached per-route.
- **CORS** — empty default allow-list; production must opt in
  explicitly via `xarch.web.cors.allowed-origins`.
- **Sensitive header redaction** — `Authorization`, `Cookie`,
  `Set-Cookie` are masked in access logs.

### Data Protection

- **Audit logging** via the `XarchLog` annotation and aspect.
  Every modification is recorded with operator, target, before/after
  diff (configurable).
- **Encrypted config** — values tagged `@Encrypted` in YAML are
  transparently decrypted using a configured `MasterKeyProvider`.
- **Storage abstraction** for file uploads supports server-side
  encryption for S3-compatible backends.

### Runtime

- **Virtual threads** are used for I/O-bound work so that the JVM
  remains responsive under load.
- **Heap dump + thread dump** are available via Spring Boot Admin
  for incident response.
- **Spring Security headers** enabled by default (HSTS, X-Frame-Options,
  X-Content-Type-Options, Referrer-Policy).

---

## Security Checklist for Contributors

Before opening a PR, verify:

- [ ] **No hardcoded secrets** — credentials are loaded from env
  vars, Vault, or Nacos encrypted config. CI fails on
  `password=` / `secret=` patterns.
- [ ] **All inputs validated** — DTOs use Jakarta Validation
  annotations; raw request bodies are never trusted.
- [ ] **Parameterized queries** — no string concatenation into SQL;
  no `#{...}` interpolation of untrusted input.
- [ ] **Tests for security boundaries** — at least one test for
  each new endpoint covering the unauthenticated / unauthorized /
  forbidden paths.
- [ ] **No new dependencies with known critical CVEs** — CI runs
  `dependencyCheck` (OWASP) on every PR.
- [ ] **No disabling of security defaults** — turning off CSRF,
  XSS, or rate limiting requires explicit justification in the PR
  description.

---

## Known Security Advisories

| ID | Title | Affected | Status | Released |
|----|-------|----------|--------|----------|
|    | *None published yet* |        |        |          |

Advisories will be linked here as they are disclosed. Subscribe to
the GitHub *Security Advisories* tab to receive notifications.

---

## Security Update Process

1. A patch is developed in a private fork against the affected
   release branch.
2. A regression test is added that fails on the vulnerable code and
   passes on the fix.
3. CI must pass on the private branch before merge.
4. The release is tagged and published; a CVE is requested via
   GitHub Security Advisories (auto-published on the GHSA database).
5. The framework's `CHANGELOG.md` `Security` section is updated and
   the entry is highlighted on the release page.
6. Email notification is sent to the maintainer distribution list
   for downstream rebuilds.

---

## Acknowledgements

We thank the security researchers and community members who have
helped improve xarch. Hall of fame entries are listed in
`docs/SECURITY_HALL_OF_FAME.md` once the first report is fixed.