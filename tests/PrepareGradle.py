"""Prepare the pinned Gradle distribution through the official GitHub asset API."""
from pathlib import Path
import hashlib
import os
import tempfile
import urllib.request
import zipfile

url = "https://services.gradle.org/distributions/gradle-8.7-bin.zip"
number = int.from_bytes(hashlib.md5(url.encode()).digest(), "big")
key = ""
while number:
    number, digit = divmod(number, 36)
    key = "0123456789abcdefghijklmnopqrstuvwxyz"[digit] + key
root = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle")) / "wrapper/dists/gradle-8.7-bin" / key
if (root / "gradle-8.7/bin/gradle").exists():
    print("Gradle 8.7 already available")
else:
    request = urllib.request.Request("https://api.github.com/repos/gradle/gradle-distributions/releases/assets/158106281", headers={"Accept": "application/octet-stream", "Authorization": "Bearer " + os.environ["GH_TOKEN"]})
    with urllib.request.urlopen(request, timeout=120) as response:
        data = response.read()
    if hashlib.sha256(data).hexdigest() != "544c35d6bd849ae8a5ed0bcea39ba677dc40f49df7d1835561582da2009b961d":
        raise ValueError("Official Gradle distribution checksum mismatch")
    root.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryFile() as archive:
        archive.write(data)
        archive.seek(0)
        with zipfile.ZipFile(archive) as package:
            package.extractall(root)
    for path in (root / "gradle-8.7/bin").iterdir():
        path.chmod(0o755)
    (root / "gradle-8.7-bin.zip.ok").touch()
    print("Official Gradle 8.7 downloaded and checksum verified")
