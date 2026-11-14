# Issue #10 — P02 | پیاده‌سازی Voice State Machine و قرارداد نسل‌های نشست

**تاریخ:** 2026-10-25  
**وضعیت:** \`SOURCE_IMPLEMENTED_UNBUILT\` / \`SOURCE_REVIEW_ONLY\` / \`TESTS_AUTHORED_NOT_RUN\`؛ این نتیجه، گواه عملکرد اجراشده، PASS معیار پذیرش، یا قابلیت میکروفون گوشی نیست.  
**Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10) · **Branch:** \`feature/10-ptt-voice-five-control-intents\`  
**مبنای فایل ضمیمه:** \`MSM2(1)(1).rar\` با SHA-256 برابر \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`. مقایسهٔ ADR-0004 و ADR-0005 و Technical Blueprint و MVP انجام شد. قراردادهای مصوب #16 و #17 و ممیزی P00/P01 اولویت دارند.

## ۱. دامنهٔ دقیق P02

- معماری حفظ شد: فقط **یک** \`VoiceSessionCoordinator\` از نوع Application-scoped و یک StateFlow غیرماندگار، بدون اضافه کردن Player/MediaSession، Destination، Service، STT provider یا permission launcher.
- نوع‌های موجود \`Idle\`، \`Listening\`، \`Processing\`، \`Confirming\`، \`Executed\` و \`Failed\` در API عمومی دست‌نخورده ماندند. مسیرهای مستقیم پنج فرمان فقط \`Idle → Listening → Processing → Executed/Failed\` را دارند؛ \`Confirming\` برای Issue #11 حفظ شد و \`confirm()\` در #10 فرمانی اجرا نمی‌کند.
- پس از پایان، وضعیت \`Executed\` یا \`Failed\` برای نمایش UI/Retry باقی می‌ماند؛ **\`cancel\`/dismiss/ازبین‌رفتن origin** آن را به \`Idle\` بازمی‌گرداند. پایان اجرای کارِ provider با پاک شدن فورى نمایش UI اشتباه گرفته نشود؛ زمان‌بندی dismiss و نمایش در P08/P09 تکمیل می‌شود.
- \`start(origin)\` همان قرارداد #16 را حفظ کرده است: **فراخوانی صریح جدید** می‌تواند نشست قبلی را جایگزین کند؛ فاز **P03** باید دو بار لمس تصادفی Mic را در سطح UI رد کند. P02 ادعای فعال شدن Full Player Mic ندارد.

## ۲. فایل‌های سورس تحویل داده‌شده

| مسیر | اقدام |
|---|---|
| [\`voice/session/DefaultVoiceSessionCoordinator.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | بازنویسی admission وضعیت‌ها، state lock، تولید و باطل‌سازی generation، مدیریت دقیق Timeout/Retry، claim یک‌بار فرمان و cleanup |
| [\`voice/session/VoiceTransitionPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/VoiceTransitionPolicy.kt) | Policy خالص: کنترل \`Listening→Processing\`، \`Processing→dispatch/Executed\`، مجوز Retry و رد نسل‌های قدیمی |
| [\`Issue10Phase2VoiceStateMachineTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase2VoiceStateMachineTest.kt) | **۸ سناریوی JUnit جدید، فقط AUTHOR_ONLY**؛ هیچ JVM/Gradle/Actions اجرا نشده |

**حدود تغییر:** \`AppNavHost\`، \`FullPlayerScreen\`، \`DrivingModeScreen\`، Android Manifest، VoiceBindings، STT/TTS provider، RestrictedVoiceIntentMapper، Playback Gateway و \`MusicPlaybackService\` در P02 تغییری ندارند. قواعد موجود پنج فرمان و ACK service از قبل حفظ شده‌اند؛ اصلاح کامل mapper در P05 و dispatch/ACK safety در P06 است.

## ۳. ماشین وضعیت و هم‌زمانی

1. آغازِ صریح \`start\`: پاک کردن gen قبلی با \`invalidateLocked\` (Idle/لغو provider/Job)، کنترل lifecycle owner و \`stt.isAvailable\`، تولید ID صعودی و \`Listening(attempt=1)\`.
2. \`beginAttemptLocked\`: ساخت \`SupervisorJob\` فرزند scope برنامه و یک کار STT وابسته؛ تمام نتیجه‌های provider/Playback باید \`id + job identity + attempt\` را با active generation تطبیق دهند. قفل مشترک انتقال‌های هم‌زمان \`start/retry/cancel\` و callbackها را serialize می‌کند.
3. \`Listening\`: دقیقاً \`withTimeoutOrNull(VoicePolicy.LISTEN_TIMEOUT_MS)\` برای هر تلاش، مقدار **۶۰۰۰ میلی‌ثانیه** و provider request هم \`maxListeningMs=6000\`. Timeout → \`VOICE_TIMEOUT\` و cleanup قبل از عرضهٔ Retry. هر استثنای غیر-cancellation به خطای نوع‌دار ایمن تبدیل می‌شود؛ \`CancellationException\` دوباره پرتاب می‌شود.
4. فقط نتیجهٔ فعال و موفق STT اجازهٔ \`Listening → Processing\` دارد. Mapper همچنان مطابق کد P01 کار می‌کند؛ خطای mapping یا نتیجهٔ \`Rejected/RequiresConfirmation\` در پنج فرمان فعلی → \`VOICE_NO_MATCH\`، **بدون ساخت یک گزینهٔ تأیید دروغین**.
5. برای intent مجاز، پس از \`Processing\` و پیش از تعلیق در \`PlaybackGateway.execute\`، \`dispatchClaimed\` به‌صورت اتمی set می‌شود؛ همان attempt قادر به ثبت دومین فرمان نیست. فقط **ACK موفقِ همان generation فعال** اجازهٔ \`Executed\` می‌دهد؛ پاسخ رد/خطا \`Failed\` است. دستور Artist در #10 فرستاده نمی‌شود.
6. فاز P02 هنوز **تضمین idempotency در سطح سرویس و ACKهای رقابتی** ندارد: تضمین نهایی receipt/command collision و safe playback response در P06 و race integration در P10 لازم است. اگر فرمانی پیش از cancel واقعاً به سرویس رسیده باشد، لغو Voice به‌معنای undo پخش نیست؛ هرگز چنین ادعایی نکن.
7. \`finally\` فرزند Job تمام‌شده را فقط اگر هنوز identityِ generation فعال است از \`childJob\` حذف و cancel می‌کند. وقتی Retry یا Start جدید جایگزینش شده، completion دیررس آن state/job جدید را پاک نمی‌کند. TTS اختیاری پس از ACK باقی مانده؛ کنترل کامل feedback/overlap در P08 است.
8. \`Failed\`: فقط Timeout/NoMatch در **attempt=1** مجوز Retry دارند. خطای provider ناموجود یا خطای اجرای Playback به Retry خودکارِ مخرب منجر نمی‌شود. \`retry()\` با همان ID منطقی، Job قبلی، STT و TTS را **قبل از راه‌اندازی attempt=2** لغو می‌کند. سومین تلاش مجاز نیست. \`cancel()\` همیشه state را Idle می‌کند و providerها را best-effort متوقف می‌کند.
9. \`close()\` coordinator را برای همیشه بسته اعلام می‌کند؛ هر start بعدی Fail بدون میکروفون و فرمان است. هیچ صوت، متن STT یا media path در StateFlow تازه ذخیره/لاگ نمی‌شود.

## ۴. تست‌های **نوشته‌شده اما اجرا‌نشده**

| شناسه در فایل JUnit P02 | مورد |
|---|---|
| 01 | رد session ID، attempt یا origin قدیمی و dispatch تکراری توسط policy |
| 02 | Retry فقط برای خطای recognition مجاز، حداکثر دو attempt |
| 03 | Executed فقط پس از ACK و آزاد شدن Job در حالت success |
| 04 | Timeout دقیق با Virtual Time و منع Retry سوم |
| 05 | Cancel هنگام Processing و نادیده گرفتن ACK دیررس با Provider/Fake |
| 06 | جایگزینی generation و رد STT قدیمیِ مقاوم به cancel |
| 07 | جملهٔ ناشناخته → Failed بدون ارسال Command |
| 08 | close → start ممنوع و state Idle |

سایر تست‌های Phase 8 و Issue 9 **تغییر داده نشده‌اند**؛ باید در یک اجرای فنی مجاز در آینده سازگاری بررسی شود. هیچ‌یک از تست‌های موجود یا جدید از سوی این فاز اجرا نشده‌اند. وضعیت T02 در دفتر QA: \`AUTHORED_ONLY\` با \`execution_status=NOT_RUN\`.

## ۵. مرزبندی بین فازها و ریسک‌های باز

- **P03:** اتصال واقعی Mic در Full Player، یک tap آگاهانه پس از permission grant و admission دو بار لمس.
- **P04:** امکان تشخیص English/OEM، Android API29/30 manual fallback، teardown provider و فقدان confidence.
- **P05:** اصلاح پیشوند گفتاری \`musico\`، واژه‌نامهٔ deterministic و محدود کردن mapping پنج intent.
- **P06:** Idempotency نهایی dispatch و ACK service، queue/route safety، وضعیت Playback authoritative.
- **P07:** Lease مخصوص Duck و restore امن با precedence تماس، فوکوس و قطع مسیر.
- **P08/P09:** feedback کوتاه و lifecycle واقعی UI/overlay، TalkBack، امکان retry/cancel.
- **P10:** race واقعی بین permission revocation، background، phone focus، Bluetooth و callbackهای service.
- **P11/P12:** Privacy/telemetry source audit و ممیزی تجمیعی. **P13** تنها build APK و سپس تست توسط مالک.

**AC01، AC04، AC10، AC11 و AC15:** فقط شواهد source P02 اضافه شده است؛ همچنان \`NOT_VERIFIED\` تا شواهد runtime/اجرای تست وجود داشته باشد. تمام ۱۶ AC، ۶۰ تست M و ۵۰ تلاش آزمایشگاهی L بدون تغییر در status پذیرش باقی مانده‌اند.

## ۶. دروازهٔ خروج P02

- [x] قرارداد نسل/Session و transitionهای مستقیم حفظ و پیاده شد.
- [x] cancellation و cleanup Terminal/Retry/Start با guardهای هویت نسل پیاده شد.
- [x] Deadline هر تلاش ۶ ثانیه، حداکثر یک Retry و Failure-to-manual در سورس تعریف شد.
- [x] پذیرش یک‌بار فرمان در Coordinator و رد ACK دیررس در سورس اعمال شد.
- [x] هشت تست JUnit source authored شدند.
- [x] بررسی ایستای GitHub انجام شد؛ duplicate StateFlow از diff اولیه اصلاح شد.
- [ ] **اجرای هر تست، Build، Lint، APK، Android، OEM و شواهد واقعی:** \`NOT_RUN\` مطابق سیاست پروژه.

**نتیجه:** \`P02_SOURCE_IMPLEMENTED_UNBUILT\`؛ خروجی commit شده، **نه** اثبات‌شده از نظر runtime. ادامه از **P03 — Mic در Full Player و Driving**.
