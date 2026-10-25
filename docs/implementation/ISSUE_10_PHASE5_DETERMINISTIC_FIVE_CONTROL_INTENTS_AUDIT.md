# Issue #10 — P05 | پنج Intent قطعی انگلیسی و قرارداد Confidence

**تاریخ:** 2026-10-25  
**وضعیت:** `SOURCE_IMPLEMENTED_UNBUILT`؛ تست‌ها `AUTHORED_ONLY / NOT_RUN`؛ هیچ معیار پذیرشی PASS نشده است.  
**Branch:** `feature/10-ptt-voice-five-control-intents` · [Issue #10](https://github.com/latticeward-labs/music-copilot/issues/10)  
**منبع پیوست:** `MSM2(1)(1).rar` SHA-256 برابر `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (۱۲ PDF). به‌ویژه ADR-0005، PRD/MVP و Technical Blueprint با [ADR پذیرفته‌شدهٔ GitHub](../adr/0005-deterministic-intent-mapping.md) و [Voice Contract](../VOICE_CONTRACT.md) تطبیق داده شدند.

> **محدودیت قطعی:** در P05 هیچ `Gradle`، کامپایل، تست JUnit/UI، `lint`، GitHub Actions، Emulator، گوشی، APK یا CI اجرا نشده است. همهٔ شواهد فقط کد و بازبینی ایستای GitHub هستند.

## ۱. تصمیم دامنه؛ شش نوع MVP در برابر پنج فرمان Issue #10

- قرارداد اصلی MVP و ADR-0005 شش Intent را **در افق کامل MVP** تعریف کرده‌اند. برای Issue #10 فقط `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK` مجاز به `VoiceIntentDecision.Direct` هستند.
- مقدار enum `RestrictedIntent.PLAY_ARTIST` برای سازگاری API و Issue #11 باقی مانده، اما **Mapper این Issue هرگز آن را Direct تولید نمی‌کند**. درخواست «Play songs by Adele» با یا بدون پیشوند رد می‌شود؛ رزولوشن Artist، Fuzzy Matching، History و Confirming مربوط به #11 است.
- تنها عامل ورود به صدا، **لمس مستقیم Mic** در P03 است؛ کلمهٔ `musico` پس از لمس صرفاً *پیشوند گفتاری اختیاری* است، نه wake word، سرویس پس‌زمینه یا قاعدهٔ شروع ضبط.
- Map خروجی purely deterministic/immutable است، جدا از STT و Media3؛ mapper هیچ Play/Pause/Next/Seek یا شبکه‌ای اجرا نمی‌کند.

## ۲. بانک عبارات محدود و نسخه‌دار

**Grammar version:** `issue10-five-controls-v1`؛ فایل [DirectControlPhrasePolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/DirectControlPhrasePolicy.kt).

| Intent | عبارت‌های دقیق مجاز پس از لمس Mic | فرمان سرویس در P06 |
|---|---|---|
| PLAY | `play`, `resume`, `resume music`, `play music` | `PlaybackCommand.Play` |
| PAUSE | `pause`, `pause music`, `stop music` | `PlaybackCommand.Pause` (بدون پاک کردن Queue) |
| NEXT | `next`, `next song`, `next track`, `skip song` | `PlaybackCommand.Next` |
| PREVIOUS | `previous`, `previous song`, `previous track` | `PlaybackCommand.Previous` با semantics فعلی سرویس |
| SEEK_BACK | `go back 10 seconds`, `back 10 seconds`, `seek back 10 seconds`, `go back ten seconds`, `back ten seconds` | `PlaybackCommand.SeekBy(-10000)`؛ Clamp در لایهٔ Media3/P06 |

**مجموع:** ۱۹ عبارت دقیق در پنج خانواده. برای هریک، فرم `musico <phrase>` پس از لمس همان Intent را می‌دهد؛ نه عبارت دیگری. «Stop music» فقط PAUSE است. `skip two songs`, `volume up`, `play YouTube`, `play songs by Adele`, فرمان چندبخشی، درخواست زمان دیگری برای Seek و جملهٔ خارج از دامنه رد می‌شود.

**ترتیب Normalize:** رد رشتهٔ تهی/بیش از ۱۲۰ نویسه/کاراکتر کنترل نامعتبر؛ `trim`؛ تبدیل lowercase با `Locale.ROOT`؛ تبدیل فاصله‌های تکراری به یک فاصله؛ حذف نشانه‌های سادهٔ پایانی `.` یا `!`؛ حذف فقط یک `musico ` ابتدایی در صورت وجود؛ Lookup دقیق در بانک ثابت. نه fuzzy match، نه نزدیک‌ترین واژه، نه مدل زبانی. از نمایش، ذخیره یا لاگ transcript خودداری می‌شود.

## ۳. Confidence و شکست ایمن

- پیش‌فرض پذیرفته‌شدهٔ [VoicePolicy](../../app/src/main/java/com/latticeward/musiccopilot/voice/policy/VoicePolicy.kt) برابر `T_DIRECT = 0.90` است. محدودهٔ مجاز **`0.80..0.98`** باقی مانده است.
- [app/build.gradle.kts](../../app/build.gradle.kts) مقدار محلی `BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD = 0.90` را بدون سرویس Remote Config/Cloud تعریف می‌کند. [VoiceBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) همین مقدار را از composition root به [RestrictedVoiceIntentMapper](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt) تزریق می‌کند.
- مقدار تنظیم‌شدهٔ خارج از محدوده، `NaN` یا Infinity به پیش‌فرض ۰٫۹۰ برمی‌گردد. این تنظیم فقط آستانه را تغییر می‌دهد و نمی‌تواند به واژه‌نامه Intent اضافه کند.
- ابتدا تشخیص **عبارت دقیق و مجاز** انجام می‌شود. Unknown/Artist/Unsupported در هر Confidence فوراً `Rejected` می‌شوند؛ برنامه آن‌ها را اشتباهاً با فرمان معتبر جایگزین نمی‌کند.
- عبارت معتبر با Confidence واقعی provider که `finite`، در `[threshold, 1.0]` و موجود است به `Direct(intent)` می‌رسد. Confidence گمشده/کم/منفی/بزرگ‌تر از ۱/نامتناهی ⇒ `RequiresConfirmation`، **اما در Issue #10 Coordinator این حالت را به `Failed` و Manual/حداکثر یک Retry می‌برد، نه اجرای خودکار یا نمایش تأیید جعلی**.
- اگر Provider زبان دیگری را صراحتاً به عنوان زبان **تشخیص‌داده‌شده** گزارش کند، نتیجه `Rejected` است. `detectedLanguageTag == null` دادهٔ ناموجود محسوب می‌شود، نه زبان انگلیسیِ تأییدشده. مطابقت انگلیسی با بانک ثابت و Confidence واقعی برقرار می‌شود.
- غیاب Confidence در بعضی STTهای اندروید ممکن است باعث افت نرخ موفقیت شود. این موضوع پنهان یا با Confidence ساختگی دور زده نشده؛ قبل از PASS باید در آزمایش **۵×۱۰** فاز P13 ارزیابی شود.

## ۴. فایل‌های Source تغییر‌یافته/ایجادشده

| مسیر | تغییر P05 |
|---|---|
| [voice/intent/DirectControlPhrasePolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/DirectControlPhrasePolicy.kt) | **جدید**؛ بانک ۱۹ عبارت، Normalize، نسخهٔ Grammar، قاعدهٔ Confidence و اعتبار threshold |
| [voice/intent/RestrictedVoiceIntentMapper.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt) | **اصلاح**؛ حذف اجبار `musico`، رد Artist و موارد ناشناخته، فقط پنج `Direct`، Confidence fail-closed |
| [app/build.gradle.kts](../../app/build.gradle.kts) | **اصلاح Source**؛ مقدار ثابت BuildConfig؛ هیچ Gradle task اجرا نشده است |
| [app/di/VoiceBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) | **اصلاح**؛ تزریق threshold محلی به Mapper در Coordinator واحد |
| [Issue10Phase5DeterministicIntentsTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase5DeterministicIntentsTest.kt) | **جدید**؛ ۲۵ متد JUnit، نوشته‌شده، `NOT_RUN` |
| [Phase8VoiceFoundationTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase8VoiceFoundationTest.kt) | **اصلاح**؛ آزمون قدیمی دارای توقع قدیمیِ Prefix اجباری و Artist مستقیم با قرارداد جدید هماهنگ شد، اجرا نشد |
| [docs/VOICE_CONTRACT.md](../VOICE_CONTRACT.md) | مرز پنج Intent در Issue #10 بدون تغییر تصمیم شش‌تایی کلی MVP افزوده شد |

**بدون دستکاری:** Android STT/TTS adapterهای P04، PlaybackGateway، MediaSessionService، MainActivity/FullPlayer/Driving UI، permissions، Manifest، Queue، DataStore و Cloud gate. هیچ CommandId یا سرویس پخش جدیدی اضافه نشده است.

## ۵. آزمون‌های نوشته‌شده؛ هنوز NOT_RUN

**۲۵ JUnit** در فایل جدید، شامل گروه‌بندی هر پنج خانواده، پیشوند اختیاری، نرمال‌سازی، wake تکراری، رد Artist/YouTube/Volume، فرمان هم‌زمان/چندبخشی، Seek غیرمجاز، Unknown با Confidence مختلف، Confidence تهی و ۰٫۸۹/۰٫۹۰/۱٫۰، `NaN/±Infinity` و محدودهٔ BuildConfig، زبان غیرانگلیسی، متن خراب، determinism و چک منبع DI/Coordinator. Fixture قدیمی Phase8 نیز فقط اصلاح source شده است.

این متدها **هیچ اجرا، خروجی JUnit، نتیجهٔ PASS/FAIL، coverage، smoke-test یا تایید ساخت ندارند**. در QA: T01=`AUTHORED_ONLY` و `execution_status=NOT_RUN`، AC06 و AC15 همچنان `NOT_VERIFIED`، M11..M22 و گروه‌های L01..L05 همچنان `NOT_RUN`.

## ۶. موارد باز و تحویل به P06

- **G03:** پیشوند اجباری در source اصلاح شد؛ تا گوشی و Provider واقعی بررسی نشود `SOURCE_FIXED_PENDING_VERIFICATION` است.
- **G04:** Null/low confidence برای پنج فرمان ایمن مدیریت می‌شود، ولی بدون Confidence خروجی واقعی اجرای مستقیم وجود ندارد و سنجش دستگاه در P13 ضروری است.
- **P06:** باید dispatch typed، ACK قطعی، فرمان یک‌بار، Empty Queue/Next end safety و Clamp Seek را با سرویس موجود تکمیل کند؛ P05 اجرای واقعی Media3 را تست یا تضمین نکرد.
- **P07–P10:** Duck/unduck، TTS، WF-08/Accessibility، permission/route/lifecycle integration و جلوگیری از اجرای دوباره در service همچنان نیازمند پیاده‌سازی/ممیزی هستند.
- **P11–P12:** بازبینی privacy/CI/source. **P13:** یک APK تجمیعی و اجرای M01–M60 و ۵۰ تلاش واقعی توسط مالک، شرط ≥۴۵/۵۰ قبل از پذیرش Gate.

## ۷. دروازهٔ خروج

- [x] پنج Intent و ۱۹ عبارت دقیق انگلیسی، با grammar version ثابت، تعریف شدند.
- [x] Bare phrases پس از لمس Mic و prefix اختیاری `musico` پذیرفته شدند؛ Wake-listener اضافه نشد.
- [x] `PLAY_ARTIST` و درخواست‌های خارج از دامنه از Direct map حذف شدند.
- [x] Threshold `BuildConfig` بومی و محدود و fallback امن به ۰٫۹۰ تعریف شد.
- [x] Null/low/invalid Confidence هیچ فرمان پخشی اجرا نمی‌کند.
- [x] ۲۵ تست جدید نوشته و fixture قدیمی با scope جدید اصلاح شد.
- [x] اسناد، Traceability و دفتر شواهد منبعی قابل ثبت هستند.
- [ ] **کامپایل/Build/Gradle/Actions/Test/APK/گوشی** — مطابق قرارداد فازها `NOT_RUN`.

**نتیجه:** `P05_SOURCE_IMPLEMENTED_UNBUILT`؛ مرحلهٔ بعد **P06 — Dispatch تایپ‌شده و ACK فرمان پخش**.
