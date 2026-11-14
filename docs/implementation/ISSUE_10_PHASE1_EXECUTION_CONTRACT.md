# Issue #10 — P01 | قرارداد اجرایی، Branch Governance و دفتر شواهد

**تاریخ:** 2026-10-25  
**وضعیت:** `P01_GOVERNANCE_SOURCE_DELIVERED`، تنها ممیزی/مستندسازی و تعریف سازوکار؛ **هیچ اجرای کد، تست یا ساخت APK انجام نشده است**.  
**Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10)  
**Repository:** `latticeward-labs/music-copilot` · **Branch:** `feature/10-ptt-voice-five-control-intents`  
**مبنای منبع:** `main` در P00 برابر `e13172683ee8495e26b7c2d56f88444e4969891e`؛ فایل RAR همان نسخهٔ قبلی با SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` و ۱۲ PDF است.

## ۱. مالکیت شاخه و انتخاب هدف Merge

- همان شاخهٔ اختصاصی P00 برای تمام P00–P13 حفظ می‌شود؛ **هیچ شاخهٔ موازی، Reset یا Rebase خودکار** ایجاد نمی‌شود.
- در شروع P01، شاخه **۶ commit جلو و ۰ commit عقب از `main`** است. چهار مسیر مستندات مربوط به Issue #10 تنها تغییرات مشاهده‌شده نسبت به main بودند.
- [CONTRIBUTING.md](../../CONTRIBUTING.md) استفاده از `develop` را ترجیح می‌دهد؛ مقایسهٔ زندهٔ P01 نشان داد `develop` در آن لحظه **۱۰۶۶ commit عقب و ۰ commit جلو از `main`** است. از این رو `main` صرفاً **هدف پیشنهادی** PR نهایی است، نه اجازهٔ Merge. در P13 باید divergence، policy و اجازهٔ مالک مجدداً بررسی شوند.
- بدون درخواست صریح بعدی برای تحویل نهایی، **PR ساخته نمی‌شود**. الگوی PR فقط روی Branch ذخیره می‌شود. Squash ترجیح مستند CONTRIBUTING است؛ بازبینی مستقل هنوز انجام نشده، و نباید بازبین یا تأییدیه ساختگی درج شود.
- commitها باید پیام scopeدار و `[skip ci]` داشته باشند تا هیچ اجرای ناخواستهٔ Actions از commitهای P01 آغاز نشود؛ *هیچ* workflow عمداً dispatch/rerun نمی‌شود. وجود `[skip ci]` به تنهایی گواه عدم اجرای تمام workflowهای نامعمول نیست؛ مرجع وضعیت واقعی GitHub است.

## ۲. دامنه، مسئولیت و قانون مالکیت

| کار | مالک مجاز | ممنوع |
|---|---|---|
| Voice session / state / retry / timeout | Voice coordinator + policy، app-scoped | UI-owned long-running recognizer |
| STT/TTS | API ports + provider adapters | cloud fallback بدون رضایت و بررسی مقصد |
| پنج Intent | deterministic mapper | اجرای `PLAY_ARTIST` (#11)، LLM و wake-listening |
| پخش/صف/Duck | PlaybackGateway و service-owned Media3 | Player دوم، volume دستکاری‌شده توسط Composable، autoplay ناامن |
| اجازه میکروفون | Android OS + ActivityResult + permission policy | فرض Granted بودن از cache و شروع پس از grant |
| UI و TalkBack | WF-08 Full Player/Driving overlay | ساخت destination مجزا برای Voice |
| QA گوشی و آزمایش ۵×۱۰ | مالک پروژه با APK P13 | ثبت نتیجهٔ ساختگی یا اجرای گوشی از سمت این فاز |
| گزارش‌نویسی و Evidence | Commitهای GitHub + شناسه‌های M/L/AC/T | ثبت raw audio/transcripts/file paths/MAC |

مرجع الزام نسخهٔ جاری [PRD](../PRD.md)، [MVP](../MVP.md)، [Voice Contract](../VOICE_CONTRACT.md)، [معماری](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) و [ممیزی P00](ISSUE_10_PHASE0_READINESS_AUDIT.md) است. RAR یک مرجع تطبیقی ثابت است، نه مجوز جایگزینی تصمیم‌های مصوب GitHub.

## ۳. ساختار تحویل و قرارداد evidence

| مسیر | منظور | مرحلهٔ به‌روزرسانی |
|---|---|---|
| `docs/implementation/ISSUE_10_PHASED_EXECUTION_PLAN.md` | برنامه P00–P13 و current-next | پایان هر فاز |
| `docs/implementation/ISSUE_10_PHASE0_READINESS_AUDIT.md` | گزارش شناخت اولیه با gapهای واقعی | Freeze P00 |
| `docs/implementation/ISSUE_10_PHASE0_TRACEABILITY.json` | مبنای شناسه‌های AC01–AC16 و blob SHA | در بازبینی ردیابی |
| `docs/implementation/ISSUE_10_PHASE1_EXECUTION_CONTRACT.md` | قوانین این فاز، handoff، PR و risk | Freeze P01 |
| `docs/implementation/ISSUE_10_EVIDENCE_INDEX.json` | index برای هر فاز/AC/M/L/T و شواهد دقیق | پس از هر تحویل/تست واقعی |
| `docs/implementation/ISSUE_10_QA_RESULTS.json` | ledger ماشین‌خوان تمام نتایج با مقدار پیش‌فرض | صرفاً پس از شواهد معتبر |
| `docs/implementation/ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md` | راهنمای ۶۰ تست گوشی و ۵ گروه آزمایش | اجرا صرفاً پس از APK فاز ۱۳ |
| `.github/PULL_REQUEST_TEMPLATE/issue-10-ptt-voice-five-control-intents.md` | الگوی PR نهایی غیرفعال/Unchecked | PR فقط در مرحلهٔ تحویل نهایی |

ثبت واقعی نتیجه باید **شناسهٔ تست، تاریخ، منبع مشاهده، نسخهٔ اندروید/دستگاه و SHA-256 APK دقیق** (هرگاه مطرح است)، پاسخ مشاهده‌شده، وضعیت مجاز و شاهد مرتبط را داشته باشد. هر مدرکی که هنوز وجود ندارد `null` یا آرایهٔ خالی می‌ماند؛ لینکی ساخته نمی‌شود.

## ۴. واژگان وضعیت (نباید مخلوط شوند)

- **Phase:** `NOT_STARTED`، `STATIC_AUDIT_COMPLETED_WITH_IMPLEMENTATION_GAPS`، `GOVERNANCE_SOURCE_DELIVERED_UNTESTED`، `SOURCE_IMPLEMENTED_UNBUILT`، `OWNER_QA_PENDING`، `EVIDENCE_RECORDED`. تکمیل فازهای source به معنای product PASS نیست.
- **Phone M01–M60:** `NOT_RUN`، `PASS`، `FAIL`، `BLOCKED`، `N/A`. در BLOCKED یا N/A **دلیل لازم است** و هیچ‌کدام به PASS تبدیل نمی‌شوند.
- **Lab L01–L05:** هر گروه ۱۰ اجرای مستقل و در مجموع ۵۰ تلاش؛ برای هر تلاش `NOT_RUN/PASS/FAIL/BLOCKED/N/A` ثبت می‌شود؛ تنها نتیجهٔ واقعی قابل شمارش است. حداقل **۴۵ فرمان درست از ۵۰ تلاش**، با گزارش تفکیکی هر فرمان؛ آزمون پیش‌شرط شروع QA میدانی است. اجرای FAIL و timeout و double-execute نباید از مخرج حذف شود؛ موارد unavailable جداگانه `BLOCKED` گزارش شده و Gate محقق نشده است.
- **Technical T01–T10:** `NOT_RUN` ابتدا؛ test-source صرفاً `AUTHORED_ONLY`. فقط run واقعی با log و تست‌های دقیق می‌تواند `PASS/FAIL` بدهد؛ `DEFERRED` مجوز تلقی PASS نیست.
- **AC01–AC16:** `NOT_VERIFIED` تا شواهد مستقل مطابق تعریف آن AC موجود باشد؛ `VERIFIED_PASS` و `VERIFIED_FAIL` فقط با evidence/decision؛ `DEFERRED` و `NOT_APPLICABLE_WITH_REASON` صریح با دلیل. `AC12` مستلزم Lab ≥45/50 است، نه استنتاج از کارکرد Source یا اجرای سه فرمان.

## ۵. نگهبان‌های قطعی P02–P12 و P13

**P02–P12:** اصلاح کد منبع، مستندسازی، بررسی ایستا و *نوشتن* test cases مجاز است؛ **اجرا کردن** هرگونه `gradle`، `assemble`، `test`، `lint`، `check`، Emulator، دستگاه، GitHub Actions، APK و ساخت خروجی اجرایی ممنوع. شواهد فقط `SOURCE_ONLY` یا `AUTHORED_ONLY` هستند.

**P13 تنها محل ساخت:** ابتدا تمام P00–P12 در یک HEAD freeze شده ثبت شوند، سپس فقط اجرای `:app:assembleDebug` به‌شکل build-only (workflow صرفاً checkout+JDK/SDK+assemble+upload) مجاز است. اگر build خطای کامپایل داد، فقط همان Build را پس از اصلاح دوباره اجرا کن؛ **تست/Lint/instrumentation را به آن اضافه نکن**. فایل `app-debug.apk`، source SHA، SHA-256 فایل، اندازهٔ بایت، شماره Run و URL واقعی Artifact را ثبت کن. بعد از آن کاربر **شخصاً** تست‌های گوشی، آزمایش داخلی ۵×۱۰ و سناریوهای Driving را اجرا و اعلام می‌کند.

هیچ قابلیت صوتی قبل از شواهد actual voice با SDK/Provider واجدشرایط `PASS` نیست؛ Android 10/11 با provider فعلی manual-only است. سناریوی انتقالی **Issue #9 M45** با **Issue #10 M45** اشتباه نشود: اولی فرمان صوتی NEXT در Driving پارک‌شده است، دومی تست background.

## ۶. تأثیر بر PR، Regression، Privacy، Rollback و Closeout

- در هر فاز فایل‌های تغییرکرده، scope، نقطهٔ ورود/خروج و عدم نقض #7/#8/#9 را مستند کن. بر نبودن Player دوم، انطباق Call/Audio Focus/BT Route Recovery و کنترل دستی دقت کن.
- در QA نام آهنگ، مسیر فایل شخصی، transcript تشخیص‌داده‌شده، صدای خام، شناسهٔ بلوتوث/خودرو، جزئیات تماس، token یا API key ذخیره نکن. حتی لاگ شکست باید بدون این داده‌ها باشد.
- دستور لغو/بازگشت: اگر تغییر رفتاری مشکل داشت، **بدون force-push و بدون تخریب تغییرات دیگران**، فقط commitهای مرتبط را با revert محدود و ثبت دلیل برگردان؛ snapshot ایمن Queue/Media3 و داده‌های شخصی را دست نزن.
- Review انسانی یا فنی مستقل، OEMها، Android 10/12/14+ و خودرو در صورت عدم انجام `NOT_RUN/DEFERRED` بمانند. صاحب پروژه پس از بررسی خروجی نهایی می‌تواند merge/close با ذکر استثنای پذیرش را صریحاً مجاز کند؛ تا آن زمان Issue #10 **OPEN** و PR **NOT_OPENED** بماند.

## ۷. دروازهٔ خروج P01 — صرفاً حاکمیت پروژه

- [x] شاخهٔ جاری و `main/develop` مقایسه و هدف PR پیشنهادی ثبت شد.
- [x] فایل RAR با SHA ثبت‌شدهٔ P00 مطابقت دارد و ۱۲ PDF شمرده شد.
- [x] چارچوب ۱۶ AC، ۶۰ مورد M، ۵ گروه L با ۱۰ تلاش و ۱۰ گروه T تعریف شد.
- [x] قرارداد منابع، boundaryهای Voice/Media3، عدم Build/Test و P13-only نوشته شد.
- [x] فایل‌های `ISSUE_10_EVIDENCE_INDEX.json` و `ISSUE_10_QA_RESULTS.json` اولیه ساخته شدند؛ **همگی NOT_RUN/NOT_VERIFIED**.
- [x] الگوی PR آینده نوشته شد؛ **هیچ PR در P01 ایجاد نشد**.
- [ ] P02..P12 پیاده‌سازی و P13 APK/phone QA — متعلق به مراحل بعد، نه P01.

**تحویل P01:** `GOVERNANCE_SOURCE_DELIVERED_UNTESTED`؛ **فاز بعدی P02**. این هیچ claim دربارهٔ PASS شدن APK، STT/TTS، TalkBack، lab یا تست فنی ندارد.
