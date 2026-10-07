package dev.atomics.arf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * ARF's published patch target list ("class#method"). Other ZombieBuddy mods that patch the same method
 * collide with ARF, so the overlap is detected and reported at startup instead of silently breaking.
 *
 * <p>The names below are PLACEHOLDERS: they must be confirmed against a B42 install (see README, "Wiring
 * ZombieBuddy"). Keep this list short and centralised so each game update is a one-file fix.
 */
public final class PatchTargets {
    public static final Set<String> ARF = Set.of(
        "zombie.iso.IsoCamera#update",                 // camera: hand view to CameraRig
        "zombie.iso.IsoWorld#render",                  // renderer entry: swap in ARF frame graph
        "zombie.input.GameKeyboard#isKeyDown",         // input remap
        "zombie.input.Mouse#update"                    // mouse look
    );

    private PatchTargets() {}

    /** Targets present in both ARF's list and {@code other}; sorted for stable messages. */
    public static List<String> conflicts(Set<String> other) {
        Set<String> hit = new TreeSet<>(ARF);
        hit.retainAll(other);
        return new ArrayList<>(hit);
    }
}
