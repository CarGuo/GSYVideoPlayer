#!/usr/bin/env python3
"""14.0.0: Gate certificate loading against actual published native bytes."""
import argparse
import hashlib
import json
import platform
import subprocess
import tempfile
import uuid
from pathlib import Path

TARGETS = {
    "arm64-v8a": "aarch64-linux-android",
    "armeabi-v7a": "armv7a-linux-androideabi",
    "x86_64": "x86_64-linux-android",
}
ROOT = Path(__file__).resolve().parents[2]


def run(command, timeout=60):
    return subprocess.run(command, text=True, stdout=subprocess.PIPE,
                          stderr=subprocess.STDOUT, timeout=timeout)


def require(command, timeout=60):
    result = run(command, timeout)
    if result.returncode:
        raise RuntimeError(f"Command failed ({result.returncode}): {result.stdout}")
    return result.stdout.strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ndk", type=Path, required=True)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--abis", nargs="+", choices=TARGETS)
    parser.add_argument("--library-dir", type=Path,
                        default=ROOT / "gsyVideoPlayer-ex_so/src/main/jniLibs")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    adb = ["adb", "-s", args.serial]
    supported = require(adb + ["shell", "getprop", "ro.product.cpu.abilist"]).split(",")
    selected = args.abis or [abi for abi in TARGETS if abi in supported]
    if not selected or any(abi not in supported for abi in selected):
        parser.error(f"Requested ABIs cannot run on this device: {supported}")
    hosts = {"Darwin": "darwin-x86_64", "Linux": "linux-x86_64"}
    host = hosts.get(platform.system())
    if not host:
        parser.error("Unsupported NDK host")
    binary_dir = args.ndk.resolve() / "toolchains/llvm/prebuilt" / host / "bin"
    report = {
        "scope": "Public OpenSSL certificate loading in actual libijkffmpeg.so; "
                 "not an end-to-end TLS, rendering, or 16 KiB runtime qualification",
        "device_abis": supported,
        "abis_not_run": [abi for abi in TARGETS if abi not in selected],
        "ndk": str(args.ndk.resolve()),
        "results": [],
    }
    # Each invocation owns one disposable directory. Never touch app data or
    # retain the generated private key in a report or a published artifact.
    remote = "/data/local/tmp/gsy-tls-" + uuid.uuid4().hex
    failed = False
    try:
        with tempfile.TemporaryDirectory(prefix="gsy-tls-") as temporary:
            work = Path(temporary)
            cert = work / "cert.pem"
            require(["openssl", "req", "-x509", "-newkey", "rsa:2048", "-nodes",
                     "-keyout", str(work / "key.pem"), "-out", str(cert),
                     "-days", "1", "-subj", "/CN=localhost"])
            require(adb + ["shell", "mkdir", "-p", remote])
            require(adb + ["push", str(cert), remote + "/cert.pem"])
            for abi in selected:
                library = args.library_dir.resolve() / abi / "libijkffmpeg.so"
                compiler = binary_dir / (TARGETS[abi] + "23-clang")
                probe = work / ("probe-" + abi)
                result = {"abi": abi, "library": str(library)}
                try:
                    result["library_sha256"] = hashlib.sha256(library.read_bytes()).hexdigest()
                    result["compiler"] = str(compiler)
                    require([str(compiler), "-O2", str(Path(__file__).with_name("TlsLoadProbe.c")),
                             "-ldl", "-o", str(probe)])
                    directory = remote + "/" + abi
                    require(adb + ["shell", "mkdir", "-p", directory])
                    require(adb + ["push", str(probe), directory + "/probe"])
                    require(adb + ["push", str(library), directory + "/libijkffmpeg.so"])
                    execution = run(adb + ["shell", directory + "/probe",
                                          directory + "/libijkffmpeg.so", remote + "/cert.pem"], 30)
                    result.update(exit_code=execution.returncode, output=execution.stdout,
                                  passed=execution.returncode == 0)
                except (OSError, RuntimeError, subprocess.TimeoutExpired) as error:
                    result.update(passed=False, error=str(error))
                report["results"].append(result)
                failed |= not result["passed"]
    finally:
        cleanup = run(adb + ["shell", "rm", "-rf", remote])
        report["device_cleanup_exit_code"] = cleanup.returncode
        failed |= cleanup.returncode != 0
    report["passed"] = not failed
    output = json.dumps(report, indent=2, ensure_ascii=False) + "\n"
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(output, encoding="utf-8")
    print(output, end="")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
