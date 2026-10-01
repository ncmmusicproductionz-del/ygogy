# Zomboid Byte Buddy agent

A [Byte Buddy](https://bytebuddy.net) Java agent for poking at Project Zomboid's engine at runtime
(camera/zoom first, shaders next). Byte Buddy rewrites already-loaded Java classes from a `-javaagent`.

## Build
    mvn package          # -> target/zomboid-agent-0.1.0.jar (Byte Buddy bundled + relocated)

## Use
1. `cp agent.properties.example agent.properties`
2. Add to the game's JVM args (ProjectZomboid64.json `vmArgs`, or Steam launch options):
   `-javaagent:/full/path/zomboid-agent-0.1.0.jar=/full/path/agent.properties`
3. Launch once. `agent-classes.txt` lists every `zombie.*` class + method that loaded.
4. Pick real targets from that file, add `hook.N=...` lines, relaunch.

## What exists
| Piece | Purpose |
|---|---|
| `Agent` | premain/agentmain, config, discovery dump, hook installer |
| `Hooks` | Advice templates: scale/offset a float/double return value |
| `Settings` | Live shared values the hooks read every call |
| `ControlMenu` | Optional Swing sliders (`menu=true`) |

## Not done yet / honest limits
- Real class names (camera, renderer) are **unverified** - they need the discovery dump from your install.
- Zomboid renders 2D isometric sprites, so a true Minecraft-style 3D camera is not a hook away; zoom, offset and
  render-scale hooks are the realistic first step. Shaders need a GL post-process pass added after we find the render loop.
- Retransforming loaded classes can't add methods/fields (`disableClassFormatChanges()`); use inline Advice only.
