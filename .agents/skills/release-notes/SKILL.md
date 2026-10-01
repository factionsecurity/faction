---
name: release-notes
description: Draft release notes for the next FACTION release by reading commit diffs since the previous tag and pushing them to GitHub as a DRAFT release via `gh release create --draft` (or updating an existing draft). Use when the user asks to draft/prepare release notes, summarize what's changed since a tag, or prepare a release writeup. Never publishes — always draft.
---

# FACTION release notes

You are drafting human-quality release notes for the FACTION project. The goal
is the kind of writeup a user reads in a GitHub Release and immediately
understands what changed, why it matters to them, and whether they need to
do anything to upgrade.

Keyword-matching commit subjects produces shallow output. **Read the diffs.**
That is the difference between "Add default-vulnerability CRUD endpoints" (a
commit subject) and a release-notes paragraph that lists each new endpoint,
its method/path, and the round-trip-cleanly fix for names containing `/`.

## Step 1 — Pick the version

If the user passed a version (e.g. `/release-notes 1.8.6`), use it.

Otherwise derive it from `pom.xml`:

```bash
grep -m1 '<version>' pom.xml | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/' | sed 's/-SNAPSHOT//'
```

If the result still contains `SNAPSHOT` or is empty, stop and ask the user
which version this release should be tagged as.

## Step 2 — Pick the previous tag

Default to the most recent annotated/lightweight tag:

```bash
git describe --tags --abbrev=0
```

If that errors (no tags exist), ask the user for a starting ref instead of
guessing. If the user passed a second arg, use that as the previous tag.

## Step 3 — Inspect what changed

Run these (in parallel where independent):

```bash
git log --reverse <prev-tag>..HEAD --pretty=format:'%h %s'
git diff --stat <prev-tag>..HEAD
```

Then **for each non-trivial commit**, read the actual diff:

```bash
git show --stat <sha>
git show <sha> -- <interesting paths>
```

Skip commits whose subject starts with `[maven-release-plugin]` — they are
version-bump noise. Also skip pure-internal hygiene (gitignore tweaks,
formatting-only changes) unless that's all that landed.

If `.github/release.yml` exists, read it — its `categories` section names
match the GitHub Release auto-categorizer (currently
`🎉 🚀 Upgrades 🎉 🚀` and `🐛 Bugfixes 🐛`). Use those exact section
titles so the file mirrors what GitHub would generate.

## Step 4 — Draft on GitHub

You will push a draft release to GitHub rather than committing a markdown
file to the repo. The user reviews and publishes from
<https://github.com/factionsecurity/faction/releases>.

Preflight:

```bash
gh auth status          # verify gh is installed and logged in
gh release view <version>   # check if a release for this version already exists
```

- If `gh auth status` fails, stop and tell the user to run `gh auth login`.
- If `gh release view` succeeds **and** the release is published (not a
  draft), stop and ask before overwriting — published releases should not be
  silently mutated.
- If `gh release view` succeeds and the release **is** a draft, you'll
  update it in place via `gh release edit` (see below).
- If `gh release view` fails with "release not found", you'll create a new
  draft via `gh release create`.

Compose the notes body using the structure below, then write it to a temp
file (`/tmp/release-notes-<version>.md`) — passing markdown via `--notes`
inline is fragile with backticks and code fences, so always use
`--notes-file`.

Body structure (omit any section that's empty):

The body starts directly with the executive summary — do **not** add a
`# FACTION <version>` title or a `_Release date: ..._` line. GitHub already
renders the release title and date from the release object itself.

```markdown
<One-paragraph executive summary — what's the headline of this release?
Mention the 2-3 biggest things in plain language. No bullet list here.>

## 🎉 🚀 Upgrades 🎉 🚀

### <Feature name>

<Prose explanation: what is it, why does it exist, what can a user now do
that they couldn't before? For API additions, include a method/path table.
For format changes, include a short example. Note backward-compat behavior
explicitly.>

## 🐛 Bugfixes 🐛

- <One bullet per fix. Lead with the user-visible symptom, then the cause
  in parens. "X was broken when Y; root cause was Z" beats "Fix Z".>

## 🧰 Internal / Test infrastructure

<Only include if there are notable test or build changes that matter to
contributors. Skip if the only internal change is a typo fix.>

## Upgrade notes

- **Database migration:** required / not required (state which).
- **API:** call out any breaking changes, or explicitly say "all existing
  endpoints continue to work unchanged" if true.
- **Configuration:** any new required env vars or settings.
- **Permissions:** any new permission scopes or role changes.

## Full changelog

<https://github.com/factionsecurity/faction/compare/<prev-tag>...<version>>
```

## Style rules

- **Read diffs, don't paraphrase commit subjects.** A commit titled "fix bug"
  tells the reader nothing; the diff tells you what actually changed.
- **User-facing voice.** "You can now …" beats "We added a method that …".
- **Group by feature, not by commit.** Three commits that together
  implement one endpoint become one bullet.
- **Tables for API additions.** Method / path / purpose. Always.
- **Backward compatibility is load-bearing.** If old clients keep working,
  say so explicitly — that's often the most reassuring sentence in the file.
- **No emojis inside body text.** They're fine in the section headers
  (because `.github/release.yml` uses them), but don't sprinkle them through
  the prose.
- **No "Co-Authored-By" anywhere.** This project's commits and release
  notes never carry that trailer.

## Step 5 — Push the draft

Write the composed body to the temp file, then create or update the draft.

**New draft** (no existing release for `<version>`):

```bash
gh release create <version> \
  --draft \
  --title "FACTION <version>" \
  --notes-file /tmp/release-notes-<version>.md \
  --target main
```

`--draft` is mandatory — never publish from this skill. The tag does not
have to exist yet; GitHub creates it at `--target` only when the draft is
published.

**Existing draft** (re-running the skill to iterate on the same release):

```bash
gh release edit <version> \
  --draft \
  --notes-file /tmp/release-notes-<version>.md
```

`gh release edit` keeps the existing title unless `--title` is also passed.
Only pass `--title` if the user explicitly asked to rename the release.

## Hand-off

After the `gh` command succeeds:
1. Print the draft URL. `gh release create` prints it on stdout; for the
   edit path, get it with `gh release view <version> --json url -q .url`.
2. Print the first ~10 lines of the body so the user can sanity-check the
   framing without leaving the terminal.
3. Tell the user the draft is **unpublished** and that they should review
   on GitHub before clicking Publish.
4. Do **not** publish, `git tag`, or `git push` anything. The skill's
   contract ends at "draft is up for review."

## When gh isn't available

If `gh` is not installed or the user is offline, fall back to writing
`RELEASE_NOTES_<version>.md` at the repo root and tell the user explicitly
that you did so because GitHub was unreachable — they can paste it into
the Releases UI manually.
