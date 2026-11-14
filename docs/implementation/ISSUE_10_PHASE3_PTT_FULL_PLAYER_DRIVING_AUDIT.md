# Issue #10 — P03 | Push-to-Talk واقعی در Full Player و Driving Mode

**تاریخ:** 2026-10-25  
**وضعیت تحویل:** `SOURCE_IMPLEMENTED_UNBUILT` · `AUTHORED_ONLY` · `NOT_RUN`  
**Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10)  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**منبع پیوست:** `MSM2(1)(1).rar` — SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (۱۲ سند PDF؛ همان بستهٔ ممیزی P00). قراردادهای جاری GitHub، معماری #16، High-Fidelity #17 و P00–P02 نیز ملاک بوده‌اند.

> این گزارش فقط پیاده‌سازی **کد منبع** و بررسی ایستای آن را نشان می‌دهد. هیچ Build، Gradle، Unit Test، UI Test، Lint، Actions، Emulator یا تست گوشی اجرا نشده است. هیچ PASS محصول یا AC ادعا نمی‌شود.

## ۱. خروجی فاز و محدوده

- **Full Player:** دکمهٔ Mic واقعی با کنترل enable/disable، نمایش وضعیت Voice و rationale مجوز، یک Retry در صورت اجازهٔ Coordinator، Cancel/Back، و بازخورد عدم پشتیبانی روی همان صفحه افزوده شد. هیچ Navigation destination جدیدی ساخته نشده و indexهای LazyColumn مربوط به صف جابه‌جا نشده‌اند.
- **Driving Mode:** مسیر Mic/Permission/Cancel/Retry قبلی #9 بازاستفاده و حفظ شد؛ همچنان **همان** `bindings.voiceSessionCoordinator` با `VoiceOrigin.Driving` فراخوانی می‌شود. سرویس پخش و چهار کنترل دستی Driving تغییر نکرده‌اند.
- **مجوز:** هر دو مبدأ دقیقاً یک `MainActivity.voicePracticeGate` و یک `ActivityResultContracts.RequestPermission` استفاده می‌کنند. تنها نمایش‌های موقت `playerMicUi` / `drivingMicUi` و مبدأ تایپ‌شدهٔ `VoiceOrigin` از هم جدا هستند؛ هیچ permission state جدید در DataStore ذخیره نمی‌شود.
- **نتیجهٔ سیستم:** پس از Grant/Deny یا بازگشت ActivityResult، فقط UI stage به‌روز می‌شود؛ **ضبط خودکار آغاز نمی‌شود**. لازم است کاربر مجدداً Mic را لمس کند، و در همان لحظه OS grant و availability واقعی STT on-device دوباره بررسی شوند.
- **پیشگیری از شروع تکراری:** بازهٔ `SessionActive` اجازهٔ لمس تازه نمی‌دهد و در handler لحظهٔ کلیک، `coordinator.state.value == Idle` دوباره کنترل می‌شود؛ دسترسی به `start` از UI تنها با gesture صریح است. P02 مالک generation و P10 مسئول ممیزی race کامل است.
- **بازگشت و lifecycle:** Back در overlay فقط Voice یا permission presentation را لغو می‌کند؛ Back بدون overlay به Full Player origin بازمی‌گردد. خروج/route change از Full Player، Voice متعلق به آن مبدأ را لغو می‌کند؛ onStop هر Session فعالِ Player/Driving را بدون توقف MusicPlaybackService خاتمه می‌دهد.
- **حریم خصوصی:** UI متن گفتار/صدای خام/Media URI را نمایش یا ذخیره نمی‌کند؛ متن status ثابت و resource-backed است. Cloud STT، wake listener، Player دوم، سرویس جدید، playback command جدید و PLAY_ARTIST اضافه نشد.

## ۲. فایل‌های واقعیِ تغییر یافته

| مسیر | نتیجه |
|---|---|
| [MainActivity.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt) | یک Permission Gate و Launcher مشترک؛ دو ارائهٔ موقت؛ مبدأ تایپ‌شده؛ restore ایمن فقط برای Android result در انتظار؛ لغو Voice دو مبدأ در onStop |
| [AppNavHost.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | اتصال واقعی `FullPlayerScreen` به coordinator واحد، Permission actions، Callbackهای Retry/Cancel/Back؛ تازه‌خوانی وضعیت در tap؛ حفظ Mic فعلی Driving |
| [FullPlayerScreen.kt](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt) | دکمه Mic، وضعیت داخلی Voice، rationale، failed/Retry/Cancel و TalkBack متنی پایه؛ از همان resources استاندارد استفاده می‌کند |
| [VoicePttAdmissionPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoicePttAdmissionPolicy.kt) | pure origin/capability/state gate بدون SDK، recognizer یا Player |
| [Issue10Phase3PushToTalkTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase3PushToTalkTest.kt) | **۱۲ متد JUnit نوشته‌شده، اجرا نشده** |

`DrivingModeScreen.kt`، `DrivingVoiceHandoffPolicy.kt`، `VoiceSessionCoordinator.kt`، `VoiceBindings.kt`، `MusicPlaybackService.kt`، `AndroidManifest.xml`، `RestrictedVoiceIntentMapper.kt` و پورت‌های STT/TTS عمداً تغییر نکرده‌اند.

## ۳. سناریوی قرارداد و پیش‌شرط‌ها

| رویداد | مسیر مورد انتظار مطابق سورس P03 |
|---|---|
| برنامه تازه شروع می‌شود، یا Bluetooth متصل می‌شود | هیچ Listening و permission request خودکار؛ UI فقط state را می‌خواند |
| Mic در Full Player، مجوز موجود نیست | `NeedsPermission → Rationale → AwaitingAndroid` پس از Continue؛ UI مستقل از Driving |
| Grant از Android | OS دوباره بررسی می‌شود؛ stage `ReadyForVoiceHandoff` در صورت provider واجدشرایط؛ **ضبط هنوز Idle** |
| Mic دوباره لمس شود | در route معتبر و Coordinator Idle، دوباره permission/provider خوانده شده، `start(VoiceOrigin.Player)` یک بار |
| Mic در Driving | `DrivingVoiceHandoffPolicy` قبلی با fresh OS/provider و `start(VoiceOrigin.Driving)` |
| Mic دو بار متوالی / جلسه در حال Processing | admission رد می‌کند؛ Start دوم از مسیر UI ساخته نمی‌شود |
| Deny/Provider unavailable/API29–30 | وضعیت واقعیِ عدم دسترس نمایش داده می‌شود، Player دستی باقی می‌ماند؛ cloud fallback فعال نمی‌شود |
| Back داخل overlay | cancel به همان origin؛ صفحهٔ پخش و صف Media3 سر جای خود |
| Player→Driving | فقط در حالت بدون Voice و بدون permission dialog معلق؛ stage نتیجهٔ قبلی پاک می‌شود |
| خروج route / permission revocation / Background | cancel Voice مربوطه و teardown فرایند ضبط؛ **بدون** pause/stop PlaybackService |

**نکتهٔ مهم:** دو حالت `Rationale/AwaitingAndroid` تنها لایهٔ UX هستند؛ در Android دیالوگ native را Compose کنترل نمی‌کند. با خروج کاربر، callback دیررس فقط UI را به‌روز می‌کند و فرمانی اجرا نمی‌کند.

## ۴. تست‌های منبعی که نوشته شدند (هنوز NOT_RUN)

| مورد | عنوان |
|---|---|
| P03-T01 | واجدشرایط بودن Full Player با provider مجاز و Idle |
| P03-T02 | رد Start بدون مجوز و مجاز بودن rationale |
| P03-T03 | نبود STT یا API ناسازگار → manual-only |
| P03-T04 | رد شروع از origin مخفی |
| P03-T05 | جلوگیری از tap دوم هنگام Listening/Processing |
| P03-T06 | Voice فعلی Driving با Player تداخل نکند |
| P03-T07 | تمام stateهای فعال فقط به Origin خودشان تعلق داشته باشند |
| P03-T08 | Gate با دسترسی تازهٔ نامعتبر یا provider ناموجود شروع نکند |
| P03-T09 | مجوز صرفاً Rationale/Awaiting/ManualFallback؛ نه recorder |
| P03-T10 | wiring یک permission gate/launcher و هر دو origin در سورس |
| P03-T11 | Callback OS هیچ `start(VoiceOrigin)` انجام ندهد |
| P03-T12 | Background cancel بدون Stop/Resume ناامن سرویس پخش |

`T07` (Compose Accessibility) همچنان متعلق به P09 و **NOT_RUN** است. این ۱۲ تست به‌عنوان `T08` و بخشی از `T02` قابل ردیابی هستند و **اجرای صفر** دارند. M01–M10، M23–M31، M44–M47 و AC02/AC08/AC10/AC11/AC16 مرتبط‌اند؛ همهٔ آن‌ها در دفتر QA تا P13 `NOT_RUN/NOT_VERIFIED` باقی می‌مانند.

## ۵. مرز تحویل به فازهای بعد

- **P04:** تشخیص English، وجود بستهٔ on-device، نبود confidence، teardown خاص OEM، وضعیت مناسب Android10/11.
- **P05:** پنج فرمان مستقیم با یا بدون پیشوند گفتاری اختیاری `musico`؛ mapper فعلی هنوز grammar قبلی را دارد.
- **P06:** ACK معتبر، idempotency نهایی Media3، پیش‌شرط‌های queue/service.
- **P07:** Duck/Restore lease، تقدم تماس/focus و route loss.
- **P08/P09:** بازخورد نهایی TTS، Voice design دقیق WF-08، animation/focus/TalkBack/200%، کنترل دستی.
- **P10:** concurrency/lifecycle/race جامع، Activity recreation/late callbacks.
- **P11/P12:** Privacy و ممیزی نهایی diffها.
- **P13:** تنها یک APK تجمیعی و QA واقعی روی گوشی کاربر.

## ۶. دروازهٔ خروج فاز ۳

- [x] Full Player میکروفون source-wired دارد.
- [x] Driving Mode به Coordinator مشترک متصل باقی مانده است.
- [x] یک VoicePracticeGate/یک RequestPermission launcher برای هر دو UI.
- [x] Grant هیچ شروع خودکار ندارد و Mic دوم ضروری است.
- [x] admission لحظهٔ لمس، origin و state/permission/provider را می‌سنجد.
- [x] مسیرهای cancel/Back/navigate/out-of-app در سورس نوشته شده‌اند.
- [x] ۱۲ مورد JUnit جدید نوشته شده، اما اجرا نشده‌اند.
- [x] متن گزارش و ردیابی در GitHub آماده است.
- [ ] `assembleDebug`، tests، Lint، Actions، APK و گوشی — **NOT_RUN** طبق قرارداد.

**نتیجه:** `P03_SOURCE_IMPLEMENTED_UNBUILT` (نه Runtime PASS). **P04_NEXT — زیرساخت قابل‌تعویض STT/TTS.**
