package dev.atomics.arf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * ARF's published patch target list ("class#method"). Other ZombieBuddy mods that patch the same method
 * collide with ARF, so the overlap is detected and reported at startup instead of silently breaking.
 *
 * <p>Both targets below are confirmed against the ZombieBuddy jar, which patches the same two methods itself
 * (so they exist in B42). Camera and input targets are NOT listed yet: they need class names from a real B42
 * install (the Byte Buddy discovery agent in the repo root can dump them). Keep this list short and centralised
 * so each game update is a one-file fix.
 */
public final class PatchTargets {
    public static final Set<String> ARF = Set.of(
        "zombie.gameStates.IngameState#UpdateStuff",   // main thread: WorldSnapshot tick
        "zombie.core.Core#EndFrameUI"                  // render thread: world/UI boundary
    );

    private PatchTargets() {}

    /** Targets present in both ARF's list and {@code other}; sorted for stable messages. */
    public static List<String> conflicts(Set<String> other) {
        Set<String> hit = new TreeSet<>(ARF);
        hit.retainAll(other);
        return new ArrayList<>(hit);
    }
}
