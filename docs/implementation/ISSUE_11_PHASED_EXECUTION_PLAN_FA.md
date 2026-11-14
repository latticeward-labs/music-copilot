# Issue #11 — PLAY_ARTIST + One-Step Ambiguity Handling
## برنامه اجرایی فازبندی‌شده — 2026-11-14

**وضعیت این سند:** برنامه‌ریزی و ممیزی سورس؛ پیاده‌سازی Issue #11 هنوز آغاز نشده. هیچ تست یا Build/APK در فازهای P00 تا P12 مجاز نیست؛ فقط P13 مرحله یکپارچه‌سازی نهایی، ساخت یک APK، تحویل چک‌لیست و اجرای تست به‌وسیله مالک است.

**شاخه:** \`feature/11-play-artist-one-step-ambiguity\` — مبنا \`main\` پس از Merge شدن #10. **مخزن:** \`latticeward-labs/music-copilot\`.

**منابع الزام‌آور:**
- [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11)؛ [PRD](https://github.com/latticeward-labs/music-copilot/blob/main/docs/PRD.md)؛ [MVP](https://github.com/latticeward-labs/music-copilot/blob/main/docs/MVP.md)؛ [Software Architecture](https://github.com/latticeward-labs/music-copilot/blob/main/docs/SOFTWARE_ARCHITECTURE.md).
- آرشیو ۱۲ PDF انگلیسی ارسالی مالک \`MSM2(1)(1).rar\` با SHA-256 برابر \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`. مخصوصاً ADR-0003/0004/0005، Technical Documentation، PRD/MVP، System Architecture.
- #6 Library (Closed)، #10 Voice (Closed/Owner exception)، #16 Software Architecture (Closed)، #17 Design (Closed). #80 کیفیت تشخیص صوتی P0 همچنان OPEN است: پنج فرمان Issue #10، برابر 24/50 = 48% و زیر Gate ≥90%.

## اصول غیرقابل‌نقض
1. شش Intent دقیقاً \`PLAY\`, \`PAUSE\`, \`NEXT\`, \`PREVIOUS\`, \`SEEK_BACK\`, \`PLAY_ARTIST\`؛ بدون NLU/LLM، Wake Word، مکالمه چندمرحله‌ای، حساب/Backend/Streaming، و بدون نیاز به اینترنت.
2. شروع STT فقط با لمس Mic؛ Coordinator مالک Session/Confirming؛ Service/Media3 مالک Queue و Playback؛ UI فقط نمایش و رویداد انتخاب.
3. مسیر واضح با شواهد confidence کافی → اجرای مستقیم؛ دو گزینه نزدیک یا confidence پایین اما قابل‌تعیین → تنها یک سؤال و پاسخ؛ ناموجود، ناشناخته، تأیید نامعتبر/دوم، لغو و خطای دسترسی → عدم تغییر پخش و بازگشت به کنترل دستی.
4. Confirming فقط فهرست immutable یک یا دو گزینه با \`VoiceCandidateToken + optionIndex\`؛ دکمه Neither/Cancel خارج از گزینه‌های هنرمند؛ پس از مصرف token دیگر قابل اجرا نیست.
5. جست‌وجوی هنرمند از مجوز MediaStore و ایندکس Room؛ قبل از Commit به Playback باید revision/مجوز/وجود فایل و قابلیت پخش دوباره بررسی شود؛ Queue فقط از فایل‌های همان هنرمند و با ترتیب پایدار تشکیل شود.
6. نظم قطعی: exact normalized > fuzzy score > playback-history (فقط tie-break مجاز) > stable artist ID. آستانه‌های baseline: direct provider 0.90 (محدوده تنظیم 0.80..0.98)، artist clear 0.88، plausible 0.72 و حداقل فاصله 0.08. نام امتیاز و الگوریتم نسخه‌دار؛ امتیاز STT و تطبیق Artist نباید به‌صورت پنهان ترکیب شوند.
7. Moonshine+Silero فعلی confidence کالیبره برای هر utterance برنمی‌گرداند؛ برای تصمیم مستقیم artist نباید confidence جعلی تولید شود. در P05 باید سیاست ایمن/مستند سازگار با استثنای Issue #10 برای عبارت پنج فرمان دقیق تعیین شود. این نقص و #80 را پنهان نکنید.
8. داده صوت خام، Transcript کامل، نام هنرمند/فایل، ID/URI و محتویات کتابخانه نباید در Telemetry/گزارش‌های GitHub بیایند. فقط Intent، bucket اطمینان/تأخیر، نتیجه/خطای دسته‌بندی‌شده.

## مشاهده شکاف واقعی کد فعلی در main
- \`RestrictedVoiceIntentMapper.kt\`: \`PLAY_ARTIST\` فقط نام رزروشده؛ هیچ map مستقیم برای هنرمند ندارد.
- \`RoomLibraryRepository.resolveVoiceCandidates()\`: کاندیدا را از \`normalizedTitle\` و وجود متن در هنرمند، با امتیازهای 1/.88/.72 می‌سازد؛ این قرارداد «حل هنرمند» نیست و باید بدون شکستن Search/Browse اصلاح/تفکیک شود.
- \`VoicePlaybackDispatcher.commandFor(PLAY_ARTIST)\` هم‌اکنون \`null\` می‌دهد.
- \`DefaultVoiceSessionCoordinator.confirm()\` هم‌اکنون با \`VOICE_STALE_SESSION\` رد می‌کند.
- \`VoiceOverlayPresentationPolicy\` حالت Confirming را \`ConfirmationUnavailable\` نمایش می‌دهد و \`VoiceSessionPanel\` گزینه‌های انتخاب هنرمند را ارائه نمی‌کند.
- \`LibraryTextNormalization\` کلید Unicode/NFKC موجود است؛ باید از آن استفاده کرد و منطق نسخه‌دار جدید را با نمایش متادیتا مخلوط نکرد.
- \`PlaybackCommand.ReplaceQueue\` و \`PlaybackGateway.execute\` از قبل برای صف یکپارچه و ACK سرویس موجودند؛ پخش‌کننده دوم ساخته نشود.

## جدول فازها (فقط اجرای کد و مستندسازی؛ بدون Build/Test تا P13)

### P00 — مرجع، وابستگی، فریز Scope و شکاف‌سنجی
- Issue #11، ADRها، PRD/MVP، Architecture، #6/#10/#16/#17 و ریسک #80 را تطبیق بده.
- فهرست دقیق فایل‌ها و APIهای مجاز برای تغییر، baseline commit و ممنوعیت تغییر STT/Playback خارج از Scope ثبت شود.
- **خروجی:** \`ISSUE_11_P00_READINESS_AND_GAPS.md\` + ثبت وضعیت readiness. **پایان فاز:** هیچ dependency حیاتی گم نشده و فرضیات باز مستندند.

### P01 — قرارداد محصول، state/نتایج و ماتریس پذیرش
- حالات Direct / Ambiguous / NotFound / Unknown / Unsupported / Cancel / Stale / PlaybackFailure را به معیارهای Issue نگاشت کن.
- مشخص کن یک پاسخ در Confirming تنها یکی از optionIndexهای فعلی یا Neither/Cancel است؛ Retry تشخیص با clarification دوم اشتباه نشود.
- **خروجی:** typed contract + State/Result matrix + traceability AC01..AC13؛ بدون فعال‌سازی رفتار.

### P02 — Grammar قطعی PLAY_ARTIST و Slot Extraction
- عبارت الزامی \`Play songs by [artist]\`، و فقط فرم‌های معادل واقعاً تأییدشده در Voice Contract را پیاده کن.
- phrase-prefixهای کنترل پنج‌گانه را تغییر نده؛ طول ورودی، نویز نقطه‌گذاری/فاصله، زبان انگلیسی، slot خالی، متن خارج از Scope را با خروجی typed تفکیک کن.
- **خروجی:** mapper/prefix grammar نسخه‌دار و آزمون‌های منبع نوشته‌شده ولی NOT_RUN.

### P03 — Artist Index/Query حقیقی و snapshot
- Contract جست‌وجوی هویت هنرمند (نه song title) را با ArtistGroup ID و revision و سطح دسترسی پیاده کن.
- از Room projection خواندن محدود/صفحه‌بندی‌شده هنرمندان و اعضای گروه بساز؛ Unknown Artist بدون متادیتای حقیقی نباید هنرمند ساختگی شود.
- در permission revoke، scan/reconcile و stale revision شکست امن.
- **خروجی:** typed artist lookup + song references bounded؛ بدون کپی کل Library به UI.

### P04 — Normalization، امتیاز fuzzy و tie-break پایدار
- normalization مرحله‌ای/نسخه‌دار، exact، fuzzy bounded (مثلاً normalized edit distance با پارامتر ثبت‌شده) و ترتیب مورد توافق ADR را پیاده کن.
- history تنها هنگامی که local history مطابق سیاست تأییدشده حاضر است، tie-break ثانویه؛ هرگز نامزد با اختلاف ناچیز را صرفاً به کمک history بی‌اجازه مستقیم اجرا نکن.
- نزدیک به هم → Ambiguous حداکثر دو مورد؛ بدون کاندیدای plausible → NotFound؛ مرتب‌سازی مستقل از ترتیب SQLite.
- **خروجی:** pure \`ArtistResolver\` و scorer، fixtures/tests فقط نوشته‌شده.

### P05 — Confidence/Threshold و تصمیم Execute/Confirm/Reject
- \`BuildConfig\` برای thresholdها/validation bounds؛ هیچ Remote Config/Backend تازه اضافه نشود.
- provider confidence و artist similarity را جدا نگه دار. missing/NaN/out-of-range → عدم حدس. برای provider Moonshine بدون confidence، سیاست شفاف و safe با توجه به قرارداد معماری و ریسک #80 مستند شود.
- direct فقط با قواعد مورد تأیید؛ low confidence با انتخاب bounded → Confirming؛ سایر موارد → Failed/manual.
- **خروجی:** pure confidence policy + نگاشت deterministic به تصمیم نهایی.

### P06 — جمع‌کردن آهنگ‌های یک هنرمند و Playback dispatch
- از ArtistGroup انتخابی، فهرست محلی آهنگ‌ها با ترتیب ثابت و سقف موجود \`ReplaceQueue\` بساز؛ group/session/revision را پیش از اجرا کنترل کن.
- \`LibraryRepository.get(ref)\` و سرویس Media3 آخرین داور مجوز، موجودی و ACK هستند؛ \`PlaybackGateway\` تنها مسیر پخش.
- بازنویسی صف تنها پس از تصمیم مجاز؛ NotFound/Cancel/Failure نباید صف قبلی را دستکاری کند. برای هر dispatch \`CommandId\` ثابت نسبت به تلاش و nonce امن.
- **خروجی:** use-case \`PlayArtist\` + ACK و خطاهای typed؛ بدون player دوم.

### P07 — Coordinator: Confirming/Token/Exactly-Once
- \`DefaultVoiceSessionCoordinator\` را به state Confirming واقعی و \`confirm(token, optionIndex)\` تک‌مصرف مجهز کن.
- immutable snapshot از 1..2 گزینه، token/session/generation معتبر، ادعای dispatch پیش از suspend، stale event/چند لمس/process recreation/timeout/cancel رد شوند.
- یک پاسخ فقط؛ پاسخ مبهم دوم → Failed/manual؛ پنجره Confirming بازگشت به Listening یا مکالمه آزاد نباشد.
- **خروجی:** finite-state transitions با حداکثر یک dispatch و cancellation درست.

### P08 — UI Compose برای Confirming در Player و Driving
- \`VoiceSessionPanel\` و projection را با متن سؤال، دکمه‌های یک یا دو انتخاب و Neither/Cancel هماهنگ کن؛ \`onConfirm(token,index)\` به Coordinator متصل شود.
- touch target و TalkBack/focus/RTL-safe handling و بازگشت درست به مبدأ؛ UI هیچ lookup دیگری انجام ندهد.
- **خروجی:** رابط تأیید قابل استفاده با دسترسی‌پذیری، بدون ناوبری/صف جدید.

### P09 — پاسخ صوتی کوتاه، NotFound و recovery
- پیام‌های کوتاه انگلیسی برای Playing، Did you mean …?، Not Found، Cancel/Unavailable و خطای خدمت پخش را به feedback catalog اضافه کن؛ ۱ جمله حداکثر برای هر پاسخ.
- TTS موفقیت فقط پس از ACK؛ TTS غایب نباید پخش را لغو کند. خروج با نتیجه مشخص و manual fallback.
- **خروجی:** feedback catalog و خطاهای typed/RecoveryAction.

### P10 — امنیت lifecycle و تداخل صوتی
- هماهنگی Duck lease در Listening، release در همه پایان‌ها؛ STT/TTS و Mic lifecycle معتبر؛ Back، تغییر مبدأ Player/Driving، تماس، قطع Bluetooth، لغو مجوز و خروج از برنامه.
- pending confirmation در Recreation/Process death نباید دوبار اجرا شود؛ restore صوتی بی‌اجازه مجاز نیست.
- **خروجی:** safety guards و regression test fixtures (فقط نوشته شوند).

### P11 — Telemetry و Privacy Gate
- فقط \`intent_type\`, \`confidence_bucket\`, \`latency_bucket\`, \`result_code\` و دسته safe برای manual correction.
- هیچ Transcript، artist name، media title، URI/ID، candidate list، مسیر فایل یا token مصرفی persist/log/upload نشود.
- بروز خطا در optional telemetry نباید Voice/Playback را بلوکه کند.
- **خروجی:** typed allowlist + privacy test fixtures بدون اجرای تست.

### P12 — تکمیل Wiring، پوشش سناریو و آماده‌سازی تحویل
- \`VoiceBindings\`/Data/Playback ports را به هم وصل کن و کدهای مستقل فازها را در همان branch کامل کن؛ پنج فرمان Issue #10، Library، Full Player و Driving Mode محفوظ.
- unit fixtures برای normalization, exact/fuzzy/ties, thresholds, NotFound, revision, idempotency و integration fixtures برای Confirming→Executed/Failed، dispatch/ACK، route/lifecycle آماده کن؛ **فقط نوشتن**.
- ماتریس AC، چک‌لیست فارسی P13، ledger با وضعیت اولیه NOT_RUN و دستور build-only تدوین شود.
- **خروجی:** سورس کامل بدون کامپایل/Build، بدون اجرای test/CI/Lint و بدون APK در این فاز.

### P13 — تنها فاز نهایی: یک APK تجمعی و QA واقعی
1. یکجا بودن کدهای P00..P12 در شاخه را بررسی کن؛ سپس اولین و تنها build تجمعی APK Issue #11 (Android/Gradle یا GitHub Actions فقط برای ساخت APK) را بگیر.
2. APK نهایی و artifact قابل دانلود، SHA-256، commit و مسیر منبع را اعلام کن. هرگونه خطای build فقط در همین فاز اصلاح و بازساخت می‌شود؛ در هیچ فاز قبلی build وجود ندارد.
3. چک‌لیست M01..M48 برای آزمون روی گوشی مالک و T01..T10 برای پوشش تخصصی از پیش آماده به مالک بده؛ نتیجه ابتدا NOT_RUN است. اجرای دستی صرفاً توسط مالک؛ نتیجه PASS/FAIL/BLOCKED بر اساس گزارش واقعی.
4. در صورت توافق مالک، برای تست‌های فنی نیز **فقط در P13** اجرای JVM/Integration/Android/Privacy و ضبط نتیجه واقعی انجام شود؛ نتیجه NOT_RUN را PASS ننام.
5. آزمون آزمایشگاهی ۶ × ۱۰: پنج فرمان قبلی + PLAY_ARTIST، حداقل **54/60 = 90%** (معیار اجرایی پیشنهادی جهت کنترل کیفیت مستقیم). همچنین بروز اجرای اشتباه ناخواسته یک P0 است؛ زمان P95 پاسخ صوتی هدف <2.5s.
6. مشکل فعال #80 (Issue #10 برابر 48% در نمونه پیشین) تا رفع/تکرار واقعی در build جدید همچنان blocking quality debt است؛ وضعیت Issue #11/PR را با نتایج واقعی ثبت کن.
7. **فقط پس از گزارش تست مالک و ممیزی AC:** PR به \`main\`، Merge و Close. هیچ success/QA/closure پیشاپیش ادعا نشود.

## چک‌لیست اصلی گوشی در P13 — همه موارد NOT_RUN
آماده‌سازی: حداقل یک هنرمند با چند آهنگ، دو نام هنرمند نزدیک، فایل هنرمند ناموجود، فایل حذف/جابجا شده، و نسخه با مجوز موسیقی. از ثبت نام واقعی فایل‌های خصوصی در گزارش GitHub خودداری کنید.

### A — فرمان و تشخیص هنرمند (M01–M08)
- M01: \`Play songs by [artist]\` برای هنرمند موجود → همان artist.
- M02: چند آهنگ از هنرمند → صف شامل همه موارد مجاز در ترتیب پایدار.
- M03: هنرمند منحصر به فرد با اعتماد کافی → اجرا بدون صفحه/تأیید اضافی.
- M04: معادل تأییدشده دستور → همان رفتار.
- M05: prefix خالی/artist slot خالی → بدون اجرا.
- M06: عبارت unsupported (playlist/streaming) → بدون اجرا.
- M07: دستور تلفظ نامشخص → failure/retry ایمن؛ حدس ممنوع.
- M08: خروج از صفحه و بازگشت → وضعیت Player واقعی مطابق Media3.

### B — نام‌گذاری، fuzzy و تشابه (M09–M16)
- M09: تفاوت حروف بزرگ/کوچک → match صحیح.
- M10: فاصله اضافه/علائم پایانی → match صحیح.
- M11: apostrophe و نمایش Unicode نرمال‌شده → نتیجه قابل پیش‌بینی.
- M12: نام نزدیک با یک گزینه واضح → مطابق آستانه‌ها.
- M13: دو نام نزدیک → دقیقاً یک Confirming.
- M14: ترتیب انتخاب‌ها در تکرار با library snapshot یکسان → یکسان.
- M15: چندگزینه‌ای با تعداد زیاد → حداکثر دو گزینه ارائه شود؛ بدون حدس.
- M16: نام ناموجود → NotFound و حفظ پخش قبلی.

### C — تأیید یک‌مرحله‌ای (M17–M24)
- M17: انتخاب گزینه اول → تنها یک‌بار اجرا.
- M18: انتخاب گزینه دوم → تنها یک‌بار اجرا.
- M19: Neither/Cancel → حفظ صف/پخش.
- M20: چند لمس سریع Confirm → یک dispatch.
- M21: بازگشت با دکمه Back در Confirming → لغو، بدون تغییر پخش.
- M22: پاسخ/توکن stale → بدون اجرا.
- M23: پاسخ مبهم دوم → پایان امن، بدون پرسش دوم.
- M24: تغییر جهت/بازسازی UI حین Confirming → بدون اجرای مضاعف.

### D — Library/Playback Safety (M25–M32)
- M25: میکروفن مجاز ولی رسانه مجوز ندارد → پیام خطا و کنترل دستی.
- M26: لغو مجوز Media وسط Voice → بدون crash/اجرای نامعتبر.
- M27: حذف فایل بعد از scan → requery/skip و بدون فایل جعلی.
- M28: تغییر revision کتابخانه بین گزینه و Confirm → بدون انتخاب stale.
- M29: Service/MediaController disconnect → بدون موفقیت کاذب.
- M30: ACK موفق معتبر → نشان دادن state واقعی پخش.
- M31: ACK/queue خطادار → عدم گزارش موفقیت.
- M32: NotFound/Cancel/Failed → صف و پخش سابق دست‌نخورده.

### E — Full Player/Driving و lifecycle (M33–M40)
- M33: تأیید در Full Player → همان صفحه و کنترل صحیح.
- M34: تأیید در Driving → کنترل‌های بزرگ، بدون صفحه اضافه.
- M35: TalkBack نام گزینه‌ها/Cancel را اعلام کند.
- M36: لمس‌ها با target استاندارد و font بزرگ کار کنند.
- M37: خروج از Driving هنگام Confirm → safe cancel.
- M38: تماس/Audio Focus در Confirm → ایمن، duck صحیح.
- M39: قطع/وصل Bluetooth در Confirm → بدون اجرای ناآگاهانه.
- M40: Background/Process recreation → Voice خودکار شروع/بازاجرا نشود.

### F — سازگاری و حریم خصوصی (M41–M48)
- M41: Play/Pause/NEXT/PREVIOUS/SEEK_BACK قبلی همچنان کار کنند.
- M42: Phone PTT با یک لمس Mic؛ بدون wake word.
- M43: حداکثر یک retry تشخیص؛ Listening ≤6s.
- M44: No TTS/offline TTS unavailable → UI و playback معتبر.
- M45: پاسخ TTS کوتاه؛ نمایش نتیجه بی‌ابهام.
- M46: Manual fallback با یک لمس قابل استفاده بماند.
- M47: هیچ اطلاعات شخصی در لاگ/diagnostic/export آزمون ظاهر نشود.
- M48: قطع اینترنت → جست‌وجوی محلی و پخش و تصمیم ادامه پیدا کند.

### T01–T10 (فقط P13؛ تا اجرا NOT_RUN)
T01 normalization/grammar؛ T02 exact+fuzzy+history+tie؛ T03 threshold edges/null/NaN؛ T04 NotFound/Unsupported؛ T05 Room artist index/revision/permission؛ T06 queue+service ACK؛ T07 Confirming state transitions/one-step؛ T08 duplicate/lifecycle/race؛ T09 Compose accessibility/Driving؛ T10 privacy/telemetry + full five-command regression.

### L01–L06 (آزمایش ده‌تایی روی گوشی مالک)
L01 PLAY ×10؛ L02 PAUSE ×10؛ L03 NEXT ×10؛ L04 PREVIOUS ×10؛ L05 SEEK_BACK ×10؛ L06 PLAY_ARTIST ×10. نتیجه هر گروه عدد 0..10؛ مجموع از 60 و نرخ درصد؛ زمان/محیط قابل گزارش بدون ثبت transcript و نام فایل خصوصی. معیار پیشنهادی Gate یکپارچه ≥54/60 و **صفر اجرای اشتباه ناخواسته**. نتایج سابق #10 (24/50) فقط baseline هستند و به‌عنوان تست APK جدید شمارش نمی‌شوند.

## نقشه شواهد و پایان کار
- Acceptance Criteria اصلی #11: 13 مورد؛ تا گزارش واقعی P13 همگی unchecked/NOT_VERIFIED باقی می‌مانند.
- برای هر فاز: فایل‌های تغییرکرده، commit، نتیجه «source delivered» و **tests NOT_RUN / apk NOT_BUILT** در GitHub ثبت شود؛ ادعای PASS اجرایی ممنوع.
- پس از P13، مالک نتیجه M/T/L را ارسال می‌کند؛ Ledger واقعی به Issue و PR پیوند داده می‌شود؛ Merge/Close تنها با دستور مالک و ممیزی مستقل معیارها.
- #80 را با موفقیت فرضی Issue #11 نبند؛ اگر 90% Gate شکست خورد، صریحاً fail/blocked ثبت شود.
