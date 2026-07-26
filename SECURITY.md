# Security Policy

## Reporting

Do not open a public issue for a vulnerability or exposed secret. Report it privately to the repository owner through GitHub's private security reporting channel when enabled.

Include:

- affected component and version,
- reproduction steps,
- expected and actual behavior,
- impact assessment,
- a minimal proof of concept if safe.

## Sensitive material

The repository must never contain:

- API keys or access tokens,
- Android signing keys,
- Firebase service-account credentials,
- production environment files,
- personal user data,
- copyrighted audio without repository distribution rights.

If a secret is exposed, revoke or rotate it immediately. Removing it from the latest commit is not sufficient because Git history may retain it.

## Supported versions

Until the first internal release, only the latest `main` state is supported.
