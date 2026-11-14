# Issue #11 — P12/P13 چک‌لیست نهایی آزمون گوشی مالک

**تاریخ تنظیم:** ۲۰۲۶-۱۱-۱۴ | **شاخه:** `feature/11-play-artist-one-step-ambiguity` | **وضعیت:** همهٔ آزمایش‌ها `NOT_RUN`.

**قانون پروژه:** این سند در P12 نوشته شده است؛ هیچ تست، کامپایل، Lint، CI یا APK در P12 اجرا/ساخته نشده است. اجرای عملی گوشی **فقط در P13 توسط مالک** است؛ نتیجهٔ PASS/FAIL/BLOCKED صرفاً پس از گزارش واقعی ثبت می‌شود.

## پیش‌نیاز گوشی و دادهٔ آزمایش

- APK تجمعی P13 با SHA-256 و شناسهٔ commit مشخص؛ Android 10+؛ تست اصلی روی گوشی واقعی مالک و هندزفری/Bluetooth واقعی.
- کتابخانهٔ محلی با یک هنرمند دارای چند آهنگ، دو هنرمند نزدیک، سه هنرمند بسیار مشابه، نام وجودنداشته، نام Unicode/apostrophe، یک آهنگ قابل حذف/انتقال، و فایل‌های بدون metadata هنرمند. **نام واقعی فایل‌ها و هنرمندان خصوصی را به GitHub نفرستید.**
- مجوزهای Music/Media و RECORD_AUDIO را مستقل بررسی کنید؛ Bluetooth، TalkBack، ۲۰۰٪ font، تغییر focus و بازگشت از Settings نیز لازم‌اند.
- **Moonshine فعلی Confidence کالیبره‌شده نمی‌دهد (`null`).** بنابراین هنرمند واضح نیز ممکن است به یک Confirming برسد؛ این اجرای مستقیم (M03) را روی همین provider قابل تأیید نمی‌کند. وضعیت واقعی را `BLOCKED` یا `FAIL` متناسب با معیار ثبت کنید، نه `PASS` فرضی.
- **M04 فقط شامل عبارت `musico play songs by [artist]` پس از لمس Mic است؛** Voice Contract هیچ مترادف جدیدی مثل `play music by` را تأیید نکرده است.

## ۴۸ تست دستی گوشی — همه NOT_RUN

### A — فرمان صوتی و تشخیص هنرمند

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M01 | پس از لمس Mic، بگویید Play songs by [artist موجود] | هنرمند درست شناسایی شود؛ با Confidence ناموجود تأیید یک‌مرحله‌ای نمایش داده شود؛ تا قبل از انتخاب پخش تازه‌ای شروع نشود. | `NOT_RUN` |
| M02 | هنرمندی را انتخاب کنید که چند آهنگ محلی دارد | پس از تأیید و ACK، صف شامل همهٔ موارد مجاز و موجود با ترتیب پایدار باشد؛ آهنگ حذف‌شده وارد نشود. | `NOT_RUN` |
| M03 | هنرمند منحصربه‌فرد را با STT دارای Confidence واقعی و بالای آستانه بررسی کنید | فقط در صورت Confidence معتبر/کافی مسیر Direct بدون صفحهٔ تأیید اجرا شود؛ Moonshine فعلی Confidence=null است، پس با این Provider نتیجه ممکن است BLOCKED باشد. | `NOT_RUN` |
| M04 | پس از لمس Mic، عبارت musico play songs by [artist] را امتحان کنید | پیشوند اختیاری پس از لمس Mic مجاز باشد؛ این Wake Word نیست؛ هیچ مترادف دیگری بدون تأیید Voice Contract فرض نشود. | `NOT_RUN` |
| M05 | فرمان بدون artist یا با slot خالی بگویید | بدون dispatch؛ خطای امن و بازگشت به کنترل دستی. | `NOT_RUN` |
| M06 | درخواست Play songs by [artist] on Spotify یا پخش Playlist بگویید | درخواست خارج از دامنه اجرا یا به هنرمند محلیِ حدسی تبدیل نشود. | `NOT_RUN` |
| M07 | نام نامفهوم/تلفظ مبهم بگویید | تشخیص نامطمئن بدون حدس و اجرای ناخواسته؛ حداکثر یک Retry مربوط به STT. | `NOT_RUN` |
| M08 | بعد از شروع پخش هنرمند از Full Player خارج شوید و برگردید | State/Queue واقعی Media3 و استمرار پخش تحت مالکیت سرویس حفظ شود. | `NOT_RUN` |

### B — Normalization، fuzzy و ابهام

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M09 | نام هنرمند با تغییر بزرگ/کوچک بودن حروف بگویید | همان هویت محلی و نتیجهٔ سازگار با Confidence. | `NOT_RUN` |
| M10 | در نام/فرمان فاصلهٔ اضافه یا علامت پایانی به کار ببرید | نرمال‌سازی ثابت بدون ایجاد نام ساختگی. | `NOT_RUN` |
| M11 | هنرمندی با Apostrophe، Unicode یا علامت معنادار انتخاب کنید | قواعد NFKC و تطبیق دوطرفه نتیجهٔ تعیین‌پذیر بدهند؛ با نام‌های حقیقی خصوصی فقط روی گوشی بررسی کنید. | `NOT_RUN` |
| M12 | نام مشابه با یک کاندیدای کاملاً برتر را بگویید | امتیاز versioned و threshold رعایت شود؛ Direct فقط با Confidence واقعی مناسب. | `NOT_RUN` |
| M13 | دو هنرمند با نام‌های نزدیک آماده و فرمان مبهم بگویید | تنها یک پنل Confirming با حداکثر دو گزینه و یک پاسخ مجاز. | `NOT_RUN` |
| M14 | فرمان یکسان را با Snapshot کتابخانهٔ بدون تغییر تکرار کنید | ترتیب گزینه‌های ممکن مستقل از ترتیب SQL و پایدار باشد. | `NOT_RUN` |
| M15 | سه یا بیشتر نام هنرمند plausibly مشابه آماده کنید | هرگز سه گزینه یا انتخاب حدسی/حذف پنهان کاندیدا؛ در حالت سه plausible مسیر failure/manual امن. | `NOT_RUN` |
| M16 | نام هنرمند ناموجود را بگویید | NotFound دقیق و حفظ صف و آهنگ قبلی؛ تنها برای Snapshot کامل و دارای مجوز. | `NOT_RUN` |

### C — Confirming یک‌مرحله‌ای و One-shot

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M17 | در Confirming گزینهٔ اول را یک‌بار بزنید | حداکثر یک ReplaceQueue و پس از ACK معتبر نتیجهٔ Executed. | `NOT_RUN` |
| M18 | برای نمونهٔ دوگزینه‌ای گزینهٔ دوم را بزنید | دقیقاً همان هنرمند دوم و فقط یک dispatch. | `NOT_RUN` |
| M19 | در پنل انتخاب، Neither یا Cancel را بزنید | صف قبلی دست‌نخورده؛ بدون فرمان پخش تازه، سؤال دوم یا ضبط خودکار. | `NOT_RUN` |
| M20 | روی گزینهٔ Confirm چند بار سریع لمس کنید | تنها اولین انتخاب معتبر پذیرفته شود؛ یک commandId/ACK، نه چند dispatch. | `NOT_RUN` |
| M21 | در Confirming دکمهٔ Back/خروج را بزنید | توکن منقضی و بازگشت به دستی؛ موسیقی جاری متوقف نشود. | `NOT_RUN` |
| M22 | پس از Cancel/خروج، پاسخ یا توکن قدیمی را از همان پنل امتحان کنید | بدون execution یا احیای نشست؛ رد fail-closed. | `NOT_RUN` |
| M23 | پاسخ همچنان مبهم/Neither بدهید | سؤال دوم و نوبت دوم STT برای همان ابهام انجام نشود. | `NOT_RUN` |
| M24 | حین Confirming صفحه را بچرخانید یا Activity را بازسازی کنید | توکن/نشست از Bundle بازگردانی نشود؛ Play دوگانه رخ ندهد. | `NOT_RUN` |

### D — کتابخانه، مجوز، صف و ACK

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M25 | میکروفن مجاز و مجوز Media denied باشد | بدون Artist Query معتبر و بدون dispatch؛ بازگشت به کنترل دستی. | `NOT_RUN` |
| M26 | حین STT/Confirming مجوز Media را در تنظیمات بردارید | رد شدن نشست/رفرنس stale و عدم crash یا اجرای فایل بدون مجوز. | `NOT_RUN` |
| M27 | فایل عضو گروه هنرمند را پس از Scan حذف یا جابه‌جا کنید | قبل از dispatch دوباره MediaStore re-query؛ فایل گمشده حذف یا failure امن، نه صف جعلی. | `NOT_RUN` |
| M28 | بین نمایش گزینه و Confirm کتابخانه را rescan کنید یا Revision را تغییر دهید | انتخاب قدیمی رد شود؛ نیازمند فرمان تازه؛ صف قبلی حفظ شود. | `NOT_RUN` |
| M29 | وقتی MediaController قطع/ناموجود است PLAY_ARTIST را امتحان کنید | نتیجهٔ موفقیت کاذب یا «Playing» صادر نشود. | `NOT_RUN` |
| M30 | فرمان موفق را از Queue/Notification/Full Player بررسی کنید | Media3 ACK معتبر و Snapshot تازه مشاهده شود؛ Buffering ACK لزوماً صدای قابل‌شنیدن را تضمین نمی‌کند. | `NOT_RUN` |
| M31 | خطای سرویس/ACK/queue را با شرایط ایمن و قابل‌بازگشت ایجاد کنید | عدم ادعای موفقیت؛ کد خطای مشخص و کنترل دستی. | `NOT_RUN` |
| M32 | NotFound یا Cancel را پیش از ارسال فرمان اجرا کنید | صف/موسیقی قبلی تغییر نکند؛ اگر فرمان از قبل توسط سرویس پذیرفته شده لغو قابل rollback نیست و جداگانه بررسی شود. | `NOT_RUN` |

### E — Full Player، Driving و Lifecycle

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M33 | در Full Player یکی از گزینه‌های هنرمند را انتخاب کنید | باقی ماندن در همان صفحه، کنترل دستی و state واقعی. | `NOT_RUN` |
| M34 | همان سناریو را در Driving Mode اجرا کنید | کنترل‌های بزرگ و یک پنل انتخاب بدون مسیر یا Player تازه. | `NOT_RUN` |
| M35 | TalkBack را در پنل Confirming فعال کنید | نام واقعی گزینه‌ها فقط روی دستگاه، ترتیب گزینه و Cancel/Neither قابل خواندن باشد؛ نام‌ها در گزارش GitHub منتشر نشوند. | `NOT_RUN` |
| M36 | اندازهٔ فونت را ۲۰۰٪ و حالت دسترسی را تغییر دهید | گزینه‌ها از صفحه بیرون نزنند؛ هدف لمس Player حداقل ۴۸dp و Driving مطابق طراحی. | `NOT_RUN` |
| M37 | در Confirming از Driving خارج شوید | توکن و TTS لغو؛ پخش جاری طبق سرویس ادامه/متوقف شود، بدون اجرای ناخواسته. | `NOT_RUN` |
| M38 | حین STT/Confirming تماس واقعی یا از دست رفتن Audio Focus رخ دهد | Voice/TTS/duck ایمن لغو و Media3 طبق سیاست service-owned pause/recovery رفتار کند. | `NOT_RUN` |
| M39 | حین Confirming AirPods/Bluetooth را قطع و وصل کنید | قطع مسیر بدون leakage به اسپیکر و reconnect بدون پخش خودکار؛ Play دستی در مسیر درست. | `NOT_RUN` |
| M40 | Home/lock/rotation و relaunch پس از process death را جدا آزمایش کنید | Voice/توکن/ضبط خودکار از سر گرفته نشود؛ MediaSessionService مستقل از UI بماند. | `NOT_RUN` |

### F — رگرسیون پنج فرمان و حریم خصوصی

| شناسه | روش اجرا | انتظار دقیق | وضعیت |
|---|---|---|---|
| M41 | پنج فرمان Play, Pause, Next, Previous, Seek Back را جدا بررسی کنید | همه از Mapper قبلی با قواعد فعلی کار کنند؛ تغییر ناخواسته نباشد. | `NOT_RUN` |
| M42 | بدون لمس Mic، فرمان یا Musico را بگویید؛ سپس با لمس Mic امتحان کنید | هیچ Wake Word یا ضبط پیش‌زمینه/پس‌زمینه بدون لمس صریح؛ یک tap شروع کند. | `NOT_RUN` |
| M43 | شنیدن بلندمدت و تشخیص ناموفق را بررسی کنید | حداکثر ۶ ثانیه Listening و نهایتاً یک Retry برای STT؛ بدون retry مکالمه‌ای ابهام. | `NOT_RUN` |
| M44 | TTS محلی را غیرفعال/غیرقابل‌دسترس کنید | Confirming/Manual/Media3 معتبر باقی بماند؛ عدم TTS به false-success یا توقف موسیقی منجر نشود. | `NOT_RUN` |
| M45 | متن/پاسخ صوتی ArtistPlaying، Confirm و NotFound را گوش کنید | حداکثر یک جمله در هر پاسخ و «Playing» فقط پس از ACK؛ سؤال و پاسخ صوتی روی هم نیفتند. | `NOT_RUN` |
| M46 | پس از NotFound/Unsupported/Failed به کنترل دستی بازگردید | با یک لمس بدون مجوز/پنل اجباری اضافی قابل‌استفاده باشد. | `NOT_RUN` |
| M47 | Logcat و تشخیص‌های برنامه را محلی بررسی کنید | هیچ transcript، نام خصوصی هنرمند/آهنگ، URI/ID، token یا path در Telemetry/Crash/Export نشت نکند؛ شواهد خصوصی را در GitHub آپلود نکنید. | `NOT_RUN` |
| M48 | بدون اتصال اینترنت و با فایل‌های آفلاین تست کنید | Library محلی، فرمان و پخش از شبکه وابسته نباشند؛ نصب APK و مدل از پیش انجام شده باشد. | `NOT_RUN` |

## ۱۰ دستهٔ تست تخصصی — تأیید فنی فقط در P13

| شناسه | موضوع و دامنه | نتیجه |
|---|---|---|
| T01 | Parser/Grammar: case, apostrophe, Unicode, punctuation, optional musico, malformed, unsupported, 5-control unchanged | `NOT_RUN` |
| T02 | Normalization, exact/fuzzy ranks, deterministic ordering, history tie-break only under approved local data, 3+ plausible fail closed | `NOT_RUN` |
| T03 | Confidence thresholds: calibrated/null/NaN/invalid, P05 Direct vs Confirm, BuildConfig bounds, Moonshine null | `NOT_RUN` |
| T04 | NotFound/Unsupported and empty/incomplete indexes; source return + no playback mutation | `NOT_RUN` |
| T05 | Room transaction/revision/accessEpoch/media permission; paging up to 5000; MediaStore live requery | `NOT_RUN` |
| T06 | PlaybackQueuePolicy, bounded media refs, service ACK correlation/queueRevision, stale controller and route | `NOT_RUN` |
| T07 | Confirming→Executed/Failed, Neither/Cancel/timeout, token single-use, one speech prompt | `NOT_RUN` |
| T08 | STT/TTS/Media3 delayed callbacks, lifecycle/permission/route/focus interruptions, duplicate taps and stale ACK | `NOT_RUN` |
| T09 | Compose UI/TalkBack/large font and touch targets in Full Player and Driving with same coordinator | `NOT_RUN` |
| T10 | Privacy gate/noop sink, optional event failure, logging audit and all five original command regression | `NOT_RUN` |

در P13 انجام تست‌های فنی فقط پس از موافقت مالک مجاز است. تست‌های فعلی Source-authored هستند؛ بدون اجرای واقعی، خروجی آن‌ها «PASS» نیست.

## آزمایش شش‌فرمانی ۶×۱۰ روی گوشی مالک

| شناسه | فرمان | تلاش‌ها | موفق صحیح | اجرای اشتباه ناخواسته | وضعیت |
|---|---|---:|---:|---:|---|
| L01 | PLAY | 10 | — | — | `NOT_RUN` |
| L02 | PAUSE | 10 | — | — | `NOT_RUN` |
| L03 | NEXT | 10 | — | — | `NOT_RUN` |
| L04 | PREVIOUS | 10 | — | — | `NOT_RUN` |
| L05 | SEEK_BACK | 10 | — | — | `NOT_RUN` |
| L06 | PLAY_ARTIST | 10 | — | — | `NOT_RUN` |

**ملاک پیشنهادی Issue #11:** مجموع موفقیت درست **حداقل ۵۴ از ۶۰ (۹۰٪)** و **صفر اجرای ناخواستهٔ فرمان اشتباه**. `PLAY_ARTIST` فقط وقتی موفق است که هنرمند درست از Library محلی پخش شود؛ اگر تأیید یک‌مرحله‌ای مجاز به‌درستی لازم بوده، کیفیت همان مسیر را جدا از «پخش خودکار» ثبت کنید. هر فرمان ۱۰ بار، ناموفق‌ها هم شمرده شوند؛ نمونه‌گیری نباید صرفاً از تلاش‌های موفق باشد.

**زمان پاسخ:** P95 فرمان صوتی هدف **کمتر از ۲٫۵ ثانیه** است. روش اندازه‌گیری و کندی ناشی از مدل/اولین اجرای برنامه را همراه جمع‌بندی اعلام کنید، ولی transcript و عنوان فایل را ثبت نکنید. فراخوانی احتمالی service ACK در حالت Buffering الزاماً صدای شنیدنی را تضمین نمی‌کند.

**بدهی کیفی باز:** Issue #80 مستقل از کد P12 باز است؛ نسبت گزارش‌شدهٔ قبلی Issue #10 برابر ۲۴/۵۰ (۴۸٪) مبنای تاریخی است و جایگزین شش‌فرمانی P13 نمی‌شود.

## فرمت گزارش مالک برای هر تست

`ID | PASS/FAIL/BLOCKED | نسخه/Commit APK | توضیح غیرشخصی و مختصر`

برای آزمایش‌های شش‌فرمانی: `L01..L06 | تعداد صحیح از ۱۰ | تعداد پخش اشتباه ناخواسته | تعداد retry/Confirm | توضیح کلی`. از فرستادن صدای خام، متن صحبت، مسیر فایل، نام هنرمند واقعی/خصوصی، شماره تماس و شناسهٔ Bluetooth پرهیز شود.

## قواعد تصمیم نهایی

- `NOT_RUN` یعنی اجرا نشده؛ `BLOCKED` یعنی اجرا ممکن نبوده (مثلاً عدم Confidence معتبر در M03). هیچ کدام `PASS` نیستند.
- هر اجرای اشتباه ناخواسته یا نشت صدا به اسپیکر هنگام قطع مسیر، **P0/STOP** است.
- بعد از جمع‌آوری شواهد مالک، ماتریس پذیرش AC01..AC13 جداگانه ارزیابی می‌شود؛ P13 باید compile و installability واقعی را نیز تأیید کند.
- PR، Merge و Close Issue #11 **فقط پس از تست‌های واقعی و تأیید صریح مالک**؛ رفع Issue #80 به‌طور خودکار از این فاز استنباط نشود.

## خروجی Build مختص P13، نه P12

Workflow از قبل در شاخهٔ ویژگی آماده شده: `.github/workflows/issue-11-p13-one-cumulative-apk.yml`. فقط **ساخت/به‌روزرسانی** فایل `.github/issue11-p13-apk-request.txt` در **P13** (بدون `[skip ci]` برای درخواست) آن را فعال می‌کند. Build فقط `:app:assembleDebug` است و فایل‌های محلی Moonshine/Silero/Sherpa را پیش از assemble تأمین می‌کند. Artifact باید APK + SHA-256 + source SHA و منشأ فایل‌های صوتی داشته باشد. هیچ Marker در P12 ایجاد نشده است.
