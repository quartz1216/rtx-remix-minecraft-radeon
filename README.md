# RTX Remix for Minecraft: Radeon / 26.3 Fork

An unofficial community fork of
[NVIDIA RTX Remix for Minecraft](https://github.com/NVIDIAGameWorks/rtx-remix-minecraft)
that aims to run its real-time path tracing on **AMD Radeon GPUs** and on
**Minecraft Java Edition 26.3**, the current release.

> **This fork is not affiliated with, endorsed by, or supported by NVIDIA.**
> Report problems with this fork here, not to the upstream project.

## Status

Work in progress, with no release yet. The mod still targets Minecraft
1.21.1, and the guides under `docs/` still describe upstream's behaviour on a
GeForce RTX GPU.

On Radeon, the mod and NVIDIA's pinned runtime already start and path-trace.
So far this has been tested on an RX 9070 XT only. The runtime detects the AMD
driver by itself: it switches to AMD's ray tracing modes, defaults to the Low
preset, and turns off DLSS, the Neural Radiance Cache, opacity micromaps, and
shader execution reordering. Known Radeon issues:

| Issue | State |
|---|---|
| Distant terrain renders mirror-like or black outside a sphere around the camera, or as noise after a resource reload | Fixed for block terrain. The runtime never writes the lower mip levels it generates on the GPU, so the mod builds the block atlas mips on the CPU instead (`rtxremix.texture.cpuAtlasMips`). Normal and height maps, entity textures, and animated blocks still use runtime-generated mips |
| GPU device loss after switching the upscaler from XeSS to TAA-U | Seen once in five tries |
| Crash after switching the GBuffer or direct-lighting Raytrace Mode to a raygen (RGS) variant in the developer menu | Reproducible; keep the defaults |
| The first world load stalls for tens of seconds while shaders compile | The developer launcher discards the pipeline cache on every launch, so the stall repeats |
| Radeon GPU Detective crash analysis crashes the game | Leave it off for this runtime |

The pinned runtime implements Remix API 0.16 and is built from a dxvk-remix
branch that NVIDIA has not published. Public dxvk-remix is at API 0.6 and
lacks functions the mod depends on. Until the matching source is public,
runtime bugs can only be worked around from the mod.

## Goals

- **Radeon support.** Run the path tracer on Radeon GPUs with hardware ray
  tracing. For now that means working around runtime bugs from the mod. Once
  NVIDIA publishes the matching dxvk-remix source, the fixes move into a
  runtime built from that source.
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
[Getting Started](docs/GETTING_STARTED.md). It fetches NVIDIA's pinned
runtime, which runs on a GeForce RTX GPU or, with the issues above, on a
Radeon GPU with hardware ray tracing. The guide's GeForce requirement predates
this fork.

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
