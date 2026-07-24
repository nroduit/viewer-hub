# Security Policy

ViewerHub manages medical-imaging viewers (Weasis, OHIF, 3D Slicer, MicroDicom)
across an IT infrastructure: it resolves which viewer to launch, builds the
manifest served to Weasis, and handles authentication (OAuth2 tokens) for
viewers accessing a PACS. Because it sits in front of sensitive health
information (PHI/PII) and runs on hospital networks, we take security issues
seriously and appreciate responsible disclosure.

## Supported Versions

Security fixes are provided for the latest released minor version. We recommend
always running the most recent release.

| Version | Supported          |
| ------- | ------------------ |
| 0.10.x  | :white_check_mark: |
| < 0.10  | :x:                |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues,
discussions, or pull requests.**

Instead, report them privately using one of the following channels:

- **Preferred:** Open a [private security advisory](https://github.com/nroduit/viewer-hub/security/advisories/new)
  via GitHub's "Report a vulnerability" feature.
- Alternatively, email the maintainers at **dicom@hcuge.ch**.

Please include as much of the following as you can to help us triage quickly:

- The type of issue (e.g. authentication bypass, token/credential exposure,
  injection, SSRF, insecure manifest generation, access-control bypass, etc.).
- The affected component(s) and version (viewer launch/display controllers,
  manifest builder, connector query flow, Vaadin admin UI, OAuth2/OIDC
  configuration, S3/Redis integration, etc.).
- Step-by-step instructions to reproduce the issue.
- Proof-of-concept or exploit code, if available.
- The impact, including how an attacker might exploit it.

**Do not include real patient data** in your report. Use synthetic or fully
anonymized DICOM data only.

## Disclosure Process

- We will acknowledge receipt of your report within **5 business days**.
- We will investigate and provide an initial assessment within **10 business
  days**, and keep you informed of progress toward a fix.
- Once a fix is available, we will coordinate a release and a public advisory.
  We are happy to credit you in the advisory unless you prefer to remain
  anonymous.

We ask that you give us a reasonable amount of time to address the issue before
any public disclosure.

## Scope

This policy covers the ViewerHub application and its source code in this
repository. Vulnerabilities in third-party dependencies should be reported to
the respective upstream projects; if a dependency issue affects ViewerHub, feel
free to let us know so we can update.

Thank you for helping keep ViewerHub and its users safe.