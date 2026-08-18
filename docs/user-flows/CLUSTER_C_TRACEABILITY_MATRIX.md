# Cluster C - Traceability Matrix

**Scope:** UF-07 Driving Mode  
**Date:** 18 August 2026  
**Status:** **PASS / COMPLETE** at User Flow specification level

## Product requirement traceability

| Requirement / rule | Priority | UF-07 proof | State proof | Authority | Result |
|---|---:|---|---|---|---|
| FR-04 - show title, artist, artwork, and four large controls | P0 | One-layer surface contract and controlled four-control resolution | CC-05, CC-06, CC-07 | English PRD; Phase 0 Constraint 7/S-07 | PASS |
| US-04 - essential controls on one screen | P0 | No nested destination/list; microphone and Exit separate | CC-05/06/07 | English MVP | PASS |
| Manual Driving Mode activation | P0 | Manual entry from Home/Library and Full Player | CC-01, CC-02 | English PRD/MVP; D-011 | PASS |
| Playing required state | P0 | Service-derived Playing state and acknowledged controls | CC-05, CC-08, CC-10 | English MVP; S-07 | PASS |
| Paused required state | P0 | No auto-resume; Play only after acknowledgement | CC-06, CC-08, CC-10 | English MVP; S-07 | PASS |
| No Track required state | P0 | At most one revalidated last-item fallback; otherwise concise Exit/manual recovery | CC-07, CC-15 | English MVP; D-021 | PASS |
| Immediate Exit | P0 | Exit/Back equivalence; return target/fallback; no playback mutation | CC-13 | English MVP; D-012 | PASS |
| No typing/deep navigation/nested menu/long list | P0 safety | Explicit one-layer prohibitions | All Driving states | English PRD safety; Phase 0 Constraint 8 | PASS |
| 48dp, contrast, scalable text, TalkBack, limited motion | P0 quality | Testable accessibility/driving section | CC-02 through CC-16 where visible | English PRD/MVP; technical blueprint | PASS |
| Bluetooth selected-device suggestion only | P1 | Preselected device; one short suggestion; explicit acceptance | CC-11, CC-12, CC-17 | FR-07; D-011 | PASS |
| Never automatic Driving Mode launch | P1 safety | Connection alone stays non-driving; acceptance required | CC-11/12/17 | English MVP; D-011 | PASS |
| P1 removable before P0 | P1 cut | Suggestion graph is separable from manual entry | CC-11/12/17 | English MVP cut rule | PASS |
| Playback authority remains service/MediaSession | P0 architecture invariant | Complete snapshot before ready; acknowledged updates only | CC-02/03/05/06/08/10 | ADR-0002; UF-05/06 handoff | PASS |
| No product backend/account/streaming | Scope | Explicit exclusions; local player only | All | ADR-0006 | PASS |
| Microphone only after explicit action | P0 privacy | Tap creates controlled UF-08/10 handoff; no capture on entry | CC-14 | PRD privacy; ADR-0004/0005 boundary | PASS |
| Driving analytics allowlist | Measurement | Only controlled start/complete properties | CC-05/06/07 entry, CC-13, CC-18 | English PRD/MVP; Phase 0 section 10 | PASS |

## Decision traceability

| Decision | Applied behavior | UF-07 location | Result |
|---|---|---|---|
| D-011 | Manual P0; selected-device Bluetooth P1 suggestion-only; explicit acceptance | Entry routes and P1 section | PASS |
| D-012 | Exit and Back immediate/equivalent; return to last non-driving surface; Home/Library fallback | Immediate Exit section; CC-13 | PASS |
| D-021 | No Track never opens a long list; valid last item may be offered; otherwise short recovery | No Track section; CC-07/15 | PASS |
| D-028 | Analytics allowlist-only and non-blocking | Analytics section; CC-18 | PASS |
| Constraint 5 | Service/MediaSession owns playback state | Entire command/state contract | PASS |
| Constraints 7-8 | One layer; required content/actions; no typing/deep navigation/list | Surface and exclusion sections | PASS |

## ADR traceability

| ADR | UF-07 effect | Evidence | Result |
|---|---|---|---|
| ADR-0001 Accepted | Android 10+ native surface and system Back/Bluetooth integration boundary | Manual/system entry and Back contract | PASS |
| ADR-0002 Accepted | Service-owned player and MediaSession command/snapshot authority | CC-02/03/05/06/08/10 | PASS |
| ADR-0003 Accepted | Local-first state; selected-device preference/local navigation context only | CC-11/12/17; persistence column | PASS |
| ADR-0004 Accepted | Voice provider is replaceable; UF-07 owns only explicit microphone handoff | CC-14 | PASS |
| ADR-0005 Accepted | No wake word/conversation or unsafe approximate execution introduced | Explicit exclusions and CC-14 handoff | PASS |
| ADR-0006 Accepted | No backend/account/streaming/catalog/sync/upload/download | Explicit exclusions | PASS |

## State coverage

| Coverage concern | States | Result |
|---|---|---|
| Manual entry and truthful controller readiness | CC-01 through CC-04 | PASS |
| Required Driving states | CC-05 through CC-07 | PASS |
| Manual command acknowledgement and recovery | CC-08 through CC-10 | PASS |
| Optional Bluetooth suggestion | CC-11, CC-12, CC-17 | PASS |
| Immediate Exit | CC-13 | PASS |
| Voice handoff | CC-14 | PASS |
| Access/item failure | CC-15 | PASS |
| Interruption/route handoff | CC-16 | PASS |
| Non-blocking analytics | CC-18 | PASS |

## Connected-flow handoffs

| From/to | Contract | Result |
|---|---|---|
| UF-05 -> UF-07 | Preserve service-owned current item, queue, position, and Playing/Paused state; never restart | PASS |
| UF-06 -> UF-07 | Use synchronized MediaSession snapshot; controller reconnect remains truthful | PASS |
| UF-07 -> UF-05/06 | Exit changes presentation only; playback continues/remains paused and surfaces remain synchronized | PASS |
| UF-07 -> UF-08/10 | Explicit microphone tap creates overlay/session handoff; manual Driving state is return context | PASS |
| UF-07 -> UF-11 | Reflect service state; calls/focus/route/disconnect policy remains downstream | PASS |
| UF-12 -> UF-07 | Selected-device preference may enable P1 suggestion; connection never auto-opens | PASS |
| UF-07 -> UF-03/04 | No Track/access failure exits to non-driving manual recovery; no long list inside Driving Mode | PASS |

## Privacy and safety traceability

| Rule | Evidence | Result |
|---|---|---|
| No full media metadata in telemetry | Prohibited payload list; every state inventory analytics cell | PASS |
| No Bluetooth device identity in telemetry | P1 suggestion contract and CC-11/12/17 | PASS |
| No raw audio/transcript | CC-14 and explicit prohibited payloads | PASS |
| Telemetry outage cannot block controls | CC-18 | PASS |
| No automatic playback on entry/reconnect | CC-02/05/06/11/12/16 | PASS |
| Immediate Exit from all states | CC-02/03/05-10/12/14-16 -> CC-13 | PASS |
| No claim of safe driving | Accessibility/driving copy boundary | PASS |

## Downstream non-blocking decisions

| Decision | Downstream owner | Why Cluster C may still pass |
|---|---|---|
| Exact Compose layout, visual hierarchy, iconography, control order | Issue #15 | User-visible actions, states, restrictions, and Exit semantics are fixed here. |
| Navigation/controller/API implementation | Issue #16 / #9 | The snapshot, authority, acknowledgement, and return contracts are fixed without selecting mechanics. |
| Bluetooth connection APIs, permissions, OEM behavior | Issue #9 / #16 | P1 suggestion is optional/removable and manual P0 is independent. |
| Voice provider/session execution | UF-08 through UF-10 / Issues #10-11 | UF-07 defines only explicit entry/return handoff. |
| Calls/Audio Focus/routes | UF-11 / Issue #8 | UF-07 preserves the handoff and does not invent policy. |
| Stationary/in-car QA | Issue #9 / #13 | This stage approves the User Flow contract, not implementation evidence. |

## Matrix decision

**PASS.** FR-04, FR-07, US-04, S-07, D-011/D-012/D-021, accepted ADR constraints, state coverage, handoffs, privacy, safety, accessibility, and exclusions are traceable with no silent product expansion.

