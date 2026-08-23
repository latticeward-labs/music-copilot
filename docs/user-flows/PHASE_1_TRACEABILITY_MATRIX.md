# Phase 1 - Complete UF-01 through UF-13 Traceability Matrix

**Product:** Music Copilot MVP v1.0  
**Tracking:** GitHub Issue #3  
**Audit date:** 23 August 2026  
**Scope:** UF-01 through UF-13; US, FR, State, Recovery, Permission/Consent, ADR, and Technical Spike traceability  
**Decision:** **PASS / COMPLETE / SOURCE-ALIGNED at User Flow and executable reference-contract level**

## 1. Authority and source-integrity record

The product authorities are the official English `Music_Copilot_PRD_v1_EN_Clarified.pdf` and `Music_Copilot_MVP_v1_EN.pdf`, represented in the repository by [`../PRD.md`](../PRD.md) and [`../MVP.md`](../MVP.md). The current GitHub ADR register controls ADR status; ADR-0001 through ADR-0006 are Accepted. Current GitHub User Flow, State Inventory, sign-off, and evidence records control workflow status.

The two supplied archives, `MSM2(2).rar` and `MSM2.rar`, were byte-identical during this audit (`SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`). They contain the English PRD/MVP, six ADR PDFs, System Architecture, Code and Technical Documentation, Phase 0 Foundation, and Phase 1 First Readiness records. Persian/Farsi PRD/MVP attachments are translations and are not product authorities.

This matrix consolidates, but does not replace, the detailed Cluster A/B/C/D/E and UF-13 records. When a historical checkpoint says later flows were still open, that statement describes the checkpoint date; the current status is controlled here and in [`../USER_FLOWS.md`](../USER_FLOWS.md).

## 2. Traceability legend

| Type | Controlled identifiers |
|---|---|
| User Stories | US-01 through US-07 |
| Functional Requirements | FR-01 through FR-09 |
| Cluster A states | S-01 through S-04, CA-01/02, SYS-01 through SYS-05 |
| Cluster B states | CB-01 through CB-33 |
| Cluster C states | CC-01 through CC-18 |
| Cluster D states | CD-01 through CD-24 |
| Cluster E states | CE-01 through CE-26 |
| UF-13 states | UL-01 through UL-14 |
| Architecture decisions | ADR-0001 through ADR-0006, all Accepted |
| Numbered Technical Spikes | TS-01 through TS-10 |

Permission/consent codes used below:

- **P-MEDIA:** Android local-media access; platform state is authoritative.
- **P-MIC:** microphone access only after an explicit user action.
- **P-NOTIF:** contextual notification boundary after playback/background value exists.
- **P-SETTINGS:** explicit Android Settings recovery followed by a fresh state check.
- **P-BT:** selected Bluetooth-device preference/capability boundary; connection alone never opens Driving Mode.
- **C-STT:** separate truthful consent/disclosure when a selected provider sends speech to a remote processor; this is not an Android permission.

## 3. Complete flow-level matrix

| Flow | Priority | US / FR coverage | Canonical state coverage | Failure and recovery contract | Permission / consent | Influencing ADRs | Technical Spike / executable evidence | Result |
|---|---:|---|---|---|---|---|---|---|
| UF-01 First Launch and Education | P0 | US-01, US-02; FR-01 first-use/readiness portion | S-01, S-02, S-04 handoff; CA-01, CA-02 | Startup retry; interrupted-step resume; non-mutating education replay; Skip/decline returns to manual local control | P-MEDIA before first scan; P-MIC only after explicit Practice/command; C-STT only if remote processing is selected | ADR-0001/0002/0003/0004/0005/0006 | TS-03 provider/disclosure boundary; TS-06 evidence inherited through the library-readiness handoff | PASS |
| UF-02 Permission Recovery | P0 | US-01; FR-01 access/recovery portion; FR-05 microphone precondition | S-03, CA-02, SYS-01..05 | Denied-requestable: Retry/Not now; permanent denial: Settings/Not now; Settings return rechecks three outcomes; provider unavailable remains distinct | P-MEDIA, P-MIC, P-NOTIF, P-SETTINGS, C-STT | ADR-0001/0002/0003/0004/0006 | TS-01, TS-03, TS-06 | PASS |
| UF-03 Library Readiness | P0 | US-02; FR-01 scan/index/readiness portion | S-04 Scanning/Empty/Ready/Recoverable Failure/Revoked-Reconciled | Retry scan; preserve verified prior state; reconcile revoked/moved/deleted/inaccessible media; rebuild derived index | P-MEDIA; P-SETTINGS via UF-02 | ADR-0001/0002/0003/0006 | TS-06 | PASS |
| UF-04 Browse and Search | P0 | US-02 downstream value; completes FR-01 with UF-01/02/03 | CB-01..13 | No Result preserves query; search error supports retry/edit/clear; stale/access failures return to UF-02/03; rejected playback preserves context | P-MEDIA; P-SETTINGS via UF-02 | ADR-0001/0002/0003/0006 | TS-06 | PASS |
| UF-05 Manual Playback and Queue | P0 + P1 restore | FR-02 manual/player portion; FR-08 restore; supports US-03 | CB-14..21, CB-32/33 | Start rejection preserves prior queue/context; corrupt/inaccessible bounded skip; controller refresh; invalid restore returns to Library; restored state never auto-plays | P-MEDIA for selected media; P-SETTINGS via UF-02 | ADR-0001/0002/0003/0006 | TS-02, TS-06, TS-08 | PASS |
| UF-06 Background and System Controls | P0 + P1 restore | US-03; completes FR-02 with UF-05; FR-08 restore | CB-21..33, including notification/lock/system surfaces | Reconnect from complete MediaSession snapshot; unavailable surface does not imply playback failure; notification denial preserves playback; lifecycle loss uses validated paused restore | P-NOTIF contextual; P-SETTINGS explicit; P-MEDIA on restored item validation | ADR-0001/0002/0003/0006 | TS-01, TS-02, TS-08 | PASS |
| UF-07 Driving Mode | P0 + separable P1 hint | US-04; FR-04; FR-07 entry/suggestion portion | CC-01..18 | Truthful controller readiness; Playing/Paused/No Track; one revalidated last-item offer at most; immediate Exit from every state; route events hand off to UF-11 | P-BT for selected-device suggestion; P-MIC only on explicit mic tap; no capture on entry | ADR-0001/0002/0003/0004/0005/0006 | No numbered Phase 0 spike is a UF-07 exit blocker; adjacent lifecycle/route implementation evidence remains downstream | PASS |
| UF-08 Direct Voice | P0 | US-05; FR-05; FR-06 certain/direct branch | CD-01..08, CD-24 | Explicit mic -> Listening/Processing; only a certain supported intent executes after acknowledgement; failure cancels safely and returns to initiating manual surface | P-MIC; C-STT where applicable | ADR-0001/0002/0003/0004/0005/0006 | TS-03, TS-09; deterministic mapper/session reference tests | PASS |
| UF-09 Ambiguity / Not Found | P0 | US-06; FR-06 ambiguous branch | CD-09..16 | One bounded confirmation; execute only the presented candidate after confirmation; missing/unknown/unsupported/not-found never approximate-executes; playback preserved | No new permission; inherits P-MIC/C-STT session established by UF-08/10 | ADR-0001/0002/0003/0004/0005/0006 | TS-07, TS-09 | PASS |
| UF-10 Timeout / Retry | P0 | US-07; FR-05 failure/retry portion | CD-17..24 | Six-second Listening timeout; one explicit retry; second failure terminal; provider/language/network failures typed; manual fallback and playback preserved | P-MIC recovery; P-SETTINGS only outside active Driving Mode; C-STT/provider disclosure | ADR-0001/0002/0003/0004/0005/0006 | TS-03, TS-09 | PASS |
| UF-11 Interruption and Audio Route | P0 | FR-03; PRD/MVP P0 QA interruption contract | CE-01..14 | Four-guard conditional resume; manual pause wins; permanent focus loss safe-pauses; disconnect immediately pauses and invalidates route; reconnect never auto-resumes; corrupt item bounded skip/queue end | Platform call/focus/route state; no new in-app runtime permission; Voice capture/session canceled on interruption | ADR-0001/0002/0003/0004/0006 | TS-04, TS-05; workflow #21 reference-contract validation | PASS |
| UF-12 Settings and Privacy | P0 control surface + P1 preference | FR-07 selected-device preference; PRD/MVP privacy/control requirements | CE-15..26 | Return from Settings rechecks truth; unavailable provider becomes Manual Only; selected clear requires confirmation and returns per-category result; protected state preserved; invalid retention disables collection | P-SETTINGS, P-BT, P-MEDIA/P-MIC/P-NOTIF recheck, C-STT consent/disclosure | ADR-0001/0002/0003/0004/0006 | TS-03 retained; TS-10; workflow #21 reference-contract validation | PASS |
| UF-13 Like / Liked Songs | P2, removable | FR-09; MVP P2 Like/capacity rule | UL-01..14 | Feature-disabled branch leaves P0/P1 intact; idempotent Like/Unlike; mutation failure preserves last confirmed state; stale/inaccessible items filtered; stale selection rejected; playback waits for service acknowledgement | P-MEDIA for playable existence; no new permission or cloud consent | ADR-0001/0002/0003/0006 | No numbered Phase 0 spike required; LikedSongsPolicy eight-test executable reference contract and workflow #25 | PASS |

## 4. User Story coverage

| Story | Primary flow(s) | Acceptance proof | Result |
|---|---|---|---|
| US-01 right-time permissions and recovery | UF-01, UF-02 | Education before request; denial never crashes; explicit Settings path; manual fallback | Covered |
| US-02 account-free local scan | UF-01, UF-03, UF-04 | Consented initial/incremental scan, truthful index/readiness, local browse/search | Covered |
| US-03 screen-off/Bluetooth continuity | UF-05, UF-06 | One service/MediaSession truth across app, notification, lock screen, and compatible external controls | Covered |
| US-04 essential driving controls | UF-07 | One layer; title/artist/artwork; four primary controls; mic and immediate Exit; no typing/deep navigation/list | Covered |
| US-05 immediate clear voice command | UF-08 | Certain supported intent executes without a new destination; short response; acknowledged state | Covered |
| US-06 ambiguity confirmation | UF-09 | One-step bounded confirmation; no guessing or approximate execution | Covered |
| US-07 one retry/manual fallback | UF-10 | One explicit retry; terminal manual fallback; playback remains preserved | Covered |

No User Story is orphaned and no flow invents an unapproved User Story.

## 5. Functional Requirement coverage

| Requirement | Priority | Complete flow coverage | Evidence status | Result |
|---|---:|---|---|---|
| FR-01 local scan/display by track, album, artist, folder | P0 | UF-01 + UF-02 + UF-03 + UF-04 | TS-06 PASS | Complete |
| FR-02 background/screen-off playback and MediaSession | P0 | UF-05 + UF-06 | TS-01/02 PASS | Complete |
| FR-03 calls, disconnect, and route changes | P0 | UF-11 | TS-04/05 executable reference contracts PASS | Complete at User Flow/reference-contract level |
| FR-04 Driving Mode information/four controls | P0 | UF-07 | Cluster C applicability/sign-off PASS | Complete |
| FR-05 mic, duck, STT, restricted mapping | P0 | UF-08 + UF-10 | TS-03/09 reference contracts PASS | Complete at User Flow/reference-contract level |
| FR-06 direct certainty / ambiguity confirmation | P0 | UF-08 + UF-09 | TS-07/09 PASS | Complete |
| FR-07 manual entry / selected Bluetooth suggestion | P1 | UF-07 + UF-12 | P1 branch remains separable/removable | Complete at User Flow level |
| FR-08 queue/position persistence after UI closes | P1 | UF-05 + UF-06 | TS-02/08 PASS | Complete at User Flow/evidence level |
| FR-09 Like and simple Liked Songs | P2 | UF-13 | Eight reference tests/workflow #25 PASS | Complete at User Flow/reference-contract level; release inclusion remains capacity-dependent |

All P0 requirements are completely mapped. P1 and P2 behavior is visibly separated and removable without weakening P0.

## 6. Permission and consent crosswalk

| Boundary | First request / evaluation | Recovery owner | Affected flows | Safety/privacy invariant | Result |
|---|---|---|---|---|---|
| P-MEDIA | Before first consented local scan | UF-02 | UF-01/02/03/04/05/06/13 | Android state is authoritative; MediaStore existence/identity is authoritative; derived index is rebuildable | Covered |
| P-MIC | First explicit Practice or real microphone action | UF-02/UF-10 | UF-01/02/07/08/10/12 | No request/capture on passive entry; denial never blocks manual player | Covered |
| P-NOTIF | Contextually after successful playback/background value | UF-02/UF-06 | UF-02/06/12 | Denial/absence never fabricates player state or stops playback | Covered |
| P-SETTINGS | User explicitly chooses a recovery/setup action | UF-02/UF-12 | UF-02/03/05/06/10/12 | Never auto-open or loop; always recheck platform/provider truth on return | Covered |
| P-BT | User selects a device for the optional suggestion | UF-12 | UF-07/11/12 | Connection alone never opens Driving Mode or auto-resumes playback | Covered |
| C-STT | Before any selected remote speech processing | UF-10/UF-12 | UF-01/02/08/10/12 | Separate truthful consent/disclosure; no generic offline guarantee; raw audio/transcript excluded from telemetry/evidence | Covered |

## 7. ADR crosswalk

| ADR | Binding decision | Primary flow application | Result |
|---|---|---|---|
| ADR-0001 | Native Android 10+ | Platform permissions/surfaces across UF-01..13; especially UF-01/02/06/07/11/12 | Covered |
| ADR-0002 | Service-owned Media3 playback | UF-04/05/06/07/08/09/10/11/13 and all recovery seams observing one player truth | Covered |
| ADR-0003 | Local-first data boundaries | UF-01/02/03/04/05/06/07/09/12/13; MediaStore vs derived index/app-owned state | Covered |
| ADR-0004 | Replaceable voice provider | UF-01/02/07/08/09/10/11/12; typed failure and consent isolation | Covered |
| ADR-0005 | Restricted deterministic intent mapping | UF-01 education and UF-07/08/09/10; exactly six English intents; no guessing | Covered |
| ADR-0006 | No backend/account/streaming | Entire UF-01..13 set; no out-of-scope recovery or hidden product dependency | Covered |

No Accepted ADR is orphaned and no flow changes ADR status or silently supersedes a decision.

## 8. Technical Spike crosswalk

| Spike | Validated question | Flow dependency | Current Stage 1 result |
|---|---|---|---|
| TS-01 | Notification/foreground media visibility, denial, recovery | UF-02, UF-06 | PASS / approved N/A where recorded; accepted Cluster B evidence bundle |
| TS-02 | UI/service/controller lifecycle boundary | UF-05, UF-06 | PASS; accepted Cluster B evidence bundle |
| TS-03 | Speech provider/language/network/consent feasibility | UF-01, UF-02, UF-08, UF-10, UF-12 | PASS at provider-policy/reference-contract level; real provider/legal enablement downstream |
| TS-04 | Conditional focus/call resume policy | UF-11 | PASS at deterministic reference-policy level |
| TS-05 | Route disconnect/reconnect safety | UF-11 | PASS at deterministic reference-policy level |
| TS-06 | MediaStore permission/reconciliation/rebuild | UF-02, UF-03, UF-04, UF-05 | PASS; accepted Cluster B evidence bundle |
| TS-07 | Deterministic artist matching/ambiguity | UF-09 | PASS at fixed-corpus/reference level |
| TS-08 | Versioned paused queue/position restore | UF-05, UF-06 | PASS; accepted Cluster B evidence bundle |
| TS-09 | Safe confidence default/fallback | UF-08, UF-09 | PASS at safe-default/reference level; final production adoption remains Issue #16 |
| TS-10 | Retention/deletion semantics and fail-closed policy | UF-12 | PASS at privacy-plan/reference-contract level; exact approved durations remain downstream |

All TS-01 through TS-10 have an explicit flow dependency and an explicit evidence/non-claim boundary. UF-07 and UF-13 correctly have no numbered Phase 0 spike as a User Flow exit blocker; their existing applicability/reference tests are recorded separately.

## 9. State and recovery completeness

| Namespace | Owner | Expected | Mapped | Orphaned | Result |
|---|---|---:|---:|---:|---|
| S / CA / SYS | UF-01..03 | S-01..04, CA-01/02, SYS-01..05 | All | 0 | PASS |
| CB | UF-04..06 | CB-01..33 | 33 | 0 | PASS |
| CC | UF-07 | CC-01..18 | 18 | 0 | PASS |
| CD | UF-08..10 | CD-01..24 | 24 | 0 | PASS |
| CE | UF-11..12 | CE-01..26 | 26 | 0 | PASS |
| UL | UF-13 | UL-01..14 | 14 | 0 | PASS |

Recovery seams are controlled as follows:

- permission denial/revocation -> UF-02;
- empty/stale/rebuildable library -> UF-03;
- browse/search retry and context preservation -> UF-04;
- player/corrupt/controller/paused restore -> UF-05/06;
- No Track/immediate driving exit -> UF-07;
- direct voice failure -> UF-10/manual initiating surface;
- ambiguity/not found -> UF-09/manual initiating surface;
- call/focus/route/corrupt interruption -> UF-11;
- Settings/provider/consent/clear control -> UF-12;
- Like mutation/stale favorite -> UF-13/Library.

Every failure has a non-crashing recovery, terminal safe state, or explicit downstream implementation/evidence owner. No recovery requires an account, backend, streaming catalog, Android Auto, Cast, iOS, or v1.1 capability.

## 10. Complete-set decision and non-claims

**PASS / COMPLETE.** The UF-01 through UF-13 set has complete US, FR, State, Recovery, Permission/Consent, ADR, and Technical Spike traceability. All individual Cluster/UF sign-offs remain valid, and the phase-wide consolidation introduces no new product capability.

This decision approves the implementation-independent User Flow/state contract and the recorded executable reference-contract evidence. It does not approve low-fidelity Wireframes, high-fidelity UI, production implementation, final Software Architecture, final provider/legal enablement, exact retention periods, final P2 release inclusion, or physical/OEM/vehicle/Beta claims not already present in accepted evidence.

