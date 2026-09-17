# HudRendererLib icon

## What this is

`icon.png` — the HudRendererLib mod icon, 1024 x 1024 PNG, SHA-256
`f2c61f3a4a5e7d6207644dee3e8da58fe5b989553ae435a7ee65336c071ed328`.

Copied byte-identically from `.local-icon-variants/provenance/from-round3/blender-z/hudrendererlib-06-library-mosaic.png`
(SHA-256 verified at the source, and again on the copy). The hash matches `png_sha256` in
`provenance/hudrendererlib-06-library-mosaic-metadata.json`.

## How it was made

Blender render, not a screenshot.

* Blender 5.1.1, headless `--background -noaudio --threads 2`, Cycles CPU (no GPU), 64 samples,
  1024 x 1024, 67.346 s. No display server, no audio sink, no Minecraft shader pack.
* Arrangement: the owner's skin head replaces the upper-right block of a two-by-two bookshelf
  mosaic — three complete unmodified vanilla bookshelf cubes plus the head. Heads use the exact
  reference base/outer head mesh and UVs (no sculpting, no stretched volumes), and positive-volume
  intersections are zero.
* Camera is orthographic and fixed: location `(6.928203582763672, -6.9282026290893555, 8.053203582763672)`,
  Euler rotation `(0.9553165435791016, -6.96e-08, 0.7853983044624329)` rad, `ortho_scale` 3.969080686569214.
  Head orientation is the NMSR one: front on screen-right, player-right side on screen-left, top visible.
* Imagery — **extracted from the real Minecraft client**, not shipped and not re-drawn. The assets come
  from the Minecraft **26.2** merged client jar at
  `~/.gradle/caches/fabric-loom/26.2/minecraft-merged.jar`:

  * `assets/minecraft/textures/block/bookshelf.png`
  * `assets/minecraft/textures/block/oak_planks.png`
  * `assets/minecraft/models/block/bookshelf.json`, `models/block/cube_column.json`, `models/block/cube.json`

  Byte-identical snapshots are in `provenance/assets/` and every snapshot hash is recorded in
  `provenance/asset-provenance.json`. The owner-supplied skin
  (SHA-256 `e9ebbeece495d9c96040e235dc865fdb1a530cf6a2243a6c8fcec22e72f03e3f`) and the reference head
  scene (`assets/reference-head.blend`, from `provenance/from-round3/blender-h/npc-addons-head-2.blend`) are also here.
  The client jar itself is not copied — re-extraction is described below.

## Provenance files

* `hudrendererlib-06-library-mosaic.py` — entry point (`scene.render('06-library-mosaic')`);
  `scene.py` — the shared scene author for the six candidate arrangements.
* `hudrendererlib-06-library-mosaic.blend` (packed, embeds its entry point and `scene.py`),
  `-metadata.json` (arrangement, geometry, camera, packed textures, render settings).
* `assets/` — the extracted client assets, the reference head scene and the supplied skin.
* `run-blender.py` — the recorded cgroup-limited CPU launcher; `inspect-source.py` — source inspection;
  `verify-scenes.py` (reopens the packed scene and asserts geometry/UV hashes) and `verify-pngs.py`
  (PNG decode + content checks).
* `asset-provenance.json`, `reproduction.json`, `script-hashes.json`, `manifest.json`,
  `png-verification.json`, `saved-scene-verification.json`, `render-report.json`, `visual-review.json`,
  `cleanup-report.json` — curated to the selected arrangement only (see `CURATION.json`).
* `evidence/*-run.json`, `evidence/reference-inspection.json`, `evidence/live-resource-snapshot.json` —
  machine-readable run and inspection records.
* `CURATION.json` — what was copied, which aggregate rows were dropped, and what was left in round-3.

## How to regenerate

From `<repo>/docs/icon/provenance`:

```sh
python3 run-blender.py hudrendererlib-06-library-mosaic.py
python3 verify-pngs.py
python3 verify-scenes.py
```

`run-blender.py` requires the pre-existing systemd user unit `render-blender.service` with
`cpu.weight = 20` and `cpu.max` quota <= 3 cores: it moves itself into that cgroup, pins the render to
two CPUs and asserts the limits. The direct equivalent it executes is:

```sh
taskset -c 0,1 nix shell nixpkgs#blender --command blender --background -noaudio \
  --threads 2 --python-exit-code 1 --python hudrendererlib-06-library-mosaic.py
```

`verify-pngs.py` needs Pillow available on `PYTHONPATH`. Re-running overwrites
`hudrendererlib-06-library-mosaic.png`.

If the extracted assets are ever lost, re-extract them from the client jar with the members listed in
`asset-provenance.json`, saving each member to the recorded `path` under `assets/`.

## Notes

* Only the selected arrangement is shipped. The five retired arrangements (`hudrendererlib-01` …
  `05`, see `round3/removals.json`) and their scene scripts, packed scenes and metadata are not copied,
  and the aggregate verification JSONs here retain only the `06-library-mosaic` rows.
* Excluded: run logs, the 128 px review preview (`evidence/hudrendererlib-06-library-mosaic-128.png`),
  the six-arrangement evidence sheets and the empty blocker list.
* Nothing else in the mod repository was modified and nothing was committed.

## Working-tree note

The round-3 working tree that produced this icon was cleaned up after integration. Every file needed to regenerate the icon was copied into `provenance/`; the copies live under `provenance/from-round3/` when they came from the working tree. Any remaining `round3/...` mention records where something came from, not a path that still exists.
