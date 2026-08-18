# Cluster B — Source Authority and Provenance

**Scope:** UF-04 Browse/Search → UF-05 Manual Playback/Queue → UF-06 Background/System Controls  
**Status:** **ACTIVE AUTHORITY RECORD**  
**Updated:** 18 August 2026  
**Tracking:** GitHub Issue #3

## Source policy

Cluster B must be evaluated, maintained, and reviewed against the **English MSM2 baseline** and the current controlled GitHub repository records.

## MSM2 baseline

The project-owner supplied archive `MSM2.rar` is the packaged baseline for this review. Its controlled source set is:

1. `Music_Copilot_PRD_v1_EN_Clarified.pdf`
2. `Music_Copilot_MVP_v1_EN.pdf`
3. `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`
4. `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`
5. `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
6. `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf`
7. `ADR-0001_Native_Android_Platform_Revised.pdf`
8. `ADR-0002_Service_Owned_Media3_Playback.pdf`
9. `ADR-0003_Local_First_Data_Boundaries_Revised.pdf`
10. `ADR-0004_Replaceable_Voice_Provider.pdf`
11. `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf`
12. `ADR-0006_No_Backend_Account_or_Streaming.pdf`

## GitHub authority

GitHub `main` is the current operational record and status authority. For Cluster B, the relevant controlled repository authorities are:

- `docs/PRD.md` — repository representation of the authoritative PRD v1.0.
- `docs/MVP.md` — repository representation of the authoritative MVP v1.0.
- `docs/TECHNICAL_DECISIONS.md` and `docs/adr/` — accepted ADR decision/status record.
- `docs/SOFTWARE_ARCHITECTURE.md` — downstream architecture baseline; open Issue #16 mechanics must not silently rewrite approved User Flow behavior.
- `docs/USER_FLOWS.md` and `docs/user-flows/` — canonical User Flow, State Inventory, Traceability, review, and evidence records.
- `docs/user-flows/evidence/` — Cluster B Technical Spike protocols and accepted evidence bundles.

When an older packaged snapshot and current GitHub status metadata differ, product/technical content remains bounded by the MSM2 baseline, while current GitHub decision/status records control present workflow state. A later GitHub change may alter the baseline only through the project's explicit product/ADR/governance process; it may not silently introduce v1.1 or out-of-scope behavior.

## Cluster B source-derived constraints

The Cluster B records must preserve the following source constraints:

- Android 10+ only for MVP v1.0.
- Lawful local audio only; no online catalog, streaming, download, account, backend, or sync.
- Browse/Search over local Songs, Artists, Albums, and Folders.
- Basic Player behavior: Play, Pause, Previous, Next, Seek, and Queue.
- Background and screen-off playback with truthful MediaSession/system controls.
- Service-owned Media3 playback independent of UI lifecycle.
- MediaStore as accessible-media authority; derived local index remains rebuildable.
- P0 active playback continuity remains distinct from P1 queue/position restore.
- Calls, Audio Focus, headset disconnect, and route recovery remain downstream UF-11/FR-03 behavior rather than being invented inside Cluster B.
- Android Auto, Cast, iOS, streaming, backend/account/sync, advanced playlist/social behavior, and v1.1 functionality remain outside Cluster B MVP scope.

## Cluster B decision

The source-authority re-audit does **not** reopen Cluster B. UF-04, UF-05, UF-06, connected-flow integration, shared State Inventory, shared Traceability, TS-01/TS-02/TS-06/TS-08 evidence, and Cluster B sign-off remain **PASS / COMPLETE** under this authority policy.

The next active stage remains **Cluster C → UF-07 Driving Mode**.
