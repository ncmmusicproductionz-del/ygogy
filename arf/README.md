# Atomics Render Framework (ARF)

ZombieBuddy-dependent Java mod that replaces Project Zomboid's isometric presentation with a true 3D view
(first/third person, deferred PBR, Iris-style shader packs). Simulation stays vanilla; ARF only changes how the
world is drawn and how camera/movement input is mapped. Built from *Atomics Render Framework - Design* (Oct 7, 2026).

    mvn test        # 38 unit tests
    mvn package     # target/ARF.jar  (copy into mod/Contents/mods/ARF/42/ for the Workshop layout)

## Status vs. the design's roadmap
| Milestone | State |
|---|---|
| **M1 Core** - bootstrap, WorldSnapshot, first-person camera | Logic done and tested. **Not wired to the game** (see below). Forward renderer + procedural walls/floors: provider chain done, GL drawing not started |
| **M2 PBR** - G-buffer, GGX, cascades, clustered lights, post | `FramePass` graph, `RenderDevice` interface and default-pack GLSL written. **No OpenGL backend yet**, GLSL never compiled on a GPU |
| **M3 Packs** - loader, options, hot reload, iris-compat | Loader, options.json to `#define`, `#include`, profiles, all-or-nothing reload, `iris-compat.glsl` done. Missing: options UI, F8 file watcher, zip packs |
| **M4 Content** - mesh providers, materials, 3rd person, LOD | Mesh provider chain, material registry/defaults, third-person spring arm done. LOD/impostors not started |
| **M5 Release** | Not started |

## Layout
| Package | Design section |
|---|---|
| `core` | Hard dependency: `ArfBootstrap` (inert on any failed check), `PatchTargets` (conflict detection) |
| `snapshot` | `WorldSnapshot` + lock-free `SnapshotBuffer` (only thing crossing main to render thread) |
| `camera` | `CameraRig` (FP/TP/free/iso, 0.25 s blends, FOV 60-110), `InputMapper` (yaw remap, clears held keys on every switch), `AimRay` |
| `render` | `RenderDevice` (Vulkan-ready seam), `FramePass` (8 deferred passes in order) |
| `pack` | Shader packs: `ShaderPack`, `ShaderPreprocessor`, `PackManager`, `ArfUniforms` (API 1), dependency-free `Json` |
| `content` | `MeshResolver` (explicit, procedural, billboard, fallback box), `MaterialRegistry` |
| `resources/arf` | Default pack (`defaultpack/`) and standard library (`lib/`: pbr, lighting, noise, iris-compat) |

## What is NOT done (needs your game install)
- **ZombieBuddy wiring.** I had no ZombieBuddy jar or Project Zomboid build to compile against, so there are no
  `@Patch` classes or `arf.*` Lua bridge. `core/PatchTargets` lists *placeholder* class names; confirm them against
  a B42 install, then add thin patch classes that feed `CameraRig`, `InputMapper`, `SnapshotBuffer`, and `MeshResolver`.
- **OpenGL 4.3 backend** for `RenderDevice` (LWJGL, via PZ's existing context).
- **Shaders are unvalidated.** Run them through `glslangValidator` or a real GL 4.3 context before trusting them.
- Sphere-cast collision (`CollisionProbe`) must be supplied from PZ tile data.
