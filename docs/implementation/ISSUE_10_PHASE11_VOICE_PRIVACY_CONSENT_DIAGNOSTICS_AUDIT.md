# Issue #10 — P11 | حریم خصوصی Voice، رضایت Cloud STT و تشخیص‌های بدون دادهٔ شخصی

**تاریخ:** 2026-10-25  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/10  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**وضعیت:** `P11_SOURCE_IMPLEMENTED_UNBUILT`  
**تست‌ها:** `AUTHORED_ONLY` / `NOT_RUN`. **هیچ کامپایل، Gradle، JUnit، Lint، GitHub Actions، شبیه‌ساز، ساخت APK یا اجرای گوشی صورت نگرفت.**

## ۱. مرجع و قرارداد الزامی

بستهٔ ضمیمهٔ `MSM2(1)(1).rar` (۱۲ PDF)، SHA-256:
`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

- **ADR-0004:** ارائه‌دهندهٔ STT/TTS تعویض‌پذیر؛ در صورت فعال‌سازی واقعی STT ابری، consent مستقل و disclosure دقیق processor/destination/purpose/retention پیش‌شرط هستند.
- **ADR-0005:** تنها پنج intent کنترل قطعی در Issue #10؛ transcript خام فقط موقتاً برای تطبیق فرمان حاضر باشد، نه برای تاریخچه/جست‌وجو/لاگ یا تشخیص آزاد هنرمند.
- **MVP/System Architecture/Code Blueprint/Voice Contract:** offline-first؛ هیچ backend/account/cloud speech/streaming به MVP افزوده نشود؛ Mic فقط با لمس صریح و تازه؛ دستی در خطا یا denied permission همیشه ممکن.
- **P10 source boundaries:** application-scoped Voice owner، service-owned Media3 playback، STT/TTS only while foreground/allowed; هیچ‌کدام نباید برای diagnostics صاحب Player یا Microphone دیگری شوند.

این گزارش شواهد **کد** و قراردادهای حریم خصوصی را ارائه می‌دهد؛ بررسی اجرای واقعی OEM، System logcat، traffic capture، Backup/Restore و نتایج Android روی دستگاه صرفاً در P13 انجام می‌شود.

## ۲. ممیزی ایستای پایهٔ GitHub قبل از P11

| لایه | یافتهٔ سورس | نتیجه و محدودیت |
|---|---|---|
| Android Manifest | `RECORD_AUDIO` با درخواست native صریح؛ `INTERNET` ندارد؛ `android:allowBackup="false"`، `android:usesCleartextTraffic="false"` | انتقال مستقیم شبکه از طریق مجوز اینترنت این اپ فراهم نیست؛ رفتار فرآیند سرویس گفتار Android/OEM از این سورس به‌تنهایی قابل اثبات نیست |
| VoiceBindings | فقط `AndroidOnDeviceSpeechToTextEngine` و `AndroidOfflineTextToSpeechEngine` در Production؛ fallback بی‌اثر | موتور cloud در Production ثبت نشده است |
| Android STT | استفاده از `SpeechRecognizer.createOnDeviceSpeechRecognizer` فقط روی API31+؛ `EXTRA_PREFER_OFFLINE=true`، API33+ checkRecognitionSupport برای installed English و no download | بستهٔ زبانی و Offline حقیقی روی دستگاه‌های OEM هنوز آزمایش نشده؛ API29/30 manual-only |
| Coordinator | transcript صرفاً `SpeechRecognitionResult` ephemeral → deterministic mapper؛ خطا با `AppErrorCodes`؛ شواهدی از ذخیرهٔ صوت/متن Voice در DB یا DataStore در مسیر بررسی‌شده نیست | خطر `toString()` پیش‌فرض data class پیش از P11 امکان درج تصادفی transcript در log/crash بود |
| TTS | فقط متن ثابت `VoiceFeedbackKey` / resource؛ voice انگلیسی بدون نیاز شبکه؛ عدم ذخیرهٔ voice output | کیفیت و تأیید offline TTS دستگاه هنوز NOT_VERIFIED |
| Quality/Analytics | `AllowlistedTelemetryPrivacyGate` شناسه/مقدار دلخواه را رد می‌کند؛ `QualityBindings` از `NoOpTelemetrySink` استفاده می‌کند | **در build موجود هیچ sink analytics/crash/telemetry قابل ارسال یا persist ثبت نشده**؛ این به معنای اثبات نبودن هر نوع log سیستم/OEM در runtime نیست |
| Preferences/Settings | `CloudSpeechConsent` دارای `Granted` قدیمی است، ولی `ApplicationBindings.remoteSpeechDecision()` آداپتر/تأیید حقوقی را `false` می‌گذارد | Granted در DataStore به‌تنهایی هرگز سرویس Cloud STT را فعال نمی‌کند؛ «لغو رضایت» preference عادی است و مجوز Android نیست |

## ۳. تغییرات سورس P11

| فایل | تغییر انجام‌شده |
|---|---|
| [SpeechToTextEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/SpeechToTextEngine.kt) | `SpeechRecognitionResult.toString()` اکنون **صرفاً** `SpeechRecognitionResult(redacted)` برمی‌گرداند؛ transcript، locale و confidence چاپ نمی‌شوند؛ فیلد transcript موقت برای Mapper همچنان باقی می‌ماند |
| [CloudSpeechConsentPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/CloudSpeechConsentPolicy.kt) | disclosure چهارقسمتی در صورت مقدار placeholder مانند UNKNOWN/TBD/N/A/PENDING یا فیلد خالی، کاراکتر کنترل یا متن بلند رد می‌شود؛ gate پنج شرط مستقل خود را حفظ کرده است |
| [VoicePrivacyDiagnosticsPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/application/voice/VoicePrivacyDiagnosticsPolicy.kt) | **جدید:** فقط چهار enum تشخیصیِ بدون PII دربارهٔ API، مجوز و وجود provider محلی؛ حضور provider را **برابر با آماده‌بودن بستهٔ English یا موفقیت تشخیص نمی‌گیرد**؛ `remoteMvp()` همیشه DisabledNoApprovedAdapter |
| [AppNavHost.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | Settings اکنون وضعیت محلی Voice، عدم فعال‌بودن سرویس ابری، نبود تاریخچهٔ ضبط، و وجود احتمالی/تأییدنشدهٔ مدل انگلیسی را با عبارت‌های قابل فهم می‌گوید؛ UI جدید مجوز/consent نمی‌گیرد یا Voice را start نمی‌کند |
| [strings.xml](../../app/src/main/res/values/strings.xml) | **هفت** متن انگلیسی ثابت و قابل بومی‌سازی برای حریم خصوصی و diagnostics؛ بدون transcript/artist/URI/device identifier |
| [Issue10Phase11VoicePrivacyBoundaryTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase11VoicePrivacyBoundaryTest.kt) | **۱۹ متد JUnit نوشته‌شده، اجرای صفر:** redaction، پنج veto مستقل Cloud، disclosure، Android10/11، denied permission، NoOp sink، whitelist telemetry، عدم INTERNET و مدل محلی |

**بدون تغییر:** سرویس Media3، Player/Queue، Audio Focus/Route، Driving invitation، تعداد intent، Core Voice state machine، موتور cloud جدید، permission جدید یا backend/account.

## ۴. ماتریس حریم خصوصی و رفتار مورد انتظار

| سناریو | سیاست ایستای سورس | وضعیت راستی‌آزمایی |
|---|---|---|
| فشردن Mic بدون RECORD_AUDIO | Native permission فقط در مسیر صریح؛ بدون اعطای مجوز شروع STT رد شود؛ بعد از grant لمس **دوباره** لازم است | کد موجود؛ P13 phone NOT_RUN |
| Android10/11 | فقط manual؛ موتور on-device STT در API29/30 غیرفعال | کد موجود؛ P13 phone NOT_RUN |
| Android12+ با provider ناموجود | Voice دستی fallback؛ بدون generic/cloud SpeechRecognizer | کد موجود؛ P13 OEM NOT_RUN |
| Android13+ English pack صرفاً downloadable | هیچ دانلود/استریم مخفی، بدون STT آنلاین؛ رد صادقانه | کد موجود؛ P13 OEM NOT_RUN |
| انصراف/لغو رضایت CloudSpeech | preference local؛ هیچ اذن تازهٔ cloud ایجاد نمی‌شود؛ Media3 local unaffected | کد موجود؛ P13 phone NOT_RUN |
| حتی CloudSpeechConsent.Granted از نسخهٔ قدیمی | `remoteAdapterApproved=false` و `legalApproval=false` ⇒ `DisabledNoApprovedAdapter` | سورس معلوم؛ تست 0 |
| خطا / unknown phrase | خروجی UI/گفتار از stable code/resource، نه متن ورودی یا exception | سورس معلوم؛ تست 0 |
| Debug string و crash breadcrumb | `SpeechRecognitionResult.toString()` redacted؛ source log sink ندارد؛ بررسی platform dumps هنوز لازم | سورس معلوم؛ تست 0 |
| Telemetry Voice intent/results | gate فقط enum/کدهای allowlisted؛ حتی sanitized data در production توسط `NoOpTelemetrySink` drop می‌شود | سورس معلوم؛ تست 0 |
| Playback/Manual | هیچ تصمیم privacy نباید Media3، queue یا کنترل دستی را متوقف کند | سورس بدون تغییر؛ تست 0 |

## ۵. قرارداد توسعهٔ آیندهٔ STT ابری (خارج از MVP)

هرگز از مقدار قدیمی `CloudSpeechConsent.Granted` یا از اجازهٔ سیستم `RECORD_AUDIO` برای «رضایت ابری» نتیجه‌گیری نشود.

قبل از هر سرویس ابری باید **همگی** تحقق یابند: (۱) آداپتر مجاز و مستقل با ممیزی فنی؛ (۲) تأیید حقوقی مناسب بازار هدف؛ (۳) Disclosure مشخصِ processor، destination، purpose و retention که صرفاً placeholder نباشد؛ (۴) Opt-in جداگانه، تازه و قابل لغو توسط کاربر؛ (۵) عدم فعال‌سازی در Driving طبق سیاست فعلی. رضایت باید دقیقاً به نسخهٔ disclosure و provider متصل شود؛ پرچم قدیمی MVP برای این کار کفایت نمی‌کند. اجرای ترافیک، مکان پردازش، حذف داده و قرارداد پردازنده قبل از عرضهٔ Cloud STT باید جداگانه بررسی شوند.

در این فاز هیچ cloud adapter، HTTP، INTERNET permission، انتقال speech، SDK analytics، logging middleware، history یا مدل STT قابل دانلود افزوده نشده است.

## ۶. تعریف Done سورس و شواهد باز

- [x] مقدار raw transcript در `SpeechRecognitionResult.toString` آشکار نمی‌شود.
- [x] چهار وضعیت diagnostics محلی، type-safe و بدون PII تولید شدند.
- [x] Settings فقط status ثابت/عمومی را نشان می‌دهد؛ هیچ transcript/raw error/filename ندارد.
- [x] جزئیات ناقص یا placeholder مربوط به processor/destination/purpose/retention در policy cloud رد می‌شوند.
- [x] آداپتر ابری Production ثبت نشده و Granted قبلی permission cloud به حساب نمی‌آید.
- [x] NoOp Telemetry sink و allowlist موجود در گزارش trace شد.
- [x] ۱۹ JUnit صرفاً نوشته شد؛ **NOT_RUN**.
- [ ] پوشش همهٔ ماژول‌ها با ابزار استاتیک/Dependency Scanner و Android manifest merged — **NOT_RUN**.
- [ ] تست شبکه/OEM/logcat/Android permissions/TalkBack/Voice privacy روی گوشی — **NOT_RUN**.
- [ ] Build/Gradle/Lint/Actions/APK — **NOT_RUN**.

**AC13/AC14:** تنها `SOURCE_EVIDENCE_AVAILABLE`، نه `PASS`.  
**G10:** با ممیزی سورس فقط partially addressed؛ تست‌های device و privacy verification هنوز باز هستند.  
**Next:** **P12 — Cumulative source audit, consistency hardening and P13 handoff**؛ **P13** تنها مرحلهٔ ساخت یک APK تجمعی و دریافت نتایج دستی کاربر.
