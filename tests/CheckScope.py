"""Select lightweight gates for narrowly scoped pull requests."""
import os
import subprocess


def check_scope(event, base, head, cwd=None):
    if event != "pull_request" or not base or not head:
        return "full"
    try:
        result = subprocess.run(
            ["git", "diff", "--no-renames", "--name-only", "-z", base, head, "--"],
            cwd=cwd, capture_output=True, check=True,
        )
    except (OSError, subprocess.CalledProcessError):
        return "full"
    paths = result.stdout.split(b"\0")[:-1]
    if paths == [b"remote/config.json"]:
        return "config"
    if paths and all(
        (b"/" not in path or path.startswith(b"docs/"))
        and path.endswith(b".md")
        for path in paths
    ):
        return "docs"
    return "full"


if __name__ == "__main__":
    print(check_scope(os.getenv("EVENT_NAME"), os.getenv("BASE_SHA"), os.getenv("HEAD_SHA")))
