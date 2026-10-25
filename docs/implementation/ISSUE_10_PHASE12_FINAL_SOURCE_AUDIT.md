# Issue #10 — P12 | ممیزی تجمعی نهایی سورس و تحویل به فاز ۱۳

**تاریخ:** 2026-10-25  
**مخزن:** `latticeward-labs/music-copilot`  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**وضعیت:** `P12_SOURCE_AUDITED_UNBUILT` — **هیچ Build، Gradle، Kotlin compile، JUnit، Android instrumentation، Lint، GitHub Actions، Emulator، APK و تست گوشی اجرا نشده است.**

## ۱. مبنا، دامنه و میزان اطمینان

- فایل ضمیمهٔ `MSM2(1)(1).rar` با SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` و ۱۲ PDF مبنا است؛ ADR-0002 (Playback service authority)، ADR-0004 (replaceable Voice provider)، ADR-0005 (restricted intent)، معماری MVP، مستند فنی و User Flows با اسناد جاری GitHub تطبیق داده شدند.
- Issue #10 پنج Intent مستقیم `PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK` را پیاده می‌کند؛ `PLAY_ARTIST`، جست‌وجوی هنرمند، انتخاب از گزینه‌ها و Confirmation واقعی در **Issue #11** باقی می‌مانند.
- مسیر اجرای صوت یک‌طرفه است: لمس Mic → بررسی permission/foreground/provider → single app-scoped Coordinator → temporary Media3 service duck ACK → on-device English STT ≤6s → deterministic mapper → typed playback gateway → **service-correlated ACK** → state feedback/TTS ≤2s. این یک **قرارداد سورس** است نه ادعای عملکرد واقعی.
- بررسی ایستای فایل‌ها به‌تنهایی **امکان تأیید کامل نحوی یا ABI Kotlin/Compose/Gradle، manifest نهایی Merge‌شده، OCR/UI، زمان‌بندی OEM و صدا را ندارد**. در فاز ۱۳ اگر build خطا داد، باید فقط رفع خطای واقعی ثبت شود؛ نتایج تست همچنان NOT_RUN هستند.

## ۲. ممیزی مرحله‌به‌مرحلهٔ P00–P11

| فاز | تحویل تأییدشده در سورس | مرز اجرایی باز |
|---|---|---|
| P00 | بررسی ۱۲ PDF، معماری، شکاف‌های ده‌گانه و traceability | Runtime/QA در P00 اجرا نشد |
| P01 | Branch، گزارش‌های evidence/QA و قرارداد `[skip ci]` | PR/Merge/Acceptance نشده |
| P02 | Voice StateFlow، attempt/generation gate، 6s recognition، یک Retry، cancel | OEM timeout و callback رفتار روی دستگاه نامعلوم |
| P03 | Full Player و Driving از یک Coordinator، فقط لمس آگاهانهٔ Mic؛ نتیجهٔ permission auto-start ندارد | اتصال و نوع ActivityResult روی گوشی نیازمند QA |
| P04 | Portهای STT/TTS تعویض‌پذیر و on-device-only؛ API29/30 manual؛ English readiness preflight | مدل/اعتماد به confidence بعضی OEMها نامعلوم |
| P05 | grammar ثابت English-first، پنج Intent؛ `musico` اختیاری پس از لمس؛ low/null confidence fail-closed | دقت واقعی 45/50 اندازه‌گیری نشده |
| P06 | CommandId، Media3 ACK و dedup سرویس؛ seek-back 10s، queue/focus guards | نیازمند احراز یکتایی حتی پس از process restart: **P12 اصلاح شد** |
| P07 | Lease مستقل Duck/restore داخل service، 22s TTL و priority تماس/Route | restore/volume روی خودرو/هندزفری نامعلوم |
| P08 | کاتالوگ ۸ بازخورد، حداکثر یک جملهٔ کوتاه بعد از ACK یا شکست نهایی، TTS اختیاری ≤2s | TTS نصب‌شده/زمان واقعی گفتار بررسی نشده |
| P09 | پنل مشترک WF-08 و TalkBack/focus/large text؛ دکمه‌های Retry، Cancel، Manual | پشتیبانی واقعی TalkBack/font-size بررسی نشده؛ **P12 رفع سایه‌اندازی نام متغیر** |
| P10 | Foreground gate، reject دوباره Mic، STT recognizer identity، TTS utterance identity، route/focus/call، unDuck controller drain | race واقعی/Process death روی گوشی بررسی نشده |
| P11 | Transcript redaction، cloud STT disabled و consent مستقل مشروط، privacy-safe diagnostics، NoOp sink | Logcat/OEM/network/merged manifest هنوز بررسی اجرایی نشده |

**نتیجه:** فازهای P02–P11 فقط `SOURCE_IMPLEMENTED_UNBUILT` هستند. P00/P01 governance/source-evidence دارند. هیچ PHASE به‌معنای PASS اجرایی تلقی نمی‌شود.

## ۳. ناسازگاری‌های سورس کشف و اصلاح‌شده در P12

### F12-01 — برخورد شناسهٔ فرمان Voice پس از Restart (HIGH / رفع در سورس)

**قبل:** `VoicePlaybackDispatcher.commandFor` شناسه را صرفاً به شکل `voice-42-1` از شمارهٔ نشست و تلاش می‌ساخت. اگر فرایند Android از نو شروع شود ولی `MusicPlaybackService` و حافظهٔ cache dedup باقی بمانند، SessionId دوباره از ۱ شروع می‌شود؛ پاسخ/اثر فرمان ممکن است با شناسهٔ قدیمی اشتباه گرفته یا به‌عنوان duplicate رد شود.

**اصلاح:** شناسهٔ جدید `voice-{32hexProcessNonce}-{sessionNumber}-{attempt}` است؛ UUID تصادفی فقط **یک‌بار برای هر فرآیند** در companion scope ساخته می‌شود. همان تلاش همواره همان id را می‌سازد، Retry و نشست بعدی id متفاوت دارند، و پس از restart احتمال برخورد عملاً ناچیز می‌شود. `VoiceCommandReceiptPolicy` ساختار جدید را می‌شناسد و خواندن فرمت قدیمی را فقط برای سازگاری با رسیدهای قدیمی حفظ می‌کند؛ service/queue/player جدیدی ساخته نشده است.

**محدودیت:** random nonce یک تضمین ریاضی مطلق نیست؛ reliability و رفتار actual Media3 dedup باید با APK فاز ۱۳ تأیید شود. تست‌های P06 نوشته‌شده با شکل جدید ID هماهنگ شدند، اما اجرا نشدند.

### F12-02 — هم‌نامی متغیر عنوان با semantics Compose (SOURCE CLEANUP)

در `VoiceSessionPanel` عنوان متنی `heading` با فراخوانی `semantics { heading() }` هم‌نام بود. نام دادهٔ محلی به `headingText` تغییر کرد تا احتمال سایه‌اندازی و ابهام Kotlin از بین برود؛ عملکرد/طراحی UI و accessibility contract تغییر نکرد. صحت واقعی Compose **پس از Build در P13** مشخص می‌شود.

## ۴. بازبینی Regression برای Issue #7، #8 و #9

| پایه | باید ثابت بماند | بازبینی ایستای P12 |
|---|---|---|
| #7 Media3 | تنها `MusicPlaybackService` مالک `ExoPlayer`, MediaSession و صف، background music باقی بماند | Voice Dispatcher/Compose حاوی سازندهٔ Player نیست؛ Controller همان PlaybackGateway است |
| #8 Audio focus/calls | تماس، route loss، noisy event و Pause دستی مقدم بر Voice/TTS هستند | Service Focus reducer و route latch حفظ شده؛ هیچ resume از مسیر P12 افزوده نشد |
| #8 Notification/Bluetooth controls | Play/Pause/Next/Previous عادی توسط Media3 محفوظ باشند | P12 فقط فرمت شناسهٔ Command مربوط به Voice را تغییر داد؛ کنترل دستی و سرویس اختصاصی جدید ندارد |
| #9 Driving | پیشنهاد Bluetooth اختیاری، بدون ورود خودکار/ضبط خودکار؛ خروج فوری از Driving | تنها shared P09 panel و P10 route Voice cancel؛ هیچ onConnect → `startVoice` یا Play افزوده نشده |
| #9 M45 | آزمون واگذارشده به Issue #10 | هنوز NOT_RUN و باید همراه GS-04 در P13 ثبت شود |
| UX #17 WF-08 | حداقل 48dp، اعلام Polite، کنترل دستی و متن 150/200٪ | source tokenها و semantics حضور دارند؛ ادعای pass واقعی TalkBack وجود ندارد |

## ۵. Traceability معیارهای پذیرش (AC01–AC16)

مرجع ماشین‌خوان: [ISSUE_10_PHASE12_ACCEPTANCE_TRACEABILITY.json](ISSUE_10_PHASE12_ACCEPTANCE_TRACEABILITY.json).

| معیار | شواهد سورس | وضعیت واقعی |
|---|---|---|
| AC01 State Machine | P02 + P09 Coordinator/Overlay | NOT_VERIFIED |
| AC02 Explicit PTT | P03 + P10 gating | NOT_VERIFIED |
| AC03 Safe Duck | P07 Media3 lease | NOT_VERIFIED |
| AC04 6s listening + one retry | P02/P04/P10 policy | NOT_VERIFIED |
| AC05 Replaceable provider | P04/P11 adapters | NOT_VERIFIED |
| AC06 Five executable intents | P05 grammar/dispatcher | NOT_VERIFIED |
| AC07 Direct typed playback ACK | P06 + F12-01 | NOT_VERIFIED |
| AC08 No duplicate UI ownership | P03/P09/P10 | NOT_VERIFIED |
| AC09 One brief TTS sentence | P08 terminal gate | NOT_VERIFIED |
| AC10 Failure/cancel manual fallback | P02/P07/P09/P10 | NOT_VERIFIED |
| AC11 Lifecycle/late-callback | P10 + F12-01 | NOT_VERIFIED |
| AC12 Lab ≥90% | [P13 5×10 corpus](ISSUE_10_P13_FIVE_BY_TEN_DIRECT_VOICE_LAB_CORPUS.json) | **NOT_MEASURED — 0/50**, not PASS |
| AC13 No raw voice history/log | P11 transcript redaction and NoOp sink | NOT_VERIFIED |
| AC14 Cloud separate consent if enabled | P11 cloud disabled gate and disclosure | NOT_VERIFIED |
| AC15 Mapper/timeout/idempotence authored coverage | P02/P05/P06/P10/P12 JUnit sources | NOT_VERIFIED; executions=0 |
| AC16 WF-08/TalkBack | P09 + F12-02 | NOT_VERIFIED |

**نکتهٔ مهم:** داشتن کد مناسب یا متد JUnit به معنای PASS یا READY برای عرضه نیست. AC12 و AC16 نیازمند **تست گوشی واقعی** هستند؛ T01–T09 بدون اجرا هستند و T10 یک QA چنددستگاهی است که در این مرحله انجام نشده.

## ۶. ماتریس ۵×۱۰ و ۶۰ آزمون دستی

- [Corpus P13](ISSUE_10_P13_FIVE_BY_TEN_DIRECT_VOICE_LAB_CORPUS.json): پنج گروه `PLAY`، `PAUSE`، `NEXT`، `PREVIOUS` و `SEEK_BACK`؛ هر گروه ۱۰ بار فرمان انگلیسی صریح، در مجموع ۵۰ رکورد `NOT_RUN`؛ حد پذیرش **۴۵ PASS واقعی از ۵۰**، بدون حذف گزینشی تلاش‌های ناموفق.
- [چک‌لیست گوشی فارسی M01–M60](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md): سناریوهای نصب، اجازهٔ Mic، پنج فرمان، state، Timeout/Retry، ACK، duck، Bluetooth/call، Driving، TalkBack و Privacy. تمام نتایج `NOT_RUN`؛ «BLOCKED» فقط با ثبت علت، بدون تبدیل خودکار به PASS.
- برای M45 موروثی Issue #9، فقط وقتی برنامه روی گوشی واقعاً قابل تشخیص Voice بود «PASS» ثبت شود. پیش از آن `BLOCKED/NOT_RUN` حقیقت دقیق است.

## ۷. فهرست موارد باز برای P13 (تایید نشده)

- **مهم:** خطاهای احتمالی Gradle/Kotlin/Compose، dependencies/build-tools/Android SDK/manifest merge ممکن است تنها هنگام `assembleDebug` آشکار شوند؛ این ممیزی آن‌ها را اجرا نکرد.
- **مهم:** provider STT OEM ممکن است `confidence=null` برگرداند؛ policy fail-closed مانع اجرای ناخواسته است، اما نرخ موفقیت ۴۵/۵۰ را تضمین نمی‌کند. هرگز برای عبور از gate confidence مصنوعی تولید نشود.
- **مهم:** Audio Focus و Route recovery و AirPods/headset با player notification و Duck service باید واقعی بررسی شوند تا هیچ پخش ناخواسته از بلندگوی گوشی رخ ندهد.
- API29/30 دستیار صوتی محلی ندارد؛ manual-only باید در UI حقیقتاً عمل کند. API31/32 English readiness پیشاپیش قطعی نیست؛ API33+ بستهٔ نصب‌شده باید وجود داشته باشد.
- Privacy: عدم وجود `INTERNET` مجوز داخل Manifest، اثبات ترافیک صفر موتور SpeechRecognizer سیستمی/OEM در تمام شرایط نیست؛ لاگ سیستم، شواهد network و merged manifest نیاز به اجرا دارند.
- تست TalkBack، 150٪ و 200٪ فونت، چرخش، background و callback دیررس هنوز انجام نشده است.
- ماتریس OEM سه دستگاه Android 10/12/14+ مطابق قرارداد MVP در P13 قابل ثبت واقعی/تعویق با توضیح است؛ نتایج یک دستگاه نباید به سه دستگاه تعمیم داده شود.

## ۸. قرارداد Build-only برای فاز ۱۳

1. Freeze کد HEAD بعد از P12 و ساخت **یک APK تجمیعی** از مجموع تمام فازهای P00–P12 (نه APK جداگانهٔ هر فاز).
2. در GitHub Actions فقط workflow اختصاصی **Build-only** با `:app:assembleDebug` مجاز است؛ test/connectedAndroidTest/lint/verify/check را اجرا نکند. در صورت شکست کامپایل، اصلاح دقیق و تکرار Build فقط برای **ساخت همان APK واحد نهایی** مجاز است.
3. Commit منبع مورد استفاده در Build، Android app ID/version، `APK SHA-256`، حجم واقعی بایت و GitHub artifact ID/download باید **از Build واقعی** استخراج و در ledger ثبت شود. هیچ آدرس دانلود یا اندازهٔ جعلی ساخته نشود.
4. فایل خروجی واحد `app/build/outputs/apk/debug/app-debug.apk` باید در Artifact قابل دریافت باشد؛ اگر Github Actions خطا داد، سورس/خطا ثبت شود، نه آنکه به کاربر APK نامعتبر داده شود.
5. نسخهٔ Build-only به معنی PASS تست فنی نیست. نتایج تمامی ۶۰ آزمون دستی و ۵۰ تلاش باید توسط مالک گوشی گزارش شوند؛ هر نتیجه با نسخهٔ APK و شواهد دستگاه به Issue #10 پیوند داده شود.
6. `AC01..AC16` تنها با شواهد واقعی لازم بازنگری می‌شوند. Merge/Close Issue #10 فقط پس از دستور صریح مالک و توافق دربارهٔ موارد معوق انجام می‌شود.

## ۹. خروجی‌ها و Definition of Done این مرحله

- [x] ممیزی سورس P00–P11 و بررسی قواعد ADR/Issue #7/#8/#9.
- [x] رفع ریسک شناسهٔ فرمان تکراری پس از restart (F12-01)، با اصلاح fixtureهای نوشته‌شده.
- [x] رفع ابهام Compose semantics در Voice panel (F12-02).
- [x] نگارش **۱۵ سناریوی JUnit جدید P12**؛ `AUTHORED_ONLY/NOT_RUN`.
- [x] ماتریس ۱۶ AC با شواهد سورس و صفر پذیرش حقیقی.
- [x] ماتریس P13 پنج گروه × ده تلاش، همه `NOT_RUN`.
- [x] چک‌لیست فارسی M01–M60 و دستور Build-only یک APK واحد موجود و مرتبط.
- [x] حفظ سکوت GitHub Actions روی commitهای P12 با `[skip ci]`.
- [ ] Compiled/Gradle/JUnit/Lint/Emulator/Phone test/APK/PR merge/closure: **عمداً انجام نشده**.

**وضعیت نهایی:** `P12_SOURCE_AUDITED_UNBUILT`. **P13 NEXT — Build-only یک APK تجمعی و تحویل چک‌لیست واقعی به کاربر برای تست گوشی.**
