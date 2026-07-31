# User Flows and State Inventory — MVP v1.0

- **Status:** Ready for definition
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Workfile:** [Music Copilot — MVP v1.0](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

This document is the repository index for approved User Flow artifacts. Until Issue #3 is approved, the flow set is not implementation-ready.

## Required flow catalog

| ID | Flow | Priority | Required terminal states |
|---|---|---|---|
| UF-01 | First launch and education | P0 | Home ready, permission recovery, empty library |
| UF-02 | Permission denial and Settings recovery | P0 | Granted, Denied, Permanently Denied |
| UF-03 | Local scan and library readiness | P0 | Scanning, Empty, Ready, recoverable error |
| UF-04 | Browse and search local music | P0 | Result, No Result, Error, playback start |
| UF-05 | Manual playback and queue control | P0 | Playing, Paused, end/queue behavior, corrupt item recovery |
| UF-06 | Background, screen-off and system controls | P0 | synchronized notification/lock-screen/Bluetooth state |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | Playing, Paused, No Track, immediate exit |
| UF-08 | Direct Voice command | P0 | Listening, Processing, Executed, Failed |
| UF-09 | Ambiguous or missing artist | P0 | Confirming, confirmed execution, not found |
| UF-10 | Voice timeout and retry | P0 | one retry, manual fallback, playback preserved |
| UF-11 | Calls, Audio Focus and route changes | P0 | deterministic pause/duck/recovery; no unintended speaker output |
| UF-12 | Settings, privacy and data clearing | P0 | Updated, cleared, consent-aware states |
| UF-13 | Liked Songs | P2 | Empty, Ready; removable from schedule |

## State record template

Every state must record:

- Entry trigger and preconditions
- Visible information and primary action
- Allowed secondary actions
- System work in progress
- Source of truth and persistence expectation
- Success transition
- Failure transition
- Recovery path
- Analytics event and allowed parameters, if any
- Accessibility requirements
- Driving restrictions, if applicable

## Approval checklist

- [ ] Every PRD/MVP P0 requirement maps to at least one User Flow.
- [ ] Every flow includes happy, empty, error and recovery behavior where applicable.
- [ ] UI closure, recreation, background playback and route interruptions are covered.
- [ ] Voice certainty, ambiguity, timeout, retry and not-found behavior are covered.
- [ ] P0/P1/P2 scope is explicit.
- [ ] Open architecture questions are linked to Issue #16 rather than silently decided in Figma.
- [ ] Figma pages and review date are linked below.

## Approved artifact links

_To be completed by Issue #3._
