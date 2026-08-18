# Cluster E - Traceability Matrix

**Scope:** UF-11 and UF-12  
**Date:** 18 August 2026  
**Status:** **PASS - requirements, decisions, states, executable contracts, and downstream evidence mapped**

| Authority / requirement | Flow | State coverage | Controlled result | Evidence / downstream boundary |
|---|---|---|---|---|
| FR-03 calls/focus/routes | UF-11 | CE-01..11 | Typed event policy; four-guard conditional resume; permanent loss safe paused | TS-04 host reference tests; OEM/physical #8/#13 |
| D-018 conditional resume | UF-11 | CE-02/03/05/06 | Previously playing + focus restored + same route + no manual pause | `InterruptionPolicyTest`; final constants/callback mapping #16 |
| D-019 disconnect/reconnect | UF-11 | CE-07/08/09 | Immediate pause, route invalidation, no reconnect auto-resume | TS-05 host idempotency tests; speaker-leak field proof #13 |
| D-022 corrupt media | UF-11 | CE-12/13/14 | Controlled skip or queue end; no file deletion | Reference policy + implementation #7/#8/#13 |
| Voice interruption | UF-11 | CE-02/07 | Cancel Voice and reject stale callbacks before any command | Cluster D token contract; integration #8/#10 |
| Service authority | UF-11 | CE-01..14 | UI/system surfaces refresh one MediaSession snapshot | ADR-0002; #7/#8/#16 |
| Coarse route diagnostics | UF-11/12 | CE-07/11/21 | Error code + coarse route only | Privacy allowlist tests; production #12 |
| FR-07 selected Bluetooth hint | UF-12 | CE-20/17 | Local selection enables suggestion only; explicit acceptance | Cluster C contract; implementation #9/#16 |
| D-014 Settings return | UF-12 | CE-18/19/17 | Recheck permission/provider; never assume success | UF-02 seam; app integration #12/#16 |
| D-016/D-030 speech mode | UF-12 | CE-18/19/22 | Truthful capability; Manual Only when unavailable/unapproved | TS-03 retained; provider/legal #12/#16 |
| D-025 selected clear | UF-12 | CE-23..26 | Confirmation; selected categories only; protected state preserved | TS-10 plan tests; production repositories #12 |
| D-026 retention | UF-12 | CE-21/22/25 | No invented period; missing/invalid policy disables collection | `PrivacyDataPolicyTest`; Privacy/#16 adoption |
| D-028 allowlist | Both | all instrumented | Reject arbitrary/prohibited fields before adapter | Host schema tests; production #12 |
| D-029 short history | UF-12 | CE-21/23..26 | Subordinate/optional local category; clearable and retention-bound | TS-10; implementation #12/#16 |
| ADR-0003 ownership | UF-12 | CE-15..26 | Media/platform/app/preferences/telemetry ownership remains distinct | Source authority + tests |
| Accessibility/driving | Both | all visible states | Concise feedback; 48dp/TalkBack; no Settings/clearing inside Driving Mode | Wireframes #15; UI/field QA #13 |
| MVP exclusions | Both | all | No account/backend/sync/streaming/Android Auto/hidden cloud dependency | Governance review |

## Coverage result

Every Cluster E state has an authority, success/failure/recovery path, persistence rule, privacy boundary, and evidence owner. Physical OEM/vehicle behavior, final retention duration, provider deletion/legal claims, and production implementation are explicitly downstream rather than silently treated as passed.
