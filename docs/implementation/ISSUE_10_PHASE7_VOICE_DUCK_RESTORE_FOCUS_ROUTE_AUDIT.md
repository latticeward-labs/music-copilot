# Issue #10 — P07 | Voice-owned Duck/Restore Lease و تقدم Audio Focus/Route

**تاریخ:** 2026-10-25  
**وضعیت:** `SOURCE_IMPLEMENTED_UNBUILT`، `AUTHORED_ONLY`، `NOT_RUN`  
**Branch:** `feature/10-ptt-voice-five-control-intents`  
**Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10)

**مبنای مستند:** بستهٔ ضمیمهٔ `MSM2(1)(1).rar` (۱۲ PDF، SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`)؛ به‌ویژه ADR-0002، معماری فنی، قرارداد Voice و Source فعلی #7/#8/#9. ADR-0002 تصریح می‌کند Audio Focus و Player فقط در لایهٔ Playback مدیریت شوند و Voice صرفاً موقتاً در هماهنگی با آن Duck کند.

> **این فاز فقط سورس را تغییر داده است.** هیچ کامپایل/Gradle/JUnit/Lint/Actions/APK/Emulator/گوشی اجرا نشده و موفقیت صوتی دستگاه/AC ادعا نمی‌شود.

## ۱. طراحی قابل‌بررسی و مرز مالکیت

- **مالک Volume:** تنها `MusicPlaybackService` به `ExoPlayer.volume` دست می‌زند؛ `FullPlayerScreen`، `DrivingModeScreen`، `DefaultVoiceSessionCoordinator` و STT/TTS، Player، `AudioManager` یا stream volume را تغییر نمی‌دهند. همان **یک** `ExoPlayer` و `MediaSessionService` برقرار است.
- **Voice adapter:** پورت [VoiceDuckingPort](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/VoiceDuckingPort.kt) با `suspend acquireVoiceDuck(token): Boolean` و `releaseVoiceDuck(token)`. Production از `PlaybackControllerConnection` استفاده می‌کند؛ نبود پورت Production به `UnavailableVoiceDuckingPort` **fail-closed** می‌رود. `NoOpVoiceDuckingPort` فقط seam پیشین تست‌های مصنوعی است و توسط Production Bindings انتخاب نمی‌شود.
- **Transport:** دو `SessionCommand` خصوصی و app-UID-verified، `VOICE_DUCK_ACQUIRE_V1` و `VOICE_DUCK_RELEASE_V1` روی **MediaSession موجود** اضافه شدند؛ رسانه، متن Voice و دادهٔ گوشی را حمل نمی‌کنند. فقط UUID کوتاه‌عمر و بدون PII به‌صورت `voice-duck-<UUIDv4>` ارسال می‌شود.
- **Admission:** آغاز STT فقط **بعد از ACK موفق کاهش صدا** است. UI ممکن است از لحظهٔ لمس، حالت Listening را نشان دهد، ولی میکروفون پیش از تأیید سرویس باز نمی‌شود؛ رد/timeout اتصال ظرف ۲ ثانیه ⇒ Voice Failed/Manual Fallback. این کار PlaybackGateway را برای فرمان‌های واقعی تغییر نمی‌دهد.
- **سرویس** بر اساس `ServiceVoiceDuckLease`، volume پایهٔ Player (ممکن است از پیش کم یا صفر باشد) را حفظ و یک ضریب موقت `0.25f` اعمال می‌کند. این ضریب مربوط به **property volume خود ExoPlayer** است و Audio Focus multiplier داخلی Media3 را جایگزین نمی‌کند. درخواست Audio Focus جدید وجود ندارد.
- **ACK و safety:** شروع Lease فقط از client خصوصی، وقتی Focus در حالت Available است، Route قبلاً invalidate نشده و playback suppression فعال نیست، پذیرفته می‌شود. نیازی به فعال بودن موسیقی یا صف نیست: Voice می‌تواند هنگامی که Player Paused است هم وارد Listening شود، بدون Resume. اگر music در طول Voice توسط کاربر شروع شود، همان Player موقتاً Duck باقی می‌ماند.

## ۲. قرارداد دقیق acquire/release

| حالت | نتیجهٔ سورس |
|---|---|
| Mic/شروع تلاش اول | ساخت UUID نو برای همان attempt؛ پیش از STT منتظر ACK سرویس |
| Volume=1.0 | Service مقدار ۱٫۰ را نگه می‌دارد و property را به ۰٫۲۵ می‌برد |
| Volume=0.6 | Service مقدار ۰٫۶ را نگه می‌دارد و به ۰٫۱۵ می‌برد |
| Volume=0 | صفر می‌ماند؛ Voice باعث پخش شدن آهنگ نمی‌شود |
| دریافت دوبارهٔ همان token | `ServiceVoiceDuckLease.acquire` بدون ضرب دوباره تأیید می‌کند؛ TTL نوسازی می‌شود |
| تلاش جدید/Retry با token تازه | baseline تلاش قبلی اگر هنوز Duck اعمال‌شده باشد بازیابی منطقی می‌شود و **0.25 × 0.25** رخ نمی‌دهد؛ token سابق tombstone می‌شود |
| Release قدیمی بعد از شروع Voice جدید | فقط token فعلی مجاز به Release است؛ Volume جدید دست‌نخورده می‌ماند |
| تغییر مستقیم property volume توسط کاربر/عامل بیرونی | اگر observed volume دیگر برابر ducked value نباشد، baseline قدیمی روی تنظیم تازهٔ کاربر نوشته نمی‌شود |
| Cancel / Back / Exit / permission lost / app background | Coordinator هم‌زمان generation را invalidate و release کوتاه‌عمر را best-effort ارسال می‌کند؛ finally همان token را idempotent release می‌کند |
| Timeout / خطای STT / Mapping / ACK / TTS | `markFailed` یا `finally` Lease را آزاد می‌کند؛ هیچ Command `Play` یا `Resume` خودکار صادر نمی‌شود |
| تماس / Focus loss / Media3 suppression / Bluetooth یا headphone route lost | Service روی Main فوراً Lease را revoke می‌کند و سایر سیاست‌های #8 را برای Pause/Suppress/No-speaker-leak حفظ می‌کند |
| Activity/Controller بسته، release packet گم، crash اپ | Service دارای **TTL حداکثر ۲۲ ثانیه** برای هر Lease است؛ بعد از timeout بدون نیاز به Voice Controller آن را آزاد می‌کند |
| onDestroy خود Service | پیش از Release Player، Restore/Cancel timer انجام می‌شود |

**محدودیت دقت:** اگر process خود Service کشته شود، ExoPlayer نیز از بین می‌رود و restoration دیگر معنای playback مداوم ندارد. TTL تضمین QA روی همهٔ OEMها نیست؛ فقط قرارداد source است. `releaseVoiceDuck` از UI/Main به شکل nonblocking می‌رود؛ اگر Controller قطع شده باشد، TTL سرویس بهترین fallback است. Camera/phone call تشخیص صریح نشده؛ فقط Media3 focus و route events قابل اتکا هستند.

## ۳. اولویت وقفه‌ها و عدم Resume ناخواسته

`Media3FocusObservationAdapter` و `PlaybackInterruptionGate` قبلی **بدون تغییر** Audio Focus و route-loss latch را مالک‌اند. P07 فقط revoke Volume را به callbackهای همین سرویس افزوده است؛ نه receiver تماس، نه Focus Manager دوم، نه تغییری در سیاست resume چهارشرطی #8. هنگام Audio Focus Lost یا headset unplug:
1. Volume lease در سرویس آزاد می‌شود.
2. Host existing callback، Voice generation/STT/TTS را cancel می‌کند.
3. همان سرویس مسئول Pause، route latch و منع autoplay است. Duck release **هیچ `player.play()`، `setPlayWhenReady(true)`، `requestAudioFocus` یا clearing manual-pause flag** انجام نمی‌دهد.

جابه‌جایی آهنگ/دکمهٔ دستی در زمان Listening همچنان در همان Player و Queue عمل می‌کند. Duck صرفاً output gain لایهٔ playback را تغییر می‌دهد و مؤلفهٔ تشخیص یا سیستم مجوز را کنترل نمی‌کند.

## ۴. فایل‌های تولید و اصلاح‌شده

| فایل GitHub | تغییر |
|---|---|
| [VoiceDuckingPort.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/VoiceDuckingPort.kt) | پورت غیرمسدودکنندهٔ Release، ACK-based Acquire و Production fail-closed |
| [ServiceVoiceDuckLease.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/ServiceVoiceDuckLease.kt) | Pure baseline/gain reducer؛ token guard، no-double-duck، protect user-adjusted volume |
| [PlaybackWireProtocol.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackWireProtocol.kt) | دو SessionCommand خصوصی و token validation، بدون تغییر نسخهٔ commands پخش |
| [PlaybackControllerConnection.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | پیاده‌سازی پورت؛ acquire با ۲s timeout/ACK دقیق، release best-effort |
| [MusicPlaybackService.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | تنظیم واقعی حجم فقط در سرویس، TTL، stop/revoke هنگام Focus/Route/Destroy، tombstone محدود ۶۴ token، شرط private caller |
| [DefaultVoiceSessionCoordinator.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | UID token در هر attempt؛ Acquire پیش از STT؛ Release در fail/stop/retry/timeout/cancel/finally |
| [VoiceBindings.kt](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) | تزریق تنها Duck port اصلی و fallback دستی هنگام نبود آن |
| [Issue10Phase7VoiceDuckingLeaseTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase7VoiceDuckingLeaseTest.kt) | **۱۳ آزمون JUnit صرفاً نوشته‌شده؛ NOT_RUN** |

**بدون تغییر:** `Media3FocusObservationAdapter.kt`، `PlaybackInterruptionGate.kt`، سیاست queue/seek، `FullPlayerScreen`، `DrivingModeScreen`، Manifest، permission، on-device STT/TTS provider، mapper پنج فرمان، persistency. هیچ Player یا background mic جدید ایجاد نشد.

## ۵. ۱۳ سناریوی JUnit (AUTHORED_ONLY)

۱) attenuation دقیق و restoration baseline؛ ۲) عدم double-duck پس از جایگزینی؛ ۳) رد release نسل قدیمی؛ ۴) حفظ volume جدید کاربر؛ ۵) revoke تکراری/ایمن؛ ۶) volume صفر؛ ۷) token یا volume نامعتبر؛ ۸) release دوباره؛ ۹) عدم ضبط STT وقتی Duck رد می‌شود؛ ۱۰) Acquire پیش از STT و Release پس از ACK؛ ۱۱) Cancel حین Listening بدون dispatch؛ ۱۲) source safety برای Focus/Route/TTL؛ ۱۳) عدم ایجاد Player دوم یا AutoPlay در lease layer.

تست‌های JUnit، phone manual، Android instrumented، OEM/route/call/focus و ۵۰ تلاش Lab **همگی NOT_RUN** هستند. ۱۳ مورد صرفاً coverage منبعی هستند.

## ۶. موارد باز و ریسک‌های ارزیابی در فازهای بعد

- **AC03:** Lease از نظر source طراحی شد؛ هنوز خروجی حقیقی، race در Disconnect و restore حجم در خودرو/AirPods تأیید نشده؛ **NOT_VERIFIED**.
- **AC04/AC10/AC11/AC15:** source cleanup تقویت شد؛ هیچ معیار با وجود کد PASS نشده.
- **G01:** از `OPEN` به `SOURCE_IMPLEMENTED_PENDING_RUNTIME_VERIFICATION` تغییر داده می‌شود، نه `CLOSED`.
- **P08:** متن TTS نهایی/زمان‌بندی success/failure و عدم overlapping باید کامل شود؛ TTL در P07 باقی است.
- **P09:** overlay و TalkBack باید طبق WF-08 کامل شوند.
- **P10:** cancel, rotation, call/focus/device-loss, route race, out-of-order acquire/release ACK, manual volume/Media3 focus multiplier و ownerScope teardown باید source و سپس دستگاه واقعی بررسی شوند.
- **P12/P13:** Static diff Audit؛ سپس فقط در فاز ۱۳ یک APK تجمعی و تست‌های واقعی کاربر.

## ۷. Definition of Done سورس این فاز

- [x] Volume فقط از یک service-owned Player تغییر می‌کند.
- [x] Acquire با توکن مجزا و Service ACK قبل از ضبط STT.
- [x] Baseline/ضریب 0.25، no-double-duck و guard against stale release.
- [x] Release مسیرهای Timeout/Retry/Failed/Executed/Cancel/Back/Origin با best-effort + finally.
- [x] Revocation فوری هنگام Focus/Route و TTL سرویس هنگام callback گم‌شده.
- [x] حفظ manual Pause و چهار شرط Focus/Route بدون AutoResume.
- [x] ۱۳ source-only JUnit case و مستندات Traceability آماده.
- [ ] Compile/Gradle/Actions/JUnit/Lint/Emulator/APK/Phone/physical audio — **NOT_RUN**.

**نتیجه:** `P07_SOURCE_IMPLEMENTED_UNBUILT`. مرحلهٔ بعد: **P08 — بازخورد کوتاه و TTS اختیاری غیرمسدودکننده**.
