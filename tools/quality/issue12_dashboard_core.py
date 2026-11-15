"""Issue #12 P11: strict, locally run, aggregate-only dashboard model.

Source authored, NOT executed or compiled. Input is NON-IDENTIFYING QA/Beta
aggregate evidence. Operator-supplied review flags are claims, not signatures.
No online services, stable IDs, raw voice, media metadata or event-level rows.
"""

from __future__ import annotations

from datetime import datetime, timedelta, timezone

SCHEMA = "music-copilot-weekly-aggregate-v1"
VERIFIED_SOURCES = frozenset({
    "QA_VERIFIED_AGGREGATE", "CONTROLLED_BETA_VERIFIED_AGGREGATE",
})
SOURCES = VERIFIED_SOURCES | frozenset({
    "SYNTHETIC_FIXTURE", "OBSERVED_EVENTS_ONLY", "NOT_PROVIDED",
})
EVENTS = (
    "driving_mode_started", "voice_button_pressed", "intent_detected",
    "intent_failed", "command_executed", "manual_correction",
    "player_error", "session_completed",
)
MAX_COUNT = 2**63 - 1
RATIO_SPECS = {
    "NS01": ("Successful Driving + Voice among weekly actives", 50, "AT_LEAST", "USED"),
    "Q01": ("Verified direct-command success", 85, "AT_LEAST", "WORKS"),
    "F01": ("Attributed manual correction", 15, "LESS_THAN", "WORKS"),
    "G01": ("Verified incorrect execution", 3, "LESS_THAN", "WORKS"),
    "S01": ("Verified crash-free started journeys", 99, "AT_LEAST", "WORKS"),
    "R01": ("W1-active participants returning in W2", 30, "AT_LEAST", "RETURNS"),
    "R02": ("Driving reuse among enrolled participants", 50, "AT_LEAST", "RETURNS"),
}
WEEK_IDS = ("NS01", "Q01", "F01", "G01", "S01")
CROSS_IDS = ("R01", "R02")
LATENCY_IDS = ("P01", "P02")
LATENCY_CONFIG = {
    "P01": ("Voice speech-end to perceivable terminal response P95", 2500,
            "VERIFIED_ACOUSTIC_TO_PERCEIVABLE"),
    "P02": ("Local accepted input to verified Media3 ACK P95", 1000,
            "VERIFIED_ALL_ELIGIBLE_LOCAL_ACK"),
}
ALLOWED_LATENCY_BOUNDARIES = {
    "P01": {"VERIFIED_ACOUSTIC_TO_PERCEIVABLE", "PROXY_VAD_TO_COMPOSE_DRAW",
            "PROXY_ANDROID_END_TO_COMPOSE_DRAW", "UNAVAILABLE"},
    "P02": {"VERIFIED_ALL_ELIGIBLE_LOCAL_ACK", "PARTIAL_UI_TO_ACK", "UNAVAILABLE"},
}
BLOCKERS = {
    "LAB_VOICE_GATE_ISSUE80", "ARTIST_QA_ISSUE82", "PLAYER_SAFETY",
    "PRIVACY_SIGNOFF", "TIMING_CALIBRATION", "SEVERE_WRONG_EXECUTION",
    "CRASH_RECONCILIATION", "BETA_RECRUITMENT", "OTHER_P0",
}
REVIEW_FIELDS = (
    "qa", "product", "privacy", "retention", "cohort_reconciled",
    "intent_labels", "voice_boundary", "local_ack_coverage",
    "player_severity", "crash_reconciled",
)
COVERAGE_FIELDS = (
    "queue_drops", "ring_evictions", "missing_participant_reports",
    "unreconciled_attempts", "invalid_unresolved", "deduplicated",
)


class AggregateSchemaError(ValueError):
    """Schema error contains only controlled field paths, never user values."""


def shape(value, path, keys, optional=()):
    if not isinstance(value, dict):
        raise AggregateSchemaError(path + ": expected object")
    expected = set(keys)
    permitted = expected | set(optional)
    extra = set(value) - permitted
    absent = expected - set(value)
    if extra or absent:
        raise AggregateSchemaError(path + ": unexpected or missing fields")
    return value


def count(value, path):
    if isinstance(value, bool) or not isinstance(value, int) or not 0 <= value <= MAX_COUNT:
        raise AggregateSchemaError(path + ": expected bounded nonnegative integer")
    return value


def boolean(value, path):
    if type(value) is not bool:
        raise AggregateSchemaError(path + ": expected boolean")
    return value


def choice(value, path, allowed):
    if not isinstance(value, str) or value not in allowed:
        raise AggregateSchemaError(path + ": invalid closed vocabulary")
    return value


def safe_sum(values, path):
    total = sum(values)
    if total > MAX_COUNT:
        raise AggregateSchemaError(path + ": aggregate overflow")
    return total


def counts(value, path):
    value = shape(value, path, ("yes", "no", "unknown"))
    out = {name: count(value[name], path + "." + name) for name in ("yes", "no", "unknown")}
    out["eligible"] = safe_sum(out.values(), path)
    return out


def utc_stamp(raw):
    if not isinstance(raw, str) or len(raw) != 20 or not raw.endswith("Z"):
        raise AggregateSchemaError("pilot_t0_utc: expected YYYY-MM-DDTHH:MM:SSZ")
    try:
        parsed = datetime.strptime(raw, "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
    except ValueError as exc:
        raise AggregateSchemaError("pilot_t0_utc: invalid UTC instant") from exc
    # Must be exact canonical UTC (reject format lookalikes).
    if parsed.strftime("%Y-%m-%dT%H:%M:%SZ") != raw:
        raise AggregateSchemaError("pilot_t0_utc: noncanonical UTC instant")
    return parsed


def classify_ratio(metric_id, truth, source, certified, complete):
    label, target, direction, section = RATIO_SPECS[metric_id]
    eligible = truth["eligible"]
    numerator = truth["yes"]
    lower = 100.0 * numerator / eligible if eligible else None
    upper = (100.0 * (numerator + truth["unknown"]) / eligible) if eligible else None
    if eligible == 0:
        status = "NOT_MEASURABLE"
    elif not certified or not complete or source not in VERIFIED_SOURCES or truth["unknown"]:
        status = "NOT_VERIFIED"
    elif (lower >= target if direction == "AT_LEAST" else lower < target):
        status = "PASS"
    else:
        status = "FAIL"
    return {
        "id": metric_id, "title": label, "section": section, "status": status,
        "source": source, "unit": "percent", "numerator": numerator,
        "denominator": eligible, "unknown": truth["unknown"], "lower": lower,
        "upper": upper, "target": target, "direction": direction,
    }


def classify_latency(metric_id, raw, source, certified, complete, approved_boundary):
    title, target, verified_name = LATENCY_CONFIG[metric_id]
    total = safe_sum((raw["below_target"], raw["at_or_above"], raw["unknown"]), metric_id)
    valid = raw["below_target"] + raw["at_or_above"]
    rank = (valid // 100) * 95 + ((valid % 100) * 95 + 99) // 100
    if valid == 0:
        status = "NOT_MEASURABLE"
    elif (not certified or not complete or not approved_boundary or
          source not in VERIFIED_SOURCES or raw["boundary"] != verified_name or
          raw["unknown"] != 0):
        status = "NOT_VERIFIED"
    elif rank <= raw["below_target"]:
        status = "PASS"
    else:
        status = "FAIL"
    return {
        "id": metric_id, "title": title, "section": "WORKS", "status": status,
        "source": source, "unit": "p95_threshold", "numerator": raw["below_target"],
        "denominator": total, "unknown": raw["unknown"], "valid": valid,
        "at_or_above": raw["at_or_above"], "rank": rank, "target": target,
        "direction": "LESS_THAN", "boundary": raw["boundary"],
    }


def classify_critical(raw, started, source, certified, complete):
    denominator = safe_sum((raw["started_verified"], raw["started_unknown"]), "S02")
    incidents = raw["incidents"]
    per100 = incidents * 100.0 / denominator if denominator else None
    if denominator == 0:
        status = "NOT_MEASURABLE"
    elif not certified or not complete or source not in VERIFIED_SOURCES or raw["started_unknown"]:
        status = "NOT_VERIFIED"
    elif per100 < 1.0:
        status = "PASS"
    else:
        status = "FAIL"
    return {
        "id": "S02", "title": "Verified critical Player incidents per 100 starts",
        "section": "WORKS", "status": status, "source": source,
        "unit": "incidents_per_100", "numerator": incidents,
        "denominator": denominator, "unknown": raw["started_unknown"],
        "observed_value": per100, "target": 1, "direction": "LESS_THAN",
    }


def report(data):
    """Return only hard-validated, non-identifying in-memory report fields."""
    required = ("schema", "dataset_kind", "evidence_source", "pilot_t0_utc",
                "cohort", "review", "coverage", "weeks", "cross_week",
                "open_blockers")
    shape(data, "$", required)
    choice(data["schema"], "schema", {SCHEMA})
    kind = choice(data["dataset_kind"], "dataset_kind", {"SYNTHETIC", "CONTROLLED_BETA"})
    source = choice(data["evidence_source"], "evidence_source", SOURCES)
    if kind == "SYNTHETIC" and source != "SYNTHETIC_FIXTURE":
        raise AggregateSchemaError("dataset_kind: synthetic must use synthetic source")
    if kind == "CONTROLLED_BETA" and source == "SYNTHETIC_FIXTURE":
        raise AggregateSchemaError("dataset_kind: real cannot use synthetic source")
    t0 = utc_stamp(data["pilot_t0_utc"])
    cohort_keys = ("qualified_enrolled", "consented", "withdrawn",
                   "distinct_vehicle_models", "days_observed",
                   "week1_active", "week2_active")
    c = shape(data["cohort"], "cohort", cohort_keys)
    cohort = {key: count(c[key], "cohort." + key) for key in cohort_keys}
    if cohort["days_observed"] > 14 or cohort["consented"] > cohort["qualified_enrolled"]:
        raise AggregateSchemaError("cohort: invalid observation or consent population")
    if cohort["withdrawn"] > cohort["qualified_enrolled"]:
        raise AggregateSchemaError("cohort: withdrawal exceeds enrollment")
    if max(cohort["week1_active"], cohort["week2_active"]) > cohort["qualified_enrolled"]:
        raise AggregateSchemaError("cohort: weekly active exceeds enrollment")
    rv = shape(data["review"], "review", REVIEW_FIELDS)
    reviewed = {key: boolean(rv[key], "review." + key) for key in REVIEW_FIELDS}
    cv = shape(data["coverage"], "coverage", COVERAGE_FIELDS)
    coverage = {key: count(cv[key], "coverage." + key) for key in COVERAGE_FIELDS}
    # Deduplicated rows have been removed, not unreported or considered successes.
    losses = any(coverage[k] != 0 for k in COVERAGE_FIELDS if k != "deduplicated")
    core_review = all(reviewed[k] for k in ("qa", "product", "privacy", "retention"))
    # This P11 dashboard is the locked 50-driver / 14-day controlled
    # Beta view, not the P10 independent laboratory trial calculator.
    # Never paint a metric green on a truncated/missing pilot.
    protocol_population_complete = (
        cohort["qualified_enrolled"] >= 50 and
        cohort["distinct_vehicle_models"] >= 3 and
        cohort["days_observed"] == 14 and
        cohort["consented"] == cohort["qualified_enrolled"] and
        cohort["withdrawn"] == 0
    )
    complete = (
        kind == "CONTROLLED_BETA" and core_review and not losses
        and protocol_population_complete
    )

    blockers_raw = data["open_blockers"]
    if not isinstance(blockers_raw, list) or len(blockers_raw) > 20:
        raise AggregateSchemaError("open_blockers: invalid list")
    blockers = []
    for item in blockers_raw:
        name = choice(item, "open_blockers", BLOCKERS)
        if name in blockers:
            raise AggregateSchemaError("open_blockers: duplicate")
        blockers.append(name)
    # A currently unresolved release/safety/lab/consent blocker must
    # override nominal Green even if per-metric QA counts look good.
    # Keep protocol completeness distinct from release readiness.
    chart_complete = complete and not blockers
    weeks_raw = data["weeks"]
    if not isinstance(weeks_raw, list) or len(weeks_raw) != 2:
        raise AggregateSchemaError("weeks: exactly anchored W1 and W2 required")

    week_reports = []
    all_metrics = []
    for idx, w in enumerate(weeks_raw, 1):
        shape(w, "weeks", ("index", "sessions", "counts", "latency",
                            "critical_errors", "observed_events"))
        if type(w["index"]) is not int or w["index"] != idx:
            raise AggregateSchemaError("weeks.index: expected ordered W1 then W2")
        s = shape(w["sessions"], "weeks.sessions",
                  ("started", "completed", "verified_crashed", "unknown",
                   "voice_driving_sessions", "voice_adoption_unknown"))
        sess = {k: count(s[k], "weeks.sessions." + k) for k in s}
        if safe_sum((sess["completed"], sess["verified_crashed"], sess["unknown"]),
                    "weeks.sessions") != sess["started"]:
            raise AggregateSchemaError("weeks.sessions: starts not reconciled")
        if safe_sum((sess["voice_driving_sessions"], sess["voice_adoption_unknown"]),
                    "weeks.sessions.voice") > sess["started"]:
            raise AggregateSchemaError("weeks.sessions: voice adoption exceeds starts")
        cm = shape(w["counts"], "weeks.counts", WEEK_IDS)
        truths = {k: counts(cm[k], "weeks.counts." + k) for k in WEEK_IDS}
        active = cohort["week1_active"] if idx == 1 else cohort["week2_active"]
        if truths["NS01"]["eligible"] != active:
            raise AggregateSchemaError("NS01: must use exact weekly active denominator")
        if truths["S01"]["eligible"] != sess["started"]:
            raise AggregateSchemaError("S01: must include ALL started journeys")
        if (truths["S01"]["yes"] > sess["completed"] or
            truths["S01"]["no"] < sess["verified_crashed"] or
            truths["S01"]["unknown"] < sess["unknown"]):
            raise AggregateSchemaError("S01: cannot label crashed or unknown journeys clean")
        if truths["NS01"]["yes"] > sess["voice_driving_sessions"]:
            raise AggregateSchemaError("NS01: verified unique successes exceed Voice sessions")
        lat = shape(w["latency"], "weeks.latency", LATENCY_IDS)
        lats = {}
        for key in LATENCY_IDS:
            row = shape(lat[key], "weeks.latency." + key,
                        ("below_target", "at_or_above", "unknown", "boundary"))
            lats[key] = {
                k: count(row[k], "weeks.latency." + key + "." + k)
                for k in ("below_target", "at_or_above", "unknown")
            }
            lats[key]["boundary"] = choice(
                row["boundary"], "weeks.latency." + key + ".boundary",
                ALLOWED_LATENCY_BOUNDARIES[key])
        cr = shape(w["critical_errors"], "weeks.critical_errors",
                   ("incidents", "started_verified", "started_unknown"))
        critical = {k: count(cr[k], "weeks.critical_errors." + k) for k in cr}
        if safe_sum((critical["started_verified"], critical["started_unknown"]),
                    "weeks.critical_errors") != sess["started"]:
            raise AggregateSchemaError("S02: denominator must include all starts")
        observed = shape(w["observed_events"], "weeks.observed_events", EVENTS)
        observed = {key: count(observed[key], "weeks.observed_events." + key) for key in EVENTS}
        # Observed events are JUST a coarse diagnostic counter: never truth for
        # accuracy, weekly distinct people, crash-free or response latency.
        rows = [
            classify_ratio(k, truths[k], source,
                           chart_complete and (reviewed["cohort_reconciled"] and
                                         reviewed["intent_labels"] if k == "NS01"
                                         else reviewed["crash_reconciled"] if k == "S01"
                                         else reviewed["intent_labels"]),
                           chart_complete)
            for k in WEEK_IDS
        ]
        rows.extend(
            classify_latency(k, lats[k], source, chart_complete, chart_complete,
                             reviewed["voice_boundary"] if k == "P01"
                             else reviewed["local_ack_coverage"])
            for k in LATENCY_IDS
        )
        rows.append(classify_critical(critical, sess["started"], source,
                                     chart_complete and reviewed["player_severity"],
                                     chart_complete))
        for row in rows:
            row["window"] = "W" + str(idx)
        all_metrics.extend(rows)
        start = t0 + timedelta(days=(idx - 1) * 7)
        stop = start + timedelta(days=7)
        week_reports.append({
            "index": idx,
            "start_utc": start.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "end_utc": stop.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "sessions": sess,
            "observed_events": observed,
            "metrics": rows,
        })

    cw = shape(data["cross_week"], "cross_week", CROSS_IDS)
    cross_truth = {key: counts(cw[key], "cross_week." + key) for key in CROSS_IDS}
    if cross_truth["R01"]["eligible"] != cohort["week1_active"]:
        raise AggregateSchemaError("R01: denominator must be W1 active")
    if cross_truth["R02"]["eligible"] != cohort["qualified_enrolled"]:
        raise AggregateSchemaError("R02: denominator must be ALL enrolled")
    if cross_truth["R01"]["yes"] > cohort["week2_active"]:
        raise AggregateSchemaError("R01: verified returners exceed W2 active")
    cross_rows = [
        classify_ratio(k, cross_truth[k], source,
                       chart_complete and reviewed["cohort_reconciled"],
                       chart_complete)
        for k in CROSS_IDS
    ]
    for row in cross_rows:
        row["window"] = "W1+W2"
    all_metrics.extend(cross_rows)

    beta_protocol_ready = (complete and reviewed["cohort_reconciled"]
                           and cohort["qualified_enrolled"] >= 50
                           and cohort["distinct_vehicle_models"] >= 3
                           and cohort["days_observed"] == 14
                           and cohort["consented"] == cohort["qualified_enrolled"]
                           and cohort["withdrawn"] == 0)
    # No machine can authenticate the operator's own claims. Verdict
    # remains a review CANDIDATE even with declared approval flags true.
    if kind == "SYNTHETIC":
        decision = "DEMO_ONLY"
    elif blockers and beta_protocol_ready:
        decision = "BLOCKED"
    elif not beta_protocol_ready or any(
            m["status"] in ("NOT_VERIFIED", "NOT_MEASURABLE") for m in all_metrics):
        decision = "INSUFFICIENT_EVIDENCE"
    elif all(m["status"] == "PASS" for m in all_metrics):
        decision = "GO_REVIEW_CANDIDATE"
    elif all_metrics and (any(m["id"] == "R01" and m["status"] == "FAIL"
                              for m in cross_rows) and
                          any(m["id"] == "R02" and m["status"] == "FAIL"
                              for m in cross_rows) and
                          all(m["id"] not in ("NS01", "Q01") or m["status"] == "FAIL"
                              for m in all_metrics)):
        decision = "STOP_REVIEW_CANDIDATE"
    else:
        decision = "ITERATE_REVIEW_CANDIDATE"
    return {
        "schema": SCHEMA, "dataset_kind": kind, "source": source,
        "synthetic": kind == "SYNTHETIC",
        "start_utc": week_reports[0]["start_utc"],
        "end_utc": week_reports[1]["end_utc"],
        "cohort": cohort, "review": reviewed, "coverage": coverage,
        "coverage_complete": complete, "beta_protocol_ready": beta_protocol_ready,
        "open_blockers": blockers, "decision": decision,
        "weeks": week_reports, "cross_week": cross_rows,
        "all_metrics": all_metrics,
        "operator_claim_notice": (
            "Review flags and aggregate source are operator declarations, "
            "not cryptographically verified or independent human sign-off."
        ),
    }
