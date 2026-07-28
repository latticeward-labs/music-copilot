# Security and Privacy Policy

Security and privacy behavior is governed by the canonical [PRD v1.0](docs/PRD.md), [MVP v1.0](docs/MVP.md).

## Reporting

Do not open a public issue for vulnerabilities, exposed secrets or privacy incidents. Use GitHub private security reporting when enabled and include component/version, reproduction, impact and a minimal safe proof.

## Prohibited repository and log material

- API keys, access tokens, signing keys and service-account credentials
- production environment files
- raw voice or microphone recordings
- full personal filenames, track titles, artists or albums
- user identifiers, contacts or precise location
- commercial audio without repository distribution rights

## High-risk behaviors

Treat these as P0 security/safety release blockers:

- unintended playback through phone speaker after Bluetooth loss,
- microphone activation without explicit push-to-talk action,
- raw voice storage or cloud STT use without the required separate consent and disclosure,
- exposure of personal media metadata in analytics/crash reports,
- repeatable wrong-command execution with serious distraction impact,
- ambiguous behavior during calls or audio-route changes.

If a secret is exposed, revoke/rotate it immediately; deleting the latest file is insufficient because Git history may retain it.

## Supported versions

Until the first Closed Beta, only the latest reviewed `main` state is supported.
