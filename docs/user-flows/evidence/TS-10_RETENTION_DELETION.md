# TS-10 - Retention and Selected Deletion Semantics

**Question:** What semantics apply to short history and diagnostics without inventing policy?  
**Result:** **PASS at executable privacy-plan level; durations/provider obligations remain Privacy/#12/#16**

## Executable contract

- Clearable categories are explicit: Short History, Local Diagnostics, and approved Provider Diagnostics.
- Confirmation is mandatory and no category is destructively preselected.
- Likes, Settings, Consent, Library Projection, live Playback/Queue, and Media Files are protected.
- The plan is idempotent and reports local/provider boundaries independently.
- External deletion failure produces Partial/Retryable, never false Cleared.
- A collected retention-bound category requires a positive approved duration; null, zero, or negative means collection disabled.
- Telemetry fields pass an allowlist and prohibited content keys are rejected before an adapter.

## Automated rows

Unconfirmed request, empty selection, protected-category attempt, history-only clear, diagnostics-only clear, mixed clear, provider partial failure, repeat request, absent retention, invalid retention, valid retention, allowed coarse event, and prohibited transcript/title/device fields are covered.

## Non-claim

The harness deliberately contains no invented retention days, cloud processor, legal statement, or deletion SLA. Production values and provider behavior require explicit Privacy/Product approval and implementation evidence.
