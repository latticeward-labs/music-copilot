# Technical Spike Result Bundle Contract

Only immutable bundles under this directory count as executed TS evidence.

Each `manifest.md` records spike/row IDs, date, repository/build SHA where retained, package/version/build, SDK/library/device data where retained, steps/expected/actual/result, artifact Git object IDs/checksums, linked decisions, and review status. Each bundle includes `matrix.md`, `state-trace.txt`, `logcat-redacted.txt`, a visual-evidence path, and reproduction/execution records as applicable.

Never commit personal titles/artists/albums/folders/paths/URIs/MediaStore IDs, audio, voice, accounts, tokens, or unrelated logs. Use synthetic/redacted tokens only.

## Historical execution exception — Cluster B 2026-08-18

For the accepted Cluster B run (`TS-01`, `TS-02`, `TS-06`, `TS-08` under `2026-08-18-81b9aad`), the project owner approved `../CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md` because several raw captures were not retained before this bundle contract was finalized.

For those four historical bundles only:

- derived `state-trace.txt` may record confirmed transitions without invented runtime timestamps,
- `logcat-redacted.txt` may record the accepted runtime outcome and explicitly state that original physical logcat retention was waived; no log lines may be fabricated,
- `screenshots-or-screen-recording/README.md` may record confirmed visible assertions where the original binary capture was not retained; no image/video may be recreated and presented as original evidence,
- unavailable exact historical device identifiers and TS-06 M12/M13 numeric measurements may be marked retention-waived only when the execution/PASS outcome is explicitly confirmed,
- Android/QA review may be recorded as project-owner-attested PASS when the historical individual reviewer identity was not retained, provided that this limitation is explicit in `review-signoff.md`.

This is a one-time retrospective governance exception, not the default for future Technical Spike runs. Future runs should retain raw captures prospectively whenever the protocol requires them.

Signed/accepted bundles are immutable; reruns use a new directory.
