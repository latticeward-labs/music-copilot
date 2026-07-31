# Definition of Done — MVP v1.0

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md). Read with the [Delivery Workflow](DELIVERY_WORKFLOW.md).

## Pre-build artifact Done

A User Flow, Wireframe, Architecture or Design artifact is Done only when:

- Its source requirements and P0/P1/P2 scope are traceable.
- Happy, empty, error, interruption and recovery behavior are covered where applicable.
- Product, Design, Android and QA review roles have approved the relevant gate.
- Open questions are recorded and assigned; no P0 decision is silently deferred to implementation.
- Structural UX changes are reflected in User Flows and Wireframes.
- Consequential architecture decisions are recorded in an ADR or superseding ADR.
- Repository documentation links to the approved artifact and evidence.

## Implementation Issue Done

- All acceptance criteria pass with evidence.
- The implementation conforms to approved User Flows, Wireframes, Design handoff and Software Architecture contracts.
- No out-of-scope account, backend, streaming, cloud-sync or advanced playlist behavior is introduced.
- Permission denial, loading, empty/error states and recovery are handled.
- UI recreation and background/lock-screen/Bluetooth playback remain synchronized with service-owned state.
- Calls, headset disconnect, Bluetooth disconnect and Audio Focus scenarios pass where applicable.
- Voice uses only the six approved commands, one short response, at most one retry and one-step ambiguity confirmation.
- Duplicate execution, stale state and race-prone terminal paths are tested where applicable.
- No raw voice or prohibited personal data is stored or logged.
- Driving Mode has at least 48dp targets, suitable contrast, scalable text and TalkBack labels.
- No typing, deep navigation, nested menus or long result lists appear in Driving Mode.
- Unit/integration/UI tests cover the approved seams and failure paths.
- CI is green and a clean checkout builds without private credentials.
- Documentation, telemetry schema and residual risks are updated.

## Release-candidate Done

- Every P0 requirement maps to passing test evidence.
- Zero open P0.
- Internal crash-free sessions are at least 99%.
- Golden scenario passes on at least three vehicle/head-unit models.
- Android 10, Android 12 and Android 14+ evidence exists across Pixel/stock, Samsung/One UI and Xiaomi/MIUI.
- No known unintended-speaker output, serious repeated wrong command or undefined call/route behavior remains.
- Privacy/consent copy is approved.
- Analytics events are complete, privacy-safe and reliable enough for the Beta decision.
- Rollback, support and tester guidance are documented.

## Closed Beta Done

- Fifty qualified drivers complete the documented 14-day protocol or missingness is explained.
- Required quantitative and qualitative evidence is analyzed.
- Metric definitions and thresholds remain consistent with PRD/MVP.
- A documented GO, ITERATE or STOP decision records evidence, uncertainty, risks and next actions.
