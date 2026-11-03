import hashlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
import zipfile


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("update_site", ROOT / "scripts/prepare-update-site.py")
SITE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SITE)


def jar(name, content):
    result = io.BytesIO()
    with zipfile.ZipFile(result, "w") as archive:
        archive.writestr(name, content)
    return result.getvalue()


class PublicationTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.archive = self.root / "edt-themes-0.1.16.zip"
        self.checksum = self.root / "edt-themes-0.1.16.zip.sha256"
        self.output = self.root / "site"
        self.version = "0.1.16.202610292155"
        self.entries = {
            "p2.index": "version = 1\n",
            "plugins/ru.litonchik.edt.themes_" + self.version + ".jar": b"bundle",
            "features/ru.litonchik.edt.themes.feature_" + self.version + ".jar": b"feature",
            "artifacts.jar": jar("artifacts.xml", '<repository><artifacts>'
                f'<artifact id="{SITE.BUNDLE}" version="{self.version}"/>'
                f'<artifact id="{SITE.FEATURE}" version="{self.version}"/>'
                '</artifacts></repository>'),
            "content.jar": jar("content.xml", '<repository><units>'
                f'<unit id="{SITE.FEATURE}.feature.group" version="{self.version}"/>'
                '</units></repository>'),
        }

    def package(self):
        with zipfile.ZipFile(self.archive, "w") as archive:
            for name, content in self.entries.items():
                archive.writestr(name, content)
        self.checksum.write_text(hashlib.sha256(self.archive.read_bytes()).hexdigest()
                                 + "  " + self.archive.name + "\n", encoding="utf-8")

    def prepare(self):
        return SITE.prepare_site(self.archive, self.checksum, "0.1.16", self.output)

    def test_preserves_repository_bytes(self):
        self.package()
        metadata = self.prepare()
        self.assertEqual(metadata["bundle_version"], self.version)
        with zipfile.ZipFile(self.archive) as archive:
            for name in self.entries:
                self.assertEqual((self.output / "repository" / name).read_bytes(), archive.read(name))

    def test_rejects_corrupted_download_before_writing(self):
        self.package()
        with self.archive.open("ab") as archive:
            archive.write(b"corruption")
        with self.assertRaisesRegex(ValueError, "SHA-256"):
            self.prepare()
        self.assertFalse(self.output.exists())

    def test_rejects_path_traversal(self):
        self.entries["../outside.txt"] = "unexpected"
        self.package()
        with self.assertRaisesRegex(ValueError, "Unsafe"):
            self.prepare()
        self.assertFalse((self.root / "outside.txt").exists())

    def test_rejects_vendor_bundle(self):
        self.entries["plugins/org.eclipse.vendor_1.0.0.jar"] = "vendor"
        self.package()
        with self.assertRaisesRegex(ValueError, "one project bundle"):
            self.prepare()

    def test_rejects_mismatched_metadata(self):
        self.entries["artifacts.jar"] = jar("artifacts.xml", "<repository><artifacts/></repository>")
        self.package()
        with self.assertRaisesRegex(ValueError, "Artifact metadata"):
            self.prepare()

    def test_rejects_overwriting_an_existing_site(self):
        self.package()
        self.output.mkdir()
        (self.output / "keep.txt").write_text("keep")
        with self.assertRaisesRegex(ValueError, "empty"):
            self.prepare()
        self.assertEqual((self.output / "keep.txt").read_text(), "keep")


if __name__ == "__main__":
    unittest.main()
