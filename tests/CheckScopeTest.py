import subprocess
import tempfile
import unittest
from pathlib import Path
from CheckScope import check_scope


class CheckScopeTest(unittest.TestCase):
    def test_config_only_and_fail_closed(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            def git(*args):
                return subprocess.check_output(["git", "-C", directory, *args], stderr=subprocess.DEVNULL).decode().strip()
            git("init", "-q")
            git("config", "user.name", "test")
            git("config", "user.email", "test@example.invalid")
            (repo / "remote").mkdir()
            (repo / "remote/config.json").write_text("{}")
            (repo / "app.java").write_text("old")
            git("add", ".")
            git("commit", "-qm", "base")
            base = git("rev-parse", "HEAD")
            (repo / "remote/config.json").write_text('{"changed":true}')
            git("add", ".")
            git("commit", "-qm", "config")
            config = git("rev-parse", "HEAD")
            self.assertEqual("config", check_scope("pull_request", base, config, directory))
            self.assertEqual("full", check_scope("push", base, config, directory))
            self.assertEqual("full", check_scope("workflow_dispatch", base, config, directory))
            self.assertEqual("full", check_scope("pull_request", base, base, directory))
            self.assertEqual("full", check_scope("pull_request", "missing", config, directory))
            (repo / "app.java").write_text("new")
            git("add", ".")
            git("commit", "-qm", "mixed")
            self.assertEqual("full", check_scope("pull_request", base, git("rev-parse", "HEAD"), directory))
            (repo / "README.md").write_text("docs")
            (repo / "docs").mkdir()
            (repo / "docs/guide.md").write_text("guide")
            git("add", ".")
            git("commit", "-qm", "docs")
            docs = git("rev-parse", "HEAD")
            self.assertEqual("docs", check_scope("pull_request", git("rev-parse", "HEAD^"), docs, directory))
            self.assertEqual("full", check_scope("pull_request", base, docs, directory))
            self.assertEqual("full", check_scope("push", git("rev-parse", "HEAD^"), docs, directory))
            (repo / ".github/workflows").mkdir(parents=True)
            (repo / ".github/workflows/test.md").write_text("workflow path")
            git("add", ".")
            git("commit", "-qm", "excluded path")
            self.assertEqual("full", check_scope("pull_request", docs, git("rev-parse", "HEAD"), directory))
            git("mv", "remote/config.json", "remote/other.json")
            git("commit", "-qm", "rename")
            self.assertEqual("full", check_scope("pull_request", config, git("rev-parse", "HEAD"), directory))


if __name__ == "__main__":
    unittest.main()
