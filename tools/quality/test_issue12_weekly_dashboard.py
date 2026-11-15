"""Issue #12 P11 — QA CASES AUTHORED ONLY, zero executed until P14.

Fixtures are intentionally SYNTHETIC. Do NOT claim these as QA results.
"""
from __future__ import annotations
import copy
import json
import tempfile
import unittest
from pathlib import Path

from issue12_dashboard_core import (
    AggregateSchemaError, SCHEMA, classify_ratio, classify_latency, counts,
    report, utc_stamp, VERIFIED_SOURCES,
)
from issue12_dashboard_render import render_html
from issue12_weekly_dashboard import unique_pairs, load_input, write_private_file

HERE = Path(__file__).parent
FIXTURE = HERE / "fixtures" / "issue12_dashboard_SYNTHETIC.json"


def synthetic():
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


def reviewed():
    """Fake QA declarations, for PURE deterministic code tests only."""
    sample = synthetic()
    sample["dataset_kind"] = "CONTROLLED_BETA"
    sample["evidence_source"] = "CONTROLLED_BETA_VERIFIED_AGGREGATE"
    sample["cohort"]["consented"] = 50
    sample["cohort"]["withdrawn"] = 0
    for key in sample["review"]:
        sample["review"][key] = True
    for key in sample["coverage"]:
        sample["coverage"][key] = 0
    sample["open_blockers"] = []
    for week in sample["weeks"]:
        s = week["sessions"]
        s["completed"] += s["unknown"]
        s["unknown"] = 0
        v = week["counts"]["S01"]
        v["yes"] += v["unknown"]
        v["unknown"] = 0
        for code, evidence in week["counts"].items():
            if code != "S01":
                evidence["no"] += evidence["unknown"]
                evidence["unknown"] = 0
        week["critical_errors"]["started_verified"] += week["critical_errors"]["started_unknown"]
        week["critical_errors"]["started_unknown"] = 0
        for code, latency in week["latency"].items():
            latency["below_target"] += latency["unknown"]
            latency["unknown"] = 0
            latency["boundary"] = (
                "VERIFIED_ACOUSTIC_TO_PERCEIVABLE" if code == "P01"
                else "VERIFIED_ALL_ELIGIBLE_LOCAL_ACK"
            )
    for item in sample["cross_week"].values():
        item["no"] += item["unknown"]
        item["unknown"] = 0
    return sample


class Phase11AggregateDashboardContract(unittest.TestCase):
    def test_synthetic_fixture_has_unambiguous_watermark_and_no_green(self):
        data = report(synthetic())
        self.assertEqual("DEMO_ONLY", data["decision"])
        self.assertTrue(data["synthetic"])
        self.assertTrue(all(x["status"] != "PASS" for x in data["all_metrics"]))
        self.assertIn("SYNTHETIC DEMONSTRATION ONLY", render_html(data))

    def test_all_three_original_product_questions_in_one_html(self):
        html = render_html(report(synthetic()))
        for word in ("USED", "WORKS", "RETURNS", "IS IT USED?",
                     "DOES IT WORK CORRECTLY?", "DOES THE USER RETURN?"):
            self.assertIn(word, html)

    def test_full_pilot_half_open_week_windows(self):
        got = report(synthetic())
        self.assertEqual("2026-01-01T00:00:00Z", got["weeks"][0]["start_utc"])
        self.assertEqual("2026-01-08T00:00:00Z", got["weeks"][0]["end_utc"])
        self.assertEqual(got["weeks"][0]["end_utc"], got["weeks"][1]["start_utc"])
        self.assertEqual("2026-01-15T00:00:00Z", got["weeks"][1]["end_utc"])

    def test_weekly_north_star_must_use_weekly_actives(self):
        s = synthetic()
        s["weeks"][0]["counts"]["NS01"]["no"] += 1
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_R01_must_use_W1_active_not_enrolled_or_W2(self):
        s = synthetic()
        s["cross_week"]["R01"]["no"] += 10
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_R02_must_use_all_enrolled(self):
        s = synthetic()
        s["cross_week"]["R02"]["no"] = 0
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_weeks_cannot_be_swapped_or_truncated(self):
        s = synthetic()
        s["weeks"] = s["weeks"][::-1]
        with self.assertRaises(AggregateSchemaError):
            report(s)
        s = synthetic()
        s["weeks"].pop()
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_S01_cannot_call_unknown_or_verified_crash_clean(self):
        s = synthetic()
        s["weeks"][0]["counts"]["S01"] = {"yes": 70, "no": 0, "unknown": 0}
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_S01_start_denominator_must_match_authoritative_starts(self):
        s = synthetic()
        s["weeks"][1]["counts"]["S01"]["no"] += 1
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_S02_critical_errors_start_denominator_matches_session_owner(self):
        s = synthetic()
        s["weeks"][0]["critical_errors"]["started_unknown"] = 0
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_R01_returners_cannot_exceed_week_two_active(self):
        s = synthetic()
        s["cross_week"]["R01"] = {"yes": 30, "no": 10, "unknown": 0}
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_NS_successful_people_cannot_exceed_voice_session_count(self):
        s = synthetic()
        s["weeks"][1]["sessions"]["voice_driving_sessions"] = 3
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_core_ratio_boundaries_lock_strict_15_and_3(self):
        for metric, target in [("F01", 15), ("G01", 3)]:
            actual = classify_ratio(metric, counts({
                "yes": target, "no": 100-target, "unknown": 0
            }, "trial"), "QA_VERIFIED_AGGREGATE", True, True)
            self.assertEqual("FAIL", actual["status"])

    def test_core_ratio_85_and_99_inclusive(self):
        for metric, good in [("Q01", 85), ("S01", 99)]:
            actual = classify_ratio(metric, counts({
                "yes": good, "no": 100-good, "unknown": 0
            }, "trial"), "QA_VERIFIED_AGGREGATE", True, True)
            self.assertEqual("PASS", actual["status"])

    def test_P95_nearest_rank_boundary_does_not_round_2500_to_pass(self):
        for metric, cutoff in [("P01", 2500), ("P02", 1000)]:
            boundary = (
                "VERIFIED_ACOUSTIC_TO_PERCEIVABLE" if metric == "P01"
                else "VERIFIED_ALL_ELIGIBLE_LOCAL_ACK"
            )
            row = {"below_target": 18, "at_or_above": 1,
                   "unknown": 0, "boundary": boundary}
            self.assertEqual("FAIL", classify_latency(
                metric, row, "QA_VERIFIED_AGGREGATE", True, True, True)["status"])
            row["below_target"] = 19
            self.assertEqual("PASS", classify_latency(
                metric, row, "QA_VERIFIED_AGGREGATE", True, True, True)["status"])

    def test_proxy_VAD_to_compose_draw_never_qualifies_as_official_P95(self):
        evidence = {"below_target": 100, "at_or_above": 0, "unknown": 0,
                    "boundary": "PROXY_VAD_TO_COMPOSE_DRAW"}
        val = classify_latency("P01", evidence,
              "CONTROLLED_BETA_VERIFIED_AGGREGATE", True, True, True)
        self.assertEqual("NOT_VERIFIED", val["status"])

    def test_missing_coverage_invalidates_even_perfect_qa_numbers(self):
        s = reviewed()
        s["coverage"]["queue_drops"] = 1
        x = report(s)
        self.assertTrue(all(z["status"] not in ("PASS", "FAIL")
                            for z in x["all_metrics"]))
        self.assertEqual("INSUFFICIENT_EVIDENCE", x["decision"])

    def test_unapproved_privacy_or_incomplete_recruitment_never_goes_green(self):
        s = reviewed()
        s["review"]["privacy"] = False
        self.assertEqual("INSUFFICIENT_EVIDENCE", report(s)["decision"])
        s = reviewed()
        s["cohort"]["days_observed"] = 13
        self.assertTrue(all(x["status"] != "PASS" for x in report(s)["all_metrics"]))

    def test_operator_approved_but_unreconciled_intent_labels_not_green(self):
        s = reviewed()
        s["review"]["intent_labels"] = False
        x = report(s)
        self.assertTrue(any(z["id"] == "Q01" and z["status"] == "NOT_VERIFIED"
                            for z in x["all_metrics"]))

    def test_partial_good_data_cannot_assert_go(self):
        s = reviewed()
        self.assertNotEqual("GO_REVIEW_CANDIDATE", report(s)["decision"])

    def test_operator_declared_full_pass_is_still_review_candidate_not_final_go(self):
        s = reviewed()
        s["cross_week"]["R02"] = {"yes": 26, "no": 24, "unknown": 0}
        for w in s["weeks"]:
            sess = w["sessions"]
            sess["completed"] = sess["started"]
            sess["verified_crashed"] = 0
            sess["unknown"] = 0
            w["counts"]["S01"] = {"yes": sess["started"], "no": 0, "unknown": 0}
            w["critical_errors"]["incidents"] = 0
            for id_, row in w["counts"].items():
                if id_ == "NS01":
                    row["yes"] = max(row["yes"], (row["yes"] + row["no"] + 1)//2)
                    row["no"] = (s["cohort"]["week1_active"] if w["index"] == 1
                                  else s["cohort"]["week2_active"]) - row["yes"]
                elif id_ == "Q01":
                    row["yes"] = row["yes"] + row["no"]
                    row["no"] = 0
                elif id_ in ("F01", "G01"):
                    row["no"] += row["yes"]
                    row["yes"] = 0
            for k in ("P01", "P02"):
                w["latency"][k]["below_target"] += w["latency"][k]["at_or_above"]
                w["latency"][k]["at_or_above"] = 0
        self.assertEqual("GO_REVIEW_CANDIDATE", report(s)["decision"])
        s["open_blockers"] = ["PLAYER_SAFETY"]
        self.assertEqual("BLOCKED", report(s)["decision"])

    def test_unknown_metric_contributes_to_denominator_and_not_green(self):
        s = reviewed()
        s["weeks"][0]["counts"]["Q01"] = {"yes": 90, "no": 9, "unknown": 1}
        x = report(s)
        row = next(m for m in x["all_metrics"]
                   if m["window"] == "W1" and m["id"] == "Q01")
        self.assertEqual(100, row["denominator"])
        self.assertEqual(1, row["unknown"])
        self.assertEqual("NOT_VERIFIED", row["status"])

    def test_observed_eight_event_counts_do_not_prove_correctness(self):
        s = synthetic()
        s["weeks"][0]["observed_events"]["command_executed"] = 1000000
        x = report(s)
        q01 = next(z for z in x["all_metrics"]
                   if z["id"] == "Q01" and z["window"] == "W1")
        self.assertEqual("NOT_VERIFIED", q01["status"])
        self.assertEqual(88, q01["numerator"])

    def test_forged_private_event_name_rejected_before_report(self):
        s = synthetic()
        s["weeks"][0]["observed_events"]["voice_intent_outcome"] = 1
        with self.assertRaises(AggregateSchemaError):
            report(s)

    def test_extra_personal_identifiers_and_raw_audio_rejected(self):
        for private_key in ("session_id", "device_id", "person_uuid",
                            "raw_audio", "transcript", "file_path", "track_title"):
            s = synthetic()
            s["cohort"][private_key] = "private-value"
            with self.assertRaises(AggregateSchemaError):
                report(s)

    def test_malformed_or_noncanonical_utc_t0_is_rejected(self):
        for bad in ("2026-02-30T00:00:00Z", "2026-01-01", "2026-01-01T00:00:00+00:00"):
            with self.assertRaises(AggregateSchemaError):
                utc_stamp(bad)

    def test_negative_boolean_nan_huge_counts_fail_closed(self):
        for malformed in (-1, True, 2**80, 3.5):
            s = synthetic()
            s["cohort"]["qualified_enrolled"] = malformed
            with self.assertRaises(AggregateSchemaError):
                report(s)

    def test_duplicate_json_keys_rejected_not_overwritten(self):
        with self.assertRaises(AggregateSchemaError):
            json.loads('{"schema":"X","schema":"Y"}',
                       object_pairs_hook=unique_pairs)

    def test_HTML_is_escaped_and_has_no_external_dependency(self):
        data = report(synthetic())
        data["operator_claim_notice"] = '<img src="https://bad.example/x">'
        html = render_html(data)
        self.assertNotIn('<img src="https://bad.example/x">', html)
        self.assertIn("&lt;img", html)
        self.assertIn("default-src &#39;none&#39;", html)
        self.assertNotIn('<script', html)

    def test_private_output_fails_if_path_exists_without_overwrite(self):
        with tempfile.TemporaryDirectory() as tmp:
            p = Path(tmp) / "dashboard.html"
            write_private_file(p, render_html(report(synthetic())))
            with self.assertRaises(FileExistsError):
                write_private_file(p, "cannot overwrite")
            self.assertIn("SYNTHETIC", p.read_text(encoding="utf-8"))

    def test_input_max_64k_and_no_bare_rows(self):
        with tempfile.TemporaryDirectory() as tmp:
            p = Path(tmp) / "rows.json"
            p.write_text('{"events":[{"transcript":"secret"}]}', encoding="utf-8")
            with self.assertRaises(AggregateSchemaError):
                load_input(p)
            p.write_text("x" * 65537, encoding="utf-8")
            with self.assertRaises(AggregateSchemaError):
                load_input(p)


if __name__ == "__main__":
    # Tests only when explicitly invoked by authorized P14 QA.
    unittest.main()
