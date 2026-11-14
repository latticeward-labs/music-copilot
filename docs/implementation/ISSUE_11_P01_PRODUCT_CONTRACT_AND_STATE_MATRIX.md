# Issue #11 — P01 Product Contract, State/Result Matrix & Handoff
## PLAY_ARTIST / One-Step Ambiguity — 2026-11-14

**Phase:** P01 (source-contract authoring only). **Status:** `SOURCE_DELIVERED_UNWIRED`.  
**References:** [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11), [P00 audit](ISSUE_11_P00_READINESS_AND_GAPS.md), [P00–P13 plan](ISSUE_11_PHASED_EXECUTION_PLAN_FA.md), [Voice Contract](../VOICE_CONTRACT.md), [Architecture public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Privacy](../ANALYTICS_AND_PRIVACY.md).  
**Attached source:** the owner's 12-PDF English archive `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Specifically ADR-0004 §§3–7, ADR-0005 §§4–12, and the Technical Blueprint's Voice command pipeline / state machine / Artist resolution.  
**New inert Kotlin contract:** [`PlayArtistContracts.kt`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/PlayArtistContracts.kt). This phase **does not** modify `VoiceSessionCoordinator`, `RestrictedVoiceIntentMapper`, playback dispatch, Room, resources or Compose rendering.

## 1. دامنه محصول و مسئولیت‌ها

- شش Intent نهایی دقیقاً `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST` است. پنج فرمان قبلی از Issue #10 **بدون تغییر** باقی می‌مانند.
- `PLAY_ARTIST` با عبارت پایه تأییدشده `Play songs by [artist]` تعریف می‌شود. هیچ مترادف، الگوی صوتی دوم، فرمان فهرست پخش/اینترنت/ولوم، یا تعمیم NLU در P01 تصویب نمی‌شود. P02 فقط معادل‌هایی را مجاز می‌کند که واقعاً در Voice Contract تصویب شده باشند.
- PTT فقط پس از لمس Mic و در همان Player/Driving فعال است. حداکثر ۶ ثانیه Listening، حداکثر یک Retry **تشخیص** و حداکثر یک سؤال/یک پاسخ **ابهام**، مستقل از یکدیگر. هیچ Wake Word یا گفت‌وگوی چندمرحله‌ای مجاز نیست.
- STT فقط متن و confidence واقعی/nullable برمی‌گرداند؛ Mapper تنها Intent و slot را تشخیص می‌دهد؛ Data فقط کاندیدای **هنرمند واقعی محلی** از ایندکس Room/MediaStore می‌دهد؛ Policy مسیر را تعیین می‌کند؛ Coordinator تنها مالک Session/Confirming است؛ Media3 service تنها مرجع Queue و Playing است؛ UI فقط مشاهده/انتخاب می‌کند.
- `MoonshineSileroSpeechToTextEngine` فعلی `providerConfidence = null` می‌دهد؛ این وضعیت `ArtistProviderConfidence.Missing` است، **نه ۰٫۹۰** یا امتیاز ساختگی. وضعیت `Invalid` جداست. سیاست پذیرش مستقیم برای artist در P05 باید safe و مستند شود؛ کپی کردن exception پنج فرمان دقیق برای نام‌های آزاد هنرمند ممنوع است.
- امتیاز Artist (exact/fuzzy) هرگز جای confidence خود provider را نمی‌گیرد. Baseline موجود: `T_DIRECT=0.90` (محدوده 0.80..0.98)، artist clear `0.88`، plausible `0.72`، جدایی دو گزینه `0.08`. این اعداد **قواعد موجود** هستند، نه نمره‌ای که در P01 تست/کالیبره شده باشد.
- ترتیب قطعی artist: exact normalized > fuzzy score descending > *local approved history priority only if truly available* > stable artist ID ascending. هم‌امتیاز/نزدیک: **Confirm**, نه خودکار. `Unknown Artist` حاصل متادیتای گمشده، کاندیدای واقعی فرض نمی‌شود.
- همه داده‌های هویتی (نام، `LibraryGroupId`، URI، MediaRef، متن گفتار و token) فقط موقتی و درون حافظه مجازند؛ **هیچ‌کدام** وارد Quality/Crash/GitHub logs نشوند. پیام TTS حداکثر یک جمله کوتاه.

## 2. قراردادهای نوع‌دار P01 (تعریف، نه فعال‌سازی)

| Type | مقادیر/فیلدهای لازم | قرارداد و مالک بعدی |
|---|---|---|
| `ArtistProviderConfidence` | `Missing`, `Invalid`, `Calibrated(probability)` | confidence واقعی جدا از similarity؛ P05 تصمیم safe می‌گیرد |
| `ArtistMatchEvidence` | `ExactNormalized`, `Fuzzy(similarity, algorithmVersion)` | نرمال‌سازی/الگوریتم fuzzy در P04؛ نسخه و محدوده finite |
| `PlayArtistCandidate` | `artistGroupId: LibraryGroupId`, `displayLabel`, `matchEvidence` | کاندیدای artist، نه song title؛ فقط محلی، محرمانه |
| `PlayArtistDecision` | `Direct`, `Ambiguous`, `NotFound`, `Unknown`, `Unsupported`, `Cancel`, `Stale`, `PlaybackFailure`, `Executed` | واژگان خروجی product؛ خود مدل **اجرایی نیست** |
| `PlayArtistDecision.Direct` | کاندیدای واحد + `expectedLibraryRevision` | فقط بعد از عبور از سیاست؛ هنوز ACK یا Playing نیست |
| `PlayArtistDecision.Ambiguous` | ۱ یا ۲ کاندیدای distinct، revision و دلیل enum | برای گزینه‌های plausible نزدیک یا confidence پایین؛ کپی محافظت‌شده |
| `PlayArtistConfirmationSnapshot` | session ID، origin، token، revision، ۱–۲ گزینه | فقط Coordinator در P07 مالک token و اعتبار آن می‌شود؛ **ephemeral** |
| `PlayArtistConfirmationOption` | `optionIndex` صفرمبنا (۰ یا ۱) + candidate | ترتیب انتخاب‌ها ثابت، هیچ جست‌وجوی ثانویه از UI |
| `PlayArtistConfirmationResponse` | `Select(token,index)`، `Neither(token)`، `Cancel(token)` | تنها پاسخ مجاز به یک Confirming؛ free-text/retry پاسخ تأیید نیست |

**Scope enforcement:** constructors صرفاً shape، bounded count، distinct group identities، finite score و index را تعریف/کنترل می‌کنند؛ **single-use token، valid current session، MediaStore re-query، ACK و dispatch idempotency هنوز در P01 پیاده نشده‌اند**. P07 باید آن‌ها را پیش از اجرای هر command در عمل enforce کند. Existing `VoiceSessionState.Confirming` و `VoiceCandidateToken` قرارداد runtime مرجع باقی می‌مانند؛ نوع P01 جایگزین coordinator یا authority موجود نمی‌شود.

## 3. ماتریس قطعی وضعیت / نتیجه / حفاظت پخش

| ID | شرط/ورودی | Product outcome | مسیر Voice و خروجی UI/TTS | آیا Playback mutation مجاز است؟ | فاز |
|---|---|---|---|---|---|
| R01 | بدون لمس صریح Mic | No session | `Idle`، کنترل دستی | **خیر** | #10 محفوظ |
| R02 | لمس Mic مجاز، provider حاضر | Listening | `Idle → Listening → Processing`، duck و release مطابق #10 | **خیر** تا تصمیم معتبر | #10 محفوظ |
| R03 | `PLAY_ARTIST`، artist واحد exact، confidence policy معتبر | `Direct` | `Processing → [ACK] → Executed`، بدون صفحه جدید | **بله، فقط** از `PlaybackGateway` بعد از re-query | P02–P06 |
| R04 | artist یکتا، fuzzy واضح و فاصله کافی، confidence معتبر | `Direct` | مانند R03؛ بدون حدس/تطبیق control fuzzy | **بله، با همان حفاظت** | P04–P06 |
| R05 | دو کاندیدای artist plausible نزدیک/هم‌امتیاز | `Ambiguous` | `Processing → Confirming`، دقیقاً یک سؤال و حداکثر ۲ دکمه | **خیر** پیش از انتخاب معتبر | P04–P08 |
| R06 | یک/دو کاندیدای plausible ولی confidence پایین | `Ambiguous` یا `Unknown` اگر بدون انتخاب ایمن | `Confirming` فقط در صورت bounded choice؛ در غیر آن `Failed` | **خیر** پیش از تأیید معتبر | P05–P08 |
| R07 | confidence provider مفقود (Moonshine فعلی) | `Ambiguous` تنها با انتخاب bounded و policy ایمن؛ وگرنه `Unknown` | **هرگز Direct با confidence ساختگی**؛ `Confirming` یا `Failed` | **خیر** تا تأیید مجاز | P05 |
| R08 | confidence نامعتبر/NaN یا داده لازم نامعتبر | `Unknown` / `Failed`؛ بدون حدس | بازگشت دستی؛ هیچ تصمیم Direct از داده نامعتبر | **خیر** | P05 |
| R09 | artist slot خالی، ناشناخته یا گفتار نگاشت‌ناپذیر | `Unknown` | `Processing → Failed`، پیام کوتاه و کنترل دستی | **خیر** | P02, P09 |
| R10 | درخواست فهمیده‌شده خارج از شش intent (playlist/streaming/volume) | `Unsupported` | `Processing → Failed`، بدون تغییر آن به نزدیک‌ترین فرمان | **خیر** | P02, P09 |
| R11 | عبارت PLAY_ARTIST معتبر ولی هیچ artist محلی plausible ندارد | `NotFound` | `Processing → Failed`، یک پیام کوتاه + manual fallback | **خیر؛ حفظ صف جاری** | P03–P04, P09 |
| R12 | `Confirming` فعلی + `Select(current token, current index)` | Selected (pending ACK) | `Confirming → [dispatch] → Executed` فقط پس از ACK؛ در خطا `Failed` | **یک dispatch حداکثر**؛ revalidate پیش از آن | P06–P08 |
| R13 | `Confirming` فعلی + `Neither(token)` یا `Cancel(token)` | `Cancel` | پایان Session → کنترل دستی | **خیر؛ پخش قبلی محفوظ** | P07–P09 |
| R14 | Back/Driving Exit/permission lost/route or focus interruption while pending | `Cancel` یا `Stale` | invalidation، `Failed/Idle` بدون auto-play | **خیر** | P07, P10 |
| R15 | token/session/optionIndex نامعتبر، consumed یا repeated click | `Stale` | reject → manual؛ هیچ confirmation دوم | **خیر؛ صفر dispatch تکراری** | P07, P10 |
| R16 | library revision تغییر کرده، مجوز revoke، فایل حذف شده | `Stale` یا typed failure | validation → `Failed`؛ انتخاب snapshot قدیمی ممنوع | **خیر** تا داده/مجوز دوباره معتبر شود | P03, P06–P07 |
| R17 | پاسخ دوم هنوز نامشخص / کاربر clarification دوم می‌خواهد | `Unknown` / `Failed` | **پرسش دوم ممنوع**؛ پایان و کنترل دستی | **خیر** | P07–P09 |
| R18 | Controller ناموجود، queue validation failure یا ACK رد/timeout | `PlaybackFailure` | `Failed`، موفقیت/TTS success اعلام نشود | **no new dispatch on known failure**؛ نتیجه واقعی Media3 مرجع است | P06–P07, P09 |
| R19 | ACK معتبر از service پس از فرمان واحد | `Executed` | `Executed` و حداکثر یک جمله TTS | نتیجه واقعی را فقط از `PlaybackSnapshot` منتشر کن | P06–P09 |
| R20 | Recognizer timeout/NoMatch در attempt اول (قبل از Confirming) | Existing #10 retryable `Failed` | حداکثر **یک recognition retry** از طریق `retry()` | **خیر** | #10 محفوظ |
| R21 | `Confirming` فعال و `retry()` یا پاسخ دوم/کاندیدای متن آزاد | Reject/ `Stale` | retry تشخیص ≠ پاسخ ابهام؛ هیچ Listening جدید/مکالمه آزاد | **خیر** | P07, P10 |
| R22 | Process death / Activity recreation حین Confirming | `Stale` اگر session از دست رفت؛ یا snapshot جاری فقط از Coordinator | بدون بازپخش/بدون double claim؛ UI observer | **خیر بدون current one-shot auth** | P07, P10 |

**ACK caveat / engineering gate:** در R18 اگر command قبلاً به سرویس رسیده ولی تأییدش نامشخص است، P01 نمی‌تواند واقعیت Queue/Playing را استنتاج یا rollback را وعده دهد. P06 باید safe commit/ACK و recovery را طوری طراحی کند که شکست‌های قبل از dispatch صف قبلی را دست‌نخورده نگه دارند، شکست‌های ambiguous را `Executed` جا نزنند و از تکرار خودکار جلوگیری کنند. سنجش واقعی این شرط در P13 است.

## 4. قرارداد confirmation تک‌مرحله‌ای؛ ترتیب پذیرش در P07

1. Coordinator پس از خروجی `Ambiguous` یک snapshot مستقل از repository ایجاد می‌کند: `(sessionId, origin, fresh token, expectedLibraryRevision, options[0..1])`. گزینه‌ها ۱ یا ۲ عدد، دارای ID یکتا و ترتیب ثابت هستند.
2. UI فقط `displayLabel` همان snapshot را نمایش می‌دهد و `Select(token, optionIndex)`، `Neither(token)` یا `Cancel(token)` می‌فرستد. گزینه‌ها باید برابر snapshot باشند؛ UI حق lookup تازه، افزودن نام هنرمند یا ارسال media URI را ندارد.
3. Coordinator ابتدا session/generation/origin و token فعلی، هنوز-مصرف-نشده بودن و membership index را **اتمیک** زیر guard چک می‌کند. سپس قبل از suspend/async dispatch، token را consumed و execution claim را می‌گیرد.
4. در مرحله اجرای انتخاب، revision و مجوز/وجود فایل دوباره چک می‌شوند؛ یک `CommandId` برای همان command، بدون اجرای دوباره، به سرویس داده می‌شود.
5. Invalid/stale/repeated response/second unclear answer همیشه safe rejection است؛ **هیچ سؤال clarification دوم و هیچ retry تشخیص از Confirming** مجاز نیست. لغو یا Neither هرگز `ReplaceQueue` نمی‌سازد.
6. Activity recreation مجوز مجدد نمی‌سازد؛ process death session/token را persist یا auto-resume نمی‌کند. `Executed` فقط بعد از ACK معتبر و publish state واقعی.
7. این توالی **یک الزام قراردادی P01** است، نه ادعای پیاده‌سازی. در سورس فعلی `DefaultVoiceSessionCoordinator.confirm()` همچنان stale برمی‌گرداند و panel فعلاً `ConfirmationUnavailable` نشان می‌دهد.

### تشخیص Retry از Confirmation

| ویژگی | Recognition retry | One-step artist confirmation |
|---|---|---|
| زمان مجاز | تنها بعد از `Failed(canRetry=true)` مربوط به STT/NoMatch و حداکثر یک بار | تنها هنگام `VoiceSessionState.Confirming` جاری |
| API جاری | `VoiceSessionCoordinator.retry()` | `confirm(token, optionIndex)` (انتخاب)، و cancel/Neither در coordinator flow |
| مصرف microphone | retry **فقط با اقدام صریح جدید** و مطابق guardهای #10 | انتخاب دکمه snapshot؛ نه آغاز دوباره STT یا ضبط آزاد |
| تعداد | ۱ retry حداکثر، در مجموع ۲ recognition attempt | ۱ سؤال و ۱ پاسخ؛ حداکثر ۲ گزینه |
| خطای دوم | `Failed` + manual | `Failed` / `Stale` + manual بدون clarification بعدی |

## 5. Mapping به error، UI و Telemetry موجود

- `Unknown` → دسته `VOICE_NO_MATCH` / `VoiceSessionState.Failed`؛ `Unsupported` → fail-safe / manual، بدون اضافه کردن intent.
- `NotFound` → خطای typed Voice/Library با متن کوتاه «Not found»؛ اگر کد جدید لازم شد، فقط در family سازگار با [Issue #16 error contract](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) و بعد از scope review P09. هیچ filename/artist در error code نباشد.
- `Stale` → `VOICE_STALE_SESSION` یا `LIBRARY_BROWSE_STALE` بسته به محل خطا؛ invalid token هرگز دوباره تلاش به dispatch نمی‌کند.
- `PlaybackFailure` → `PLAYBACK_REJECTED`, `PLAYBACK_FAILED`, `CONTROLLER_DISCONNECTED` یا `MEDIA_ITEM_UNPLAYABLE` به‌شکل sanitized و بدون محتویات خصوصی. UI/TTS success فقط بعد از ACK واقعی.
- `Cancel` → restore manual controls؛ صف و پخش به‌خاطر لغو یک Voice request تغییر نمی‌کنند. `Neither` فرایند انتخاب را رد می‌کند و نباید Retry یا new candidate تلقی شود.
- `Executed` → فیدبک کوتاه و رویداد `CommandExecuted` فقط بعد از ACK. Telemetry فقط **schema موجود** را استفاده می‌کند: `IntentDetected(intentType, confidenceBucket)`, `IntentFailed(intentType, failureCode, confidenceBucket, latencyBucket)`, `CommandExecuted(intentType, latencyBucket)`, `ManualCorrection(previousIntent, correctionType)` با codeهای allowlisted. فیلد جدیدی به Quality schema در P01 اضافه نشده است.
- **Privacy exclusions:** artist label/ID, track title, transcript, audio, URI, local history, token, candidate list, queue contents, precise position و free-form exception strings کاملاً ممنوع. این انواع می‌توانند در display خصوصی UI باشند، **نه** log یا telemetry.
- `TTS` عدم حضور یا خطای خود را به Playback منتقل نمی‌کند؛ پس از ACK موفق، شکست TTS موفقیت Media3 را برنمی‌گرداند.

## 6. معیارهای پذیرش و مرز شواهد

[Machine-readable traceability](ISSUE_11_P01_ACCEPTANCE_TRACEABILITY.json) شامل ۱۳ معیار اصلی Issue #11، نتیجه‌های مرتبط، فاز مالک و وضعیت دقیق است. هیچ معیار اصلی در P01 PASS نشده است. نگارش contract ≠ implementation ≠ build ≠ test execution.

**P01 acceptance checks (source-only):**
- [x] تمام outcomeهای Direct/Ambiguous/NotFound/Unknown/Unsupported/Cancel/Stale/PlaybackFailure و Executed مدل شده‌اند.
- [x] snapshot تک‌مرحله‌ای token+index، ۱–۲ option، و None/Cancel typed و جدا از recognition Retry تعریف شده‌اند.
- [x] وظایف Mapper/Data/Policy/Coordinator/Media3/UI/Telemetry و مرز اطلاعات خصوصی مشخص شده‌اند.
- [x] حالات Edge/NotFound/duplicate/revision/ACK و قوانین fallbacks به ماتریس state/result نگاشت شده‌اند.
- [x] AC01–AC13 با status واقعی قابل رهگیری و phase owner ثبت شده‌اند.
- [ ] اجرای واقعی رفتار PLAY_ARTIST: **NOT_IMPLEMENTED** (P02–P12).
- [ ] build / test / APK / physical phone QA: **NOT_RUN** (P13 only).

## 7. P02–P13 handoff و ریسک‌های باز

- **P02** grammar versioned for exactly approved `Play songs by [artist]`, extract slot; no changes to five direct-control phrases.
- **P03** artist-group lookup on local repository with permission/revision; **P04** deterministic normalization/scoring/tie/optional approved local history; **P05** safe missing-provider-confidence decision; **P06** artist queue from valid MediaRefs and one service-owned ACK.
- **P07** real one-shot coordinator confirmation; **P08** Compose selection in Full Player/Driving; **P09** short TTS + typed errors; **P10** lifecycle/race/duck guards; **P11** privacy-safe events; **P12** wiring and authored-only QA fixtures.
- **P13 ONLY:** cumulative build-only APK; owner phone M01–M48, specialized T01–T10, 6×10 lab L01–L06, source/result provenance and owner-approved merge disposition.
- **Open P0 quality blocker [#80](https://github.com/latticeward-labs/music-copilot/issues/80):** Issue #10 owner reported **24/50 (48%)**, below ≥90%; no new voice lab has run. Provider `null` confidence and artist P05 policy remain unresolved/explicit. No claim of release readiness.

**P01 disposition:** `SOURCE_DELIVERED_UNWIRED`; proceed to **P02** without enabling this capability early, without changing running app behavior, and without any Gradle, compiler, lint, test runner, GitHub Actions, emulator, physical-phone or APK step.
