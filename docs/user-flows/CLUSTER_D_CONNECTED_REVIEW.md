# Cluster D - Connected Review and Exit Record

**Scope:** UF-08 Direct Voice -> UF-09 Ambiguity/Not Found -> UF-10 Timeout/Unavailable/Retry  
**Date:** 18 August 2026  
**Tracking:** GitHub Issue #3 / PR #26  
**Decision:** **PASS / COMPLETE at User Flow and executable spike-contract level**

## Artifacts reviewed

- `CLUSTER_D_SOURCE_AUTHORITY.md`
- `UF-08_DIRECT_VOICE.md`
- `UF-09_AMBIGUITY_NOT_FOUND.md`
- `UF-10_TIMEOUT_RETRY.md`
- `CLUSTER_D_STATE_INVENTORY.md`
- `CLUSTER_D_TRACEABILITY_MATRIX.md`
- `mermaid/CLUSTER_D_VOICE_FLOW.md` and `.mmd`
- `evidence/TS-03_SPEECH_PROVIDER_FEASIBILITY.md`
- `evidence/TS-07_ARTIST_MATCHING.md`
- `evidence/TS-09_CONFIDENCE_DEFAULT.md`
- `evidence/CLUSTER_D_EVIDENCE_SUMMARY_2026-08-18.md`
- Android reference harness voice contracts and tests

## Connected path under review

1. UF-07/Full Player supplies the initiating surface and authoritative playback snapshot.
2. Explicit mic action checks permission and the approved speech mode before capture.
3. Listening ducks playback for no more than six seconds and remains cancellable.
4. STT supplies transient text/confidence/failure only; the deterministic mapper owns interpretation.
5. A certain supported result submits one command and becomes Executed only after service acknowledgement.
6. One/two plausible candidates enter a single bounded confirmation; no candidate enters Not Found/Unknown/Unsupported with no command.
7. First eligible recognition failure offers one explicit retry; any second failure is terminal manual fallback.
8. Every terminal state releases ducking, ignores stale callbacks, preserves service truth, and returns to the initiating manual surface.
9. Calls/focus/routes hand to UF-11; permission/provider/privacy setup hands to UF-12 outside active Driving Mode.

## Review matrix

| Criterion | Result |
|---|---|
| English MSM2/GitHub authority and exclusions fixed | PASS |
| Exactly six English intents; no wake word/open conversation | PASS |
| Overlay/session ownership; no Voice navigation destination | PASS |
| Explicit mic, six-second bound, duck/release, Cancel | PASS |
| Certain direct execution only after service acknowledgement | PASS |
| Low confidence/multiple artists -> one confirmation | PASS |
| Unknown/unsupported/missing/not-found never approximate | PASS |
| One retry maximum; no automatic retry or Settings | PASS |
| Manual controls/Exit and playback preservation | PASS |
| TTS one sentence and failure non-rollback | PASS |
| Provider/language/network/consent/retention fail-closed policy | PASS |
| No generic offline claim; `EXTRA_PREFER_OFFLINE` not treated as proof | PASS |
| TS-07 deterministic artist ordering and ambiguity boundary | PASS |
| TS-09 safe default/range/remote-failure behavior | PASS |
| TS-03 host/AOSP fallback policy | PASS |
| CD-01 through CD-24 field coverage | PASS |
| Traceability and connected handoffs | PASS |
| Mermaid Markdown/raw logical parity | PASS |
| Privacy/telemetry allowlist | PASS |
| Accessibility/driving constraints | PASS |
| MVP exclusions | PASS |

## CI and evidence truth

Workflow run #8 (`32152607787`) passed on code head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`: build, strict lint, host unit tests, debug APK, and 4/4 AOSP instrumentation tests on each of API 29, 31, and 34 (12/12 total).

No physical/OEM provider availability, audio capture quality, processing destination/retention, real-library artist accuracy, road noise, vehicle, or Beta KPI is inferred from host/AOSP automation. Production enablement remains fail-closed and routed to Issues #10/#11/#12/#13/#16.

## Exit decision

**PASS — Cluster D / UF-08 through UF-10 is COMPLETE / SOURCE-ALIGNED at the User Flow and executable spike-contract stage.** Issue #3 remains open for UF-11 through UF-13 and final complete-set approval.
