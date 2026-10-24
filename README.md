# On-Device Unit Conversion and File Optimization in an Android Application: A Source-Level Study

[![Paper DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.23600900.svg)](https://doi.org/10.5281/zenodo.23600900)

**Read the paper:** [10-page PDF](paper/paper3_review.pdf) · [LaTeX source](paper/main.tex)

This public preprint is published on Zenodo ([DOI: 10.5281/zenodo.23600900](https://doi.org/10.5281/zenodo.23600900)), and has not been peer reviewed. Cite the paper for its analysis and this repository for code reuse. No journal publication is claimed.

This repository pairs a source-level research paper with selected, unchanged source files from the Vanexa Android application. The files under `android-source/` show the converter, image re-encoder, PDF processor, and their UI call paths. They are evidence snapshots, not a standalone Android application. Building them requires the original app's Android, Compose, PDFBox-Android, Apache POI, and coroutine dependencies.

The actual converter model and use case are also copied into `src/main/kotlin/` so their original unit tests can run independently on the JVM. Use JDK 17 and Gradle 8.11.1, then run `./gradlew test`. This tests numerical conversion only. It does not establish image fidelity, PDF integrity, physical-device performance, or that the entire application operates offline. The [paper](paper/paper3_review.pdf) records those limitations and a validation protocol.

The selected research code is released under [MIT](LICENSE). The manuscript source and PDF in `paper/` are licensed under [CC BY 4.0](paper/LICENSE). Author: Ikbal Fadilah ([ORCID](https://orcid.org/0009-0008-4853-135X)). See `ANDROID_SOURCE_MANIFEST.json` for relative paths and hashes of the original source files.

