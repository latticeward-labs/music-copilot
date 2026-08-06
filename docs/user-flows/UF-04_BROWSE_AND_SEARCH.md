# UF-04 - Browse and Search Local Music

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking  
**Updated:** 06 August 2026  
**Status:** User Flow specification and traceability - **PASS**; TS-06, and reviewer evidence remain open  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-04_BROWSE_AND_SEARCH.md`](mermaid/UF-04_BROWSE_AND_SEARCH.md)  
**Raw Mermaid source:** [`mermaid/UF-04_BROWSE_AND_SEARCH.mmd`](mermaid/UF-04_BROWSE_AND_SEARCH.mmd)

## Authority and boundary

UF-04 defines how a user with a verified local library browses Songs, Artists, Albums, and Folders; searches the derived local index; recovers from empty, stale, inaccessible, and query-error states; selects a playable local track; and hands an acknowledged playback start to UF-05.

The official English PRD/MVP are the product authorities. The current GitHub ADR register is the architecture authority: ADR-0001 through ADR-0006 were reviewed and accepted on 06 August 2026. Older attached PDF or dated User Flow snapshots that still label these ADRs Proposed do not override the current repository record.

This flow does not approve visual layout, list density, artwork treatment, default sort order, search ranking, tokenization, queue-population rules, navigation implementation, Room schema, repository classes, or playback-service APIs. Those decisions remain with Issue #15, Issue #16, TS-06, and UF-05 as identified below.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-04 |
| User goal | Find lawful local music by browsing or search and start the intended accessible track without an account, network dependency, or false playback state. |
| Priority | P0 - Beta blocking |
| User Story | US-02 supplies the account-free local-index precondition; the PRD/MVP Library and Search scope supplies the browse/search goal. No new User Story is invented. |
| Functional Requirement | **FR-01 complete at User Flow specification level when UF-01 through UF-04 are read together**: consent, scan/index/readiness, and display by track, album, artist, and folder. Local Search is also required by the PRD/MVP product and screen scope. Executed Android evidence remains open under TS-06. |
| Screens and states | S-04 Home / Library Ready with Songs, Artists, Albums, and Folders; S-05 Search Idle/Result/No Result/Error; stale or inaccessible selection recovery; playback-start handoff to S-06 Full Player/UF-05. |
| Influencing ADRs | ADR-0001 Accepted - native Android observable UI state; ADR-0002 Accepted - service/MediaSession owns playback and acknowledges selection; ADR-0003 Accepted - MediaStore authority plus derived/rebuildable browse/search index; ADR-0006 Accepted - no account, backend, sync, catalog, streaming, upload, or download. |
| Failure and recovery | Empty library returns to UF-03; revoked access returns to UF-02; stale/moved/deleted selection is reconciled and remains recoverable; No Result preserves the query; Search Error preserves the query and verified library and offers Retry; failed playback acknowledgement never fabricates Playing and permits another selection. |
| Data and permissions | Android media access is authoritative for permission; MediaStore is authoritative for accessible media identity/existence; the local relational index is a derived/rebuildable browse/search projection; playback service/MediaSession is authoritative after selection; no personal media metadata is approved for telemetry. |
| Open assumptions or evidence | TS-06 device evidence; Issue #15 visual/navigation decisions; Issue #16 query/repository contracts; UF-05 queue population/order and manual playback behavior; Product/Design/Android/QA/Privacy review. ADR status itself is not open. |

## Preconditions and entry

UF-04 may start only when Home / Library has a verified current state from UF-03.

| Verified entry state | Required route |
|---|---|
| Ready with accessible supported media | Allow Browse or Search. |
| Empty | Stay in the truthful Empty state and hand Rescan/recovery to UF-03; do not show fabricated browse results. |
| Unknown, stale, interrupted, or reconciliation required | Hand off to UF-03 before presenting the affected result as playable. |
| Media access denied or revoked | Hand off to UF-02; do not trust cached rows as playable. |
| Active playback already exists | Preserve and observe it; browsing/searching must not restart, replace, or duplicate the player until the user selects a track and the service accepts the command. |

Search is available from Home / Library. Typing and long result lists are not introduced into Driving Mode.

## Browse contract

Home / Library remains one top-level destination with four internal views. This is an information and transition contract, not a Wireframe.

| View | Required behavior | Selection behavior | Controlled unknowns |
|---|---|---|---|
| Songs | Present the verified accessible track collection from the derived index. | Selecting a track enters common selection validation. | Exact row fields, grouping, default order, and sort controls remain Issue #15/#16 decisions. |
| Artists | Present artist groupings derived from accessible indexed media. | Selecting an artist opens that artist's local track collection; selecting a track enters common validation. | Unknown/missing-artist labeling, collation, and ordering remain open; no fuzzy execution rule is created here. |
| Albums | Present album groupings derived from accessible indexed media. | Selecting an album opens its local track collection; selecting a track enters common validation. | Unknown/missing-album labeling, artwork policy, disc ordering, and sorting remain open. |
| Folders | Present folder groupings for accessible indexed local audio without exposing paths to telemetry. | Selecting a folder opens its local track collection; selecting a track enters common validation. | Display-name, hierarchy/flattening, and ordering remain Issue #15/#16 decisions. |

All four views must preserve a reachable path back to Home / Library and Search. The exact navigation graph and Back-stack implementation are not fixed by UF-04.

## Search contract

1. The user opens Search from Home / Library.
2. Empty input shows an Idle state and does not fabricate results.
3. A non-empty query searches only the derived on-device index of user-authorized local media.
4. The system resolves to `Result`, `No Result`, or `Error`.
5. `Result` exposes selectable results that resolve to local tracks. Exact ranking, entity grouping, tokenization, case/diacritic normalization, debounce, and minimum query length remain Issue #15/#16 decisions.
6. `No Result` preserves the query so the user can edit or clear it; it does not offer streaming, upload, download, online catalog, or account paths.
7. `Error` preserves the query and verified library, shows a concise failure, and offers Retry plus clear/edit query. Search failure never deletes or invalidates a verified library by itself.
8. Selecting a result enters the same common selection validation used by Browse.

## Common selection and UF-05 handoff

1. Revalidate the selected media reference against current access and MediaStore availability.
2. If access is revoked, stop the selection attempt and hand off to UF-02.
3. If the entry is stale, moved, deleted, or otherwise inaccessible, remove or refresh the stale derived reference through UF-03 reconciliation, keep the user in a recoverable browse/search context, and never show false Playing.
4. If the reference is currently accessible, send a selection request through the shared playback command boundary.
5. The UI waits for authoritative service/MediaSession state; it does not optimistically become the playback owner.
6. Only an acknowledged playback start may transition to Full Player and UF-05.
7. If playback start fails, retain browse/search context, present a concise recoverable error, and allow another selection. Corrupt-item queue continuation after playback ownership begins belongs to UF-05/UF-11.

UF-04 deliberately does not decide whether the surrounding browse/search result set replaces, appends to, or otherwise forms the active queue. UF-05 must define that user-visible queue contract, and Issue #16 must define its technical implementation.

## Detailed state inventory

| State | Trigger / entry | Visible state | Allowed actions | System action / source of truth | Success transition | Failure transition | Recovery | Persistence | Analytics / privacy | Accessibility / driving |
|---|---|---|---|---|---|---|---|---|---|---|
| Home / Library Ready | UF-03 verifies accessible media | One destination; Songs/Artists/Albums/Folders and Search are available | Choose view, Search, select track, Rescan, open existing player | Observe derived index and service-owned playback | Browse view or Search | Stale/unknown -> UF-03; revoked -> UF-02 | Reconcile or recover permission without false Ready | Preserve verified index; live playback remains service-owned | No filenames, titles, artists, albums, folders, paths, URIs, or library contents in telemetry | Labeled destinations, scalable text, TalkBack order; no driving typing/list use |
| Songs | User chooses Songs | Verified accessible track collection | Select track; return; Search | Query derived index; availability is not inferred from a stale row | Common selection validation | Query/index error or stale item | Retry/UF-03 reconciliation | View preference only if later approved; no playback authority | No new event approved | Each row has an accessible name/action; state not color-only |
| Artists | User chooses Artists | Artist groupings from accessible indexed media | Select artist; return; Search | Query derived groupings | Artist tracks | Query/index error | Retry without deleting verified library | Same as Songs | Same as Songs | Group and navigation labels exposed to TalkBack |
| Artist tracks | User selects artist | Local track collection scoped to selected artist | Select track; return | Query derived index | Common selection validation | Empty-after-reconcile or index error | Refresh; return to Artists; UF-03 if stale | Navigation context may be transient | Same privacy rule | Clear heading and back semantics; no long-list driving use |
| Albums | User chooses Albums | Album groupings from accessible indexed media | Select album; return; Search | Query derived groupings | Album tracks | Query/index error | Retry without fabricating results | No new persistence requirement | Same privacy rule | Accessible group labels and actions |
| Album tracks | User selects album | Local track collection scoped to selected album | Select track; return | Query derived index | Common selection validation | Empty-after-reconcile or index error | Refresh; return to Albums; UF-03 if stale | Navigation context may be transient | Same privacy rule | Clear heading and back semantics |
| Folders | User chooses Folders | Folder groupings for accessible indexed media | Select folder; return; Search | Query derived groupings; never use telemetry as catalog | Folder tracks | Query/index error | Retry/UF-03 reconciliation | No new persistence requirement | Paths and folder names prohibited from telemetry | Folder labels are readable; hierarchy is not encoded by color alone |
| Folder tracks | User selects folder | Local track collection scoped to selected folder | Select track; return | Query derived index | Common selection validation | Empty-after-reconcile or index error | Refresh; return to Folders; UF-03 if stale | Navigation context may be transient | Same privacy rule | Clear heading and back semantics |
| Search Idle / Query | User opens Search or edits input | Empty Idle state or current query | Type, edit, clear, submit, return | Query work is local; exact timing/tokenization deferred | Result, No Result, or Error | Local query/index failure | Retry or edit/clear; library remains intact | Query need not persist beyond the active Search experience | Query text and matched media metadata are not telemetry | Labeled input/clear; keyboard flow accessible; unavailable in Driving Mode |
| Search Result | Local query succeeds with matches | Selectable results resolving to local tracks | Select result; edit/clear query; return | Derived index provides candidates; selection is revalidated | Common selection validation | Stale/inaccessible result or later start failure | Reconcile and retain recoverable Search context | Preserve query while Search remains active | No query, title, artist, album, folder, path, URI, or result payload telemetry | Announce result state/count only if privacy-safe and useful; rows accessible |
| Search No Result | Local query succeeds without matches | Explicit No Result and preserved query | Edit, clear, retry, return | No fabricated remote fallback | New query -> Result/No Result/Error | Subsequent query error | Preserve query and manual Browse access | Preserve within active Search experience | No query logging | Textual state; focus does not trap user |
| Search Error | Local query cannot complete | Concise Error with preserved query and verified library | Retry, edit, clear, return to Browse | Typed local failure; do not corrupt index merely because query failed | Result or No Result after recovery | Repeated failure | Browse remains available; UF-03 only when index truth is actually stale/broken | Preserve verified library and active query context | Coarse reviewed error category only if separately approved | Error and Retry announced; no color-only meaning |
| Selection validation | Track chosen from any browse/search list | Short pending state only if visible feedback is needed | Cancel/back if supported; choose another item after failure | Android permission + MediaStore revalidation | Submit selection request | Revoked, stale, moved, deleted, inaccessible | UF-02 or UF-03; retain source context | No duplicate live state | No media identifier in logs/analytics | Progress/error meaning announced without blocking navigation |
| Playback-start handoff | Accessible selection submitted | Existing service state remains authoritative until acknowledgement | Return after failure; open Full Player after success | Playback service/MediaSession accepts command and reports actual state | Full Player / UF-05 with playback started | Start rejected/unreadable/controller unavailable | Concise error; remain in browse/search; allow another selection | Queue/live position belong to playback service; P1 restore is outside UF-04 | Coarse `player_error` only under the existing allowlist; no personal metadata | No false Playing announcement; recovery action labeled |

## Invariants

- Browse and Search work without an account, product backend, online catalog, streaming service, upload/download path, or required network connection.
- UI, Room, and Search results never become playback authority.
- Cached or indexed presence alone never proves that a file is currently accessible or playable.
- Search Error never destroys a verified library; No Result is not an Error.
- The query remains editable after No Result or Error.
- Selection never displays Playing before service/MediaSession acknowledgement.
- Active playback already in progress is not interrupted merely because the user browses, searches, clears a query, or encounters a search failure.
- UF-04 introduces no new analytics event and never transmits personal media metadata.
- Browse/Search typing and long lists are excluded from Driving Mode.
- No unresolved sort, ranking, grouping, queue, layout, or navigation choice is silently presented as approved.

## Open decisions and evidence

| Gate | Owner | Required evidence / decision | Effect on UF-04 status |
|---|---|---|---|
| TS-06 | Android + QA | MediaStore permission/revocation, move/delete, stale-index reconciliation, rebuild, and approximately 5,000-file behavior across supported Android versions/OEMs | Flow specification passes; executed implementation evidence remains open |
| Issue #15 | Product Design | Exact information hierarchy, row content, grouping, sort controls, Search presentation, loading feedback, empty/error copy, and Back behavior | Blocks Wireframe approval, not UF-04 traceability |
| Issue #16 | Android + Product | Query normalization/ranking contract, folder projection, repository/use-case boundaries, refresh/invalidation, controller acknowledgement, and performance/concurrency behavior | Blocks final architecture/implementation approval |
| UF-05 | Product + Design + Android | Whether the selected context replaces/appends/forms the queue, queue ordering, and manual playback recovery after ownership begins | Blocks Cluster B completion, not UF-04 browse/search handoff |
| Role review | Product, Design, Android, QA, Privacy | Review against PRD/MVP, accepted ADRs, recovery, privacy, and accessibility | Final Issue #3 approval remains open |

## Exit checklist and decision

- [x] One ten-field UF-04 traceability record exists.
- [x] Songs, Artists, Albums, and Folders entry, drill-in, track selection, and recovery behavior are represented without locking a Wireframe.
- [x] Search Idle, Result, No Result, and Error are distinct.
- [x] No Result and Error preserve an editable query; Error also preserves the verified library.
- [x] Empty, stale index, moved/deleted media, revoked access, inaccessible selection, and failed playback acknowledgement have explicit recovery.
- [x] Android permission, MediaStore, derived index, and playback-service authority are distinct.
- [x] Playback starts only after service/MediaSession acknowledgement and hands off to UF-05.
- [x] FR-01 is complete at User Flow specification level across UF-01 through UF-04; TS-06 evidence remains open.
- [x] ADR-0001 through ADR-0006 use the current GitHub Accepted status.
- [x] No account/backend/sync/catalog/streaming/upload/download/Android Auto/iOS/v1.1 scope was introduced.
- [x] Privacy, accessibility, and Driving Mode exclusions are explicit.
- [x] The Markdown diagram and raw Mermaid source are logically identical.
- [ ] recorded.
- [ ] TS-06 executed evidence recorded.
- [ ] Product/Design/Android/QA/Privacy review recorded.
- [ ] UF-05 and UF-06 completed; Cluster B exit decision recorded.

**Decision:** UF-04 is complete and passes at the User Flow specification and traceability level. This decision completes FR-01 only at that documentation level. It does not approve the Cluster B set, Technical Spike evidence, Wireframes, final Software Architecture, implementation, or closure of Issue #3.
