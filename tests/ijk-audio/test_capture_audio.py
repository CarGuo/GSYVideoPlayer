"""Offline end-to-end tests with a scripted ADB stand-in (no hardware)."""
import json
import os
from pathlib import Path
import stat
import subprocess
import sys
import tempfile
import textwrap
import unittest

SCRIPT = Path(__file__).with_name("capture_audio.py")


class CaptureTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.calls = self.root / "calls.jsonl"
        self.pid = self.root / "logcat.pid"
        self.out = self.root / "out"
        self.env = dict(os.environ, CALLS=str(self.calls), PID=str(self.pid))
        self.adb = self.root / "mock-adb"
        self.adb.write_text("#!" + sys.executable + "\n" + textwrap.dedent("""
            import json, os, sys, time
            from pathlib import Path
            args = sys.argv[1:]
            with open(os.environ["CALLS"], "a") as f:
                f.write(json.dumps(args) + "\\n")
            assert args[:2] == ["-s", "explicit-device"]
            if args[2:] == ["get-state"]:
                print(os.environ.get("STATE", "device"))
            elif args[2] == "logcat":
                Path(os.environ["PID"]).write_text(str(os.getpid()))
                if os.environ.get("FAIL_LOG"):
                    print("logcat: buffer unavailable")
                    sys.exit(1)
                print("AudioTrack: fake evidence", flush=True)
                while True:
                    time.sleep(0.04)
            elif args[2:6] == ["shell", "dumpsys", "-t", "1"]:
                if args[-1] == os.environ.get("FAIL_SERVICE"):
                    print("Can't find service: " + args[-1])
                elif (os.environ.get("FAIL_SECOND_FLINGER") and args[-1] == "media.audio_flinger"
                      and Path(os.environ["CALLS"]).read_text().count('"media.audio_flinger"') == 3):
                    print("pErMiSSion dENIED", file=sys.stderr)
                    print("fake partial dump")
                else:
                    print("fake dump " + args[-1])
            else:
                raise AssertionError(args)
        """), encoding="utf-8")
        self.adb.chmod(0o700)

    def capture(self, inputs="\n\nyes\n\nno\n"):
        return subprocess.run(
            [sys.executable, "-B", str(SCRIPT), "--serial", "explicit-device",
             "--backend", "audiotrack", "--route", "bluetooth", "--dump-timeout", "1",
             "--adb", str(self.adb), "--out", str(self.out)],
            env=self.env, input=inputs, text=True, capture_output=True, timeout=15,
        )

    def report(self):
        return json.loads((self.out / "report.json").read_text())

    def test_complete_and_no_global_log_changes(self):
        p = self.capture()
        self.assertEqual(p.returncode, 0, (p.stdout, p.stderr))
        data = self.report()
        self.assertTrue(data["complete"])
        self.assertEqual(data["checkpoints"]["first-active"]["audible"], "yes")
        self.assertEqual(data["checkpoints"]["second-active"]["audible"], "no")
        self.assertIn("fake evidence", (self.out / "logcat.txt").read_text())
        calls = [json.loads(line) for line in self.calls.read_text().splitlines()]
        self.assertEqual(len(calls), 13)
        active = data["checkpoints"]["first-active"]["services"]
        self.assertIn("media.audio_flinger.after", active)
        self.assertTrue((self.out / "first-active.media.audio_flinger.after.txt").is_file())
        dumps = [c[-1] for c in calls if c[2] == "shell"]
        self.assertEqual(dumps, ["media.audio_flinger", "audio", "media.audio_policy"] +
                         ["media.audio_flinger", "audio", "media.audio_policy", "media.audio_flinger"] * 2)
        self.assertTrue(all(c[:2] == ["-s", "explicit-device"] for c in calls))
        self.assertTrue(any(c[2] == "logcat" and "-T" in c and "-c" not in c for c in calls))
        self.assertEqual(stat.S_IMODE(self.out.stat().st_mode), 0o700)
        self.assertEqual(stat.S_IMODE((self.out / "report.json").stat().st_mode), 0o600)
        with self.assertRaises(ProcessLookupError):
            os.kill(int(self.pid.read_text()), 0)

    def test_failed_services_not_reported_as_complete(self):
        self.env["FAIL_SERVICE"] = "media.audio_policy"
        self.assertEqual(self.capture().returncode, 1)
        report = self.report()
        self.assertFalse(report["complete"])
        self.assertEqual(report["checkpoints"]["second-active"]["services"]["media.audio_policy"]["state"], "failed")

    def test_stderr_permission_denied_on_second_flinger_is_incomplete(self):
        self.env["FAIL_SECOND_FLINGER"] = "1"
        self.assertEqual(self.capture().returncode, 1)
        active = self.report()["checkpoints"]["first-active"]["services"]
        self.assertEqual(active["media.audio_flinger"]["state"], "ok")
        self.assertEqual(active["media.audio_flinger.after"]["state"], "failed")

    def test_early_logcat_failure_reported(self):
        self.env["FAIL_LOG"] = "1"
        self.assertEqual(self.capture().returncode, 1)
        self.assertEqual(self.report()["logcat"]["state"], "failed")

    def test_disconnected(self):
        self.env["STATE"] = "offline"
        self.assertEqual(self.capture().returncode, 2)
        self.assertFalse(self.out.exists())

    def test_eof_keeps_partial_report_and_stops_collection(self):
        self.assertEqual(self.capture("").returncode, 1)
        self.assertTrue(self.report()["interrupted"])
        self.assertFalse(self.report()["complete"])


if __name__ == "__main__":
    unittest.main()
