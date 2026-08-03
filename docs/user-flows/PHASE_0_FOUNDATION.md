# Music Copilot - User Flow Phase 0 Foundation

**Version:** 1.0  
**Date:** 01 August 2026  
**Status:** Phase 0 complete - approved for User Flow drafting  
**Product version:** Music Copilot MVP v1.0  
**Tracking:** GitHub Issue #3  
**Workfile:** Figma - `User Flows / State Inventory`  
**Product authority:** English PRD v1.0 and English MVP v1.0

> This artifact completes the input, scope, state, assumption, conflict, decision, and coverage foundation required before drawing MVP v1.0 User Flows. It authorizes flow drafting. It does not approve final User Flows, low-fidelity Wireframes, the final Software Architecture baseline, or implementation.

---

## 0. Completion Decision

Phase 0 is **complete** because every identified question now has one of four controlled outcomes:

1. an approved user-visible behavior for User Flow drafting;
2. a fixed product constraint inherited from PRD/MVP;
3. an explicit technical spike with an owner and evidence gate; or
4. an explicit transfer to Issue #2, #15, or #16.

### Gate status

| Gate | Current state | Meaning |
|---|---|---|
| Product scope - Issue #1 | Done | PRD/MVP v1.0 scope is locked. |
| Preliminary ADR gate - Issue #2 | Open / In Progress | ADRs constrain drafting but are not yet binding as final accepted records. |
| Phase 0 foundation - Issue #3 | Complete | User Flow drafting may start from this artifact. |
| Complete User Flows - Issue #3 | Not yet complete | UF-01 through UF-13 must still be drawn, reviewed, and approved. |
| Low-fidelity Wireframes - Issue #15 | Backlog | Starts after stable User Flows. |
| Final Software Architecture - Issue #16 | Backlog | Starts after User Flows and low-fidelity Wireframes are stable. |
| Android foundation - Issue #4 | Blocked | Must remain blocked until Issue #16 is approved. |

### Approval boundary

- **Approved now:** source hierarchy, MVP scope, priorities, fixed constraints, surface/state inventory v0, provisional user-visible routing and recovery policies for User Flow drafting, flow catalog, terminology, conflict routing, and spike list.
- **Not approved now:** numeric confidence/fuzzy thresholds, exact `Previous` position threshold, final provider selection, persistence representation, process-death limits, exact retention periods, final navigation graph and Back behavior, final Voice overlay/state/navigation form and ownership, ViewModel/use-case boundaries, package/module layout, or any app code.
- **Final-flow blocker:** Issue #2 and the ADR register must agree on explicit ADR outcomes before the complete User Flow set is marked Approved.

### Architecture deferral clarification

- User Flows may record the user-visible destination or recovery expectation, but they do not select the final navigation graph, Back-stack implementation, or destination ownership.
- Voice feedback is represented provisionally as state associated with the initiating playback experience. Whether it is implemented as an overlay, screen state, or another approved presentation pattern remains open for Issue #15 and Issue #16.
- Final Voice state ownership, shared-state boundaries, and navigation responsibility must be approved in Issue #16; no coordinator, ViewModel, destination, or package name is binding at Phase 0.

---

## 1. Source Register

### 1.1 Authority order

| Rank | ID | Source | Status | Governs |
|---:|---|---|---|---|
| 1 | P1 | `Music_Copilot_PRD_v1_EN_Clarified(1).pdf` | Approved for MVP | Product problem, users, experience, scope, FR-01 through FR-09, safety, privacy, metrics, and proposed technology. |
| 2 | P2 | `Music_Copilot_MVP_v1_EN(2).pdf` | Ready for Planning | Priorities, user stories, acceptance, ten-week timebox, QA, Beta, telemetry, and Go/No-Go. |
| 3 | A1 | `ADR-0001_Native_Android_Platform_Revised.pdf` | Proposed - Revised | Android 10+, native platform, Kotlin/Compose baseline, and replaceable engineering choices. |
| 3 | A2 | `ADR-0002_Service_Owned_Media3_Playback.pdf` | Proposed | Service-owned player, MediaSession authority, UI observation, and playback continuity. |
| 3 | A3 | `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | Proposed - Revised | MediaStore authority, rebuildable index, local state, and telemetry boundaries. |
| 3 | A4 | `ADR-0004_Replaceable_Voice_Provider.pdf` | Proposed | STT/TTS interfaces, voice session limits, failure isolation, and consent. |
| 3 | A5 | `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | Proposed | Exactly six intents, deterministic mapping, no guessing, and one confirmation. |
| 3 | A6 | `ADR-0006_No_Backend_Account_or_Streaming.pdf` | Proposed | No backend, account, sync, catalog, streaming, upload, download, or distribution. |
| 4 | S1 | `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | Proposed synthesis | Cross-source architecture interpretation and open final-gate decisions. |
| 5 | T1 | `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | Proposed blueprint | Engineering contracts, verification seams, traceability, and delivery governance. |
| 6 | G | GitHub repository and Issues | Workflow evidence | Current issue state, ADR register state, document links, gates, and delivery dependencies. |
| Aid only | F1/F2 | Persian PRD/MVP PDFs | Reading companions | Translation support only; they do not override P1 or P2. |

### 1.2 Conflict rule

If an ADR, architecture document, technical document, repository document, Issue, Figma artifact, or implementation proposal conflicts with P1/P2, **P1/P2 win**. A derived artifact may narrow an implementation choice but may not add product scope.

### 1.3 Evidence snapshot

- Repository: `latticeward-labs/music-copilot` - private, default branch `main`.
- Issue #2: open and `In Progress` in its metadata.
- Issue #3: open and `Ready` in its metadata before Phase 0 registration.
- Issue #15: open and Backlog.
- Issue #16: open and Backlog.
- `docs/adr/README.md`: all six ADRs are `Proposed`.
- `docs/TECHNICAL_DECISIONS.md`: preliminary review in progress.
- `docs/USER_FLOWS.md`: repository index exists; the complete flow set is not approved.

---

## 2. Scope and Priority Matrix

### 2.1 P0 - Beta-blocking flow coverage

| Capability | Required flow coverage | Cut rule |
|---|---|---|
| First use | Two-slide introduction, contextual permissions, library scan, six-command education, Home/Library readiness | Cannot be omitted. |
| Permission recovery | Media, microphone, and notification Granted / Denied / Permanently Denied; Settings return | Cannot crash or dead-end. |
| Local library | Initial and incremental scan; songs, artists, albums, folders; Scanning / Empty / Ready / recoverable failure | Cannot be omitted. |
| Search | Query, Result, No Result, Error, track selection, and playback start | Cannot be omitted. |
| Manual playback | Queue creation, play, pause, next, previous, seek, Full Player, corrupt item, and end of queue | Cannot be omitted. |
| Background/system control | UI close/recreation, screen off, notification, lock screen, Bluetooth/head unit, controller reconnection | Cannot be omitted. |
| Driving Mode | Manual entry, Playing / Paused / No Track, one-layer controls, immediate exit | Cannot be omitted. |
| Voice | Mic tap, Listening, Processing, direct execution, Confirming, Failed, one retry, TTS fallback | Cannot be omitted. |
| Interruptions/routes | Calls, Audio Focus, headset/Bluetooth disconnect, reconnect, route change, no speaker leakage | Cannot be omitted. |
| Settings/privacy | Bluetooth selection, voice disclosure/consent, clearing history/diagnostics, privacy controls | Cannot be omitted where data is collected. |
| Safety/accessibility | No driving typing/deep navigation/long lists; 48dp minimum; scalable text; TalkBack; short feedback | Any serious violation blocks Beta. |

### 2.2 P1 - Included but removable before P0

| Capability | Phase 0 policy |
|---|---|
| Bluetooth entry suggestion - FR-07 | Suggest only after a user-selected device connects. Never open Driving Mode automatically. |
| Queue and track-position restoration - FR-08 | Restore as a non-playing state with an explicit Resume action. Active playback continuity remains a separate P0 behavior. |

### 2.3 P2 - First scope cut

| Capability | Phase 0 policy |
|---|---|
| Like / Liked Songs - FR-09 | Keep UF-13 and its Empty/Ready states visible, label them P2, and remove them first if capacity is unavailable. |

### 2.4 In scope but not allowed to create hidden priority

- Short local playback history is permitted by the product scope and must be clearable if collected.
- It has no standalone required screen and no explicit P0 functional-requirement ID.
- Its implementation must not displace P0 work; Product owns its final scheduling treatment before Issue #16.

### 2.5 Explicitly excluded

Account, sign-up, authentication, product backend, cloud sync, cross-device restore, online catalog, streaming, music download/upload/distribution, Cast, iOS, Android Auto, CarPlay, Kotlin Multiplatform, Flutter, React Native, wake word, always-listening, open conversation, multi-turn clarification, LLM execution decisions, recommendations, advanced playlists/social, EQ, crossfade, DSP, motion detection, song/humming recognition, and generative music.

---

## 3. Fixed Product Constraints

These rules appear on every flow review checklist.

1. MVP v1.0 is a native Android 10+ product for lawful, user-authorized local audio.
2. No user account or product backend is required at any point.
3. Core local browsing and playback do not depend on network availability or an external product service.
4. Playback remains active and synchronized when the UI closes, recreates, backgrounds, or the screen turns off.
5. The service/MediaSession is the playback authority; UI surfaces observe it and do not create a second authoritative state.
6. An unexpected headset or Bluetooth disconnect pauses immediately and must not leak playback to the phone speaker.
7. Driving Mode is one layer with title, artist, artwork, microphone, four large controls, and immediate exit.
8. Driving Mode contains no typing, deep navigation, nested menu, or long result list.
9. Voice begins only after explicit microphone action; there is no wake word.
10. Listening lasts no more than six seconds, playback ducks while listening, and recognition retry is allowed once.
11. Exactly six English intents exist: `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST`.
12. A certain supported command executes directly. Ambiguity allows one short confirmation. Unknown or unsupported commands never execute by approximation.
13. TTS is no more than one short sentence. TTS failure does not invalidate an already executed command.
14. Manual controls remain immediately available after voice failure.
15. Raw audio is not stored by default and never enters analytics, crash reports, or logs.
16. Full personal file titles, full transcripts by default, precise location, contacts, and unnecessary identifiers are prohibited from telemetry.
17. Cloud speech processing, if used, requires separate consent plus destination, purpose, and retention disclosure.
18. Local-first does not mean that STT is guaranteed offline; the UI and copy must not make that claim without validated provider evidence.
19. A corrupt item is skipped in a controlled way; the remaining queue continues where possible.
20. P0 behavior cannot be traded for P1/P2 scope. Any incomplete P0 blocks Beta.

---

## 4. Application Surface and State Inventory v0

### 4.1 Entry, behavior, transition, and recovery

| ID | Surface / required states | Trigger and visible state | Allowed user action / system action | Success, failure, and recovery |
|---|---|---|---|---|
| S-01 | **Splash** - Loading, Migration Error | Cold/warm start. Shows short preparation only. | Wait; retry on recoverable migration failure. System validates local schema, permissions, service connection, and next destination. | Ready routes by startup policy. Failure shows concise recovery; never fabricates library/player state. |
| S-02 | **Onboarding** - First Run, Skip | First install or explicit replay from Settings. Two value slides and road-attention warning. | Continue or Skip; replay later from Settings. System records completion locally. | Continue enters contextual permission/scan sequence. Skip never forces microphone access and routes to media setup/Home recovery. |
| S-03 | **Permissions** - Granted, Denied, Permanently Denied | A capability is first needed. Copy explains one permission at a time. | Continue, Not now, Retry, Open Android Settings. System requests/rechecks the applicable platform access. | Granted continues the blocked action. Denied preserves a retry/manual path. Permanently Denied exposes Settings; return triggers recheck. |
| S-04 | **Home / Library** - Scanning, Empty, Ready | Default application destination. One destination with Songs, Artists, Albums, and Folders internal views. | Browse, rescan, search, select media, open Full Player/Driving Mode/Settings. System observes scan and playback state. | Ready supports selection. Empty explains local-file requirement and scan/permission recovery. Scan failure remains recoverable. |
| S-05 | **Search** - Result, No Result, Error | Search opened from Home/Library; not available as typing inside Driving Mode. | Enter query, select result, clear query, retry. System searches the derived local index only. | Selection builds/updates queue and opens playback. No Result preserves query. Error offers retry without corrupting library. |
| S-06 | **Full Player** - Playing, Paused, Corrupt File | Opened from track selection, mini-player, or external media control fallback. | Play/pause, next, previous, seek, open queue, enter Driving Mode, back. Commands go through the shared playback command surface. | The provisional user-visible return target is the preceding non-driving app surface, with Home/Library as fallback; playback continues. Final Back-stack and navigation implementation remain open for Issue #15 and Issue #16. Corrupt file is skipped and explained briefly. |
| S-07 | **Driving Mode** - Playing, Paused, No Track | Manual P0 entry or accepted P1 Bluetooth suggestion. Never automatic. | Four fixed controls, microphone, and immediate Exit only. | Exit is immediate. The provisional user-visible return target is the most recent non-driving surface, with Home/Library as fallback. Final system Back equivalence, Back-stack behavior, and navigation implementation remain open for Issue #15 and Issue #16. No Track offers Play fallback if a valid last track exists, otherwise an exit/manual recovery. |
| S-08 | **Voice Feedback State** - Listening, Processing, Confirming, Executed, Failed | Microphone tap from an allowed playback surface. Voice feedback is provisionally represented in association with the initiating playback experience; its final overlay, screen-state, or navigation form remains open for Issue #15 and Issue #16. | Cancel; provide one spoken command; answer one bounded confirmation; retry once after recognition failure. | Terminal behavior returns control to the initiating playback experience. Certain intent executes directly. Unknown/unsupported preserves playback. Failed returns immediately to manual control. Final presentation and navigation ownership remain open for Issue #16. |
| S-09 | **Liked Songs** - Empty, Ready - P2 | Opened from Home/Library only if P2 ships. | Like/unlike, select track, play list, back. | Empty explains the list. Ready plays a local queue. Missing files reconcile safely. Entire surface is removable without breaking P0 flows. |
| S-10 | **Settings** - Default, Updated | Opened from non-driving UI; deep settings are not opened from active Driving Mode. | Select Bluetooth hint device, review/replay education, manage voice consent, clear history/diagnostic data, open Android Settings. | Update is stored locally. Clear actions require confirmation and show completion. Settings return rechecks access/consent. |

### 4.2 Ownership, persistence, evidence, and constraints

| ID | Source of truth / persistence expectation | Analytics and privacy | Accessibility / driving constraint |
|---|---|---|---|
| S-01 | Local schema/repositories; no playback authority. Onboarding completion persists. | No personal file metadata. Migration failure uses typed coarse code only if allowed. | Short text; retry is labeled and reachable. |
| S-02 | Local completion flag; replay remains available. | Do not create a product identity. | Scalable text, TalkBack order, explicit road-attention copy. |
| S-03 | Android permission state is authoritative; explanation state is local UI state. | Permission outcome is not permission to collect sensitive media metadata. | One purpose per prompt; no coercive loop. Driving Mode never opens Android Settings. |
| S-04 | MediaStore owns media existence; local DB index is derived/rebuildable; playback service owns mini-player state. | Never send full titles/library contents. | Smooth at 5,000 files; scalable list; all actionable icons labeled. |
| S-05 | Derived local index; selected track resolves to accessible MediaStore identity. | Query/transcript is not logged by default. | No search typing during Driving Mode. |
| S-06 | MediaSessionService owns playing/current/queue/position. P1 restore data is local. | `player_error` uses coarse code/route only. | 48dp minimum; content descriptions; no hidden playback state. |
| S-07 | MediaSessionService owns playback; mode UI observes it. Mode entry state may persist only as a preference if approved. | `driving_mode_started(entry_source, bt_connected)` only. | One layer, high contrast, limited motion, large fixed controls, immediate exit. |
| S-08 | Playback remains service-owned. The owner of ephemeral voice/confirmation state, its public contract, and its overlay/screen/navigation presentation are not finalized and remain for Issue #16. | Allowed: intent type, confidence/latency buckets, result. Prohibited: raw audio and full transcript by default. | Short feedback; no long choices; one confirmation; manual controls remain visible/reachable. |
| S-09 | Local application persistence; media validity still comes from MediaStore. | Likes remain local; no sync/account profile. | P2 label stays visible in design/review artifacts. |
| S-10 | Preferences repository/DataStore baseline. Data deletion affects only the selected app-owned categories. | Consent and diagnostic controls are explicit. No hidden remote deletion promise where no remote product data exists. | Destructive clear action has accessible confirmation; unavailable while actively driving. |

### 4.3 Android and system surfaces

| ID | System surface / trigger | Required behavior | Recovery / invariant |
|---|---|---|---|
| SYS-01 | Android media permission dialog | Trigger after media-purpose explanation and before first scan. | Denial returns to app explanation; permanent denial offers Settings. |
| SYS-02 | Android microphone permission dialog | Trigger just-in-time at the first actual voice action or controlled command practice. | Denial preserves all manual playback. No microphone starts before user action. |
| SYS-03 | Android notification permission dialog, where applicable | Trigger only after value exists - first successful playback/background-control handoff - and after an app-level explanation. | Exact Android-version behavior is validated in TS-01. Denial must not crash or falsify player state. |
| SYS-04 | Android Settings | Open only after explicit user action for permanent denial or provider configuration. | On return/resume, recheck. Granted continues the blocked action; still denied returns to explanation/manual fallback. |
| SYS-05 | Media notification | Observe the MediaSession state and expose real play/pause/next/previous. | Controller failure shows recoverable state; never creates a second queue/player. |
| SYS-06 | Lock-screen media controls | Same authoritative state and commands as notification. | UI recreation cannot desynchronize visible state. |
| SYS-07 | Bluetooth/head-unit controls | Use shared playback command surface. | Disconnect pauses immediately; no unintended phone-speaker playback. Reconnect remains paused until explicit resume. |
| SYS-08 | Phone call / Audio Focus | Apply approved pause/duck policy and record pre-interruption state. | Resume only when the user had been playing, focus returns, route is still valid, and the user did not manually pause; otherwise remain paused. Validate in TS-04. |
| SYS-09 | Audio route change | Observe route, synchronize surfaces, and log only coarse route category. | Wrong-route or speaker-leak behavior is P0 and blocks Beta. |

---

## 5. Assumption and Decision Log

`Approved` means approved for User Flow drafting. `Routed` means the routing and provisional user-visible boundary are approved, but the named technical decision remains open.

| ID | Decision / approved outcome | Type | Owner | Status | Gate / affected flows |
|---|---|---|---|---|---|
| D-001 | English PRD and MVP are the only product authorities. | Governance | Product | Approved | All flows |
| D-002 | Phase 0 may finish while Issue #2 remains open; complete User Flow approval may not. | Governance | Product + Android | Approved | Issue #2 / #3 |
| D-003 | ADR PDF outcome templates are not current status evidence; GitHub register plus explicit Issue #2 evidence determines status. | Governance | Android + Product | Routed | Issue #2 |
| D-004 | `Home / Library` is one top-level destination with Songs, Artists, Albums, and Folders internal views. | UX | Product + Design | Approved | UF-03, UF-04 |
| D-005 | Returning-user start routes to the appropriate Home/Library state; an active service is observed, never restarted. | UX / lifecycle | Design + Android | Approved | UF-03, UF-06 |
| D-006 | Media permission is explained and requested before the first scan. | UX / privacy | Product + Design | Approved | UF-01, UF-02, UF-03 |
| D-007 | Microphone permission is requested just-in-time on first real voice action or controlled practice, not blindly at app start. | UX / privacy | Product + Privacy | Approved | UF-01, UF-02, UF-08 |
| D-008 | Notification permission is contextual after the user has started playback and before background/system-control value is needed; platform details require TS-01. | UX / technical | Design + Android | Routed | UF-02, UF-06 |
| D-009 | Onboarding can be skipped; education can be replayed from Settings. Skipping never grants or forces microphone access. | UX | Product + Design | Approved | UF-01, UF-12 |
| D-010 | Provisional user-visible policy: leaving Full Player returns toward the preceding non-driving app experience, with Home/Library fallback, while playback continues. Final Back-stack, navigation graph, and destination ownership remain open. | UX / navigation | Design + Android | Routed | Issue #15 / Issue #16 / UF-05 |
| D-011 | Driving Mode entry is manual P0. Bluetooth is P1 suggestion-only and requires explicit acceptance. | UX / safety | Product + Design | Approved | UF-07 |
| D-012 | Provisional user-visible policy: Driving Mode provides immediate Exit and returns toward the most recent non-driving experience, with Home/Library fallback. Final system Back equivalence, Back-stack behavior, and navigation implementation remain open. | UX / navigation / safety | Design + Android | Routed | Issue #15 / Issue #16 / UF-07 |
| D-013 | Voice feedback is provisionally associated with the initiating playback experience. Whether it is an overlay, screen state, or another presentation pattern, and which component owns the state, remain open. | UX / architecture | Design + Android | Routed | Issue #15 / Issue #16 / UF-08, UF-09, UF-10 |
| D-014 | Returning from Android Settings always rechecks the relevant permission/provider condition. | UX / lifecycle | Design + Android | Approved | UF-02, UF-12 |
| D-015 | FR-08 restore is P1: rebuild queue/position in a paused state and require explicit Resume; never surprise auto-resume. Representation and process-death limits remain for TS-02/TS-08/#16. | UX / persistence | Product + Android | Routed | UF-05, UF-06 |
| D-016 | Beta baseline uses Android STT/TTS behind replaceable interfaces. No app-selected cloud-only provider is required. Do not claim offline STT. Provider/disclosure feasibility requires TS-03. | Product / privacy / technical | Product + Privacy + Android | Routed | UF-08, UF-10, UF-12 |
| D-017 | `PREVIOUS` uses a fixed position threshold `T_PREVIOUS`: before it, previous track; at/after it, restart current track. Numeric value is deliberately unresolved. | UX policy | Product + Android | Routed | #16 / UF-05, UF-08 |
| D-018 | Post-call/focus resume is conditional: previously playing, focus restored, same valid route, no manual pause. Otherwise remain paused. | Safety / playback | Android + QA | Routed to TS-04 | UF-11 |
| D-019 | Bluetooth/headset disconnect pauses immediately. Reconnect does not auto-resume. | Safety / playback | Product + Android | Approved | UF-11 |
| D-020 | Permission revocation reconciles the derived index, marks inaccessible media unavailable, and routes to recovery without a crash. | Data / UX | Android + Design | Approved | UF-02, UF-03, UF-05 |
| D-021 | Driving `No Track` never opens a long list. A valid last local track may be offered to `PLAY`; otherwise provide a short message and immediate exit/manual recovery. | UX / safety | Product + Design | Approved | UF-07, UF-08 |
| D-022 | Corrupt media is skipped in a controlled way; if no playable item remains, pause/end the queue and expose a concise error. | Playback / recovery | Android + QA | Approved | UF-05, UF-11 |
| D-023 | `NEXT` at the end of queue does not invent repeat behavior. Remain at end/paused-completed and announce the end briefly when invoked by voice. | Playback / voice | Product | Approved | UF-05, UF-08 |
| D-024 | No artist match preserves current playback and returns a short not-found response. | Voice / safety | Product | Approved | UF-09 |
| D-025 | Settings clearing uses an explicit confirmation and clears only the selected app-owned history/diagnostic categories; likes/settings are not silently erased. | Privacy / UX | Product + Privacy | Approved | UF-12 |
| D-026 | Exact history and diagnostic retention periods are not invented in UX. Privacy owns the policy and #16 owns enforceable storage behavior. | Privacy | Privacy + Product | Routed | TS-10 / #16 / UF-12 |
| D-027 | Liked Songs remains a separate P2 surface and is the first complete flow/screen cut. | Scope | Product | Approved | UF-13 |
| D-028 | Analytics is allowlist-only and non-blocking. No screen or flow may pass raw entity objects or personal titles to a provider. | Privacy / architecture | Privacy + Android | Approved | All instrumented flows |
| D-029 | Short local playback history may exist, but it cannot become hidden P0 scope; if collected, clearability and retention rules apply. | Scope / privacy | Product + Privacy | Routed | #3 / #16 / UF-12 |
| D-030 | STT unavailable or missing language support shows a short failure and keeps manual controls; deep provider settings are offered only outside active Driving Mode. | UX / recovery | Design + Android | Approved | UF-10, UF-12 |

---

## 6. Conflict Register

| ID | Conflict or ambiguity | Resolution for Phase 0 | Owner / closure gate | Severity |
|---|---|---|---|---|
| C-001 | Repository `docs/PRD.md` and `docs/MVP.md` describe themselves as derived from approved Persian documents, while the project governance and supplied architecture artifacts identify the attached English PDFs as sole product authorities. | Use English P1/P2 for all decisions. Correct repository provenance wording without changing product behavior. | Product / repository docs | Medium governance |
| C-002 | Each ADR PDF header is Proposed, but its final `Decision outcome` section contains an unfilled future `Status: Accepted` template. | Treat the header and GitHub register as current status. Never treat the template as approval evidence. | Issue #2 | High governance |
| C-003 | ADR-0001 and ADR-0003 PDF headers say Proposed-Revised while GitHub register says Proposed. | Register remains non-binding but must reflect reviewed outcome and revision state before #2 closes. | Issue #2 | High governance |
| C-004 | PRD presentation names `Home` and `Library` in one version-screen row, while MVP uses `Home / Library` as one screen. | One top-level `Home / Library` destination with internal library views. | Approved in D-004; validate in #15 | Medium UX |
| C-005 | The first-use diagram places files and microphone together, while privacy says microphone permission is requested only when needed. | Keep both within first-use education, but request the OS microphone permission just-in-time at voice practice/first voice action. | Approved in D-007 | High privacy/UX |
| C-006 | Product language says core commands remain available without internet, while Android STT may be network-assisted. | Local player and deterministic command execution remain local-first; never claim guaranteed offline recognition. Validate provider and disclosure behavior in TS-03. | Privacy + Android / TS-03 | High product truth |
| C-007 | FR-02 requires P0 playback continuity after UI closure; FR-08 defines P1 queue/position persistence after UI closure. | Separate active-service continuity (P0) from restoration after active lifecycle/process loss (P1, paused and explicit Resume). | D-015 / TS-02 / #16 | High scope |
| C-008 | PRD/MVP include short local history, but do not assign it a standalone FR priority; derived technical material labels it P1. | Do not elevate it to P0. Keep it subordinate to P0, and enforce privacy controls if collected. | Product / #3 and #16 | Medium scope |
| C-009 | PRD/MVP shorthand lists Voice Overlay states as Listening/Confirm/Failed, while technical synthesis includes Processing/Executed. | Use the complete state machine: Idle, Listening, Processing, Confirming, Executed, Failed. The final overlay/state/navigation presentation remains routed to Issue #15 and Issue #16. | D-013 / Issue #15 / Issue #16 | Low naming / architecture |
| C-010 | Exact retention, `Previous` threshold, confidence threshold, fuzzy threshold, and post-call policy are not numerically specified. | Keep user-visible safe branches, but route numbers/mechanics to named spikes or #16. | D-017, D-018, D-026 / #16 | High design integrity |

No unresolved conflict authorizes a capability outside MVP v1.0.

---

## 7. Flow Coverage Matrix

### 7.1 Required flow catalog

| ID | Flow | Priority | Primary surfaces/system surfaces | Required terminal states | Product mapping |
|---|---|---|---|---|---|
| UF-01 | First launch and education | P0 | Splash, Onboarding, Permissions, Home/Library | Home Ready; permission recovery; Empty Library | PRD first-use; US-01, US-02 |
| UF-02 | Permission denial and Settings recovery | P0 | Permissions, Android dialogs, Android Settings | Granted; Denied; Permanently Denied; manual fallback | US-01; privacy principles |
| UF-03 | Local scan and library readiness | P0 | Home/Library, media permission, MediaStore/index | Scanning; Empty; Ready; recoverable scan/revocation error | FR-01; US-02 |
| UF-04 | Browse and search local music | P0 | Home/Library, Search, Full Player | Result; No Result; Error; playback started | FR-01; Player/Library scope |
| UF-05 | Manual playback and queue control | P0 plus P1 restore branch | Full Player, Home mini-player | Playing; Paused; end of queue; corrupt skip; paused restore | FR-02, FR-03, FR-08 |
| UF-06 | Background, screen-off, and system controls | P0 plus P1 restore branch | Notification, lock screen, Bluetooth/head unit, service reconnection | Synchronized active playback; recoverable controller state; paused restore | FR-02, FR-08; US-03 |
| UF-07 | Driving Mode entry, use, and exit | P0; P1 hint | Home/Library, Full Player, Driving Mode, Bluetooth suggestion | Playing; Paused; No Track; immediate exit | FR-04, FR-07; US-04 |
| UF-08 | Direct Voice command | P0 | Driving/Full Player, provisional Voice feedback state, playback service | Executed; Failed-safe; manual fallback | FR-05, FR-06; US-05 |
| UF-09 | Ambiguous or missing artist | P0 | Provisional Voice feedback state, playback service | Confirmed execution; not found; failed-safe | FR-06; US-06 |
| UF-10 | Voice timeout, unavailable provider, and retry | P0 | Provisional Voice feedback state, provider/system settings outside driving | One retry; failed; manual fallback; playback preserved | US-07; ADR-0004 boundary |
| UF-11 | Calls, Audio Focus, route changes, and corrupt queue item | P0 | Phone call, focus, Bluetooth/headset, route system surfaces | Conditional resume; remain paused; no speaker leakage; queue continues | FR-03; QA P0 matrix |
| UF-12 | Settings, privacy, Bluetooth selection, and data clearing | P0 control surface | Settings, Android Settings | Updated; consent accepted/declined; data cleared; permission rechecked | Privacy; telemetry; FR-07 setup |
| UF-13 | Liked Songs | P2 | Home/Library, Liked Songs, Full Player | Empty; Ready; playback started; missing item reconciled | FR-09 |

### 7.2 Functional requirement traceability

| Requirement | Priority | Covered by | Phase 0 proof |
|---|---:|---|---|
| FR-01 - consented local scan by track/album/artist/folder | P0 | UF-01, UF-02, UF-03, UF-04 | S-03/S-04; D-004/D-006/D-020 |
| FR-02 - background/screen-off playback and MediaSession | P0 | UF-05, UF-06 | Constraint 4-5; SYS-05/SYS-06/SYS-07 |
| FR-03 - calls, disconnect, routes, logical pause/resume | P0 | UF-11 | SYS-07/SYS-08/SYS-09; D-018/D-019/D-022 |
| FR-04 - Driving Mode information and four controls | P0 | UF-07 | S-07; Constraints 7-8 |
| FR-05 - mic tap, duck, STT, restricted mapping | P0 | UF-08, UF-10 | S-08; D-007/D-016/D-030 |
| FR-06 - direct certainty, confirmation for ambiguity | P0 | UF-08, UF-09 | Constraints 11-13; D-024 |
| FR-07 - manual entry / selected Bluetooth suggestion | P1 | UF-07, UF-12 | D-011; SYS-07 |
| FR-08 - queue and position after UI closes | P1 | UF-05, UF-06 | D-015; C-007; TS-02/TS-08 |
| FR-09 - Like and simple Liked Songs | P2 | UF-13 | S-09; D-027 |

### 7.3 Cross-cutting coverage

| Concern | Flow coverage | Review rule |
|---|---|---|
| Permission denial/revocation | UF-01, UF-02, UF-03, UF-05, UF-10 | Every denied state has a non-crashing recovery or manual fallback. |
| UI close/recreation | UF-05, UF-06 | Player state comes back from the controller/service; no duplicated authority. |
| Process death/restoration | UF-05, UF-06 | P1 branch only; paused, explicit Resume; exact limits remain #16. |
| Voice certainty/ambiguity/failure | UF-08, UF-09, UF-10 | Direct, one confirmation, one retry, safe failure; final presentation and ownership remain #15/#16. |
| Driving safety | UF-07 through UF-11 | No typing, deep navigation, long list, surprise resume, or speaker leakage. |
| Privacy/telemetry | UF-08 through UF-12 | Allowlist only; no raw audio/transcript/title payload. |
| Accessibility | All UI flows | 48dp, scalable text, TalkBack, concise messages, accessible error recovery. |

---

## 8. Terminology and Naming Rules

| Preferred term | Meaning / usage | Do not use |
|---|---|---|
| **Music Copilot** | Product name. | Spotify replacement, streaming service. |
| **MVP v1.0** | The locked Android local-file experiment. | v1.1 scope inside current artifacts. |
| **Home / Library** | One top-level destination with Songs/Artists/Albums/Folders views. | Separate Home and Library top-level destinations unless later approved. |
| **Full Player** | Standard playback and queue-control surface. | Playback owner. The service owns playback. |
| **Driving Mode** | One-layer, glance-minimized playback surface. | Safe-driving mode, crash-prevention mode, Android Auto. |
| **Voice feedback state** | Provisional User Flow term for Listening, Processing, Confirming, Executed, and Failed behavior associated with the initiating playback experience. Final overlay/screen/navigation form and ownership remain Issue #15/#16 decisions. | Treating `Voice Overlay` as a final architecture destination or binding owner before approval. |
| **Push-to-Talk** | Explicit microphone tap starts a bounded session. | Wake word, always listening. |
| **Media access** | User-granted Android access to supported local audio. | Ownership verification or supplied catalog. |
| **Local-first** | Local media and product state remain on-device; core player has no product backend. | Guaranteed offline STT. |
| **Direct execution** | Supported, complete, above-threshold mapping executes without an extra screen. | Automatic guess. |
| **Confirmation** | One short bounded choice for a valid ambiguity. | Multi-turn conversation. |
| **Manual fallback** | Playback controls remain immediately reachable after voice failure. | Dead end or forced Settings journey. |
| **Active playback continuity** | P0 service behavior while playback is active despite UI closure/recreation. | Queue restoration. |
| **Queue/position restore** | P1 reconstruction after the active lifecycle is lost; paused until explicit Resume. | Automatic playback resume. |
| **Bluetooth suggestion** | P1 prompt after a selected device connects. | Automatic Driving Mode launch. |
| **Liked Songs** | Simple P2 local list. | Playlist system, sync, recommendation. |

User-facing language must never claim that Music Copilot makes driving safe, licenses the user's files, offers guaranteed offline speech recognition, or supports capabilities outside the six-intent contract.

---

## 9. Technical Spike Register

| ID | Question and required evidence | Owner | Blocks | Exit evidence |
|---|---|---|---|---|
| TS-01 | How do notification permission, foreground media service visibility, denial, and Settings recovery behave on Android 10, 12, and 14+? | Android + QA | Final UF-02/UF-06 and #16 | Device results, screenshots/state traces, chosen prompt point, denial/recovery contract. |
| TS-02 | Where is the exact boundary among UI close, activity recreation, task removal, service lifetime, and process death? | Android | Final UF-05/UF-06 and #16 | Lifecycle matrix proving P0 continuity and P1 restore boundaries. |
| TS-03 | Can the configured Android STT mode/provider expose availability, language support, network dependence, processing destination, and retention sufficiently for truthful consent? | Android + Privacy | Final UF-08/UF-10/UF-12 and Voice Beta | Provider matrix; validated copy requirements; on-device/network-assisted decision; fallback proof. |
| TS-04 | Which focus/call callbacks and OEM behaviors permit conditional resume without surprise playback? | Android + QA | Final UF-11 and #16 | Pixel/Samsung/Xiaomi traces, policy table, manual-pause preservation proof. |
| TS-05 | Can Bluetooth/headset disconnect and reconnect reliably prevent phone-speaker leakage across the device/vehicle matrix? | Android + QA | Final UF-11 and Beta | Route traces on 3+ head units; immediate-pause proof; no auto-resume proof. |
| TS-06 | How do MediaStore access, scoped-storage permission changes, moved/deleted files, and incremental reconciliation behave across Android versions? | Android + QA | Final UF-02/UF-03/UF-05 and #16 | Permission/revocation/move/delete test matrix and rebuild contract. |
| TS-07 | What deterministic artist normalization/fuzzy thresholds reach quality targets without unsafe top-match execution? | Android + Product + QA | Final UF-09 and #16 | Fixed corpus, repeatable scoring, ambiguity boundary, false-execution rate. |
| TS-08 | What minimum local representation restores queue and track position without turning the database into live playback authority? | Android | Final P1 branch and #16 | Data contract, migration/error behavior, paused-restore proof. |
| TS-09 | What confidence threshold source is safe, testable, and locally defaulted when remote configuration is unavailable? | Android + QA | Final UF-08/UF-09 and #16 | Safe build default, range validation, non-blocking remote failure test. |
| TS-10 | What exact retention and deletion semantics apply to short history and diagnostics? | Product + Privacy + Android | Final UF-12, Privacy copy, #16 | Approved durations/categories, clear confirmation, storage/provider deletion contract. |

The numeric `T_PREVIOUS` policy is a Product/Android decision for Issue #16. It may use a focused usability check, but no implementation should guess it before approval.

---

## 10. Analytics and Privacy Coverage

### 10.1 Allowed event contract

| Event | Flow | Allowed properties |
|---|---|---|
| `driving_mode_started` | UF-07 | `entry_source`, `bt_connected` |
| `voice_button_pressed` | UF-08/UF-10 | approved session category, `playback_state` |
| `intent_detected` | UF-08/UF-09 | `intent_type`, `confidence_bucket` |
| `intent_failed` | UF-09/UF-10 | approved coarse reason category; exact schema remains reviewable |
| `command_executed` | UF-08/UF-09 | `intent_type`, `latency_bucket` |
| `manual_correction` | UF-08 through UF-10 | `previous_intent`, `correction_type` |
| `player_error` | UF-05/UF-06/UF-11 | `error_code`, `route_type` |
| `session_completed` | UF-07 through UF-11 | `duration_bucket`, `commands_count` |

### 10.2 Prohibited data

- raw audio;
- full recognized transcripts by default;
- complete titles, filenames, artists, albums, folders, or library contents;
- precise location;
- contacts;
- unnecessary persistent identifiers;
- arbitrary entity serialization;
- any telemetry used as a hidden user account, sync layer, library mirror, or playback store.

Quality-service outage must not block library browsing, manual playback, Driving Mode manual controls, or command execution.

---

## 11. Phase 0 Exit Checklist

| Criterion | Result | Evidence |
|---|---|---|
| English PRD/MVP authority is fixed. | Pass | Source Register and D-001. |
| P0/P1/P2 boundaries are explicit. | Pass | Scope Matrix. |
| All required application surfaces are inventoried. | Pass | S-01 through S-10. |
| Android/system surfaces are inventoried. | Pass | SYS-01 through SYS-09. |
| Every open question has Owner, Status, and Gate. | Pass | Decision and Spike Registers. |
| User-visible routing/recovery expectations are explicit, while final navigation, Back behavior, and Voice presentation/ownership are routed rather than silently locked. | Pass | Approval boundary; S-06 through S-08; D-010, D-012, D-013; Issue #15/#16. |
| Permission denial, revocation, and Settings return are covered. | Pass | S-03, SYS-01 through SYS-04, D-006 through D-008/D-014/D-020. |
| Voice certainty, ambiguity, failure, and retry are covered. | Pass | S-08, D-013/D-016/D-024/D-030. |
| Calls, routes, disconnect, reconnect, and corrupt media are covered. | Pass | SYS-07 through SYS-09, D-018/D-019/D-022. |
| P0 active continuity and P1 restoration are separated. | Pass | D-015 and C-007. |
| Cloud/network-assisted STT is not hidden or mislabeled offline. | Pass with routed evidence | D-016, C-006, TS-03. |
| No outside-MVP capability entered the flow catalog. | Pass | Scope exclusions and Flow Matrix. |
| Conflict register is explicit. | Pass | C-001 through C-010. |
| Figma has a dedicated `User Flows / State Inventory` Phase 0 section. | Pass | Figma section `00 / Phase 0 Foundation`. |
| GitHub has a durable Phase 0 record linked from the User Flow index. | Pass after repository writeback | `docs/user-flows/PHASE_0_FOUNDATION.md`. |

### Exit decision

**Approved to begin drafting UF-01 through UF-13.** The first drawing task is UF-01/UF-02/UF-03 as one connected first-use and permission-recovery cluster. Do not mark the complete User Flow set Approved until Issue #2 evidence and ADR register status are aligned.

---

## 12. Flow Drafting Order

1. **Cluster A - First readiness:** UF-01 First Launch, UF-02 Permission Recovery, UF-03 Library Scan.
2. **Cluster B - Manual music:** UF-04 Browse/Search, UF-05 Manual Playback, UF-06 Background/System Controls.
3. **Cluster C - Driving:** UF-07 Driving Mode.
4. **Cluster D - Voice:** UF-08 Direct Command, UF-09 Ambiguity/Not Found, UF-10 Timeout/Unavailable/Retry.
5. **Cluster E - Recovery and control:** UF-11 Interruptions/Routes, UF-12 Settings/Privacy.
6. **Optional scope:** UF-13 Liked Songs - P2.

### First drafting brief - UF-01/UF-02/UF-03

Start states:

- first install;
- returning user with onboarding incomplete;
- media permission denied or revoked;
- library Scanning, Empty, Ready, or scan error;
- command education skipped or replayed.

Required decisions already fixed:

- two-slide value introduction;
- media permission before scan;
- microphone permission just-in-time at actual command practice/first voice action;
- notification permission deferred until playback/background value exists;
- Onboarding can be skipped and replayed;
- Settings return rechecks permission;
- Home/Library is one destination;
- no Account, Backend, Streaming, or forced cloud dependency.

Required endpoints:

- Home/Library Ready;
- Home/Library Empty with scan/permission recovery;
- permission Denied with Retry;
- Permanently Denied with Android Settings path;
- microphone declined with manual controls preserved;
- recoverable scan failure;
- inaccessible/revoked media reconciled safely.

---

## 13. Review Sign-off Record

| Role | Phase 0 responsibility | Phase 0 result |
|---|---|---|
| Product Owner | Scope, priority, user-visible policy, conflict authority | Approved through user confirmation on 01 August 2026. |
| Product Design | Provisional user-visible routing/recovery expectations and state completeness | Ready to draft flows; final navigation, Back behavior, and Voice presentation remain Issue #15/#16 decisions. |
| Android Engineering | Feasibility boundaries and spike ownership | Preliminary constraints recorded; Issue #2/#16 evidence still required. |
| QA | Edge cases, safety invariants, device/vehicle evidence | Required scenarios and spikes recorded. |
| Privacy Review | Microphone, STT disclosure, telemetry, retention, clearing | Boundaries fixed; TS-03 and TS-10 remain evidence gates. |

---

## 14. Source References

- P1 - Music Copilot Product Requirements Document v1.0, English, July 2026.
- P2 - Music Copilot Minimum Viable Product Plan v1.0, English, July 2026.
- A1 through A6 - attached Architecture Decision Record PDFs dated 31 July 2026.
- S1 - Music Copilot System Architecture MVP v1.0, proposed synthesis, 01 August 2026.
- T1 - Music Copilot Code & Technical Documentation Blueprint MVP v1.0, proposed blueprint, 01 August 2026.
- GitHub - `latticeward-labs/music-copilot`: README, Issues #2/#3/#15/#16, `docs/PRD.md`, `docs/MVP.md`, `docs/TECHNICAL_DECISIONS.md`, `docs/adr/README.md`, and `docs/USER_FLOWS.md`; snapshot 01 August 2026.
- F1/F2 - Persian PRD/MVP PDFs, read as companion translations only.

---

**Final Phase 0 statement:** Music Copilot MVP v1.0 now has a controlled, traceable foundation for User Flow drafting. Nothing in this artifact expands the product beyond the approved English PRD/MVP, promotes a proposed ADR to accepted status, locks final navigation/Back/Voice ownership before Issue #16, or authorizes implementation before the final architecture gate.
