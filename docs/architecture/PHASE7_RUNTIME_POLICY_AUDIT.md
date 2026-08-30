# Issue #16 Phase 7 — Runtime Policy Audit

- **Status:** **PASS / COMPLETE / SOURCE-ALIGNED**
- **Date:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Primary artifact:** [Runtime Policies](RUNTIME_POLICIES.md)
- **Attached English source identity:** `MSM2(2).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Controlling ADRs:** ADR-0001..0008, especially [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) and [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md)
- **Implementation contract:** [Phase 6 Implementation Structure and Public Contracts](IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md)
- **UI/recovery evidence:** approved [Wireframes](../WIREFRAMES.md), [Navigation/Behavior Audit](../wireframes/PHASE_7_NAVIGATION_BEHAVIOR_AUDIT.md), [Recovery Terminal Matrix](../wireframes/recovery-system/RECOVERY_TERMINAL_MATRIX.md)
- **Privacy evidence:** [Analytics and Privacy](../ANALYTICS_AND_PRIVACY.md)

## 1. PASS rule

Phase 7 passes only when **Lifecycle, Concurrency, Persistence, Navigation, Error and Privacy are all implementation-ready and unambiguous** for Issue #4.

The audit additionally requires:

- no new source of truth;
- no contradiction with Accepted ADRs;
- no P1 restore promoted into P0;
- no duplicate Voice/pending-confirmation owner;
- no sensitive telemetry expansion;
- no Quality/Remote Config path becoming product authority;
- no Persian/Farsi product source used as authority;
- no out-of-scope v1.1 capability introduced.

## 2. Direct source/baseline conformance

The attached English source package identity is unchanged from the directly verified Issue #16 source register. The runtime policy remains within the source/ADR direction:

- native Android 10+;
- service-owned Media3 Player/MediaSession independent of UI lifecycle;
- local-first MediaStore/Room/DataStore boundaries;
- replaceable Voice provider;
- deterministic bounded intent mapping;
- privacy-safe/non-authoritative quality telemetry;
- no backend/account/cloud sync/streaming/online catalog;
- P0 background/interruption stability with P1 restoration separate.

Runtime details that the older English architecture/technical documents left open were already formalized by ADR-0007/0008 and Phase 6; Phase 7 consolidates those decisions rather than attributing them retroactively to the source PDFs.

**Persian/Farsi PRD/MVP used as Phase 7 authority: `0`.**

## 3. User-requested Phase 7 checklist

| Requirement | Runtime policy resolution | Result |
|---|---|---:|
| Playback/Queue/Position truth only Service/MediaSession | Section 3.1 sole authority; competing UI/Room/DataStore/Voice authority forbidden | **PASS** |
| UI Observer + Command sender | Section 3.2 | **PASS** |
| UI recreation/controller reconnect | complete snapshot atomically replaced before `Ready`; app-scoped connection | **PASS** |
| UI closure/screen-off | active service continues legitimately; screen lifecycle does not own/release Player | **PASS** |
| Process death/restoration boundary | P0 no guaranteed process-death/force-stop survival; P1 validated Paused-only restore | **PASS** |
| P0 active vs P1 paused restoration | explicit priority table; no autoplay; persisted snapshot non-authoritative | **PASS** |
| Coroutine scope ownership | Section 5 scope matrix | **PASS** |
| Main/IO/Default dispatcher boundaries | Section 6 | **PASS** |
| Cancellation/timeout/stale suppression | Section 7; one Voice generation; 6s timeout; one retry; stale tokens ignored | **PASS** |
| MediaStore/Room/DataStore ownership | Section 9 ownership matrix | **PASS** |
| One Voice/pending confirmation owner | `VoiceSessionCoordinator` only | **PASS** |
| Navigation destinations/Back | Section 11 destination and Back tables | **PASS** |
| Voice Overlay as Overlay/State | explicitly not destination/deep branch | **PASS** |
| Typed failures + Recovery actions | Section 12 category/code/recovery matrix | **PASS** |
| Privacy allowlist/prohibited fields | Section 13 mirrors approved privacy contract | **PASS** |
| Raw audio/transcript/title/library content forbidden | explicit prohibited list | **PASS** |
| Analytics failure cannot stop Playback | Section 14.1 non-authoritative sink | **PASS** |
| Remote Config failure cannot stop Playback | Section 14.2 local defaults/non-authoritative boundary | **PASS** |

Required items: **18/18 PASS**.

## 4. Lifecycle audit

| Lifecycle question | Final answer | Evidence alignment | Result |
|---|---|---|---:|
| Who owns Player/MediaSession? | `MediaSessionService` | ADR-0002/0007; Phase 6 Playback boundary | PASS |
| Who owns live queue/position? | Playback service/MediaSession | ADR-0007; Phase 5/6 | PASS |
| Does UI closure destroy playback authority? | No | ADR-0007; approved WF-06 behavior | PASS |
| Does recreation reconstruct Player? | No; controller reconnects | ADR-0007; WF-06 recreation contract | PASS |
| When is reconnect Ready? | after complete authoritative snapshot replacement | ADR-0007/Phase 6 | PASS |
| Does screen-off transfer ownership? | No | PRD/MVP direction + ADR-0007 | PASS |
| Does force-stop/process death have P0 survival guarantee? | No | ADR-0007 | PASS |
| Can P1 restore autoplay? | No | ADR-0007; FR-08 separation | PASS |
| Can persisted restore override live MediaSession? | No | ADR-0007/Phase 6 | PASS |

**Lifecycle ambiguity found:** `0`.

## 5. Concurrency audit

| Concern | Final policy | Result |
|---|---|---:|
| Playback mutation | one service-owned sequential consumer | PASS |
| Command queue | bounded 32; typed Busy overflow; accepted commands never silently dropped | PASS |
| UI scope | `viewModelScope` | PASS |
| Playback scope | service `SupervisorJob` + Main/Main.immediate | PASS |
| Controller connection | application-scoped | PASS |
| Voice | one cancellable child session under coordinator | PASS |
| Repository long work | application/repository-owned and restartable | PASS |
| Quality dispatch | app-scoped best-effort; not critical path | PASS |
| `GlobalScope` | forbidden | PASS |
| Main | UI publication + Player/MediaSession mutation | PASS |
| IO | MediaStore/Room/DataStore/blocking adapters | PASS |
| Default | normalization/matching/CPU-bound pure work | PASS |
| Voice timeout | 6 seconds | PASS |
| Voice retry | one explicit retry / max 2 attempts | PASS |
| stale Voice callback | generation-token reject | PASS |
| stale route auto-resume | route generation invalidates eligibility | PASS |

The policy intentionally does **not** invent an unsupported exact numeric controller timeout. Controller/command timeout outcome is architecture-fixed (typed failure, no fabricated success); the exact adapter duration remains an implementation/test parameter unless separately approved.

**Concurrency/runtime ambiguity requiring a new boundary or owner:** `0`.

## 6. Persistence audit

| State/data | Owner | Could another store become authority? | Result |
|---|---|---:|---:|
| media existence/identity | MediaStore | No | PASS |
| derived library/search index | Room | only derived/rebuildable | PASS |
| released optional history/likes | Room with migrations | not media/playback truth | PASS |
| settings/onboarding/preferences/consent | DataStore | not Android capability truth | PASS |
| permission/provider availability | Android/provider fresh truth | not DataStore-cached truth | PASS |
| live playback/queue/position | service/MediaSession | Room/DataStore forbidden | PASS |
| P1 restore snapshot | Proto DataStore through `RestorableQueueStore` | non-authoritative | PASS |
| Voice transcript/audio/confirmation | ephemeral Voice coordinator | persistence forbidden | PASS |

P1 restore validates schema and MediaStore, drops invalid items, clamps position and prepares Paused only. ADR-0007's versioned/atomic/5-second checkpoint rule remains intact.

**Persistence authority conflicts:** `0`.

## 7. Navigation/Voice ownership audit

Final app destinations are Bootstrap, Onboarding, Permission recovery, Home/Library, Search, Full Player, Driving, Settings and removable P2 Liked Songs.

System permission dialogs, Android Settings, notification, lock-screen and external controls remain Android-owned surfaces, not destinations.

Voice:

- origin: Player or Driving;
- representation: overlay/state;
- owner: `VoiceSessionCoordinator`;
- Back: cancel Voice first;
- Driving Back/Exit: immediate exit to recorded origin;
- no standalone Voice navigation stack;
- no pending confirmation persistence;
- no Voice route bypassing Driving restrictions.

This matches the approved Wireframe navigation/behavior evidence.

**Navigation/overlay contradictions:** `0`.

## 8. Error/recovery audit

All ten Phase 6 public error families remain mapped:

1. Permission -> Retry/OpenSettings/fresh re-check;
2. Library -> Retry/Rebuild;
3. MediaItem -> controlled Skip/queue continuation or Safe Paused end;
4. Playback -> Retry/ManualResume without optimistic success;
5. Controller -> reconnect/Retry;
6. Voice -> one Retry then ManualFallback; stale ignored;
7. Interruption -> guarded resume or Safe Paused/ManualResume;
8. Storage -> Retry/ignore invalid restore; no autoplay;
9. Privacy -> required control/Retry/None; remote processing gated;
10. Quality -> None for product behavior; diagnostic failure is contained.

Public runtime state does not expose raw infrastructure exceptions or sensitive diagnostic values.

Recovery remains consistent with approved Wireframe terminals: Manual Playback or service-owned Safe Paused where playback failure/safety is involved.

**New undocumented recovery family introduced:** `0`.

## 9. Privacy/telemetry audit

The Phase 7 allowlist matches `ANALYTICS_AND_PRIVACY.md`:

- `driving_mode_started`;
- `voice_button_pressed`;
- `intent_detected`;
- `intent_failed`;
- `command_executed`;
- `manual_correction`;
- `player_error`;
- `session_completed`.

Only typed/coarse documented properties may pass the privacy gate.

Explicitly prohibited:

- raw audio/waveform/recording;
- transcript;
- artist/album/folder/title;
- filesystem path/content URI/MediaStore ID;
- search query/results;
- library contents;
- queue contents;
- precise playback position/location;
- contacts/call identity;
- Bluetooth/vehicle identity;
- credentials/tokens/secrets;
- arbitrary/free-form payloads.

**Required explicit prohibition check:** raw audio = PASS; transcript = PASS; title = PASS; library content = PASS.

Cloud speech remains separately gated by disclosure/consent/provider/destination/purpose/retention approval; speech processing does not authorize speech content in telemetry.

## 10. Quality / Remote Config fault-isolation audit

Quality remains non-authoritative:

- telemetry is not awaited before playback acknowledgement;
- analytics/crash sink failure is dropped/contained;
- Quality cannot decide Player/queue/Voice/permission/route state;
- permission/interruption/route safety does not wait for Quality;
- optional Remote Config uses compiled/local approved defaults on unavailable/malformed/late response;
- Remote Config cannot widen intents, bypass confirmation, enable cloud speech without consent or alter Accepted ADR invariants;
- configuration/analytics outage cannot Pause/Stop Playback or block local/manual P0 functionality.

**Playback-stop path from Analytics/Remote Config failure:** `0`.

## 11. Cross-phase consistency audit

| Upstream artifact | Phase 7 consistency | Result |
|---|---|---:|
| ADR-0002 service-owned Media3 playback | sole playback/queue/position authority preserved | PASS |
| ADR-0003 local-first data boundaries | MediaStore/Room/DataStore ownership preserved | PASS |
| ADR-0004 replaceable Voice provider | provider remains behind port; cloud path separately gated | PASS |
| ADR-0005 deterministic intent mapping | bounded six-intent/confirmation/no-guess rules preserved | PASS |
| ADR-0006 no backend/account/streaming | no new product backend/network dependency | PASS |
| ADR-0007 runtime/concurrency/restoration | consolidated without silent change | PASS |
| ADR-0008 dependency/module strategy | no new runtime boundary/module introduced | PASS |
| Phase 5 diagrams | runtime policies match lifecycle/state/privacy/navigation views | PASS |
| Phase 6 public contracts | ownership/signatures/error model remain compatible | PASS |
| Approved Wireframes | Back/Exit/Voice/recovery/system-surface rules preserved | PASS |
| Analytics/Privacy | allowlist and prohibited fields preserved | PASS |

Material contradictions found: **0**.

## 12. Issue #4 no-runtime-invention test

An Android engineer can answer without inventing a runtime policy:

| Implementation question | Answer fixed? |
|---|---:|
| playback/queue/position authority | YES |
| UI role | YES |
| controller reconnect algorithm | YES |
| UI closure/screen-off ownership | YES |
| process-death/P1 restore boundary | YES |
| P0/P1 distinction | YES |
| coroutine scopes | YES |
| dispatchers | YES |
| command serialization/overflow | YES |
| cancellation/timeout/stale-result policy | YES |
| MediaStore/Room/DataStore authority | YES |
| Voice/pending confirmation owner | YES |
| navigation/Back/Exit | YES |
| Voice overlay model | YES |
| typed errors/recovery | YES |
| privacy allowlist/prohibited fields | YES |
| analytics failure isolation | YES |
| Remote Config failure isolation | YES |

**New runtime Boundary/Ownership/Recovery inventions required for Issue #4 start: `0`.**

## 13. Non-claims

This PASS does not claim:

- production Android implementation exists;
- runtime tests have already executed in production code;
- OEM/background/device/vehicle behavior is field-validated;
- real phone-call callback ordering is validated;
- audible route leakage testing is complete;
- provider/legal/retention approval is complete;
- Beta telemetry adapter is approved/enabled;
- Issue #16 final cross-role review/merge/exit gate is complete;
- Issue #4 is formally unblocked.

Those remain downstream gates.

## 14. Final gate result

- User-requested runtime requirements: **18/18 PASS**
- Lifecycle policy: **PASS**
- Concurrency policy: **PASS**
- Persistence policy: **PASS**
- Navigation/Voice Overlay policy: **PASS**
- Typed Error/Recovery policy: **PASS**
- Privacy/Telemetry policy: **PASS**
- Analytics/Remote Config fault isolation: **PASS**
- Material ADR conflicts: **0**
- Material Wireframe conflicts: **0**
- New runtime authority invented: **0**
- Persian/Farsi authority used: **0**
- Out-of-scope v1.1 capability introduced: **0**

# **PHASE 7: PASS / COMPLETE / RUNTIME-IMPLEMENTATION-READY**

Issue #16 remains open until the remaining testing-architecture, cross-role approval, final documentation/merge and exit gates pass. Issue #4 remains blocked until that formal Issue #16 exit gate.
