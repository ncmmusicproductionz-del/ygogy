package dev.atomics.arf.patches;

import dev.atomics.arf.core.ArfRuntime;
import me.zed_0xff.zombie_buddy.Patch;

/** Render-thread hook at the world/UI boundary. Target is the same one ZombieBuddy itself patches in B42. */
@Patch(className = "zombie.core.Core", methodName = "EndFrameUI")
public class Patch_Core_EndFrameUI {
    @Patch.OnEnter
    public static void enter() { ArfRuntime.onRenderFrameEnd(); }
}
