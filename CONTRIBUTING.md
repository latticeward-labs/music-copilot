# Contributing to Music Copilot

## Before starting

- Select an approved GitHub Issue.
- Confirm its acceptance criteria, dependencies, owner, and status.
- Keep one focused outcome per branch and pull request.

## Branch naming

- `feature/<issue>-<short-name>`
- `fix/<issue>-<short-name>`
- `docs/<issue>-<short-name>`
- `chore/<issue>-<short-name>`

Example: `feature/12-playlist-create`

## Commit messages

Use an imperative, scoped message:

```text
feat(player): add play and pause state
fix(search): handle empty result response
docs(scope): clarify licensed audio requirement
```

## Pull requests

A pull request should:

- link its issue,
- explain the user or system outcome,
- list validation performed,
- include screenshots or recordings for UI changes,
- call out privacy, analytics, migration, and rollback impact,
- remain small enough to review confidently.

## Review rules

- Do not approve your own change as the only reviewer.
- Resolve review discussions with evidence.
- Squash merge is preferred for focused feature work.
- Delete merged feature branches.
- Keep `main` stable.

## Security

Never commit API keys, service-account files, signing keys, production configuration, user data, or commercial audio files.
