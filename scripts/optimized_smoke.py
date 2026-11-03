"""Smoke the optimized beta through its UI and Telecom; do not inject a test runtime."""
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

NUMBER = "+33612345678"
results = []

def adb(*args, timeout=60):
    p = subprocess.run(["adb", *args], capture_output=True, text=True, timeout=timeout)
    if p.returncode:
        raise RuntimeError(p.stderr + p.stdout)
    return p.stdout

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/spamoff-ui.xml")
    return list(ET.fromstring(adb("shell", "cat", "/sdcard/spamoff-ui.xml")).iter("node"))

def tap_node(predicate):
    for node in nodes():
        if predicate(node):
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
            adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
            time.sleep(0.7)
            return
    raise AssertionError("UI node not found")

def text(label):
    tap_node(lambda n: n.get("text") == label)

def configure(allow):
    text("Listes")
    tap_node(lambda n: n.get("class") == "android.widget.EditText")
    adb("shell", "input", "text", NUMBER)
    adb("shell", "input", "keyevent", "4")
    text("Autoriser" if allow else "Bloquer")
    time.sleep(1)
    text("Accueil")

def call(blocked):
    try:
        adb("emu", "gsm", "call", NUMBER)
        time.sleep(5)
        calls = adb("emu", "gsm", "list")
        assert (NUMBER.lstrip("+") not in calls) == blocked, calls
    finally:
        subprocess.run(["adb", "emu", "gsm", "cancel", NUMBER], capture_output=True, timeout=30)

try:
    out = adb("install", "-r", "app/build/outputs/apk/beta/app-beta.apk", timeout=120)
    assert "Success" in out, out
    adb("shell", "am", "start", "-n", "fr.spamoff/.MainActivity")
    time.sleep(3)
    assert any(n.get("text") == "SpamOff" for n in nodes())
    assert any(n.get("text") == "Protection activée" for n in nodes())
    results.append({"case": "optimized_install_and_launch", "result": "PASS"})
    text("Actualiser les listes")
    deadline = time.monotonic() + 70
    while time.monotonic() < deadline:
        texts = [n.get("text", "") for n in nodes()]
        if "Listes vérifiées et actualisées" in texts:
            results.append({"case": "optimized_https_update", "result": "PASS"})
            break
        if any("Actualisation impossible" in t for t in texts):
            raise AssertionError("Optimized HTTPS update failed")
        time.sleep(0.5)
    else:
        raise TimeoutError("Update confirmation not observed")
    configure(False)
    call(True)
    results.append({"case": "optimized_blacklist_callback", "result": "PASS"})
    configure(True)
    call(False)
    results.append({"case": "optimized_whitelist_callback", "result": "PASS"})
    text("Listes")
    text("Supprimer")
    text("Accueil")
    destination = Path("app/build/reports/screenshots/beta-home-api35.png")
    destination.parent.mkdir(parents=True, exist_ok=True)
    image = subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True, check=True, timeout=30)
    destination.write_bytes(image.stdout)
    print("OPTIMIZED_SMOKE_OK", flush=True)
finally:
    p = Path("app/build/reports/optimized-smoke.json")
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(results, indent=2) + "\n")
