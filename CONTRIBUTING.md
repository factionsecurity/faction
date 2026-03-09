# Contributing to Faction

We support and encourage contributions to Faction. This project was built out of our love for open source tools and we feel its our way to give back to a community that we have benifeted from so much durring our carrers. 

## Community

* Get in touch via the [OWASP Slack Community](https://owasp.org/slack/invite) (#project-faction)
* Follow and get the latests updates on [BlueSky](https://bsky.app/profile/factionsecurity.com)
* Follow our [Blog](https://medium.com/@we-are-faction) for more information and tutorials 


## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Submitting Extensions](#submitting-extensions)
  - [Pull Requests](#pull-requests)
  - [Submitting Security Issues](#submitting-security-issues)
- [Contact](#contact)

## Code of Conduct

By participating in this project, you agree to maintain a respectful and collaborative environment. We expect all contributors to be professional and considerate in their interactions.

## How Can I Contribute?

### Reporting Bugs

Before submitting a bug report, please check the [existing issues](https://github.com/factionsecurity/faction/issues) to avoid duplicates.

When reporting a bug, please include:

- **Clear title and description** - Summarize the issue concisely
- **Steps to reproduce** - Detailed steps to recreate the bug
- **Expected behavior** - What you expected to happen
- **Actual behavior** - What actually happened
- **Environment details**:
  - Faction version
  - Operating system and version
  - Docker version (if applicable)
  - Browser and version (for UI issues)
  - MongoDB version
- **Screenshots or logs** - If applicable, include error messages or visual evidence
- **Possible solution** - If you have an idea of what might be causing the issue

**To submit a bug report:**

1. Go to the [Issues page](https://github.com/factionsecurity/faction/issues)
2. Click "New Issue"
3. Select the bug report template (if available) or create a blank issue
4. Fill in all relevant information
5. Add appropriate labels (e.g., `bug`, `needs-triage`)

### Suggesting Enhancements

We welcome suggestions for new features and improvements! Before submitting an enhancement:

- Check existing issues to see if someone has already suggested it
- Consider whether the feature fits Faction's core mission of pen testing collaboration
- Think about how the feature would benefit the broader user base

When suggesting an enhancement, please include:

- **Clear title and description** - What feature you'd like to see
- **Use case** - Why this feature would be valuable
- **Proposed solution** - How you envision it working
- **Alternatives considered** - Other approaches you've thought about
- **Additional context** - Screenshots, mockups, or examples from other tools

**To submit an enhancement:**

1. Go to the [Issues page](https://github.com/factionsecurity/faction/issues)
2. Click "New Issue"
3. Use the title prefix `[Enhancement]` or `[Feature Request]`
4. Fill in the details
5. Add appropriate labels (e.g., `enhancement`, `feature-request`)

### Submitting Extensions

Faction supports custom extensions to expand functionality, similar to Burp Extender. If you've developed an extension that you'd like to be officially recognized and listed on our site:

**Extension Submission Process:**

1. **Complete your extension** - Ensure it's fully functional and well-tested
2. **Host on GitHub** - Your extension should be publicly available in a GitHub repository
3. **Create documentation** - Include a clear README with:
   - Description of what the extension does
   - Installation instructions
   - Usage examples
   - Screenshots (if applicable)
   - Requirements and dependencies
4. **Email us** - Send an email to **develop@factionsecurity.com** with:
   - Link to your GitHub repository
   - Brief description of what the extension does (2-3 sentences)
   - Your contact information

**After Submission:**

- Our team will review your extension for functionality, security, and code quality
- If accepted, we will fork your repository to maintain a stable version
- Your extension will be listed on the official Faction website and documentation
- You'll be credited as the author

**Extension Requirements:**

- Must use the [FactionExtender](https://github.com/factionsecurity/FactionExtender) library
- Should follow Java best practices
- Must not contain malicious code or vulnerabilities
- Should include appropriate error handling
- Must have clear documentation

For technical guidance on building extensions, see the [Extension Development](#extension-development) section below.

### Pull Requests

We actively welcome pull requests for bug fixes, enhancements, and documentation improvements.

**Before submitting a pull request:**

1. **Search existing PRs** - Check if someone is already working on something similar
2. **Create an issue first** - For significant changes, open an issue to discuss your approach
3. **Fork the repository** - Create your own fork to work in
4. **Create a feature branch** - Use a descriptive branch name (e.g., `fix-authentication-bypass`, `add-report-export`)

**Pull request process:**

1. **Make your changes** - Follow the [Coding Guidelines](#coding-guidelines)
2. **Test thoroughly** - Ensure your changes work and don't break existing functionality
3. **Update documentation** - Add or update docs if needed
4. **Write a clear PR description**:
   - Reference any related issues (e.g., "Fixes #123")
   - Describe what changed and why
   - Note any breaking changes
   - Include screenshots for UI changes
5. **Submit the PR** - Push to your fork and create a pull request to the `main` branch
6. **Respond to feedback** - Be prepared to make changes based on code review

**PR Guidelines:**

- Keep changes focused - One PR should address one issue or feature
- Write meaningful commit messages
- Ensure your code compiles and runs without errors
- Be patient - Reviews may take time depending on complexity

### Submitting Security Issues
Please submit security issues to us privately via info@factionsecurity.com. When submitting a security issue be sure to provide clear reproducible steps and a suggested severity for the finding. 

## Contact

- **General inquiries:** [OWASP Slack Community](https://owasp.org/slack/invite) (#project-faction)
- **Extension submissions:** develop@factionsecurity.com
- **Security issues:** Please report security vulnerabilities privately to info@factionsecurity.com

## Sponsorship

Love Faction? Consider becoming a sponsor! Sponsors get:

- Prioritized bug reports
- Direct support from the team
- Your company placement on the repo and website

Click the sponsor links at the top of the repository or contact us at info@factionsecurity.com.

---

Thank you for contributing to Faction! Your efforts help make penetration testing collaboration better for everyone.
