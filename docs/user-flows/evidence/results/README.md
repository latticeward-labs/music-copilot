# Technical Spike Result Bundle Contract

Only immutable bundles here count as executed TS evidence. Each `manifest.md` records spike/row IDs, date, repository/build SHA, package/version/build, SDK/library versions, device/OEM/API and physical/emulator status, synthetic corpus, steps/commands, expected/actual, PASS/FAIL/BLOCKED, artifact checksums, linked defect, and Android/QA reviewer/date/decision.

Each bundle includes `matrix.md`, timestamped `state-trace.txt`, narrow `logcat-redacted.txt`, recordings/screenshots for visible assertions, and reproduction commands. Never commit personal titles/artists/albums/folders/paths/URIs/MediaStore IDs, audio, voice, accounts, tokens, or unrelated logs. Use synthetic media tokens. Signed bundles are immutable; reruns use a new directory.
