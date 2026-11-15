"""Issue #12 P11: static, printable three-panel aggregate dashboard.

HTML has no JavaScript, images, fonts, tracking, network requests or external
assets. All text interpolated into the output is HTML-escaped.
Source authored; NOT executed, built or tested before P14.
"""
from __future__ import annotations
from html import escape


def esc(value):
    return escape(str(value), quote=True)


def num(value):
    return f"{value:,}"


def label_value(value):
    if value is None:
        return "N/A"
    return f"{value:.3f}".rstrip("0").rstrip(".")


def metric_display(item):
    if item["unit"] == "percent":
        return f'{label_value(item["lower"])}% [lower]' if item["lower"] is not None else "N/A"
    if item["unit"] == "incidents_per_100":
        return f'{label_value(item["observed_value"])} incidents/100' if item["observed_value"] is not None else "N/A"
    return f'below cutoff: {num(item["numerator"])}/{num(item["denominator"])}'


def metric_target(item):
    sign = "&#8805;" if item["direction"] == "AT_LEAST" else "&lt;"
    suffix = "ms (nearest-rank)" if item["unit"] == "p95_threshold" else (
        "incidents / 100 starts" if item["unit"] == "incidents_per_100" else "%"
    )
    return f'{sign} {esc(item["target"])} {suffix}'


def metric_row(item):
    s = item["status"]
    return (
        "<tr>"
        f'<td><strong>{esc(item["id"])}</strong> {esc(item["title"])}</td>'
        f'<td>{esc(item["window"])}</td>'
        f'<td class="mono">{num(item["numerator"])} / {num(item["denominator"])}</td>'
        f'<td>{esc(metric_display(item))}</td>'
        f'<td>{metric_target(item)}</td>'
        f'<td class="mono">{num(item["unknown"])}</td>'
        f'<td><span class="state {esc(s.lower().replace("_","-"))}">{esc(s)}</span></td>'
        "</tr>"
    )


def section_rows(report, section):
    rows = [i for i in report["all_metrics"] if i["section"] == section]
    # Preserve W1->W2 and source metric ordering, not alphabetical.
    return "\n".join(metric_row(i) for i in rows)


def table(report, section):
    return (
        '<div class="tablewrap"><table><thead><tr>'
        '<th>Metric</th><th>Window</th><th>Verified N / eligible D</th>'
        '<th>Observed (not proof)</th><th>Locked threshold</th>'
        '<th>UNKNOWN</th><th>Evidence</th></tr></thead><tbody>'
        + section_rows(report, section)
        + '</tbody></table></div>'
    )


def summary_metric(label, value, qualifier=""):
    return (
        '<div class="mini"><div class="mini-label">' + esc(label) + '</div>'
        '<div class="mini-value">' + esc(value) + '</div>'
        '<div class="mini-detail">' + esc(qualifier) + '</div></div>'
    )


def summary_panel(title, subtitle, content):
    return ('<section class="summary-panel"><h2>' + esc(title) + '</h2>'
            '<p class="muted">' + esc(subtitle) + '</p>'
            '<div class="mini-grid">' + content + '</div></section>')


def evidence_rows(report):
    rows = []
    for name, val in report["coverage"].items():
        rows.append('<tr><td>' + esc(name) + '</td><td class="mono">'
                    + num(val) + '</td></tr>')
    for name, val in report["review"].items():
        rows.append('<tr><td>approval.' + esc(name) + '</td><td>'
                    + ('Operator-declared YES' if val else 'NOT APPROVED') + '</td></tr>')
    rows.append('<tr><td>Beta protocol evidence</td><td>'
                + ('Operator-declared ready' if report["beta_protocol_ready"] else 'NOT VERIFIED')
                + '</td></tr>')
    return "\n".join(rows)


def render_html(report):
    kind = report["dataset_kind"]
    synthetic = report["synthetic"]
    w1, w2 = report["weeks"]
    cohort = report["cohort"]
    decision = report["decision"]
    demo = (
        '<div class="demo">SYNTHETIC DEMONSTRATION ONLY — ZERO REAL BETA EVIDENCE. '
        'Every number below is an invented fixture, not an actual measurement.</div>'
        if synthetic else
        '<div class="caution">CONTROLLED BETA INPUT — assertions and review flags were '
        'provided by an operator; independently verify the QA audit trail before '
        'any Product, Privacy or release decision.</div>'
    )
    used = (
        summary_metric("Actual enrolled / consented (operator-supplied)",
                       f'{num(cohort["qualified_enrolled"])} / {num(cohort["consented"])}')
        + summary_metric("Verified Driving starts W1 / W2",
                         f'{num(w1["sessions"]["started"])} / {num(w2["sessions"]["started"])}')
        + summary_metric("Voice-linked Driving sessions W1 / W2",
                         f'{num(w1["sessions"]["voice_driving_sessions"])} / '
                         f'{num(w2["sessions"]["voice_driving_sessions"])}',
                         "Observational only, not a proven correct Voice intent")
        + summary_metric("Distinct week-active people W1 / W2",
                         f'{num(cohort["week1_active"])} / {num(cohort["week2_active"])}',
                         "Separate roster-backed aggregate required")
    )
    work_status = sum(1 for m in report["all_metrics"]
                      if m["section"] == "WORKS" and m["status"] == "PASS")
    work_total = sum(1 for m in report["all_metrics"] if m["section"] == "WORKS")
    works = (
        summary_metric("Verified WORKS targets", f"{work_status} / {work_total}",
                       "NOT_VERIFIED must never count as PASS")
        + summary_metric("Unresolved coverage losses",
                         num(sum(report["coverage"][k] for k in
                                 ("queue_drops", "ring_evictions",
                                  "missing_participant_reports",
                                  "unreconciled_attempts", "invalid_unresolved"))),
                         "Losses must be reconciled, never silently removed")
        + summary_metric("Unknown started journeys W1 / W2",
                         f'{num(w1["sessions"]["unknown"])} / '
                         f'{num(w2["sessions"]["unknown"])}',
                         "Incomplete/process deaths are not crash-free")
        + summary_metric("P01 voice timing provenance", "UNVERIFIED PROXY"
                         if any(m["id"] == "P01" and m["status"] != "PASS"
                                for m in report["all_metrics"]) else "OPERATOR-VERIFIED",
                         "Physical speech-end and first perceivable feedback required")
    )
    r01 = next(m for m in report["cross_week"] if m["id"] == "R01")
    r02 = next(m for m in report["cross_week"] if m["id"] == "R02")
    returns = (
        summary_metric("W1 -> W2 return (R01)", metric_display(r01),
                       f'Count {num(r01["numerator"])}/{num(r01["denominator"])}; '
                       f'UNKNOWN {num(r01["unknown"])}')
        + summary_metric("Driving reuse (R02)", metric_display(r02),
                         f'Count {num(r02["numerator"])}/{num(r02["denominator"])}; '
                         f'UNKNOWN {num(r02["unknown"])}')
        + summary_metric("Vehicle/head-unit models (declared)",
                         num(cohort["distinct_vehicle_models"]),
                         "Minimum three for the actual controlled Beta")
        + summary_metric("Actual days observed (declared)",
                         num(cohort["days_observed"]),
                         "Fourteen full days required for Beta protocol")
    )
    window = (f'{esc(w1["start_utc"])} inclusive to '
              f'{esc(w2["end_utc"])} exclusive')
    blocker = (", ".join(esc(b) for b in report["open_blockers"])
               if report["open_blockers"] else "No blockers declared")
    stylesheet = """
:root{color-scheme:light;font-family:system-ui,-apple-system,Segoe UI,sans-serif;
 color:#202b42;background:#f5f7fb}
*{box-sizing:border-box}body{margin:0 auto;max-width:1440px;padding:24px}
header{background:#142c4d;color:white;padding:28px;border-radius:15px}
header h1{font-size:25px;margin:0 0 9px}header p{opacity:.9;margin:5px 0}
h2{font-size:17px;margin:0 0 5px}h3{font-size:16px;margin:10px 0}
p{line-height:1.5}.muted{color:#617188;font-size:13px}
header .muted{color:#e0e8f4}
.demo,.caution{padding:17px 20px;margin:16px 0;border-radius:10px;
 font-weight:750;border:2px solid #a3542e;background:#fff0e1;color:#702910}
.caution{font-weight:550;border-color:#b28b42;background:#fff8e8}
.deck{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px}
.summary-panel,.paper{background:#fff;border:1px solid #e2e7ef;border-radius:12px;
 padding:20px;margin:16px 0;box-shadow:0 1px 4px #dce2ed5c}
.mini-grid{display:grid;gap:10px;margin-top:14px}
.mini{padding:10px 12px;background:#f6f8fc;border-radius:8px}
.mini-label{font-size:12px;color:#55647d}.mini-value{font-size:23px;font-weight:750}
.mini-detail{font-size:11px;color:#6c7b90}.pill{display:inline-block;padding:7px 13px;
 font-weight:750;border-radius:8px;background:#e7ecf5;color:#1e3b5d}
.tablewrap{overflow-x:auto}table{border-collapse:collapse;width:100%;font-size:12.5px}
th,td{padding:11px;text-align:left;vertical-align:top;border-bottom:1px solid #e3e8ee}
th{font-size:11px;letter-spacing:.015em;color:#42546c;background:#edf1f7}
.mono{font-variant-numeric:tabular-nums;white-space:nowrap}
.state{font-size:10px;font-weight:700;display:inline-block;line-height:1.4;
 border:1px solid #c7ced8;background:#f0f2f5;color:#465166;
 padding:3px 7px;border-radius:7px}
.state.pass{background:#e1f3e8;color:#11603a;border-color:#9fc9af}
.state.fail{background:#fff0e9;color:#9c3527;border-color:#ecc0b5}
.flag{border-top:2px solid #dbdfe7;margin-top:12px;padding-top:13px}
footer{color:#5c6980;font-size:12px;margin:24px 0}
@media(max-width:1020px){.deck{grid-template-columns:1fr}}
@media print{body{padding:8px;background:#fff}header{color:#111;background:#e8eef5}
 .summary-panel,.paper{box-shadow:none;break-inside:avoid}
 .deck{display:block}.tablewrap{overflow:visible}.demo{border:2px solid #555}}
"""
    html = [
        "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">",
        '<meta name="viewport" content="width=device-width,initial-scale=1">',
        '<meta http-equiv="Content-Security-Policy" content="default-src &#39;none&#39;; '
        'style-src &#39;unsafe-inline&#39;; img-src &#39;none&#39;; '
        'base-uri &#39;none&#39;; form-action &#39;none&#39;">',
        "<title>Music Copilot — Issue 12 Weekly Evidence Dashboard</title>",
        "<style>", stylesheet, "</style></head><body>",
        "<header><h1>Music Copilot — Weekly Measurement Dashboard v1.0</h1>",
        "<p>Three product questions: IS IT USED? DOES IT WORK CORRECTLY? DOES THE USER RETURN?</p>",
        f'<p class="muted">UTC pilot: {window} &nbsp;|&nbsp; '
        f'Source: {esc(report["source"])} &nbsp;|&nbsp; '
        f'Classification: {esc(kind)}</p></header>',
        demo,
        '<div class="paper"><h2>Decision evidence — NOT an automatic Product approval</h2>',
        '<p class="pill">' + esc(decision) + '</p>',
        '<p class="muted">Thresholds are locked PRD/MVP values. Individual PASS only if '
        'complete and operator-declared QA/Privacy/Product evidence is available. '
        'Unknown data, synthetic inputs, queue drops or calibration gaps do not go Green.</p>',
        '<p><strong>Open blockers:</strong> ' + blocker + '</p>',
        '<p class="muted">' + esc(report["operator_claim_notice"]) + '</p></div>',
        '<div class="deck">',
        summary_panel("USED", "Sessions · adoption · weekly North Star", used),
        summary_panel("WORKS", "Correctness · correction · P95 · crash/player", works),
        summary_panel("RETURNS", "Weekly retention · actual Driving reuse", returns),
        '</div>',
        '<div class="paper"><h2>USED — Is it used?</h2>',
        '<p class="muted">NS01 counts DISTINCT WEEK-ACTIVE PEOPLE, not sessions. '
        'Voice sessions are contextual usage only, not verified correct commands.</p>',
        table(report, "USED"), '</div>',
        '<div class="paper"><h2>WORKS — Does it work correctly?</h2>',
        '<p class="muted">Q01 requires intended-vs-actual QA proof. P01 strictly '
        '&lt;2500ms from PHYSICAL SPEECH END to PERCEIVABLE response; '
        'Silero VAD&#8594;Compose draw is an uncalibrated proxy. '
        'P02 strictly &lt;1000ms. Incident rate is not a percent.</p>',
        table(report, "WORKS"), '</div>',
        '<div class="paper"><h2>RETURNS — Does the user return?</h2>',
        '<p class="muted">R01 denominator = W1-active people; '
        'R02 denominator = ALL enrolled Beta participants. '
        'W2 is not interchangeable with W1, and sessions are not people.</p>',
        table(report, "RETURNS"), '</div>',
        '<div class="paper"><h2>Coverage, consent & provenance</h2>',
        '<p class="muted">Zero counters require real evidence; missing data '
        'is not proof of error-free operation. Dataset review booleans were '
        'supplied by an operator, never authenticated by this script.</p>',
        '<div class="tablewrap"><table><thead><tr><th>Control</th><th>Declared value</th>'
        '</tr></thead><tbody>', evidence_rows(report), '</tbody></table></div>',
        '<h3>Source v1.0 event families — observational only</h3>',
        '<div class="tablewrap"><table><thead><tr><th>Event</th><th>W1</th><th>W2</th>'
        '</tr></thead><tbody>',
    ]
    for family in w1["observed_events"]:
        html.append("<tr><td>" + esc(family) + "</td><td class=\"mono\">" +
                    num(w1["observed_events"][family]) + "</td><td class=\"mono\">" +
                    num(w2["observed_events"][family]) + "</td></tr>")
    html.extend([
        "</tbody></table></div>",
        '<p class="muted flag">Source denominator, not eight event-family counts, '
        'defines correctness and unique retention. Source queue drop and '
        'RAM eviction invalidate any automatically complete dashboard.</p></div>',
        '<footer>Issue #12 / P11 — Product/QA internal aggregate review only. '
        'No app analytics SDK, network, tracking, session ID, private music, '
        'transcript, location or external upload. All report values come '
        'from the provided aggregate JSON; verify with its owner.</footer>',
        "</body></html>",
    ])
    return "".join(html)
