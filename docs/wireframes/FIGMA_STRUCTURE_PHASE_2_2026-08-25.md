# Figma Structure — Issue #15 Phase 2

**Product:** Music Copilot MVP v1.0  
**Date:** 25 August 2026  
**Issue:** [#15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Official Figma workfile:** [Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)  
**File key:** `CC6I0e23Zwe6qMhRGaqRrT`  
**Physical page:** `0:1` — `00_Index`  
**Decision:** **PASS / COMPLETE — single-page structure approved for Phase 2**

## Controlled single-page accommodation

The connected Figma account is on the Starter tier, which enforces a three-page limit. After the multi-page attempt was rejected atomically by Figma, the user explicitly authorized all Phase 2 workspaces to be organized inside the existing single page and instructed that no new Page be created.

Therefore:

- the physical Figma Page `0:1` was renamed from `Page 1` to `00_Index`;
- the former logical Page names are represented by top-level Figma Section nodes on that Page;
- this changes physical organization only and does not change UF, State, GS, priority, ownership, or planned `WF-*` behavior;
- GitHub remains the controlling behavioral and approval authority.

## Structure register

| Logical workspace / lane | Figma type | Node ID | Result |
|---|---|---:|---|
| `00_Index` | Section | `12:2` | PASS |
| `01_Foundations` | Section | `12:3` | PASS |
| `02_Splash` | Section | `12:4` | PASS |
| `03_Onboarding` | Section | `12:5` | PASS |
| `04_Permissions` | Section | `12:6` | PASS |
| `05_Home_Library` | Section | `12:7` | PASS |
| `06_Search` | Section | `12:8` | PASS |
| `07_Full_Player` | Section | `12:9` | PASS |
| `08_Driving_Mode` | Section | `12:10` | PASS |
| `09_Voice` | Section | `12:11` | PASS |
| `10_Liked_Songs_P2` | Section | `12:12` | PASS |
| `11_Settings_Privacy` | Section | `12:13` | PASS |
| `12_System_Transitions` | Section | `12:14` | PASS |
| `13_Review` | Section | `12:15` | PASS |
| `P0` | Section | `12:16` | PASS |
| `P1` | Section | `12:17` | PASS |
| `P2` | Section | `12:18` | PASS |
| `System-only` | Section | `12:19` | PASS |

## Naming and authority guide

The `01_Foundations` Section contains the visible naming contract:

- required pattern: `WF-##_SCREEN_STATE`;
- example: `WF-01_SPLASH_LOADING`;
- uppercase snake case after the stable `WF-##` prefix;
- every future Figma `WF-*` node must be synchronized with its reviewed GitHub record.

The `00_Index` Section records:

- single-page Phase 2 topology;
- Coverage Manifest status: 126/126 PASS;
- GitHub as behavioral/approval authority;
- Figma as the editable low-fidelity workfile;
- visual frames remain unapproved until linked to GitHub records.

## Validation evidence

| Check | Expected | Actual | Result |
|---|---:|---:|---|
| Physical Pages | 1 | 1 | PASS |
| Physical Page name | `00_Index` | `00_Index` | PASS |
| Logical workspace Sections | 14 | 14 | PASS |
| Priority/ownership Sections | 4 | 4 | PASS |
| Total top-level Sections | 18 | 18 | PASS |
| Missing Sections | 0 | 0 | PASS |
| Extra Sections | 0 | 0 | PASS |
| Duplicate Sections | 0 | 0 | PASS |
| Test text `Hello` | 0 | 0 | PASS |
| Visible naming contract | 1 | 1 | PASS |
| Invalid existing `WF-*` names | 0 | 0 | PASS |
| Section overlaps | 0 | 0 | PASS |

## Exit decision

**Phase 2 is complete.** The Figma file now has a verified, non-overlapping, single-page construction structure for all planned low-fidelity workspaces, priorities, and system-only transitions.

This PASS authorizes construction of actual low-fidelity `WF-*` frames. It does not approve any visual Wireframe and does not close Issue #15.
