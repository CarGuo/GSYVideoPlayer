#!/usr/bin/env python3
"""Capture local Android audio diagnostics for two successive plays; no settings changes."""
import argparse
import datetime as dt
import json
import os
from pathlib import Path
import subprocess
import sys
import threading
import time

SERVICES = ("media.audio_flinger", "audio", "media.audio_policy")
FLINGER_AFTER = "media.audio_flinger.after"


def services_for(checkpoint):
    return (*SERVICES, FLINGER_AFTER) if checkpoint != "baseline" else SERVICES
TAGS = ("AudioTrack:V", "AudioFlinger:V", "AudioPolicyManager:V",
        "AudioPolicyService:V", "AudioService:V", "AudioManager:V",
        "MediaFocusControl:V", "ijkplayer:V", "IJKMEDIA:V", "GSYVideoPlayer:V", "*:S")
CHECKPOINTS = (
    ("baseline", "Before starting video, leave the chosen output connected. Press Enter."),
    ("first-active", "Start playback. Keep it playing and press Enter while the first play is active."),
    ("second-active", "Go back to the menu, re-enter and start the same video on the same route. "
                      "Keep it playing and press Enter during the second play (even if silent)."),
)
FILE_LIMIT = 8 * 1024 * 1024


def utc():
    return dt.datetime.now(dt.timezone.utc).isoformat(timespec="milliseconds")


def write_private(path, data):
    with open(path, "wb", opener=lambda p, f: os.open(p, f, 0o600)) as stream:
        stream.write(data)


def take(adb, service, path, timeout):
    stamp = utc()
    try:
        p = subprocess.run([*adb, "shell", "dumpsys", "-t", str(timeout), service],
                           capture_output=True, timeout=timeout + 2)
        data, errors = p.stdout, p.stderr
        message = (data + errors).lower()
        bad = (not data.strip() or b"can't find service:" in message or
               b"permission denial" in message or b"permission denied" in message or
               b"dump timeout" in message)
        row = {"state": "ok" if p.returncode == 0 and not bad else "failed",
               "exit_code": p.returncode}
    except subprocess.TimeoutExpired as exc:
        data, errors = exc.stdout or b"", exc.stderr or b""
        row = {"state": "timeout"}
    except OSError as exc:
        data, errors = b"", str(exc).encode()
        row = {"state": "failed"}
    if len(data) > FILE_LIMIT or len(errors) > FILE_LIMIT:
        row["state"] = "truncated"
    write_private(path, data[:FILE_LIMIT])
    if errors:
        write_private(path.with_suffix(".stderr.txt"), errors[:FILE_LIMIT])
    return {**row, "started_utc": stamp, "finished_utc": utc(), "file": path.name}


class Logs:
    def __init__(self, adb, out, window):
        self.out = out
        self.started = utc()
        self.truncated = False
        self.error = None
        self.planned_stop = False
        self.expired = False
        self.lock = threading.Lock()
        self.process = subprocess.Popen(
            [*adb, "logcat", "-b", "main", "-b", "system", "-v", "threadtime", "-T", "1", *TAGS],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        )
        self.reader = threading.Thread(target=self.drain, daemon=True)
        self.reader.start()
        self.deadline = threading.Timer(window, self.expire)
        self.deadline.daemon = True
        self.deadline.start()

    def expire(self):
        with self.lock:
            self.expired = True
            if self.process.poll() is None:
                self.process.kill()

    def drain(self):
        try:
            with self.process.stdout as pipe, open(
                    self.out, "wb", opener=lambda p, f: os.open(p, f, 0o600)) as dst:
                remaining = FILE_LIMIT
                while True:
                    data = pipe.read1(65536)
                    if not data:
                        break
                    dst.write(data[:remaining])
                    if len(data) > remaining:
                        self.truncated = True
                    remaining = max(0, remaining - len(data))
        except OSError as exc:
            self.error = str(exc)

    def stop(self):
        with self.lock:
            self.deadline.cancel()
            if self.process.poll() is None:
                self.planned_stop = True
                self.process.terminate()
        self.deadline.join(timeout=1)
        try:
            self.process.wait(timeout=3)
        except subprocess.TimeoutExpired:
            self.process.kill()
            self.process.wait(timeout=3)
        self.reader.join(timeout=3)
        state = ("timeout" if self.expired else
                 ("failed" if self.error or self.reader.is_alive() or not self.planned_stop
                  else ("truncated" if self.truncated else "ok")))
        return {"state": state, "started_utc": self.started, "finished_utc": utc(),
                "exit_code": self.process.returncode, "file": self.out.name,
                "reader_error": self.error,
                "note": "Capture begins with up to one buffered line. File may be empty if no tag matched."}


def run(args):
    adb = [args.adb, "-s", args.serial]
    try:
        probe = subprocess.run([*adb, "get-state"], capture_output=True, timeout=5)
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"Cannot access ADB: {exc}", file=sys.stderr)
        return 2
    if probe.returncode or probe.stdout.strip() != b"device":
        print("Selected device is not online/authorized. No capture started.", file=sys.stderr)
        return 2
    try:
        args.out.mkdir(mode=0o700, parents=False, exist_ok=False)
    except OSError as exc:
        print(f"Expected a new output directory with an existing parent: {exc}", file=sys.stderr)
        return 2
    report = {"schema": 1, "backend_label": args.backend, "route_label": args.route,
              "started_utc": utc(), "log_window_s": args.log_window,
              "checkpoints": {}, "interrupted": False}
    logging = None
    start = time.monotonic()
    try:
        try:
            logging = Logs(adb, args.out / "logcat.txt", args.log_window)
        except OSError as exc:
            report["logcat"] = {"state": "failed", "error": str(exc)}
        for label, prompt in CHECKPOINTS:
            input(f"[{label}] {prompt} ")
            checkpoint = report["checkpoints"][label] = {
                "marked_utc": utc(), "elapsed_monotonic_s": round(time.monotonic() - start, 3),
                "services": {}}
            for key in services_for(label):
                service = "media.audio_flinger" if key == FLINGER_AFTER else key
                checkpoint["services"][key] = take(
                    adb, service, args.out / f"{label}.{key}.txt", args.dump_timeout)
            if label != "baseline":
                answer = input("Was audio audible at that checkpoint? [yes/no/unknown] ").strip().lower()
                checkpoint["audible"] = answer if answer in ("yes", "no") else "unknown"
    except (EOFError, KeyboardInterrupt):
        report["interrupted"] = True
    finally:
        if logging is not None:
            try:
                report["logcat"] = logging.stop()
            except (OSError, subprocess.TimeoutExpired) as exc:
                report["logcat"] = {"state": "failed", "error": str(exc)}
        report["finished_utc"] = utc()
        report["complete"] = (
            not report["interrupted"] and
            report.get("logcat", {}).get("state") == "ok" and
            len(report["checkpoints"]) == len(CHECKPOINTS) and
            all(set(row["services"]) == set(services_for(label)) and
                all(s["state"] == "ok" for s in row["services"].values())
                for label, row in report["checkpoints"].items())
        )
        write_private(args.out / "report.json", (json.dumps(report, indent=2) + "\n").encode())
    print(f"Local capture {'complete' if report['complete'] else 'incomplete'}: {args.out / 'report.json'}")
    print("Inspect and redact device/system logs before sharing.")
    return 0 if report["complete"] else 1


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True, help="Exact device serial from adb devices")
    parser.add_argument("--backend", required=True, choices=("audiotrack", "opensles", "exo"))
    parser.add_argument("--route", required=True, choices=("wired", "bluetooth", "speaker", "usb", "other"))
    parser.add_argument("--out", required=True, type=Path, help="New directory in a local private parent")
    parser.add_argument("--adb", default="adb", help="ADB executable (default: adb)")
    parser.add_argument("--dump-timeout", type=int, default=3, help="Seconds per device dumpsys (1–30)")
    parser.add_argument("--log-window", type=int, default=180, help="Maximum live logcat seconds (1–900)")
    args = parser.parse_args(argv)
    if not (1 <= args.dump_timeout <= 30 and 1 <= args.log_window <= 900 and args.serial.strip()):
        parser.error("need a non-empty serial, --dump-timeout in 1–30 and --log-window in 1–900")
    return run(args)


if __name__ == "__main__":
    raise SystemExit(main())
