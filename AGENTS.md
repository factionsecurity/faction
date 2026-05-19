# AGENTS.md

Guidance for AI coding agents (Claude Code, Cursor, Aider, etc.) working in
this repository. Human contributors should read `CONTRIBUTING.md` and
`README.md` instead.

## Project at a glance

FACTION is an OWASP project — a pentest reporting and collaboration
framework. Java (JAX-RS resources under `src/com/fuse/api/`), Hibernate
OGM against MongoDB for persistence, built with Maven, released via the
`maven-release-plugin`. The web UI lives under `WebContent/`.

See `README.md` for the user-facing introduction and `CONTRIBUTING.md`
for human contributor guidelines.

## Project-specific skills

This repo ships custom skills in `.claude/skills/`. **Always prefer these
over reinventing the workflow they cover.**

| Skill | When to use |
| ----- | ----------- |
| `release-notes` (`.claude/skills/release-notes/SKILL.md`) | Drafting release notes for the next FACTION release. Reads commit diffs since the previous tag and pushes a **draft** release to GitHub via `gh release create --draft` (or updates an existing draft via `gh release edit`). Never publishes. Invokable as `/release-notes [version] [prev-tag]`. |

If you find yourself manually composing release notes, stop and use the
skill — it encodes the section structure and tone that matches
`.github/release.yml`.

## Conventions

### Commits

- Short imperative subject in the style of recent history: `Add X`,
  `Replace Y with Z`, `Remove obsolete W`. No conventional-commit prefix.
- **No `Co-Authored-By` trailer.** This project's commits never carry
  one, regardless of how the change was authored.
- Group changes by logical concern — one commit per coherent change, not
  one giant "WIP" commit. If staging surfaces unrelated edits, ask the
  user how to split them before committing.
- Commits starting with `[maven-release-plugin]` are produced by the
  release plugin; do not write these by hand.

### Tests

Run the Java test suite with the project's MongoDB sysprops so tests
that touch the persistence layer can connect:

```bash
mvn test \
  -DFACTION_MONGO_HOST=localhost \
  -DFACTION_MONGO_PORT=27017 \
  -DFACTION_MONGO_DATABASE=faction-test
```

`FSUtils.getEnv` reads JVM system properties as a fallback for
`FACTION_MONGO_*`, so `-D` flags are sufficient — no OS env vars
required. Integration tests can also bring up MongoDB via Testcontainers
if no external instance is configured.

### What not to commit

- `logs/` — runtime log output. Stays untracked.
- `release.properties` and `pom.xml.releaseBackup` after a manual
  release-plugin run — clean these with `mvn release:clean` rather than
  committing the leftover state.

## Security

Vulnerabilities go through the disclosure flow in `SECURITY.md` (private
reporting via GitHub Security Advisories or the project email). Never
open a public GitHub issue for a suspected vulnerability.
