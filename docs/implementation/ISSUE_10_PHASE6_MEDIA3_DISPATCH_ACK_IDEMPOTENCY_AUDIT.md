# Issue #10 — P06 | Typed Voice→Media3 Dispatch، ACK معتبر و جلوگیری از تکرار

**تاریخ:** 2026-10-25

**وضعیت:** `SOURCE_IMPLEMENTED_UNBUILT`، `AUTHORED_ONLY`، `NOT_RUN`. هیچ اجرای Build/Test/Actions/Emulator/APK/گوشی انجام نشده و هیچ AC به PASS تبدیل نشده است.

**Branch:** `feature/10-ptt-voice-five-control-intents` · [Issue #10](https://github.com/latticeward-labs/music-copilot/issues/10)

**منبع:** فایل ضمیمهٔ MSM2(1)(1).rar با SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (۱۲ PDF؛ متن ADR-0002، ADR-0005، Code/Technical، System Architecture برای P06 به‌طور مستقیم بازبینی شد). قراردادهای جاری [ADR-0002](../adr/0002-service-owned-media3-playback.md)، ADR-0005، مستندات #7/#8/#9 و برنامهٔ #10 مرجع هستند.

## ۱. مشاهدات دقیق پیش از تغییر

- سرویس از قبل یک `ExoPlayer`، یک `MediaSessionService` و **یک** `PlaybackCommandProcessor` با یک Worker و حداکثر ۳۲ فرمان منتظر + یک in-flight داشت؛ با `CommandId` ثابت، تکرار *همان* payload را در دفتر in-memory تا ظرفیت ۲۵۶ entry یکی می‌کند؛ reuse شناسه با payload متفاوت را رد می‌کند.
- `PlaybackControllerConnection.execute` از قبل جواب `SessionResult` را منتظر می‌ماند، Snapshot مرجع سرویس را decode می‌کرد، Timeout و قطع اتصال Controller را شکست امن می‌دانست؛ اما **رسید موفق مشخص نمی‌کرد کدام `commandId` را ACK کرده است**.
- `DefaultVoiceSessionCoordinator` از P02 پیش از await به‌شکل اتمی `dispatchClaimed` را set می‌کرد و پاسخ قدیمی را به‌کمک generation/attempt رد می‌کرد. P05 فقط پنج Intent مستقیم می‌سازد.
- `MusicPlaybackService` در وضعیت انتهای صف، فرمان Next دستی را طبق قرارداد #7 به حالت Pause می‌برد و همچنان Success برمی‌گرداند؛ این برای **Voice NEXT** بدون ترک بعدی نباید «Executed» کاذب بسازد. رفتار دستی باید بدون تغییر بماند.
- سیاست قبلی `PlaybackNavigationPolicy.seekBy` پیشاپیش `-10000ms` را بدون مقدار منفی به صفر Clamp می‌کند؛ لازم نیست منطق Seek یا Player جدید ساخته شود.

## ۲. تغییرات واقعی سورس

| فایل | نقش و مرز مالکیت |
|---|---|
| [VoicePlaybackDispatcher.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/VoicePlaybackDispatcher.kt) | **جدید**: آداپتر کوچک Voice به `PlaybackGateway`؛ پنج فرمان typed، شناسهٔ `voice-<sessionId>-<attempt>` و مانع شروع هنگام Controller غیرآماده، بدون دسترسی به Player یا STT |
| [DefaultVoiceSessionCoordinator.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | حذف mapping فرمان تکراری؛ استفاده از Dispatcher با همان atomic claim قبل از تعلیق، guard generation/attempt و موفقیت صرفاً پس از `AppResult.Success` معتبر |
| [VoiceCommandReceiptPolicy.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/VoiceCommandReceiptPolicy.kt) | **جدید**: تشخیص شناسهٔ PTT، تطبیق دریافت ACK با همان ID، و تصمیم Pure دربارهٔ وجود ترک بعدی |
| [PlaybackWireProtocol.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackWireProtocol.kt) | کلید محدود `command_ack_id` برای **جواب موفق Execute**، همراه با Snapshot فعلی و بدون transcript/media path |
| [PlaybackControllerConnection.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | ACK Voice بدون شناسه یا با شناسهٔ نادرست، Success محسوب نمی‌شود؛ controller generation/connection و snapshot revision guards قبلی محفوظ |
| [MusicPlaybackService.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | بستهٔ ACK فقط بعد از خروجی پذیرفته‌شدهٔ Worker؛ NEXT صوتی هنگام پایان/صف خالی رد؛ PLAY صوتی وقتی تمرکز/route یا playWhenReady پذیرفته نشده رد؛ transport دستی و سایر مسیرها دست‌نخورده باقی مانده‌اند |
| [Issue10Phase6VoicePlaybackAckTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase6VoicePlaybackAckTest.kt) | **۱۵ تست JUnit نوشته‌شده، ۰ اجرا**؛ pure policies، fake port، CommandProcessor و source contract |

**بدون تغییر:** `PlaybackCommandProcessor.kt` و بانک idempotency موجود؛ `PlaybackNavigationPolicy.kt` و Clamp آن؛ مدل PlaybackCommand/PlaybackSnapshot، Queue schema، MediaStore resolver، ExoPlayer instance count، MediaSession callbacks عمومی، Bluetooth focus/route، صفحه‌های UI و AndroidManifest.

## ۳. مسیر واقعی پنج فرمان

```text
Full Player/Driving explicit PTT (P03)
  → STT on-device (P04) → deterministic Mapper (P05)
  → Voice Coordinator: claimDispatch once (P02/P06)
  → VoicePlaybackDispatcher.commandFor(intent, sessionId, attempt)
  → PlaybackGateway.execute(typed command with stable CommandId)
  → private Media3 Controller sendCustomCommand(EXECUTE_V1)
  → single service PlaybackCommandProcessor dedupe/receipt safety
  → one ExoPlayer authoritative queue/seek/playback mutation
  → service worker AppResult.Success(snapshot) or typed Failure
  → SessionResult success + authoritative snapshot + command_ack_id
  → controller validates matching Voice ACK + live controller generation
  → Voice Coordinator validates active voice generation + successful ACK
  → Executed; otherwise Failed/manual (no success TTS)
```

**Command identity:** یک attempt واحد از Session ID مثبت و شمارهٔ تلاش ۱ یا ۲ همان شناسهٔ opaque را می‌سازد. تکرار همان CommandId و payload تا زمانی که در دفتر bounded سرویس موجود است، side-effect جدیدی ندارد. استفادهٔ همان ID با payload متفاوت در `PlaybackCommandProcessor` موجود رد می‌شود. این ledger **داخل حافظه و حداکثر ۲۵۶ entry** است، دائمی یا تضمین exactly-once جهانی/پس از process death نیست؛ هیچ ادعایی برای تراکنش پردازش توزیع‌شده وجود ندارد.

**ACK:** `PlaybackWireProtocol.acknowledgedSnapshot` فقط پس از تکمیل موفق Worker و قبل از جواب Media3 استفاده می‌شود. Controller در فرمان Voice نبود/عدم تطابق `command_ack_id` را شکست ایمن تلقی می‌کند، حتی اگر SessionResult به اشتباه `RESULT_SUCCESS` باشد. فرمان‌های دستی برای سازگاری #7 همچنان Snapshot wire قدیمی را می‌پذیرند. Snapshot بعد از ACK بر مبنای Revision و اتصال جاری service باقی می‌ماند؛ Voice از UI state، پیش‌بینی Player یا TTS برای اثبات اجرای Media3 استفاده نمی‌کند.

## ۴. نتیجهٔ فرمان و Edge case

| حالت | نتیجهٔ source P06 |
|---|---|
| PLAY با Queue معتبر | خدمت Player دستور `Play` را اجرا می‌کند؛ اگر Focus/Route یا `playWhenReady` آن را رد کرده باشد، Voice نتیجهٔ Failure می‌گیرد؛ از حالت Playing فرضی استفاده نمی‌کند |
| PLAY با Queue خالی | Service `PLAYBACK_REJECTED`، بدون ساخت Queue خیالی |
| PAUSE | همان Pause سرویس، صف و position حفظ می‌شوند؛ مسیر safety فوری #8 در Receipt قبل از Worker دست‌نخورده است |
| NEXT با ترک بعدی معتبر | مسیر QueuePolicy فعلی؛ Worker یک بار فرمان را می‌پذیرد |
| NEXT صوتی در آخر صف/خالی | **رد Voice-specific پیش از تغییر Player**؛ دیگر `Executed` کاذب ندارد؛ Next دستی رفتار پایان صف سابق را حفظ می‌کند |
| PREVIOUS | قوانین ۳ ثانیه‌ای و ناوبری موجود بدون تغییر |
| SEEK_BACK | همان `PlaybackCommand.SeekBy(-10000)`؛ سرویس از `PlaybackNavigationPolicy.seekBy` استفاده و در ۵ ثانیهٔ اول تا صفر Clamp می‌کند |
| خطای Queue/Player/کنترلر/قطع اتصال | Failure نوع‌دار؛ Voice وارد Executed یا بازخورد Success نمی‌شود |
| ACK بی‌نام یا با نام اشتباه / Callback قدیمی | در Controller برای Voice رد می‌شود؛ P02 همچنین generation stale را رد می‌کند |
| چند Submit با ID یکسان و payload یکسان | `PlaybackCommandProcessor.recent` همان Deferred را بازاستفاده می‌کند، نه یک side-effect تازه؛ حداکثر ۲۵۶ ID قدیمیِ تکمیل‌شده در حافظه نگه می‌دارد |

### محدودیت تأیید که نباید نادیده گرفته شود

- پاسخ موفق Media3 به معنای پذیرش و اعمال command در Player است؛ **الزاماً ثابت نمی‌کند صدا همان لحظه از بلندگو پخش شده**. Media3 می‌تواند حالت Buffering داشته باشد. Play واقعی، Route، audio focus، playback errors و end-to-end در P10/P13 بررسی خواهند شد.
- Cancellation بعد از رسیدن فرمان به Worker نمی‌تواند mutation قبلی را rollback کند. P02 می‌تواند callback دیررس را ignore کند و P06 از Submit تکراری جلوگیری می‌کند؛ تضمین عدم تغییر صوت پس از Cancel که command پیش‌تر به سرویس رسیده، بدون transaction/cancel service وجود ندارد.
- دفتر dedup به ۲۵۶ شناسه محدود است؛ بعد از eviction یا service restart، guarantee exactly-once سراسری مطرح نیست. همین محدودیت به تست گوشی برای duplicate/late ACK و بررسی P10 گزارش می‌شود.
- رتبه‌بندی ۵۰ آزمایش تشخیص گفتار تحت تأثیر Provider Confidence واقعی است؛ P06 آن را دور نمی‌زند.

## ۵. ۱۵ سناریوی JUnit جدید — فقط نوشته‌شده

۱. نگاشت تایپ‌شدهٔ پنج فرمان؛ ۲. رد PLAY_ARTIST/Session نامعتبر؛ ۳. تمایز ID در Retry دوم؛ ۴. تطبیق دقیق ACK؛ ۵. سازگاری Ack دستور دستی؛ ۶. صف خالی/End Voice NEXT؛ ۷. coherence queue Snapshot؛ ۸. Clamp Seek ده‌ثانیه‌ای؛ ۹. Controller غیرآماده؛ ۱۰. پاسخ Fake Gateway؛ ۱۱. خطای Gateway؛ ۱۲. dedup Processor و فقط یک Receipt؛ ۱۳. ID conflict؛ ۱۴. بررسی source ACK envelope و service integration؛ ۱۵. فقط یک Player/Worker.

تمام سناریوها `AUTHORED_ONLY/NOT_RUN` هستند؛ اجرای JVM، تست واقعی Service با Android، تست Queue روی گوشی و QA نهایی همچنان **صفر** است.

## ۶. Traceability و تحویل

- **AC06** پنج فرمان مطابق Mapper و typed dispatcher منبعی مستند؛ **AC07** عبور از same Media3 Gateway و ACK؛ **AC10** fallback؛ **AC11** stale ACK/idempotency؛ **AC15** وجود تست‌های جدید — همگی `NOT_VERIFIED` باقی می‌مانند.
- **G06** فقط در سطح source تا حدی رفع شده؛ race میان route loss، process lifecycle، Cancel و MediaSession command latency در P10 و P13 باید بررسی شود.
- **P07** Duck/Unduck lease مستقل طبق AudioFocus/Route safety؛ این فاز حجم صوت، MusicPlaybackService.player یا Focus path را برای duck دستکاری نکرد.
- **P08/P09** TTS و TalkBack WF-08؛ **P10** race lifecycle؛ **P11/P12** privacy/static audit؛ **P13** تنها APK تجمیعی + ۶۰ تست گوشی + ۵۰ تلاش آزمایشگاهی ≥۴۵ صحیح توسط مالک.

## ۷. دروازهٔ خروج P06

- [x] Adapter typed برای پنج فرمان و شناسهٔ opaque per Session/attempt اضافه شد.
- [x] همان Player، MediaSession و CommandProcessor مرجع موجود استفاده شد.
- [x] ACK شناسه‌دار برای Voice و رد رسید فاقد ID/ناهمخوان پیاده شد.
- [x] صف خالی و End-of-Queue Voice NEXT موفقیت کاذب ندارند.
- [x] SeekBack در Source از همان ثابت ۱۰ ثانیه و Clamp سرویس استفاده می‌کند.
- [x] جلوگیری از تکرار در claim Coordinator و bounded dedup Processor موجود پیوند خورد.
- [x] ۱۵ تست جدید فقط به‌صورت سورس نوشته شدند.
- [x] گزارش static source/evidence به GitHub تحویل شد.
- [ ] **Build، Lint، JVM، Actions، Emulator، APK، تست دستی/لابراتواری** — همگی `NOT_RUN`.

**نتیجه:** `P06_SOURCE_IMPLEMENTED_UNBUILT`، فاز بعد **P07 — Voice-owned Duck/Restore Lease با رعایت Audio Focus و Route**.
