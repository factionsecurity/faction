# Security Policy

Thank you for helping keep FACTION and its users safe. We take security issues
seriously and appreciate responsible disclosure.

## Supported Versions

Security fixes are applied to the latest released version of FACTION. We
recommend always running the most recent release.

| Version | Supported          |
| ------- | ------------------ |
| Latest release (1.8.x) | :white_check_mark: |
| Older releases         | :x:                |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues,
discussions, or pull requests.**

Instead, report them using one of the following private channels:

1. **GitHub Private Vulnerability Reporting** (preferred): open a report at
   <https://github.com/factionsecurity/faction/security/advisories/new>.
2. **Email**: send details to `develop [at] factionsecurity.com` with the subject
   line `SECURITY: <short summary>`.

Please include as much of the following as you can:

- A description of the issue and its potential impact.
- Steps to reproduce, ideally with a minimal proof of concept.
- The affected version(s), commit hash, or deployment configuration.
- Any suggested mitigation or patch, if you have one.

If you would like your report encrypted, mention this in your initial email and
we will provide a public key.

## Our Process

- We will acknowledge your report within **5 business days**.
- We will work with you to validate and reproduce the issue, and keep you
  updated on remediation progress.
- Once a fix is available, we will coordinate a release and a public advisory.
  We are happy to credit reporters in the advisory unless you prefer to remain
  anonymous.
- We ask that you give us a reasonable window (typically up to **90 days**) to
  release a fix before any public disclosure.

## Scope

In scope:

- The FACTION application source code in this repository.
- Official Docker images and deployment artifacts published by the project.

Out of scope:

- Vulnerabilities in third-party dependencies that have not yet been patched
  upstream (please report these to the upstream project; we will track and
  upgrade once a fix is available).
- Issues that require physical access to a user's machine, social engineering,
  or already-compromised credentials.
- Denial-of-service findings that rely solely on excessive request volume.

## Safe Harbor

We will not pursue or support legal action against researchers who:

- Make a good-faith effort to comply with this policy.
- Avoid privacy violations, data destruction, and service disruption.
- Give us reasonable time to address the issue before disclosing it publicly.

Thank you for helping make FACTION more secure.
