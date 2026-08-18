# UF-13 Traceability Matrix

| Source requirement / decision | UF-13 coverage | State / diagram evidence | Executable reference check | Result |
|---|---|---|---|---|
| PRD FR-09: like a track and play it from simple Liked Songs | Like/unlike and play-list/row paths | UL-04 through UL-10 | duplicate-safe mutation; queue/start index | Covered |
| MVP P2 Like; include only if capacity remains | Capability removal is explicit and dependency-free | UL-01; `P2 not shipped` branch | disabled projection returns Removed | Covered |
| MVP Liked Songs Empty/Ready | Two required screen states | UL-03/UL-04 | empty/ready projection tests | Covered |
| Phase-0 S-09: local queue and safe missing-file reconciliation | Accessible projection only; service handoff | UL-09/UL-10/UL-13 | stale filtering and stale playback rejection | Covered |
| Phase-0 D-027: separate P2 first cut | Not merged into Library playlist behavior | Scope boundary and Mermaid entry gate | feature-disabled test | Covered |
| Phase-0 catalog: Home/Library, Liked Songs, Full Player | Entry and return surfaces are explicit | Mermaid and UF-13 sections | N/A | Covered |
| ADR-0002: service-owned player | UI submits queue and waits for acknowledgement | UL-09/UL-10/UL-11 | playback plan is command data only | Covered |
| ADR-0003: local app-owned Likes; MediaStore authority | Separate Like intent from playable existence | UL-02/UL-06/UL-13 | reconcile accessible IDs; preserve order | Covered |
| ADR-0003: stale/deleted media not playable | Filter and reconcile before display/play | UL-11/UL-13 | missing item and stale selection tests | Covered |
| ADR-0006: no backend/account/sync/streaming | Local-only surface and data | Scope boundary | no network/provider dependency in contract | Covered |
| Privacy D-028 | No new telemetry; personal media values prohibited | Ten-field record | contract contains no telemetry adapter | Covered |
| Accessibility / driving | Semantics, announced selected state, 48dp; no Driving destination | UF-13 accessibility section | Downstream UI verification #15/#17 | Covered at flow stage |

## Deliberately downstream

Room/DataStore schema choice, migrations, final ordering, visual copy, Compose behavior, Media3 wiring, physical-device evidence, and capacity decision remain implementation/design work in Issues #6/#15/#16/#17. None is silently approved by this User Flow.
