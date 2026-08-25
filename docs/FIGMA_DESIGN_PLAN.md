# Figma Design Plan — Issue #15 Low-fidelity Wireframes

**Status:** **ACTIVE FOR ISSUE #15 LOW-FIDELITY WIREFRAMES**  
**Activation date:** 25 August 2026  
**Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Official workfile:** [Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)  
**File key:** `CC6I0e23Zwe6qMhRGaqRrT`  
**Verified page:** `0:1`  
**Page name at verification:** `Page 1`  
**Verification result:** PASS — authenticated design-file access confirmed  
**Contract addendum:** [Figma Wireframe Contract Addendum — 25 August 2026](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)  
**Delivery plan:** [Design Delivery Plan](DESIGN_DELIVERY_PLAN.md)

## Purpose

Figma is the official editable visual workfile for Issue #15 low-fidelity Wireframes. It is used to build and inspect grayscale structural frames for the approved MVP v1.0 screens, states and recoveries before final visual design begins.

GitHub remains the canonical behavioral, traceability, review and approval workspace. Figma does not replace the English PRD/MVP, User Flows, canonical State Inventory, Golden Scenarios, ADRs, repository Wireframe records or Issue/PR evidence.

## Authority and precedence

1. [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md).
2. Approved GitHub UF, State, Golden Scenario, ADR and Technical Spike records.
3. [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15) and [WIREFRAMES.md](WIREFRAMES.md).
4. Repository `WF-##_SCREEN_STATE.md` records.
5. Synchronized Figma frames using the same stable `WF-*` identifiers.

If a Figma frame conflicts with an approved GitHub behavioral record, GitHub controls until a reviewed repository change resolves and synchronizes the conflict.

## Verified starting point

The official workfile and page `0:1` were verified on 25 August 2026. The file is a Figma Design file and is available for Issue #15. The observed page name is `Page 1`; later workspace setup may rename it or add pages without changing the verified file identity.

This activation does not claim that any required Wireframe already exists or is approved.

## Scope

The workfile may contain low-fidelity structure for:

- Splash and startup recovery;
- two-slide Onboarding;
- permission rationale, denial and permanent-denial recovery;
- Home/Library, Search and Full Player states;
- Driving Mode and Voice overlay states;
- Liked Songs with visible P2/removable status;
- Settings, Privacy, Bluetooth and data/history clearing;
- annotated Android/system-only handoffs;
- playback interruption, lifecycle recreation and route recovery.

It must not introduce final visual styling, backend/account/streaming, Android Auto, iOS, v1.1 functionality or invented app screens for Android-owned surfaces.

## Naming and traceability

- Figma frames use stable names in the form `WF-##_SCREEN_STATE`.
- Every app-visible Figma frame has a corresponding repository `WF-##_SCREEN_STATE.md` record.
- Every repository record links the exact Figma page/node URL.
- Each pair records UF, canonical State and GS ownership plus P0/P1/P2 scope.
- System-only transitions receive explicit records or annotations and are never presented as invented app destinations.

## Reviewable output contract

A Wireframe is reviewable only when:

- its Figma structure exists and uses the stable identifier;
- its repository Markdown record exists;
- Figma node and repository links are reciprocal or unambiguous;
- navigation, Back, entry, exit, source of truth, recovery and persistence are documented;
- accessibility and Driving Mode constraints are annotated;
- Product, Design and Android findings are recorded in GitHub.

A Figma URL alone is not approval evidence.

## Historical disposition

This file was previously retained as a superseded no-Figma plan. That status was accurate for the 23 August 2026 checkpoint recorded in [GitHub-native Design Sync](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md). The project owner changed the active delivery decision on 25 August 2026.

The historical record is preserved unchanged. The dated [Figma contract addendum](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md) activates this plan for Issue #15 without retroactively altering earlier evidence.

## Current gate

**Phase 0 PASS:** workfile, file key, page ID, date, authority split and synchronization rules are registered.  
**Wireframe approval PENDING:** Issue #15 remains open until all required `WF-*` artifacts and reviews pass.
