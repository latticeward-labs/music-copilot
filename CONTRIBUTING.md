# Contributing to Music Copilot

## Before starting

- Select a GitHub Issue marked **Ready** on the project board.
- Confirm its acceptance criteria, priority, dependency, week/gate and PRD/MVP requirement IDs.
- P0 Player, Voice and Driving work takes precedence over P1/P2 work.
- Do not implement account, backend, streaming, discovery feed or advanced playlists in MVP v1.
- Keep one focused outcome per branch and pull request.

## Branch naming

- `feature/<issue>-<short-name>`
- `fix/<issue>-<short-name>`
- `docs/<issue>-<short-name>`
- `chore/<issue>-<short-name>`

Examples: `feature/5-local-library-scan`, `feature/10-voice-control-intents`.

## Commits

Use imperative scoped messages:

```text
feat(playback): persist queue in media service
fix(audio): prevent speaker playback after bluetooth loss
docs(voice): clarify low-confidence confirmation
```

## Pull requests

Each pull request must:

- link its Issue and requirement IDs,
- explain the user/system outcome,
- list validation and physical-device/vehicle evidence when applicable,
- include UI screenshots/recordings,
- disclose privacy, telemetry, migration, route-audio and rollback impact,
- remain small enough for confident review.

## Review and merge

- Do not self-approve as the only reviewer.
- Resolve discussions with evidence.
- Squash merge is preferred.
- Delete merged feature branches.
- Keep `main` stable; use `develop` for integration.
- A merged change moves to **Testing**, not automatically to **Done**.

## Security and content

Never commit API keys, service accounts, signing keys, production config, user data, raw voice/transcripts, personal media metadata or copyrighted audio without repository distribution rights.
