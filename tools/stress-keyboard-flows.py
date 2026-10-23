#!/usr/bin/env python3
"""Run each keyboard flow class in fresh Maven JVMs, retaining every result (no retries)."""

import argparse
import datetime
import json
from pathlib import Path
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
CLASSES = {
    "com.uzima.ui.quote.QuoteEditorFlowTest": 6,
    "com.uzima.ui.clients.ClientsFlowTest": 10,
}
CI_FLAGS = [
    "-Dtestfx.robot=glass",
    "-Dtestfx.headless=true",
    "-Dglass.platform=Monocle",
    "-Dmonocle.platform=Headless",
    "-Dprism.order=sw",
]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--runs", type=int, default=30)
    parser.add_argument("--mode", choices=("default", "ci", "both"), default="both")
    parser.add_argument("--focus-timeline", action="store_true")
    parser.add_argument("--stop-on-failure", action="store_true",
                        help="Stop the diagnostic batch at its first failure; never retry")
    args = parser.parse_args()
    if args.runs < 1:
        parser.error("--runs must be positive")

    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    output = ROOT / "target" / "keyboard-stress" / stamp
    output.mkdir(parents=True)
    version = subprocess.run(
        ["./mvnw", "-version"], cwd=ROOT, text=True, capture_output=True, check=True
    ).stdout
    modes = ("default", "ci") if args.mode == "both" else (args.mode,)
    results = {"jvm": version, "runs_per_mode": args.runs, "results": []}
    print(f"Evidence: {output}", flush=True)
    print(version, flush=True)

    for mode in modes:
        counts = dict.fromkeys(CLASSES, 0)
        for iteration in range(1, args.runs + 1):
            destination = output / f"{mode}-{iteration:02d}"
            destination.mkdir()
            command = [
                "./mvnw", "-B", "-ntp",
                "-Dtest=QuoteEditorFlowTest,ClientsFlowTest",
                *([] if mode == "default" else CI_FLAGS),
                *(["-Duzima.focusTimeline=true"] if args.focus_timeline else []),
                "test",
            ]
            started = time.time_ns()
            with (destination / "maven.log").open("w") as log:
                completed = subprocess.run(
                    command, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT
                )
            result = {
                "mode": mode, "iteration": iteration, "command": command,
                "exit_code": completed.returncode, "classes": {},
                "passed": completed.returncode == 0,
            }
            for name, expected_tests in CLASSES.items():
                report = ROOT / "target" / "surefire-reports" / f"TEST-{name}.xml"
                passed = False
                if report.exists() and report.stat().st_mtime_ns >= started:
                    shutil.copy2(report, destination)
                    suite = ET.parse(report).getroot()
                    actual = {
                        key: int(suite.get(key, "0"))
                        for key in ("tests", "failures", "errors", "skipped")
                    }
                    passed = actual == {
                        "tests": expected_tests, "failures": 0, "errors": 0, "skipped": 0
                    }
                    result["classes"][name] = actual
                else:
                    result["classes"][name] = {"error": "No fresh Surefire report"}
                counts[name] += int(passed)
                result["passed"] = result["passed"] and passed
            results["results"].append(result)
            (output / "summary.json").write_text(json.dumps(results, indent=2) + "\n")
            progress = ", ".join(
                f"{name.rsplit('.', 1)[-1]} {count}/{iteration}"
                for name, count in counts.items()
            )
            print(f"{mode} {iteration:02d}/{args.runs}: {progress}", flush=True)
            if args.stop_on_failure and not result["passed"]:
                print(f"Summary: {output / 'summary.json'}", flush=True)
                return 1

    print(f"Summary: {output / 'summary.json'}", flush=True)
    return int(any(not result["passed"] for result in results["results"]))


if __name__ == "__main__":
    raise SystemExit(main())
