# Issue #10 — P00 | ممیزی ایستای منابع، کد، وابستگی‌ها و Traceability

**تاریخ ثبت:** 2026-10-25  
**وضعیت P00:** `SOURCE_AUDIT_COMPLETED_WITH_IMPLEMENTATION_GAPS` — تکمیل بررسی و مستندسازی، **نه** PASS اجرا یا تأیید قابلیت Voice.  
**Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10) · **Branch:** `feature/10-ptt-voice-five-control-intents`  
**مبنای GitHub:** `main` @ `e13172683ee8495e26b7c2d56f88444e4969891e`؛ پیش از شروع P00، branch نسبت به main دقیقاً **۲ commit جلو و ۰ commit عقب** بود؛ تنها دو سند برنامه و چک‌لیست Issue #10 تفاوت داشتند.  
**فایل مرجع:** `MSM2(1)(1).rar`، SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`؛ **۱۲ فایل PDF** استخراج و به شکل متنی بررسی شدند.

> **حدود ادعا:** این ممیزی از جنس بررسی ایستای کد GitHub و اسناد ضمیمه است. هیچ تست، اجرای برنامه، Gradle، Lint، GitHub Actions، شبیه‌ساز، ساخت APK یا تست گوشی اجرا نشده است. وضعیت تمام ۱۶ AC این Issue برابر `NOT_VERIFIED` باقی می‌ماند. نه این سند و نه Issue #10 مجاز نیستند گواه runtime PASS تلقی شوند.

## ۱) فهرست مرجع و تقدم قراردادها

بستهٔ PDF شامل ADR-0001..0006، PRD v1 Clarified، MVP v1، System Architecture MVP v1، Code & Technical Documentation MVP v1 و دو سند User Flow Phase 0 / Phase 1 است. ۱۲ سند خوانده شد. برای تصمیم‌های اصلاح‌شده و جاری، منابع GitHub شامل [PRD](../PRD.md)، [MVP](../MVP.md)، [Voice Contract](../VOICE_CONTRACT.md)، [Software Architecture](../SOFTWARE_ARCHITECTURE.md)، [#16 implementation contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) و [#17 design acceptance](../design-system/ISSUE_17_FINAL_ACCEPTANCE_AUDIT.md) بررسی شد.

- Android 10+، Kotlin/Compose، معماری local-first، یک Media3 PlaybackService با مالکیت انحصاری Queue/Player/MediaSession، Push-to-Talk صریح، کنترل دستی در همهٔ شکست‌ها.
- ADR-0004: STT/TTS باید قابل تعویض باشد؛ اگر Cloud STT فعال شود، رضایت جداگانه و مقصد/هدف/نگهداری داده آشکار شود. صرف API Android تضمین آفلاین بودن نیست. آداپتر فعلی تنها `createOnDeviceSpeechRecognizer` را مجاز می‌کند.
- ADR-0005 و اسناد محصول مجموعاً **شش** Intent MVP را تعریف می‌کنند؛ در **Issue #10 تنها پنج** فرمان `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK` قابل اجرا هستند. `PLAY_ARTIST`، تطبیق Artist و Confirming واقعی به [#11](https://github.com/latticeward-labs/music-copilot/issues/11) تعلق دارند. نوع `Confirming` برای سازگاری معماری حفظ شود، ولی فرمان ششم در #10 اجرا نشود.
- نمونه‌های رسمی `Play`, `Pause`, `Next song`, `Previous song`, `Go back 10 seconds` هستند؛ Wake Word همیشه‌شنوا صراحتاً خارج دامنه است. تصمیم ثبت‌شدهٔ برنامه: `musico` صرفاً **پیشوند گفتاری اختیاری بعد از Mic tap** باشد، نه شرط لازم و نه فعال‌سازی پنهانی.

## ۲) وضعیت وابستگی‌های واقعی GitHub

| ورودی | وضعیت قابل مشاهده | نتیجهٔ P00 |
|---|---|---|
| [#7](https://github.com/latticeward-labs/music-copilot/issues/7) | CLOSED؛ Media3 service/queue تحویل شده | مبنای اجرای فرمان موجود؛ تضمین Voice runtime نیست |
| [#8](https://github.com/latticeward-labs/music-copilot/issues/8) | CLOSED؛ Audio Focus/route guard تحویل شده | قبل از Duck باید precedence و safe-paused رعایت شود |
| [#9](https://github.com/latticeward-labs/music-copilot/issues/9) | CLOSED؛ Driving/BT تحویل شده | Mic handoff بخشی از کد دارد؛ **M45 / GS-04 BLOCKED** و به #10 منتقل شده |
| [#16](https://github.com/latticeward-labs/music-copilot/issues/16) | CLOSED؛ قرارداد معماری approved | شرط طراحی و قراردادها فراهم است؛ اجرای #10 هنوز بررسی نشده |
| [#17](https://github.com/latticeward-labs/music-copilot/issues/17) | CLOSED؛ WF-08 و طراحی Voice تأیید شده | فایل‌های handoff موجود؛ ادعای TalkBack/Phone PASS نیست |
| [#74](https://github.com/latticeward-labs/music-copilot/issues/74) / [#78](https://github.com/latticeward-labs/music-copilot/issues/78) | OPEN | بدهی‌های technical QA قدیمی حفظ می‌شوند؛ مانع اختراع PASS |

**نتیجهٔ DoR:** وابستگی‌های مستنداتی بسته شده‌اند و می‌توان فازهای بعدی را مطابق طرح آغاز کرد؛ اما همهٔ ۴ checkbox تاریخى Definition of Ready در متن #10 به معنای **تأیید اجرای صوتی و تست مستقل** نیستند. شرط «#7 و #9 پایدار در محیط واقعی با صوت» را فقط در حد تحویل owner-approved قبلی می‌توان پذیرفت؛ نه به عنوان QA کامل. متن Issue و checkboxها عمداً بدون PASS تغییر می‌کنند.

## ۳) مالکیت لایه‌ها و ثبت کد فعلی

| حوزه | سورس بررسی‌شده | مشاهدهٔ دقیق |
|---|---|---|
| وضعیت و طول عمر صوت | [VoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt)، [DefaultVoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | StateFlow و generation/job هست؛ `withTimeoutOrNull(6000)`، attempt=1/2 و guard `isCurrent` دیده می‌شود؛ پایان خودکار هر terminal path و raceهای ACK باید تکمیل/ممیزی شود |
| STT | [SpeechToTextEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/SpeechToTextEngine.kt)، [AndroidOnDeviceSTT](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt) | provider قابل‌جایگزینی؛ مجوز تازه OS و API>=31 شرط است؛ API29/30 fail-closed؛ confidence ممکن است null باشد؛ availability در همهٔ OEM/language-packها آزمایش نشده |
| TTS | [TextToSpeechEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/TextToSpeechEngine.kt)، [AndroidOfflineTTS](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt) | voice بدون network گزینش می‌شود؛ TTS اختیاری؛ فعلاً متن `Done` در success؛ پیام‌های کامل خطا/پایان نیازمند P08 |
| Mapper | [RestrictedVoiceIntentMapper](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt)، [VoicePolicy](../../app/src/main/java/com/latticeward/musiccopilot/voice/policy/VoicePolicy.kt) | type شامل شش intent؛ dispatcher `PLAY_ARTIST` را اجرا نمی‌کند. Mapper فعلی `musico ` را **اجباری** می‌کند؛ null/low confidence → RequiresConfirmation و coordinator → Failed. حد `T_DIRECT=0.90` ثابت در سورس است؛ BuildConfig ندارد |
| UI / Full Player | [FullPlayerScreen](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt)، [AppNavHost](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | Full Player در امضای callback خود `onMic` یا `voiceState` ندارد؛ Mic overlay هنوز به آن متصل نیست |
| UI / Driving | [DrivingModeScreen](../../app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt)، [DrivingVoiceHandoffPolicy](../../app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingVoiceHandoffPolicy.kt) | Mic/cancel/retry، eligibility و overlay نمونه موجود است؛ ایمنی/Voice واقعی GS-04 هنوز تست نشده |
| OS permission | [MainActivity](../../app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt)، [ApplicationBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | ActivityResult مجوز OS را تازه می‌خواند، پس از grant ضبط خودکار نمی‌کند؛ coordinator واحد app-scoped در DI |
| فرمان و authority | [PlaybackCommand](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackCommand.kt)، [PlaybackGateway](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackGateway.kt) | Play/Pause/Next/Previous/SeekBy موجود؛ Voice `SeekBy(-10000)` می‌سازد؛ `execute` منوط به ACK سرویس است |
| Service / interruption | [MusicPlaybackService](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt)، [PlaybackVoiceInterruptionHost](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackVoiceInterruptionHost.kt) | Focus/route invalidation می‌تواند Voice را لغو کند؛ **Lease مستقلی برای Duck/Unduck صوت فرمان Voice** در coordinator و قرارداد Gateway فعلی دیده نشد |
| Capability / privacy | [VoiceBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt)، [AndroidManifest](../../app/src/main/AndroidManifest.xml)، [CloudSpeechConsentPolicy](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/CloudSpeechConsentPolicy.kt) | production فعلاً on-device STT و TTS غیرشبکه‌ای دارد؛ هیچ اجازه‌ای برای افزودن cloud fallback بدون consent وجود ندارد |

**محدودیت بررسی:** مشاهدهٔ source و قراردادهای GitHub، معادل اجرای device، Lint، تست concurrency یا ممیزی کامل telemetry نیست.

## ۴) شکاف‌ها و ترتیب برطرف‌سازی

| شناسه | شدت | شکاف مشاهده‌شده / تصمیم لازم | فاز مسئول |
|---|---|---|---|
| G01 | BLOCKER | Duck/restore با lease مالکیت Voice و precedence رویدادهای #8 وجود ندارد؛ لازم است بدون Player دوم طراحی شود | P07 |
| G02 | BLOCKER | میکروفون Full Player و overlay آن هنوز متصل نیست | P03/P09 |
| G03 | BLOCKER | پیشوند اجباری `musico` خلاف نمونه‌های bare-command پس از tap است؛ باید اختیاری شود | P05 |
| G04 | HIGH | null/low confidence به اجرا نرسد و fallback صادقانه نشان داده شود؛ fail-closed و quality policy تثبیت شود، confidence اختراع نشود | P04/P05 |
| G05 | HIGH | STT فعلی Android 10/11 را پشتیبانی نمی‌کند؛ manual-only در API29/30 حقیقتاً نمایش داده شود؛ فعال‌سازی provider جدید خارج قرارداد ممنوع | P04 |
| G06 | HIGH | اتمیک بودن dispatch/ACK، retry، callback دیررس، cancel، recreation و تکرار فرمان باید بررسی و پیاده شود | P02/P06/P10 |
| G07 | HIGH | TTS بازخورد ترمینال، TalkBack/focus/200% و Full Player WF-08 کامل نیست | P08/P09 |
| G08 | HIGH | M45 از #9 / GS-04 فقط بعد از Voice واقعی #10 قابل اجراست و فعلاً BLOCKED | P13 |
| G09 | QUALITY_GATE | `5×10=50` تلاش واقعی آزمایشگاهی لازم است؛ حداقل **45/50** موفقیت ثبت شود | P13 |
| G10 | RISK | اجرای تست‌های خودکار و پوشش OEM/Android 10,12,14+ و privacy/telemetry هنوز شواهد این Issue ندارند | P11/P12/P13 |

هیچ تغییر کد اجرایی برای حل موارد فوق در **P00** مجاز یا انجام نشده است. رفع شکاف‌ها به فازهای مشخص نگاشت شده تا کنترل scope و rollback ممکن بماند.

## ۵) Traceability اولیهٔ تمام ۱۶ AC اصلی Issue #10

همهٔ موارد هنوز `NOT_VERIFIED` و آزمایش‌های این Issue `NOT_RUN` هستند؛ «قابل مشاهده در سورس» با «تأییدشده در گوشی» متفاوت است.

| AC | الزام | فازهای مالک | وضعیت | توضیح مبنا |
|---|---|---|---|---|
| AC01 | Voice State Machine: Idle → Listening → Processing → (Confirming if needed) → Executed/Failed | P02, P09 | NOT_VERIFIED | State types exist; direct-path transitions and terminal lifecycle need hardening. |
| AC02 | Explicit mic tap only; no always-on / wake word | P03 | NOT_VERIFIED | Driving explicit tap partially wired; Full Player microphone missing. |
| AC03 | Service-respecting Voice duck while Listening and reliable unduck | P07 | NOT_VERIFIED | No Voice-scoped duck/restore lease found in reviewed public contract/coordinator. |
| AC04 | Listening ≤6s; no more than one retry | P02, P04 | NOT_VERIFIED | 6000ms and 2 attempts declared; timeout/cancellation/races not verified. |
| AC05 | Replaceable STT and TTS providers | P04 | NOT_VERIFIED | Replaceable interfaces exist; device/provider/language readiness unverified. |
| AC06 | Only five executable controls in #10 | P05 | NOT_VERIFIED | Six-type source declares PLAY_ARTIST, coordinator does not dispatch it; mapping needs PTT/bare-phrase policy. |
| AC07 | Direct recognized command → service without navigation | P06 | NOT_VERIFIED | Typed gateway dispatch exists; ACK, safety and real executions not verified. |
| AC08 | UI observes Voice and does not own playback/recognition | P03, P09 | NOT_VERIFIED | Driving observes coordinator; Full Player overlay not wired. |
| AC09 | At most one short TTS sentence | P08 | NOT_VERIFIED | Short 'Done' on ACK is source-visible; all terminal feedback/error/cancel cases incomplete. |
| AC10 | Timeout/failure/cancel preserves playback and manual controls | P02, P07, P10 | NOT_VERIFIED | Manual fallback rules exist; duck cleanup/route safety not proven. |
| AC11 | Lifecycle/recreation/background/interruption no duplicate command | P10 | NOT_VERIFIED | Generation checks exist; late RPC/ACK and lifecycle state race unverified. |
| AC12 | Lab direct-command success ≥90% | P13 | NOT_VERIFIED | Zero issue-10 lab executions; later 5×10 attempts require ≥45/50. |
| AC13 | No raw voice recording or logging | P11 | NOT_VERIFIED | No persistence appears in reviewed coordinator; app-wide runtime/telemetry audit pending. |
| AC14 | If cloud STT, separate consent and processing/retention disclosure | P11 | NOT_VERIFIED | Production binds on-device/no-network fallback; conditional cloud gate stays mandatory if scope changes. |
| AC15 | Mapper/timeout/states/idempotence test coverage | P02, P05, P06, P10, P12 | NOT_VERIFIED | Previous source test fixtures exist; new #10 authored tests NOT_STARTED; none executed for #10. |
| AC16 | WF-08 voice presentation, TalkBack and visible state | P09 | NOT_VERIFIED | Driving has partial Voice semantics; P09 Full Player/WF08 and physical TalkBack not verified. |

**کلید شواهد برای این جدول:** [Issue #10](https://github.com/latticeward-labs/music-copilot/issues/10) معیار اصلی است؛ مسیرهای دقیق هر AC به‌صورت ماشین‌خوان در [P00 traceability JSON](ISSUE_10_PHASE0_TRACEABILITY.json) آمده‌اند. تست‌های M01–M60 و L01–L05 در [چک‌لیست P13](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md) صرفاً **طرح** هستند، نه نتیجه.

## ۶) پیش‌نیاز فنی و مصوبات دامنه برای P01..P13

1. **یک coordinator app-scoped** و یک service-owned Media3 Player، بدون Player دوم؛ UI فقط state را نمایش دهد.
2. **Mic باید لمس شود**؛ بعد از grant/عودة از Settings/اتصال Bluetooth هرگز خودکار Listening شروع نشود.
3. فقط **پنج** فرمان مستقیم و لغت‌نامهٔ ثابت؛ `PLAY_ARTIST` و Confirmation هنرمند در #11 باقی بمانند. فرمان‌های مبهم/ناشناخته/بی‌confidence ایمن رد شوند.
4. تأیید پخش صرفاً با پاسخ معتبر `PlaybackGateway.execute`؛ `SEEK_BACK` = منفی ده ثانیه، محدودشده به صفر توسط سرویس.
5. **حداکثر شش ثانیه برای هر Listening**، مجموعاً حداکثر دو تلاش، cancellation/generation-safe، یک اجرای مؤثر به‌ازای فرمان، بدون ACK دیررس مجدد.
6. Duck به شکل lease قابل بازگردانی و تابع تقدم call/focus/route؛ هرگز ترک را برخلاف Pause دستی یا قطع هندزفری auto-resume نکند.
7. API29/30 فعلاً مسیر کنترل دستی؛ API31+ قابلیت STT به **availability واقعی و بستهٔ English روی دستگاه** وابسته است؛ وعدهٔ عمومی offline بودن بدون شواهد ممنوع.
8. رضایت cloud STT، processor/destination/purpose/retention فقط اگر cloud واقعاً پیشنهاد/فعال شود؛ در scope فعلی **هیچ cloud/STT online اضافه نشود**. دادهٔ صوتی، متن تشخیص‌داده‌شده، مسیر فایل و شناسهٔ شخصی در لاگ/دیگنستیک ذخیره نشوند.
9. پیروی از WF-08 طراحی #17، کنترل‌های قابل‌دسترسی و TalkBack؛ `TTS` یک جملهٔ کوتاه و اختیاری؛ failure حق تغییر نتیجهٔ playback ACK را ندارد.
10. **فازهای P00 تا P12:** صرفاً source، review، docs و نوشتن test code؛ صفر build/test/Actions. **P13:** فقط یک APK تجمیعی از تمام فازها، `assembleDebug` بدون هیچ تسک تست/Lint، با ثبت SHA واقعی و نتیجهٔ تست توسط کاربر. تمام موارد بدون شاهد `NOT_RUN` بمانند.

## ۷) دروازهٔ خروج P00 / تحویل P01

- [x] RAR و ۱۲ PDF خوانده شد؛ checksum ثبت شد.
- [x] `main`، branch اختصاصی و وضعیت Issue/وابستگی‌ها بررسی شد.
- [x] حدود پنج Intent در #10 در مقابل شش Intent کل MVP مشخص شد.
- [x] کد Voice/Media3/Driving/permission و owner هر لایه بررسی شد.
- [x] شکاف‌ها با شدت، فاز مسئول و مسیر مرجع ثبت شد.
- [x] تمام ۱۶ AC به فازهای اجرا نگاشت شدند؛ status آنها تغییر نکرد.
- [x] سیاست «بدون build/test تا P13» ثبت شد.
- [ ] **پیاده‌سازی G01..G10 و تست واقعی** — متعلق به فازهای بعد، نه شرط اتمام خود ممیزی P00.

**نتیجه:** P00 به‌عنوان *ممیزی ایستا و گزارش آماده‌سازی* کامل است. فاز بعدی **P01 (حاکمیت branch، قرارداد تحویل، evidence ledger و QA JSON با وضعیت NOT_RUN)** است. Issue #10 باز و ادعای پذیرش نهایی وجود ندارد.
