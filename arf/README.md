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

## ZombieBuddy wiring (verified against ZombieBuddy.jar)
`src/zb/java/dev/atomics/arf/patches` is compiled only when `libs/ZombieBuddy.jar` exists (not committed; copy your
jar there). `mod.info` sets `javaPkgName=dev.atomics.arf.patches`, so ZombieBuddy finds `Main.main(String[])` and the
`@Patch` classes there.
- `Main` registers on `Callbacks.onDisplayCreate`, then `ArfRuntime.bootstrap` checks ZombieBuddy and GL (GL via reflection).
- `Patch_IngameState_UpdateStuff` (main-thread tick) and `Patch_Core_EndFrameUI` (render thread) are no-ops until bootstrap
  passes and disable ARF for the session on any exception.

## What is NOT done
- **Camera/input patches and the `arf.*` Lua API.** Real camera/input class names are still unknown; no Project Zomboid jar here.
  Run the repo-root discovery agent on a B42 install and send me `agent-classes.txt`.
- **WorldSnapshot is not filled from live game state** (frame counter only); needs verified PZ accessors.
- **OpenGL 4.3 backend** for `RenderDevice`; nothing draws yet.
- **Shaders are unvalidated.** Run them through `glslangValidator` or a real GL 4.3 context.
- ZombieBuddy may ask users to approve unsigned mods on first load.
- Sphere-cast collision (`CollisionProbe`) must be supplied from PZ tile data.
