"""Actual emulator Telecom callbacks, not a mock of the repository. API 35 CI only."""
import json
import re
import subprocess
import time
from pathlib import Path

NUMBER = "+33612345678"
RUNNER = "fr.spamoff.test/androidx.test.runner.AndroidJUnitRunner"
results = []

def adb(*args, timeout=60):
    p = subprocess.run(["adb", *args], capture_output=True, text=True, timeout=timeout)
    if p.returncode:
        raise RuntimeError(p.stderr + p.stdout)
    return p.stdout

def phase(value):
    out = adb("shell", "am", "instrument", "-w", "-e", "class", "fr.spamoff.ScreeningHarnessTest", "-e", "phase", value, RUNNER)
    if "OK (1 test)" not in out or "FAILURES" in out:
        raise AssertionError(out)

def call_case(name, config, blocked):
    if config is not None:
        phase(config)
    try:
        adb("emu", "gsm", "call", NUMBER)
        time.sleep(3)
        registry = adb("shell", "dumpsys", "telephony.registry")
        states = re.findall(r"^\s*mCallState=(\d+)\s*$", registry, re.MULTILINE)
        if not states:
            raise AssertionError("No current TelephonyRegistry call state found")
        active = any(s in ("1", "2") for s in states)
        phase("assert-block" if blocked else "assert-allow")
        if active == blocked:
            raise AssertionError("Unexpected live telephony call states: " + str(states))
        results.append({"case": name, "result": "PASS"})
        print(name + ": PASS", flush=True)
    finally:
        subprocess.run(["adb", "emu", "gsm", "cancel", NUMBER], capture_output=True, timeout=30)

try:
    call_case("personal_blacklist", "block", True)
    call_case("whitelist_priority", "allow", False)
    call_case("protection_disabled", "disabled", False)
    adb("shell", "svc", "wifi", "disable")
    adb("shell", "svc", "data", "disable")
    call_case("offline_blocking", "block", True)
    adb("shell", "svc", "wifi", "enable")
    adb("shell", "svc", "data", "enable")
    phase("block")
    adb("reboot")
    adb("wait-for-device", timeout=120)
    deadline = time.monotonic() + 120
    while adb("shell", "getprop", "sys.boot_completed").strip() != "1":
        if time.monotonic() > deadline:
            raise TimeoutError("Reboot timeout")
        time.sleep(2)
    adb("shell", "input", "keyevent", "82")
    deadline = time.monotonic() + 120
    while True:
        holders = adb("shell", "cmd", "role", "get-role-holders", "--user", "0", "android.app.role.CALL_SCREENING")
        users = adb("shell", "dumpsys", "user")
        if "fr.spamoff" in holders and "RUNNING_UNLOCKED" in users:
            print("Retained screening role restored after reboot: " + holders.strip(), flush=True)
            break
        if time.monotonic() > deadline:
            raise TimeoutError("Screening role or unlocked user not restored after reboot: " + holders)
        time.sleep(2)
    time.sleep(2)
    call_case("blocking_after_reboot", None, True)
except Exception:
    p = Path("app/build/reports/telecom-diagnostics.txt")
    p.parent.mkdir(parents=True, exist_ok=True)
    parts = []
    for args in [("shell", "dumpsys", "role"), ("shell", "dumpsys", "user"), ("shell", "dumpsys", "telecom"), ("shell", "dumpsys", "telephony.registry"), ("logcat", "-d", "-t", "700")]:
        result = subprocess.run(["adb", *args], capture_output=True, text=True, timeout=60)
        parts.append(result.stdout + result.stderr)
    p.write_text("\n".join(parts))
    print("\n".join(parts)[-18000:], flush=True)
    raise
finally:
    adb("shell", "svc", "wifi", "enable")
    adb("shell", "svc", "data", "enable")
    phase("cleanup")
    destination = Path("app/build/reports/emulator-calls.json")
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(results, indent=2) + "\n")
