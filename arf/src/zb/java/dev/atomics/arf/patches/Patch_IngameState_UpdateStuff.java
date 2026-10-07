package dev.atomics.arf.patches;

import dev.atomics.arf.core.ArfRuntime;
import me.zed_0xff.zombie_buddy.Patch;

/** Main-thread tick: take the WorldSnapshot. Target is the same one ZombieBuddy itself patches in B42. */
@Patch(className = "zombie.gameStates.IngameState", methodName = "UpdateStuff")
public class Patch_IngameState_UpdateStuff {
    @Patch.OnExit
    public static void exit() { ArfRuntime.onMainUpdate(); }
}
