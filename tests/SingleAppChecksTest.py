"""Keep the removed parcel OCR/plugin surfaces out of the application and release."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
NAME = '{http://schemas.android.com/apk/res/android}name'


class SingleAppChecksTest(unittest.TestCase):
    def test_only_the_application_is_built(self):
        settings = (ROOT / 'settings.gradle').read_text(encoding='utf-8')
        self.assertIn("include ':app'", settings)
        self.assertNotIn('ocr-', settings)
        for workflow in ('android-checks.yml', 'release.yml'):
            text = (ROOT / '.github/workflows' / workflow).read_text(encoding='utf-8')
            self.assertNotIn(':ocr-', text)
            self.assertNotIn('OcrPluginChecks', text)

    def test_no_parcel_image_sharing_or_plugin_installer(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml')
        self.assertNotIn('android.permission.REQUEST_INSTALL_PACKAGES', [p.get(NAME) for p in manifest.findall('./uses-permission')])
        activities = manifest.findall('./application/activity')
        self.assertFalse(any('OcrPlugin' in a.get(NAME, '') for a in activities))
        parcel = next(a for a in activities if a.get(NAME) == '.ParcelActivity')
        mime = '{http://schemas.android.com/apk/res/android}mimeType'
        self.assertEqual(['text/plain'], [d.get(mime) for d in parcel.findall('./intent-filter/data')])
        page = (ROOT / 'app/src/main/java/cn/courselens/ParcelPage.java').read_text(encoding='utf-8')
        self.assertNotIn('OcrPluginActivity', page)
        self.assertNotIn('image/*', page)


if __name__ == '__main__':
    unittest.main()
