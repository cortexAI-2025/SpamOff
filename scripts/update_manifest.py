from pathlib import Path
import hashlib
import json
root = Path(__file__).resolve().parents[1]
body = (root / "data/rules.json").read_bytes()
rules = json.loads(body)
manifest = json.dumps({"schema": 1, "version": rules["version"], "sha256": hashlib.sha256(body).hexdigest()}, indent=2) + "\n"
(root / "data/manifest.json").write_text(manifest)
(root / "app/src/main/assets/manifest.json").write_text(manifest)
(root / "app/src/main/assets/rules.json").write_bytes(body)
print("Manifest and bundled seed synchronized")
