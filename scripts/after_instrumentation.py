"""AGP/UTP may uninstall target packages after connected tests: reinstall for host scenarios."""
import subprocess
import sys
import shutil
from pathlib import Path

api = sys.argv[1]

def run(*args, timeout=120):
    p = subprocess.run(args, capture_output=True, text=True, timeout=timeout)
    print(p.stdout, flush=True)
    if p.returncode:
        raise RuntimeError(p.stderr + p.stdout)
    return p.stdout

run("adb", "install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
run("adb", "install", "-r", "-t", "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk")
output = run("adb", "shell", "am", "instrument", "-w", "-e", "class", "fr.spamoff.ActivitySmokeTest", "fr.spamoff.test/androidx.test.runner.AndroidJUnitRunner")
if "OK (1 test)" not in output or "FAILURES" in output:
    raise AssertionError(output)
shots = Path("app/build/reports/screenshots")
shots.mkdir(parents=True, exist_ok=True)
image = subprocess.run(["adb", "exec-out", "run-as", "fr.spamoff", "cat", f"files/home-api-{api}.png"], capture_output=True, check=True, timeout=60)
(shots / f"home-api-{api}.png").write_bytes(image.stdout)
if api == "35":
    packages = run("adb", "shell", "pm", "list", "packages")
    dialer = next((p for p in ("com.google.android.dialer", "com.android.dialer") if "package:" + p + "\n" in packages), None)
    if dialer is None:
        raise AssertionError("The Telecom emulator must have an ordinary dialer installed")
    run("adb", "shell", "cmd", "role", "add-role-holder", "--user", "0", "android.app.role.DIALER", dialer)
    print("TEST_DIALER=" + dialer, flush=True)
    subprocess.run([sys.executable, "scripts/emulator_calls.py"], check=True, timeout=600)
    subprocess.run(["./gradlew", "assembleBeta"], check=True, timeout=900)
    subprocess.run([sys.executable, "scripts/optimized_smoke.py"], check=True, timeout=600)
    apks = Path("app/build/reports/verified-apks")
    apks.mkdir(parents=True, exist_ok=True)
    shutil.copyfile("app/build/outputs/apk/debug/app-debug.apk", apks / "app-debug-tested.apk")
    shutil.copyfile("app/build/outputs/apk/beta/app-beta.apk", apks / "app-beta-tested.apk")
