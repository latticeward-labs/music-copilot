# Issue #10 — P04 | زیرساخت STT/TTS قابل‌تعویض و آمادگی واقعی دستگاه

**تاریخ:** 2026-10-25  
**وضعیت:** `SOURCE_IMPLEMENTED_UNBUILT`، `SOURCE_REVIEW_ONLY`، `AUTHORED_ONLY`، `NOT_RUN`  
**Branch:** `feature/10-ptt-voice-five-control-intents` · [Issue #10](https://github.com/latticeward-labs/music-copilot/issues/10)  
**منبع ضمیمه:** `MSM2(1)(1).rar` با SHA-256 برابر `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` و ۱۲ PDF. ADR-0004 و ADR-0005، PRD/MVP و System Architecture تطبیق داده شدند؛ مبنای قرارداد اجرایی، نسخهٔ جاری GitHub و برنامهٔ P00–P13 است.

> این فاز هیچ Build، کامپایل، JVM/UI Test، Lint، GitHub Actions، Emulator، APK یا تست گوشی انجام نداده است. `SOURCE_IMPLEMENTED_UNBUILT` فقط تأیید ثبت سورس در شاخه است، نه PASS عملیاتی STT/TTS یا سنجش کیفیت صدا.

## ۱. دامنه و تصمیم‌های معماری

- پورت‌های موجود [SpeechToTextEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/SpeechToTextEngine.kt) و [TextToSpeechEngine](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/TextToSpeechEngine.kt) **بدون تغییر API عمومی** حفظ شدند؛ Coordinator، UI و IntentMapper همچنان به Android Speech SDK وابسته نیستند و می‌توان برای QA Provider مصنوعی تزریق کرد.
- [VoiceBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) **بدون تغییر** باقی ماند: Provider اصلی فقط `AndroidOnDeviceSpeechToTextEngine`، Provider بازخورد فقط `AndroidOfflineTextToSpeechEngine` و در نبود Android Context، Providerهای `Unavailable` هستند. fallback مخفیانه به cloud/generic STT ثبت نشده است.
- تفکیک نقش‌ها: STT تنها متن و Confidence اختیاری برمی‌گرداند، Mapper مسئول Intent است، Coordinator صاحب Session و Timeout است، PlaybackService تنها مالک Media3 است و TTS فقط بازخورد اختیاری می‌دهد.
- `6,000ms` سقف هر تلاش توسط Coordinator P02 و request cap اعمال می‌شود؛ این فاز timer دوم یا Retry جدیدی اضافه نکرد. Feedback کوتاه و قواعد محتوای آن در P08 تکمیل می‌شود.

## ۲. تغییرات کد P04

| فایل | کار انجام‌شده |
|---|---|
| [AndroidOnDeviceSpeechToTextEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt) | یک recognizer صریح on-device در هر نشست، بررسی permission/SDK، پیش‌بررسی زبان API33+، اسکن Confidence اختیاری، خطاهای نوع‌دار و teardown با شناسایی identity نشست |
| [OnDeviceEnglishSupportPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/policy/OnDeviceEnglishSupportPolicy.kt) | Policy خالص برای `en-US`: مدل نصب‌شده، نیازمند دانلود، پشتیبانی‌نشده؛ بدون شبکه و بدون دانلود |
| [AndroidOfflineTextToSpeechEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt) | رفع race callback اولیهٔ `TextToSpeech`، انتخاب Voice انگلیسی غیرشبکه‌ای، guard شناسهٔ utterance، ایمنی stop/close و callbackهای دیررس |
| [Issue10Phase4SpeechProvidersTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase4SpeechProvidersTest.kt) | ۱۲ سناریوی pure/mock/source-check، **فقط نوشته‌شده و NOT_RUN** |

بدون تغییر در `AndroidManifest.xml`، `MainActivity.kt`، `AppNavHost.kt`، `FullPlayerScreen.kt`، `DrivingModeScreen.kt`، `DefaultVoiceSessionCoordinator.kt`، `RestrictedVoiceIntentMapper.kt` یا پخش Media3. در Manifest فعلی مجوز `INTERNET` افزوده نشده است.

## ۳. رفتار STT بر حسب دستگاه — بدون ادعای غیرواقعی

| نسخه Android | قابلیت platform | رفتار سورس |
|---|---|---|
| Android 10 / 11، API29/30 | `createOnDeviceSpeechRecognizer` در این APIها وجود ندارد | `isAvailable=false` و manual-only؛ هیچ generic/cloud fallback |
| Android 12 / 12L، API31/32 | ساخت recognizer صریح on-device ممکن است؛ Android **API بررسی نصب بودن pack** ندارد | availability سریع صرفاً وجود سرویس/مجوز را نشان می‌دهد؛ تشخیص واقعی best-effort و خطای language/provider → `VOICE_UNAVAILABLE`؛ تضمین نصب مدل داده نمی‌شود |
| Android 13+، API33+ | `checkRecognitionSupport`، `installedOnDeviceLanguages` و فهرست مدل‌های قابل/در انتظار دانلود | پیش از `startListening` وجود مدل نصب‌شدهٔ `en-US` یا `en` بررسی می‌شود؛ فقط Installed اجازهٔ Listening عملی می‌دهد؛ DownloadRequired/Unsupported/Callback error → fallback بدون ضبط و دانلود |

در API33+، `SpeechRecognizer.checkRecognitionSupport(intent, app.mainExecutor, callback)` استفاده شده است. **`triggerModelDownload` صدا زده نمی‌شود** و مدل انگلیسی بدون اطلاع کاربر دانلود نمی‌شود. `EXTRA_PREFER_OFFLINE=true` یک ترجیح سیستم است، نه مجوز برای generic/cloud provider. Provider ساختارش تنها on-device است. `isAvailable()` **فقط بررسی سریع ساختاری** است و به‌تنهایی نشانهٔ موجود بودن pack یا موفقیت STT نیست.

Locale لازم `en-US` است، `en` نصب‌شده به‌عنوان generic English قابل قبول است؛ `en-GB`، `de-DE` یا localeهای دیگر به‌عنوان `en-US` گزارش نمی‌شوند. چنانچه provider نصب بودن مدل را اثبات نکند، هیچ فرمانی از صدای نامطمئن اجرا نمی‌شود.

## ۴. خطا، حریم خصوصی، cancellation و TTS

- Provider در خطای speech timeout از `VOICE_TIMEOUT`، در `ERROR_NO_MATCH` از `VOICE_NO_MATCH` و در خطاهای سرویس/permissions/language/unavailable از `VOICE_UNAVAILABLE` استفاده می‌کند؛ پیام exception، متن گفتگو یا مسیر رسانه وارد کدهای خطا/telemetry نمی‌شود.
- `transcript` فقط در نتیجهٔ موقت و محدود (حداکثر ۲۵۶ نویسه) برگردانده می‌شود؛ نه در StateFlow، persistence، log یا TTS. `confidence` فقط در صورت مقدار معتبر `0..1` از خود provider گرفته می‌شود؛ مقدار تهی به `null` تبدیل می‌شود؛ confidence **ساختگی تولید نمی‌شود**. فیلد `detectedLanguageTag=null` است مگر API واقعاً زبان کشف‌شده را ارائه کند؛ locale درخواستی به اشتباه به‌عنوان زبان شناسایی‌شده گزارش نمی‌شود.
- هر STT یک `Session(id, recognizer)` دارد؛ callback/cancel/close تنها شیء دقیق همان نسل را نابود می‌کند؛ teardown روی Main Looper انجام می‌شود. Late result مربوط به Session نابودشده نمی‌تواند وضعیت جدید را به‌روزرسانی کند. باز هم raceهای بین Media3/permission/lifecycle در P06/P10 باقی هستند.
- TTS فقط Voice نصب‌شده با `isNetworkConnectionRequired=false` و English Locale را انتخاب می‌کند؛ اگر نصب نیست یا init ناموفق است، `isAvailable=false` و پاسخ صوتی اختیاری حذف می‌شود. Service پخش هیچ توقف یا Pause اجباری از خطای TTS دریافت نمی‌کند.
- TTS callbacks با `activeUtteranceId` و Session ID کنترل و در Main serial می‌شوند؛ `stop` و `close` بهترین تلاش برای متوقف کردن speech هستند. **پاسخ صوتی موفقیت Media3 را اثبات نمی‌کند** و ACK اصلی همچنان از PlaybackGateway است.
- هیچ cloud STT، HTTP، wake-word، raw recording/file export، شباهت‌یابی آهنگ، Play Artist یا ذخیرهٔ transcript ایجاد نشده است.

## ۵. تست‌های JUnit که نوشته شدند — اما اجرا نشدند

| شماره | سناریوی موجود در فایل |
|---|---|
| 01 | درخواست دقیق English `en-US` و رد زبان دیگر |
| 02 | نصب واقعی `en-US` در فهرست support |
| 03 | پشتیبانی generic English `en` و نرمال‌سازی locale |
| 04 | رد `en-GB` به‌عنوان نصب بودن مدل US |
| 05 | مدل قابل‌دانلود یا در انتظار، با Installed متفاوت است |
| 06 | نبود مدل/زبان بی‌ربط → Unsafe/Unsupported |
| 07 | Fake STT با confidence و detected language ناموجود |
| 08 | Fake STT غیرقابل دسترس و خطای دستی |
| 09 | Fake TTS صرفاً بازخورد، نه فرمان پخش |
| 10 | کنترل source عدم generic/cloud STT و دانلود خودکار |
| 11 | کنترل source صدای English آفلاین، stop و late callback |
| 12 | کنترل source Manifest و VoiceBindings بدون `INTERNET` |

این ۱۲ سناریو، به‌همراه آزمون‌های قدیمی Provider از Phase8، **`AUTHORED_ONLY` هستند**. هیچ نتیجهٔ PASS/FAIL واقعی وجود ندارد. دفتر QA گروه `T06` یا `T08` را صرفاً به سبب وجود کد تست اجراشده اعلام نمی‌کند؛ گروه `T03` (timeout) و `T09` (privacy) هم همچنان NOT_RUN هستند.

## ۶. Traceability / موارد باقی‌مانده

- **AC05 (STT/TTS قابل‌تعویض):** شواهد Source P04 افزوده شد؛ اما performance، English pack واقعی، OEM، TTS output و contract swap در گوشی/اجرای تست هنوز `NOT_VERIFIED` هستند.
- **AC04، AC09، AC10، AC13، AC14:** P04 در source گاردهای مربوط را تکمیل کرد، ولی معیارهای پذیرفته‌شده هنوز `NOT_VERIFIED` هستند و P07/P08/P10/P11/P13 وابستگی دارند.
- **G04 (confidence):** missing confidence واقعی بدون ساختن مقدار ناشناخته از API برمی‌گردد؛ Mapper تعیین تکلیف fail-safe در P05 است.
- **G05 (API29/30):** manual-only حفظ شده؛ هیچ offline STT روی Android 10/11 ادعا نشده است. API31/32 بدون امکان introspection کامل language pack است.
- **G07:** TTS source lifecycle بهبود یافت، ولی WF08 و TalkBack در P09 باقی است.
- **G09:** آزمایش پنج فرمان × ده تلاش، ۰ از ۵۰ است و فقط در P13 با گوشی واقعی اندازه‌گیری خواهد شد.

## ۷. دروازهٔ خروج فاز ۴

- [x] Providerهای STT/TTS قابل‌تعویض، مستقل از UI/Media3.
- [x] API29/30 manual-only بدون ادعای پشتیبانی کاذب.
- [x] شناسایی `installed English` پیش از گوش دادن در API33+؛ مدل قابل دانلود، آماده محسوب نمی‌شود.
- [x] بستن STT/TTS و نادیده گرفتن callbackهای دیررس و خطاهای type-safe در source.
- [x] Confidence و detectedLanguage اختیاری بدون ساخت داده.
- [x] Voice انگلیسی TTS بدون وابستگی اینترنت و fallback دستی.
- [x] ۱۲ سناریوی جدید نوشته‌شده، اجرا نشده.
- [ ] **Build، تست، Lint، اجرای دستگاه یا تلفن، Actions و APK** — همه `NOT_RUN`، مطابق قرارداد فازها.

**نتیجه:** `P04_SOURCE_IMPLEMENTED_UNBUILT`. **گام بعدی: P05 — تشخیص قطعی پنج Intent کنترلی.**
