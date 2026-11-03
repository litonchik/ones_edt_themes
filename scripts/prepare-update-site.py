#!/usr/bin/env python3
"""Publish the verified contents of a release ZIP as a static Eclipse p2 site."""

import argparse
import hashlib
import html
import io
import json
from pathlib import Path, PurePosixPath
import re
import stat
import xml.etree.ElementTree as ET
import zipfile


BUNDLE = "ru.litonchik.edt.themes"
FEATURE = BUNDLE + ".feature"


def prepare_site(archive, checksum, version, output):
    archive, checksum, output = Path(archive), Path(checksum), Path(output)
    if not re.fullmatch(r"\d+\.\d+\.\d+", version):
        raise ValueError("Only stable three-part release versions are supported")
    expected_name = f"edt-themes-{version}.zip"
    if archive.name != expected_name:
        raise ValueError("Archive name does not match the release version")
    checksum_match = re.fullmatch(
        r"([0-9a-fA-F]{64})[ \t]+\*?" + re.escape(expected_name) + r"\s*",
        checksum.read_text(encoding="utf-8-sig"),
    )
    actual_hash = hashlib.sha256(archive.read_bytes()).hexdigest()
    if not checksum_match or checksum_match[1].lower() != actual_hash:
        raise ValueError("Release ZIP SHA-256 verification failed")
    if output.exists() and any(output.iterdir()):
        raise ValueError("Output directory must be empty")

    with zipfile.ZipFile(archive) as package:
        files = {}
        seen = set()
        for entry in package.infolist():
            path = PurePosixPath(entry.filename)
            if (path.is_absolute() or ".." in path.parts or "\\" in entry.filename
                    or ":" in entry.filename or stat.S_ISLNK(entry.external_attr >> 16)
                    or entry.filename.casefold() in seen):
                raise ValueError("Unsafe or duplicate archive entry")
            seen.add(entry.filename.casefold())
            if not entry.is_dir():
                files[entry.filename] = entry
        required = {"p2.index", "content.jar", "artifacts.jar"}
        if not required.issubset(files):
            raise ValueError("Missing p2 metadata")
        metadata = required | {"content.xml.xz", "artifacts.xml.xz"}
        bundles = [name for name in files if name.startswith("plugins/")]
        features = [name for name in files if name.startswith("features/")]
        if len(bundles) != 1 or len(features) != 1:
            raise ValueError("Only one project bundle and one feature may be published")
        bundle_match = re.fullmatch(
            r"plugins/" + re.escape(BUNDLE) + r"_(" + re.escape(version) + r"\.[A-Za-z0-9_-]+)\.jar",
            bundles[0],
        )
        if not bundle_match or features[0] != f"features/{FEATURE}_{bundle_match[1]}.jar":
            raise ValueError("Bundle/feature IDs or versions do not match the release")
        if set(files) - metadata - set(bundles) - set(features):
            raise ValueError("Unexpected files in the p2 repository")
        if sum(entry.file_size for entry in files.values()) > 50 * 1024 * 1024:
            raise ValueError("Unexpectedly large release archive")
        with zipfile.ZipFile(io.BytesIO(package.read("artifacts.jar"))) as artifact_metadata:
            root = ET.fromstring(artifact_metadata.read("artifacts.xml"))
        artifacts = {(entry.get("id"), entry.get("version")) for entry in root.findall("./artifacts/artifact")}
        if artifacts != {(BUNDLE, bundle_match[1]), (FEATURE, bundle_match[1])}:
            raise ValueError("Artifact metadata differs from the packaged project bundles")
        with zipfile.ZipFile(io.BytesIO(package.read("content.jar"))) as content_metadata:
            content = ET.fromstring(content_metadata.read("content.xml"))
        if not any(unit.get("id") == FEATURE + ".feature.group" and unit.get("version") == bundle_match[1]
                   for unit in content.findall("./units/unit")):
            raise ValueError("Installable feature is missing from p2 content metadata")

        repository = output / "repository"
        repository.mkdir(parents=True, exist_ok=True)
        for name in files:
            destination = repository.joinpath(*PurePosixPath(name).parts)
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(package.read(name))

    site_url = "https://litonchik.github.io/ones_edt_themes/repository/"
    release_url = f"https://github.com/litonchik/ones_edt_themes/releases/tag/v{version}"
    metadata = {"version": version, "bundle_version": bundle_match[1], "archive": expected_name,
                "sha256": actual_hash, "release_url": release_url}
    (repository / "release.json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    page = f"""<!doctype html>
<html lang="ru"><meta charset="utf-8"><meta name="viewport" content="width=device-width">
<title>Темы для 1С:EDT — сайт обновлений</title>
<body style="font:18px/1.6 system-ui;max-width:850px;margin:48px auto;padding:0 24px">
<h1>Темы для 1С:EDT</h1><p>Версия {html.escape(version)}. 11 тёмных палитр, настройка цветов и шрифта.</p>
<p>В EDT откройте <strong>Справка → Установить новое ПО… → Добавить…</strong>
и укажите адрес:</p><p><code>{site_url}</code></p>
<p>Выберите «Темы EDT — цветовые палитры», завершите установку и перезапустите EDT.</p>
<p>Проверенные Windows x64: EDT 2026.1.2+2 / 2026.1.3+25 с Java 17 и 2026.2.2+21 с Java 25.</p>
<p><a href="{release_url}">Релиз и ZIP для офлайн-установки</a> ·
<a href="https://github.com/litonchik/ones_edt_themes">Исходники, инструкция и ограничения</a></p>
</body></html>
"""
    for page_path in (output / "index.html", repository / "index.html"):
        page_path.write_text(page, encoding="utf-8")
    (output / ".nojekyll").touch()
    return metadata


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--zip", required=True, type=Path)
    parser.add_argument("--checksum", required=True, type=Path)
    parser.add_argument("--version", required=True)
    parser.add_argument("--output", required=True, type=Path)
    arguments = parser.parse_args()
    result = prepare_site(arguments.zip, arguments.checksum, arguments.version, arguments.output)
    print(json.dumps(result, indent=2))
