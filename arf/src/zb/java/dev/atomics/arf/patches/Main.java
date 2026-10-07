package dev.atomics.arf.patches;

import dev.atomics.arf.core.ArfRuntime;
import me.zed_0xff.zombie_buddy.Callbacks;

/**
 * ZombieBuddy calls {@code main(String[])} on the class named Main in the mod's javaPkgName. We only register
 * for the display-created callback: bootstrap needs a live GL context, so it cannot run any earlier.
 */
public class Main {
    public static void main(String[] args) {
        Callbacks.onDisplayCreate.register(() -> ArfRuntime.bootstrap(new ZbEnvironment()));
    }
}
