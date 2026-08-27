# Phase 5 Architecture View and Diagram Audit — Issue #16

- **Status:** **PASS / COMPLETE / SOURCE-ALIGNED**
- **Date:** 27 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Controlling document:** [`docs/SOFTWARE_ARCHITECTURE.md`](../SOFTWARE_ARCHITECTURE.md)
- **Diagram directory:** [`docs/architecture/diagrams/`](diagrams/)
- **Source basis:** directly attached English `MSM2(2).rar`, current Accepted ADR-0001..0008, [Phase 2 Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md), [Phase 3 Decision/Spike Register](DECISION_AND_SPIKE_REGISTER.md), [Phase 4 ADR Audit](ADR_PHASE4_AUDIT.md), approved Wireframes and recovery/system evidence.

## 1. Attached-source integrity

The currently attached `MSM2(2).rar` was re-identified for this Phase 5 run.

- Archive SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- Archive members: 12 English PDF files plus the container directory entry.
- Included English authorities/inputs: `Music_Copilot_PRD_v1_EN_Clarified.pdf`, `Music_Copilot_MVP_v1_EN.pdf`, ADR-0001..0006 PDFs, `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`, `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`, and two English User Flow PDFs.
- This hash matches the already controlled package identity in the Source/Authority Register and the Phase 4 direct-source revalidation.
- **Persian/Farsi PRD/MVP usage in Phase 5: 0.**

Because the attached archive is byte-identical to the English package already source-audited in Phase 4, Phase 5 uses its controlled English source findings together with current GitHub evidence rather than a translation or inferred substitute.

## 2. Required architecture-view inventory

| # | View | Independent `.mmd` | Git blob | Inline in main document | Result |
|---:|---|---|---|---|---|
| 1 | System Context | [`01-system-context.mmd`](diagrams/01-system-context.mmd) | `f02432a6a9516d0b7bef0bfbddae9c22da47fbc3` | Section 4.1 | PASS |
| 2 | Container/Component Architecture | [`02-container-component-architecture.mmd`](diagrams/02-container-component-architecture.mmd) | `2e0b70f0a763b24af6db4bbffd6dd323d6fd746f` | Section 4.2 | PASS |
| 3 | Allowed and Forbidden Dependency Direction | [`03-dependency-direction.mmd`](diagrams/03-dependency-direction.mmd) | `7749c16f3b569d6327704e62de4d394e3897a8e8` | Section 4.3 | PASS |
| 4 | State Ownership | [`04-state-ownership.mmd`](diagrams/04-state-ownership.mmd) | `093ba5e2a4c4428588e963cc11ecc8389fb79dc4` | Section 4.4 | PASS |
| 5 | Playback Ownership and Lifecycle | [`05-playback-ownership-lifecycle.mmd`](diagrams/05-playback-ownership-lifecycle.mmd) | `6c178837712d5e4e4d5f6ae9bd86d4eb24816369` | Section 4.5 | PASS |
| 6 | Navigation and Voice Overlay | [`06-navigation-voice-overlay.mmd`](diagrams/06-navigation-voice-overlay.mmd) | `b3647ecc8ea2bf5f59261add7dfe62ecfcdc6469` | Section 4.6 | PASS |
| 7 | Critical Data Flows | [`07-critical-data-flows.mmd`](diagrams/07-critical-data-flows.mmd) | `e933c1938b8b06db7e9411adbefc9cd90a7cc272` | Section 4.7 | PASS |
| 8 | Privacy and Telemetry Boundary | [`08-privacy-telemetry-boundary.mmd`](diagrams/08-privacy-telemetry-boundary.mmd) | `9ca5851a2005c918084be0ad7191c2ccf5a47b2c` | Section 4.8 | PASS |

**Required view families present:** 8/8.  
**Independent Mermaid sources present:** 8/8.  
**Main-document Mermaid blocks present:** 8/8.

The independent `.mmd` content and the matching main-document Mermaid block use the same graph source for each view. A later diagram edit must update both in the same change; the textual architecture/ADRs remain the authority if a visual/source mismatch is introduced.

## 3. Critical Data Flow coverage

| Mandatory Phase 5 path | Diagram flow | Architecture path / authority | Recovery/ownership result |
|---|---|---|---|
| MediaStore scan → normalization → Room → Library UI | F1 | AP-03; ADR-0003/0008 | MediaStore remains truth; Room is rebuildable projection; UI observes projection |
| Track selection → queue → MediaSessionService → ExoPlayer | F2 | AP-04; ADR-0002/0007 | Live queue and Player remain service-owned |
| Manual command → controller → service → synchronized state | F3 | AP-04/AP-05; ADR-0002/0007 | One serialized command path; UI/system surfaces observe authoritative snapshot |
| Voice → STT → mapper → confirmation/execution → playback → TTS | F4 | AP-07/AP-08/AP-09; ADR-0004/0005/0007 | deterministic six-intent mapping, at most one confirmation, one Retry, playback acknowledgement before success TTS |
| Call/Focus/Route interruption → safety policy → synchronized surfaces | F5 | AP-10; ADR-0002/0007 | four-guard conditional resume; route reconnect remains Safe Paused unless explicit Resume |
| Typed telemetry → privacy filter → optional quality adapter | F6 | AP-11; ADR-0003/0004/0006/0008 | allowlist gate; quality non-authoritative and non-blocking |
| Permission revocation → index reconciliation → truthful UI recovery | F7 | AP-02/AP-03; ADR-0001/0003/0004 | fresh platform truth, stale projection invalidation, Retry/Settings recovery without fabricated readiness |
| Corrupt file → controlled skip → queue continuation | F8 | AP-04/AP-10; ADR-0002/0003/0007 | typed failure, bounded skip, continue next valid item or Safe Paused queue end |

An additional F9 flow preserves the already-approved P1 cold-restoration contract: versioned Proto DataStore snapshot → schema check → MediaStore re-query → invalid-item removal/position clamp → **Paused-only** queue preparation. It does not upgrade FR-08 to P0.

**Mandatory Phase 5 flows covered:** 8/8 PASS.  
**Previously required P1 restore path preserved:** PASS.

## 4. ADR/text consistency audit

| Architecture invariant | View evidence | Text/ADR control | Result |
|---|---|---|---|
| Native Android/local-media MVP; no product backend/account/sync/streaming/catalog | System Context; Dependency | ADR-0001/0006; source lock | PASS |
| Player/MediaSession independent of UI and service-owned | Component; State; Playback Lifecycle; F2/F3 | ADR-0002/0007 | PASS |
| Same process does not mean UI-owned; no second Player authority | Playback Lifecycle | ADR-0007 | PASS |
| All playback mutation serialized through one service command path | Component; Playback Lifecycle; F3/F4 | ADR-0007 | PASS |
| P0 UI recreation/background continuity is separate from P1 cold restore | Playback Lifecycle; F9 | ADR-0002/0007; FR-08 priority split | PASS |
| Persisted restore is non-authoritative, MediaStore-validated and never autoplays | State; Playback Lifecycle; F9 | ADR-0007 | PASS |
| MediaStore authoritative; Room derived/rebuildable; live queue not in Room | Component; State; F1/F2 | ADR-0003/0008 | PASS |
| Voice provider replaceable; deterministic mapper remains execution gate | Component; Dependency; F4 | ADR-0004/0005/0007 | PASS |
| One active Voice generation; stale/cancelled callbacks cannot mutate playback | Component; Playback/Voice path | ADR-0007 | PASS |
| Hilt is not turned into an irreversible architecture decision | Dependency view text | ADR-0008 + attached Technical Blueprint boundary | PASS |
| Telemetry/quality never becomes product or safety authority | Dependency; Privacy; F6 | ADR-0003/0006/0008; Analytics/Privacy contract | PASS |
| Prohibited Voice/media/identity content does not cross telemetry boundary | Privacy | ADR-0003/0004/0006; Analytics/Privacy contract | PASS |
| Manual-Only/NoOp remain foundation fail-closed defaults, not Beta completion | System/Privacy view annotations + main text | Phase 4 source correction; English MVP Done/measurement requirements | PASS |
| P2 Liked Songs remains removable/non-blocking | Navigation; main text | ADR-0008; Phase 2 traceability | PASS |

**Material text/ADR contradictions found:** 0.

## 5. Wireframe consistency audit

The diagrams were checked against the approved repository-native Wireframe contract rather than inventing screen/system ownership.

| Wireframe rule | Phase 5 representation | Result |
|---|---|---|
| Voice states are Listening, Processing, Confirming, Executed and Failed without a new complex destination | Navigation/Voice Overlay + F4 | PASS |
| Voice ambiguity is bounded; no guessed execution | F4 direct/ambiguous/failure split | PASS |
| Voice timeout gets one explicit Retry then manual fallback | F4 | PASS |
| Playback changes only after MediaSession acknowledgement | Component/F4 | PASS |
| Driving Mode has one fixed interaction layer and immediate Exit | Navigation view note/Exit path | PASS |
| No permission/provider setup is opened inside active Driving Mode | Navigation uses external system handoff from Permissions/Settings only | PASS |
| UI recreation reconnects to one complete service snapshot and never creates a second Player | Playback Lifecycle | PASS |
| Notification, lock screen and external controls converge on the same MediaSession | Playback Lifecycle/F3 | PASS |
| Route reconnect never auto-resumes | Playback Lifecycle/F5 | PASS |
| Failure recovery reaches manual controls or service-owned Safe Paused | F4/F5/F7/F8 and lifecycle view | PASS |
| Android permission dialogs, Settings, notification/lock-screen and external controls remain system-owned | System Context + Navigation view | PASS |

The controlling Wireframe navigation audit records 32/32 app Entry/Exit/Back, 13/13 system/recovery authority records, bounded Voice confirmation/retry, one-layer Driving Mode, service-authoritative recreation and zero contradictory playback ownership/auto-resume rules. Phase 5 mirrors those rules rather than redefining them.

**Material Wireframe contradictions found:** 0.

## 6. Privacy/telemetry audit

The Privacy view and F6 preserve the existing allowlist boundary:

- permitted data is typed/coarse (`intent_type`, confidence/latency buckets, stable non-personal error code, coarse route type, duration/count fields);
- raw audio/transcripts, artist/album/folder/title, paths/content URIs/MediaStore IDs, search data, queue contents, precise playback position/location, contacts/call identity, Bluetooth/vehicle identity, credentials/tokens/secrets are prohibited;
- any remote speech path remains behind separate capability/disclosure/consent approval;
- quality failures are non-authoritative and cannot delay playback acknowledgement, Voice cancellation, permission recovery, interruption handling or route safety;
- NoOp quality is a safe foundation state only and cannot be claimed as final Beta measurement completion.

**Privacy boundary contradiction or scope expansion:** 0 / PASS.

## 7. Mermaid/source control checks

- All eight independent files begin with Mermaid `flowchart` syntax and are plain UTF-8 `.mmd` sources.
- Required view IDs are unique and paths are stable.
- The main architecture document links all eight independent sources and embeds the corresponding Mermaid graph for human review.
- No diagram introduces a new ADR-level decision; consequential decisions remain in ADR-0007/0008 and compatible policies remain textual in the main architecture.
- No physical-device/OEM/vehicle/provider/legal/Beta execution is inferred from a diagram.
- This audit is a source/consistency check; it does not claim a separate external Mermaid CLI/browser rendering run.

## 8. Phase 5 gate

| Gate | Expected | Observed | Result |
|---|---:|---:|---|
| Required architecture view families | 8 | 8 | PASS |
| Independent `.mmd` sources | 8 | 8 | PASS |
| Inline main-document Mermaid views | 8 | 8 | PASS |
| Mandatory Critical Data Flows | 8 | 8 | PASS |
| Existing P1 cold-restore flow preserved | 1 | 1 | PASS |
| Material ADR/text contradictions | 0 | 0 | PASS |
| Material Wireframe contradictions | 0 | 0 | PASS |
| Hidden consequential decisions introduced only by diagrams | 0 | 0 | PASS |
| Persian/Farsi authority used | 0 | 0 | PASS |
| New v1.1 product scope introduced | 0 | 0 | PASS |

**PHASE 5: PASS / COMPLETE / SOURCE-ALIGNED.** The required Architecture Views and Critical Data Flows are now maintained both inline and as independent Mermaid sources, and the Phase 5 PASS condition is satisfied: the main diagrams do not contradict the controlling text, Accepted ADRs or approved Wireframe behavior.

This Phase 5 result does **not** complete Issue #16 or unblock Issue #4. The remaining contract/specification, cross-role review and final merge/exit gates still apply.