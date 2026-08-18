# Cluster D - Traceability Matrix

**Scope:** UF-08, UF-09, UF-10  
**Date:** 18 August 2026  
**Status:** **PASS - requirements, states, decisions, spikes, and handoffs mapped**

| Authority / requirement | Flow(s) | State coverage | Controlled behavior | Evidence / downstream boundary |
|---|---|---|---|---|
| US-05 clear command | UF-08 | CD-02/03/05/06/07 | Explicit mic, direct certain execution, no new screen, one short result | Mapper/session unit tests; production #10 |
| US-06 ambiguity | UF-09 | CD-09/10/11/12 | One bounded confirmation, only presented candidate, no loop | TS-07 corpus; production #11 |
| US-07 retry/manual | UF-10 | CD-17/18/04/19/20 | One explicit retry, second failure terminal, playback preserved | Session tests; production #10 |
| FR-05 push-to-talk | UF-08/10 | CD-01/02/03/04/24 | Explicit mic only, six seconds, duck/release, cancel | ADR-0004; Android probe/fake contract |
| FR-05 restricted mapping | UF-08/09 | CD-05/13/14/15/16 | Exactly six English intents; unknown/unsupported distinct | ADR-0005; deterministic tests |
| FR-06 direct/confirm | UF-08/09 | CD-06/07/09/10/11/12 | Certain direct; uncertain one confirmation; no approximate execution | TS-07/TS-09 evidence |
| S-08 Voice Overlay | UF-08/09/10 | CD-02 through CD-24 | Overlay/session, not navigation; initiating surface retained | Connected Mermaid parity |
| D-013 overlay ownership | All | CD-02/24 | No Voice destination/back stack | Issue #16 mechanics only |
| D-016 provider boundary | UF-08/10 | CD-02/21/22/23 | Replaceable STT/TTS; no generic offline claim; cloud consent/disclosure | TS-03; ADR-0004 |
| D-017 Previous | UF-08 | CD-06/07 | Symbolic `T_PREVIOUS` only | Numeric value remains #16 |
| D-021 No Track | UF-08 | CD-06/08/20 | Never fabricate playback item | UF-05/07 handoff |
| D-023 NEXT end | UF-08 | CD-06/07 | No wrap/repeat; truthful ended/paused-completed | UF-05 contract |
| D-024 no artist match | UF-09 | CD-13 | Preserve playback and short Not Found | TS-07 negative corpus |
| D-028 telemetry | All | Every state | Allowlist, non-blocking, no personal payload | #12 implementation; privacy review here |
| D-030 unavailable language/provider | UF-10 | CD-22/23/20 | Short failure/manual; setup only outside active Driving Mode | TS-03 policy/probe |
| C-006 offline conflict | UF-08/10 | CD-22/23 | Local-first data is not an offline-STT guarantee | Official Android API review |
| C-010 threshold conflict | UF-08/09 | CD-05/09/10 | Named production boundary; conservative spike candidate; never confuse KPI with confidence | TS-07/09; final adoption #16 |
| Privacy: no raw audio/transcript/title | All | CD-02 through CD-24 | Transient capture/input only; prohibited telemetry/evidence | Privacy assertions and review |
| Cloud consent/disclosure | UF-10/12 | CD-23 | Separate opt-in with destination, purpose, retention | TS-03; legal/provider review downstream |
| Accessibility/driving | All | Every visible state | Large actions, TalkBack, non-color state, short copy, no keyboard/list/loop | Wireframes #15/#17 and app QA #10/#13 |
| TS-03 | UF-08/10/12 | CD-02/21/22/23 | Capability matrix and fail-closed provider policy | `evidence/TS-03_*`; physical/provider rows downstream |
| TS-07 | UF-09 | CD-05/09/10/13/16 | Fixed corpus, deterministic scoring/order, ambiguity boundary, zero negative-row execution | `evidence/TS-07_*`; field tuning #11/#13/#16 |
| TS-09 | UF-08/09 | CD-05/06/09 | Safe build candidate, bounded validation, last-valid/default fallback | `evidence/TS-09_*`; production adoption #16 |
| UF-07 entry/return | All | CD-01/20/24 | Driving Mode is initiating/manual fallback surface | Cluster C contract |
| UF-11 interruption | All | CD-03/04/06/09 | Voice never overrides focus/call/route policy | UF-11 active next cluster |
| UF-12 permission/privacy | UF-10 | CD-21/22/23 | Recovery/setup/clear outside active driving | UF-12 downstream |

## Coverage result

Every Cluster D state has a product/ADR authority, failure and recovery path, privacy boundary, and evidence/downstream owner. Final OEM/road-noise accuracy, consent copy legal approval, UI implementation, and Beta metrics are not falsely recorded as User Flow evidence.
