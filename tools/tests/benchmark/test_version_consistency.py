# SPDX-FileCopyrightText: Copyright (c) 2026 NVIDIA CORPORATION & AFFILIATES. All rights reserved.
# SPDX-License-Identifier: Apache-2.0

import re
import tomllib
import unittest
from pathlib import Path

from rtx_tools.benchmark import util

REPO_ROOT = util.PROJECT_DIR


def gradle_mod_version() -> str:
    for line in (REPO_ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        if line.startswith("mod_version="):
            return line.split("=", 1)[1].strip()
    raise AssertionError("gradle.properties has no mod_version")


def gradle_def(name: str) -> str:
    text = (REPO_ROOT / "build.gradle").read_text(encoding="utf-8")
    match = re.search(rf"(?m)^def {re.escape(name)} = '([^']+)'$", text)
    if not match:
        raise AssertionError(f"build.gradle has no def {name}")
    return match.group(1)


def powershell_literal(path: Path, variable: str) -> str:
    text = path.read_text(encoding="utf-8")
    match = re.search(rf"\${re.escape(variable)}\s*=\s*'([^']+)'", text)
    if not match:
        raise AssertionError(f"{path} has no ${variable} string assignment")
    return match.group(1)


class VersionConsistencyTests(unittest.TestCase):
    def test_packaging_version_defaults_match_gradle(self):
        expected = gradle_mod_version()
        self.assertEqual(
            expected,
            powershell_literal(REPO_ROOT / "packaging" / "installer.ps1", "script:APP_VERSION"),
        )

    def test_runtime_dependency_versions_match_gradle(self):
        expected = {
            "minecraft": gradle_def("minecraftVersion"),
            "neoforge": gradle_def("neoForgeVersion"),
        }
        installer = REPO_ROOT / "packaging" / "installer.ps1"

        for path, variables in (
            (
                installer,
                {
                    "minecraft": "script:MC_VERSION",
                    "neoforge": "script:NEOFORGE_VERSION",
                },
            ),
        ):
            with self.subTest(path=path):
                for component, variable in variables.items():
                    self.assertEqual(expected[component], powershell_literal(path, variable))

    def test_runtime_code_does_not_hardcode_mod_version(self):
        benchmark_controller = (
            REPO_ROOT
            / "src"
            / "main"
            / "java"
            / "com"
            / "nvidia"
            / "rtxremix"
            / "minecraft"
            / "benchmark"
            / "BenchmarkController.java"
        ).read_text(encoding="utf-8")
        mod_entrypoint = (
            REPO_ROOT / "src" / "main" / "java" / "com" / "nvidia" / "rtxremix" / "minecraft" / "RtxRemixMod.java"
        ).read_text(encoding="utf-8")
        self.assertNotRegex(benchmark_controller, r'modVersion",\s*"\d+\.\d+\.\d+"')
        self.assertNotRegex(mod_entrypoint, r"Renderer v\d+\.\d+\.\d+")

    def test_neoforge_metadata_matches_repository_contract(self):
        metadata = tomllib.loads(
            (REPO_ROOT / "src" / "main" / "resources" / "META-INF" / "neoforge.mods.toml").read_text(encoding="utf-8")
        )
        build = (REPO_ROOT / "build.gradle").read_text(encoding="utf-8")

        self.assertEqual("Apache-2.0", metadata["license"])
        (mod,) = metadata["mods"]
        # NeoForge mod ids cannot hold hyphens; the content namespace keeps them.
        self.assertRegex(mod["modId"], r"^[a-z][a-z0-9_]{1,63}$")
        self.assertIn(f"'{mod['modId']}' {{", build)
        dependencies = {d["modId"]: d for d in metadata["dependencies"][mod["modId"]]}
        # processResources fills both ranges from the build's pins.
        self.assertEqual("[${neoForgeVersion},)", dependencies["neoforge"]["versionRange"])
        self.assertEqual("[${minecraftVersion}]", dependencies["minecraft"]["versionRange"])
        self.assertEqual({"required"}, {d["type"] for d in dependencies.values()})
        self.assertEqual(["rtx-remix-renderer.mixins.json"], [m["config"] for m in metadata["mixins"]])

        logo = mod.get("logoFile")
        if logo:
            self.assertTrue((REPO_ROOT / "src" / "main" / "resources" / logo).is_file())

        self.assertNotIn("rtxremix-java", mod.get("displayURL", ""))

    def test_readme_license_matches_repository_license(self):
        license_text = (REPO_ROOT / "LICENSE").read_text(encoding="utf-8")
        readme = (REPO_ROOT / "README.md").read_text(encoding="utf-8")

        self.assertIn("Apache License", license_text)
        self.assertIn("Apache-2.0", readme)
        self.assertNotRegex(readme, r"(?m)^MIT\s+[\u2014-]\s+See LICENSE file$")


if __name__ == "__main__":
    unittest.main()
