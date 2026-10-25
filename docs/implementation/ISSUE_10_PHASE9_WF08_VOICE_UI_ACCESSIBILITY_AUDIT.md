# Issue #10 — P09 | WF-08 Voice UI، TalkBack و بازگشت ایمن به کنترل دستی

**تاریخ:** 2026-10-25  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**وضعیت:** `P09_SOURCE_IMPLEMENTED_UNBUILT` / `AUTHORED_ONLY` / `NOT_RUN`  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/10

**مرجع ضمیمه:** `MSM2(1)(1).rar` (۱۲ PDF، SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`). ADR-0004/0005، Clarified PRD، Code & Technical Blueprint و System Architecture برای UI بررسی شدند. قراردادهای فنی پروژه شامل WF-08، یک لایهٔ Driving، حداقل 48dp، خوانایی با 150–200٪ اندازه قلم، TalkBack، عدم ضبط خودکار، بازگشت فوری به دستی و اجرای فرمان تنها پس از ACK است.

> **در P09 هیچ Build/Gradle/کامپایل/Lint/Unit/Compose/UI/Instrumented Test/Actions/Emulator/گوشی/APK انجام نشده است.** تمام موارد زیر شواهد **کد و طراحی منبع** هستند، نه PASS رفتاری یا تایید TalkBack روی گوشی.

## ۱. یافته‌های ممیزی Source پیش از تغییر

- Full Player در P03 یک `PlayerVoiceControls` درون همان `LazyColumn` داشت؛ Driving هم `DrivingVoiceSessionOverlay` جداگانه. هر دو وضعیت‌ها را از `VoiceSessionState` واقعی می‌گرفتند و Back هنگام Recording را Cancel می‌کردند؛ **ولی متن/ترتیب کنش‌ها/فوکوس یکپارچه نبود**.
- Full Player برای برچسب وضعیت و کنترل Mic حداقل اندازه داشت، اما **بازگشت فوکوس بعد از خروج از نتیجهٔ Voice** را مدیریت نمی‌کرد.
- Driving از قبل Mic focus restore، صفحهٔ اسکرول‌شوندهٔ 200٪ و Exit ثابت داشت. P09 باید این رفتارها را نگه دارد؛ ورود به صفحهٔ دوم، گالری دستورات یا پنل چندمرحله‌ای حین رانندگی مجاز نیست.
- عنوان عمومی Failed و دکمهٔ `Cancel voice` حتی برای `Executed` یا Failed غیرقابل‌Retry دقیق نبودند. UI نباید از TTS یا فرض اجرای Player، موفقیت نمایشی بسازد.
- Stage `Listening` در P07 قبل از دریافت ACK Duck منتشر می‌شود؛ بنابراین عبارت وضعیت جدید **به‌صراحت «Preparing or listening»** است و به‌دروغ ضبط فعال قبل از آماده شدن میکروفون را ادعا نمی‌کند. تغییر state machine و جداسازی `Preparing` به P10 موکول شد.

## ۲. معماری و کد P09

| فایل GitHub | تغییر |
|---|---|
| [VoiceOverlayPresentationPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoiceOverlayPresentationPolicy.kt) | **جدید**: projection خالص، تایپ‌دار و fail-closed از `VoiceSessionState` + `VoicePracticeStage` به ۱۰ وضعیت نمایشی و سه نوع Exit؛ هیچ مسئولیت Android/STT/Media3 ندارد |
| [VoiceSessionPanel.kt](../../app/src/main/java/com/latticeward/musiccopilot/feature/voice/VoiceSessionPanel.kt) | **جدید**: مؤلفهٔ مشترک Compose برای Full Player و Driving؛ heading/readout با LiveRegion مودبانه، توضیح محدود، Continue/Retry/Exit مجاز، focus ring و touch target مناسب |
| [FullPlayerScreen.kt](../../app/src/main/java/com/latticeward/musiccopilot/feature/player/FullPlayerScreen.kt) | مدل مستقل قبلی با panel مشترک جایگزین شد؛ focus به Mic پس از بستن panel بازمی‌گردد؛ Back تصویری هنگام Voice ابتدا Voice/permission panel را می‌بندد، بدون بستن Player |
| [DrivingModeScreen.kt](../../app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt) | حذف تکرار `DrivingVoiceSessionOverlay`، استفاده از panel مشترک؛ Exit ثابت، scroll با فونت بزرگ، mic focus restoration، Play/Pause/Next/Previous موجود و پیام ایمنی جاده حفظ شدند |
| [strings.xml](../../app/src/main/res/values/strings.xml) | **۲۱ منبع متنی انگلیسی** برای WF-08، نوع خطا، پیام دستی و TalkBack؛ بدون نمایش transcript، مسیر فایل یا محتوای دلخواه Provider |
| [Issue10Phase9VoiceOverlayAccessibilityTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase9VoiceOverlayAccessibilityTest.kt) | **۲۰ متد JUnit جدید، فقط نوشته‌شده و NOT_RUN** |

**بدون تغییر** در `Media3 PlaybackGateway`، ExoPlayer/MediaSession، `DefaultVoiceSessionCoordinator`، STT/TTS P04/P08، Mapper P05، Audio Focus/Route P07، Manifest، Cloud consent، PermissionMachine، Queue، Room/DataStore و Navigation destinations. UI `VoiceSessionCoordinator.start()`، `SpeechRecognizer` یا `play()` را فراخوانی نمی‌کند؛ رویدادهای کاربر از callbackهای قبلی NavHost عبور می‌کنند.

## ۳. Truth table رابط WF-08

| وضعیت واقعی | نمایش/کنش مجاز |
|---|---|
| Idle و permission prompt غیرفعال | پنل Voice وجود ندارد؛ Mic اصلی و دلایل غیرفعال بودن قابل نمایش‌اند |
| Mic بدون مجوز؛ Rationale | توضیح کاربرد/محلی بودن میکروفون + Continue اختیاری و Not now؛ هیچ ضبطی آغاز نمی‌شود |
| AwaitingAndroid | انتظار پاسخ Android؛ Continue تکراری ندارد؛ Not now فقط قصد UI را لغو می‌کند، نه دیالوگ سیستم را |
| Permission Granted | پنل بسته می‌شود؛ نمایش «Tap Mic again» از UI قبلی؛ فقط لمس جدید اجازهٔ ضبط دارد |
| Listening | عنوان «Preparing or listening» + راهنمای فرمان انگلیسی و **Cancel voice**؛ نه ادعای ضبط زودهنگام |
| Processing | نمایش انتظار ACK سرویس، Cancel در دسترس؛ نتیجهٔ دیررس نباید خودسرانه اجرا شود |
| Confirming | چون PLAY_ARTIST متعلق به #11 است، فقط Unsupported + Back to manual؛ هیچ انتخاب یا تایید ساختگی |
| Executed | «Command acknowledged by the playback service» + Back to manual؛ موفقیت فقط از State ماشین که P06 ACK را تایید کرده می‌آید |
| Failed VOICE_TIMEOUT | پیام دقیق Timeout؛ Retry فقط در `canRetry=true`؛ بعد از تلاش دوم بدون Retry |
| Failed VOICE_NO_MATCH | پیام دقیق No Match؛ Retry فقط وقتی مجاز باشد؛ بدون اجرای قریب‌بهترین فرمان |
| Failed سایر خطاها | پیام کلی «Voice command unavailable»، بدون exception/path/code خصوصی؛ Back to manual و Retry فقط مطابق State حقیقی |

در هر پنل فقط **یک مسیر خروج اصلی** نشان داده می‌شود: Cancel capture، Dismiss permission یا Back to manual. هیچ متن STT، confidence خام، عنوان/نام شخصی آهنگ یا telemetry در UI اضافه نشده است. بازخورد صوتی از P08 بدون تغییر و اختیاری می‌ماند.

## ۴. دسترس‌پذیری و مدیریت تمرکز

- Heading مربوط به Voice یک گره `focusable` با `semantics { heading(); liveRegion = LiveRegionMode.Polite }` است. LiveRegion فقط هنگام تغییر متن وضعیت پشتیبانی بومی اعلام می‌شود؛ اعلان تهاجمی و `AccessibilityManager.announceForAccessibility` اضافی وجود ندارد.
- دکمه‌های Retry، Continue و Exit `contentDescription` مشخص، ترتیب طبیعی عمودی، indicator برای keyboard/switch access و touch target با حداقل توکن `MusicSize.touchTargetMinDp` در Player و `MusicSize.drivingExitMinDp` در Driving دارند.
- هیچ حرکت یا انیمیشن اجباری جدید، تایپ، لیست انتخاب، پنجرهٔ جداگانه یا قطع کردن سرویس پخش وجود ندارد. پنل در `LazyColumn` پلیر و Scroll body رانندگی قرار دارد؛ **Exit رانندگی ثابت** می‌ماند.
- در Full Player، فوکوس ورودی فقط روی **تغییر باز/بسته شدن پنل** (نه هر تغییر Listening/Processing/Executed) به Heading یا دکمهٔ Mic برمی‌گردد؛ در Driving همان focus model P03 قبلی نگه داشته شد.
- TalkBack، keyboard switch focus، 150%/200% فونت، 320dp/narrow device و نشستن داخل خودرو هنوز **در گوشی آزموده نشده‌اند**؛ رفتار فوکوس واقعی TalkBack به OS/OEM وابسته است و P13 در M48/M49/M50 بررسی می‌شود.

## ۵. منطق بازگشت ایمن / No unwanted playback

- Cancel هنگام Listening/Processing از callback `onVoiceCancel` موجود به Coordinator می‌رود و Generation را invalidate می‌کند. UI مستقلاً `tts.stop` یا `stt.cancel` فراخوانی نمی‌کند.
- Success/Failed dismiss از همان callback استفاده می‌کند، State به Idle برمی‌گردد و دیگر فرمان جدیدی فعال نمی‌شود؛ **Resume، Play Last، Reset Queue، شبیه‌سازی Touch یا Retry خودکار رخ نمی‌دهد**.
- Permission Back و Not now از `onVoicePermissionCancel` قبلی عبور می‌کنند؛ `ActivityResult` هرگز به‌تنهایی `start` را فراخوانی نمی‌کند.
- بازگشت به Mic به معنی گرفتن فوکوس navigation/switch است **نه کلیک برنامه‌ای**. ورود به Driving/Full Player، Bluetooth reconnect یا onResume مجوز اجرای Voice محسوب نمی‌شود.
- برای فاز بعد P10 نیازمند audit مشترک Focus/Route/foreground، ownerScope cancellation و race بین TTS duck و گزارش ACK هستیم.

## ۶. Coverage نوشته‌شده، اجرای صفر

۲۰ متد `Issue10Phase9VoiceOverlayAccessibilityTest.kt` شامل: Idle hidden، Rationale، Android pending، grant بدون auto-start، denial، Listening، Processing، Executed، Timeout Retry، Retry دوم، No Match، Provider unavailable، sanitized unknown error، یکسانی projection دو Origin، خروج دستی، یک panel مشترک بدون Voice Activity، Polite LiveRegion و touch targets، focus restore، نبود raw transcript و منابع دسترس‌پذیر.

**وضعیت همهٔ این آزمون‌ها `AUTHORED_ONLY / NOT_RUN` است.** Build، compile، Lint، JVM، Compose tests، Emulator و تلفن اجرا نشده‌اند. AC08/AC16 و M23–M30, M43, M46, M48–M50 هنوز `NOT_VERIFIED/NOT_RUN` هستند.

## ۷. Traceability، ریسک و تحویل

- **AC01:** همهٔ UI statusهای مرتبط با State Machine در source Projection آمده‌اند؛ بعد از تست دستگاه باید تطبیق State حقیقی تأیید شود.
- **AC02:** PTT-only و permission tap-twice حفظ شدند؛ هنوز روی گوشی تأیید نشده‌اند.
- **AC08:** UI فقط projection و callback است، مالک player/recognizer نیست؛ نیازمند QA فنی و Phone.
- **AC10:** Cancel، Back و Manual fallback قابل دسترسی‌اند؛ نتیجهٔ عملی Audio Focus/Queue هنوز اثبات نشده است.
- **AC16:** WF-08 مشترک، قابل خواندن، برچسب‌دار و فوکوس‌دار در source؛ TalkBack و scaled text واقعی نیازمند گوشی.
- **G07:** از نظر کد تا حدی address شد؛ **CLOSED یا PASS نیست**؛ با M48/M49/M50 و P10/P13 تأیید نهایی لازم است.
- **P10:** وقفه‌ها، background و نژاد callback؛ **P11:** Privacy؛ **P12:** Static cumulative review؛ **P13:** تنها APK از مجموع همهٔ فازها و QA واقعی مالک.

### خروجی فاز ۹

- [x] مؤلفهٔ مشترک WF-08 در Full Player و Driving.
- [x] نمایش تفکیک‌شدهٔ حالات اصلی بدون وابستگی به transcript.
- [x] Retry فقط بر اساس `canRetry` واقعی؛ Manual/Dismiss/Cancel معنایی مجزا.
- [x] ۲۱ string resource و TalkBack hints + polite status.
- [x] Fallback دستی و تمرکز صفحهٔ مبدا بدون start/play خودکار.
- [x] حفظ یک لایه، Exit فوری، چیدمان عمودی برای بزرگ‌نمایی.
- [x] ۲۰ JUnit method نوشته شدند؛ **هیچ تستی اجرا نشد**.
- [ ] **کامپایل، Build، QA، Actions، APK، تست گوشی** — همگی NOT_RUN.

**نتیجه:** `P09_SOURCE_IMPLEMENTED_UNBUILT`، مرحلهٔ بعد **P10 — Interruption، Background، Late-Callback و Race Hardening**.
