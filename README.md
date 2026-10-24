# RTX Remix for Minecraft: Radeon / 26.3 Fork

An unofficial community fork of
[NVIDIA RTX Remix for Minecraft](https://github.com/NVIDIAGameWorks/rtx-remix-minecraft)
that aims to run its real-time path tracing on **AMD Radeon GPUs** and on
**Minecraft Java Edition 26.3**, the current release.

> **This fork is not affiliated with, endorsed by, or supported by NVIDIA.**
> Report problems with this fork here, not to the upstream project.

## Status

Work in progress. The fork does not yet run on Radeon or on Minecraft 26.3,
and there is no release. Until there is, the code and the guides under
`docs/` describe upstream's behaviour: Minecraft 1.21.1 on a GeForce RTX GPU.

## Goals

- **Radeon support.** Run the path tracer on Radeon GPUs with hardware ray
  tracing, through a dxvk-remix runtime built from source. Most of this work
  is in the runtime rather than in the mod.
- **Minecraft 26.3.** Port the mod from 1.21.1 to the current Minecraft
  release. The mod hooks deep into vanilla's renderer and chunk pipeline, so
  that is a port of many mixins, not just a version bump.
- **Keep GeForce working.** NVIDIA's SDK terms require applications built on
  its RTX SDKs to stay interoperable with NVIDIA GPUs. The fork adds Radeon
  support alongside GeForce rather than replacing it.

### NVIDIA-only components

The upstream runtime carries components that are licensed or built only for
NVIDIA GPUs: DLSS (including Ray Reconstruction and Frame Generation), the
Neural Radiance Cache, Reflex, RTX IO, Nsight Aftermath, and the CUDA runtime.
Radeon builds of this fork do not ship or use them, and need vendor-neutral
alternatives where a feature depends on them. See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the upstream inventory.

## What upstream does

**Every block, mob, and item is lit by a path tracer.** Light bounces off walls,
through glass, and out of torches the way it does in a film renderer, so
shadows, reflections, and the colour a nearby block casts on its neighbours are
all computed rather than painted in.

**The look follows the game.** Time of day, weather, and being underwater each
bring their own lighting, and a map author can attach a look to a place in the
world.

**A new block.** The Copper Beacon is a directional lamp you can place or
carry, and its beam is a real light.

**Camera flythroughs.** Record a path by walking it, then replay it, for a video
or for an exact before-and-after comparison.

**A settings screen in the game.** Distances, framerate, sky, lighting,
exposure, and materials, all in Options, plus a button into Remix's own
Graphics Settings.

## Building From Source

The build is unchanged from upstream for now; see
[Getting Started](docs/GETTING_STARTED.md). It still fetches NVIDIA's pinned
runtime and needs a GeForce RTX GPU to run what it builds.

## Source And Tooling Layout

| Path | Purpose |
|---|---|
| `src/` | Mod implementation and JVM tests |
| `rtxremix-java/` | Minecraft-agnostic Java facade and JNI bridge over the RTX Remix C API |
| `tools/` | Developer launcher, benchmarks, profiling, SDK staging, and development installation |
| `packman/` | Packman bootstrap and runtime package pins |
| `packaging/` | Public installer, runtime defaults, and distribution payload |
| `docs/` | Player, authoring, and source-build guides (upstream's, for now) |

## Showcase World And Art Pack

Upstream's developer launcher downloads two optional extras: a showcase world
built by [BlockWorks](https://www.blockworks.uk/) and an RTX art pack made by
an NVIDIA art team. Neither is part of this repository, and this fork does not
redistribute them.

## Contributing

Issues and pull requests are welcome in
[this fork](https://github.com/quartz1216/rtx-remix-minecraft-radeon-26.3).
Fixes that also apply to upstream's 1.21.1 GeForce build are better sent
[upstream](https://github.com/NVIDIAGameWorks/rtx-remix-minecraft), which asks
contributors to sign off each commit with `git commit -s` under the
[Developer Certificate of Origin](https://developercertificate.org/).

## License

This fork, like upstream, is licensed under the Apache License, Version 2.0
(`Apache-2.0`). See the [LICENSE](LICENSE) file. Upstream's attribution notices
are kept in [NOTICE](NOTICE), and third-party software is documented in
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). NOTICE also states that this
fork modifies the original work; the changes are recorded in Git.

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR
MICROSOFT. Minecraft is a trademark of Mojang Synergies AB.

NVIDIA, GeForce, RTX, and RTX Remix are trademarks and/or registered trademarks
of NVIDIA Corporation in the U.S. and other countries. AMD and Radeon are
trademarks of Advanced Micro Devices, Inc. Their use here only identifies the
hardware and software this fork works with.
