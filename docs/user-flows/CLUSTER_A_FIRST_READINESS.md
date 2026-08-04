# Cluster A - First Readiness

**Product:** Music Copilot MVP v1.0  
**Flows:** UF-01, UF-02, UF-03  
**Priority:** P0 - Beta blocking  
**Updated:** 04 August 2026  
**Decision:** Stage 1 Traceability Matrix — **PASS**  
**Final User Flow status:** **Not approved; Issue #3 remains open**  
**Tracking:** GitHub Issue #3

## Meaning of this record

Cluster A connects first launch, permission recovery, and local-library readiness. The PASS means that each Cluster A flow has a complete ten-field traceability record and the corrected connected logic is suitable for continued documentation work.

The PASS does **not** mean that FR-01, ADR governance, Technical Spikes, Figma synchronization, UF-04 through UF-13, final role review, Wireframes, final architecture, or implementation is complete.

## Controlled authority

1. `Music_Copilot_PRD_v1_EN_Clarified.pdf` — official product authority.
2. `Music_Copilot_MVP_v1_EN.pdf` — official priority, User Story, state, acceptance, QA, and release authority.
3. Phase 0 Foundation — approved UF catalog, drafting decisions, and evidence routing.
4. ADR-0001 through ADR-0006 — architectural influence only; every ADR remains Proposed pending Issue #2.
5. System Architecture and Technical Documentation — technical interpretation only; cannot expand PRD/MVP scope.
6. Corrected repository records — canonical current User Flow documentation until Figma is synchronized.

## Artifact register

| Artifact | Role | Status |
|---|---|---|
| [UF-01](UF-01_FIRST_LAUNCH_AND_EDUCATION.md) | Launch, resume, replay, education, readiness handoff | Traceability PASS |
| [UF-02](UF-02_PERMISSION_RECOVERY.md) | Permission, Settings, provider/consent separation, fallback | Traceability PASS |
| [UF-03](UF-03_LIBRARY_READINESS.md) | Scan, returning-state routing, reconciliation, readiness | Traceability PASS |
| [01.5 Combined Flow](mermaid/01_5_COMBINED_CLUSTER_A_FLOW.md) | Canonical connected Cluster A logic | Synchronized Markdown/Mermaid evidence |
| [01.6 Traceability](mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) | Canonical ten-field matrix and requirement/ADR/open-gate record | Stage 1 PASS |
| [01.7 Review Record](mermaid/01_7_REVIEW_AND_APPROVAL_RECORD.md) | Limited gate decision | PASS only for Stage 1 matrix |

Figma URLs remain useful references, but Figma synchronization is **Open**. No older visual snapshot may override the corrected repository behavior or serve as final approval evidence.

## Connected behavior

1. Launch routes from verified startup, onboarding, permission, library, education, and playback state.
2. Onboarding has two slides, is skippable, and is replayable without resetting permissions, library, queue, or active playback.
3. Media access is explained before the first consented scan.
4. Media denial preserves Retry and Not now. Permanent denial preserves Open Android Settings and Not now.
5. Returning from Settings distinguishes `Granted`, `Denied - requestable`, and `Still permanently denied`.
6. A returning user with a verified current Ready or Empty state is shown that state directly; visible Scanning is used only when real scan/reconciliation work is required.
7. MediaStore remains authoritative; the local index remains derived and rebuildable; the playback service remains authoritative for active playback.
8. Six-command education contains exactly `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, and `PLAY_ARTIST`.
9. Microphone permission is just-in-time. Provider availability and cloud-processing consent are separate checks after permission.
10. Every Voice denial/failure preserves manual browsing, queue controls, and service-owned playback.
11. Notification permission stays deferred to UF-06/TS-01.

## Requirement coverage

| Requirement/story | Cluster A mapping | Status |
|---|---|---|
| PRD first-use journey | UF-01, UF-02, UF-03 | Covered at flow/specification level |
| FR-01 consented scan and Songs/Artists/Albums/Folders behavior | UF-01, UF-02, UF-03, **UF-04** | **Partial** — Cluster A covers consent, scan, index, readiness, and recovery; UF-04 must complete browse/search behavior |
| US-01 permission timing and recovery | UF-01, UF-02 | Covered in Cluster A; device/provider evidence remains open |
| US-02 account-free initial/incremental scan | UF-01, UF-03 | Covered at flow/specification level; TS-06 evidence remains open |

No Cluster A artifact claims FR-02 through FR-09 complete.

## ADR influence and governance

| ADR | Cluster A influence | Current status |
|---|---|---|
| ADR-0001 | Native Android 10+, state ownership, platform permission surfaces | Proposed - Revised source |
| ADR-0002 | Service/MediaSession owns active playback; UI observes without restart/duplication | Proposed |
| ADR-0003 | MediaStore authority, derived index, permission/revocation boundaries | Proposed - Revised source |
| ADR-0004 | Replaceable provider and provider/consent/failure isolation | Proposed |
| ADR-0005 | Deterministic allowlist of exactly six Voice intents | Proposed |
| ADR-0006 | No account, backend, cloud sync, catalog, streaming, upload, or download | Proposed |

Issue #2 must record explicit Accepted or Revised outcomes before any ADR is treated as approved architecture.

## Scope, privacy, and accessibility contract

- Android 10+ native app for user-authorized local audio.
- No account, backend, sync, catalog, streaming, upload/download, content distribution, Android Auto, iOS, or v1.1 scope.
- No guaranteed-offline speech claim without provider evidence.
- Cloud speech, if selected, requires separate truthful disclosure of destination, purpose, and retention before processing.
- Raw audio, full transcripts, personal media metadata, paths/URIs, location, contacts, and unnecessary identifiers are excluded from telemetry by default.
- Recovery labels, scalable text, TalkBack order, non-color-only state meaning, and non-coercive permission behavior remain required.

## Open gates

| Gate | Current state | Effect |
|---|---|---|
| UF-04 | Open | Required to complete FR-01 |
| Issue #2 / ADR register | Open | Blocks approved-architecture claims |
| TS-01 | Open | Blocks final notification/foreground-media behavior in UF-06 |
| TS-03 | Open | Blocks provider-specific Voice/cloud claims and final Voice approval |
| TS-06 | Open | Blocks final storage/reconciliation implementation claims |
| Figma synchronization | Open | Visual snapshots are non-canonical until updated |
| UF-04 through UF-13 / Issue #3 | Open | Blocks complete User Flow approval |
| Final reviewer sign-off | Not recorded | No role approval is inferred from document completeness |

## Exit decision

The Stage 1 Cluster A Traceability Matrix is complete and may be used as controlled input for continued User Flow and Golden Scenario drafting. Stage 2 artifacts remain Draft/Planned until their own exit criteria are met. This record does not authorize Wireframes, final architecture, implementation, or Issue #3 closure.
