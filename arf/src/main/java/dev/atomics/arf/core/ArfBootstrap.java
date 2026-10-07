package dev.atomics.arf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Runs on the main thread before any render-thread patch is armed. If any check fails ARF stays
 * {@link State#INERT}: no patches applied, vanilla isometric mode, and {@link Result#problems()} feeds the
 * in-game notice. ARF must never leave the game half-patched.
 */
public final class ArfBootstrap {
    public enum State { ACTIVE, INERT }

    public record Result(State state, List<String> problems, boolean glPreferredMet) {
        public boolean active() { return state == State.ACTIVE; }
    }

    /** What the host (ZombieBuddy + LWJGL context) can tell us. Abstracted so the checks are testable. */
    public interface Environment {
        Optional<String> zombieBuddyVersion();
        Optional<String> glVersion();
        Set<String> glExtensions();
    }

    private ArfBootstrap() {}

    public static Result run(Environment env, Properties cfg) {
        List<String> problems = new ArrayList<>();

        ArfVersion minZb = ArfVersion.parse(cfg.getProperty("min.zombiebuddy", "0"));
        env.zombieBuddyVersion().ifPresentOrElse(v -> {
            if (!ArfVersion.parse(v).atLeast(minZb)) problems.add("ZombieBuddy " + v + " is too old (need " + cfg.getProperty("min.zombiebuddy") + "+)");
        }, () -> problems.add("ZombieBuddy is not loaded"));

        ArfVersion glReq = ArfVersion.parse(cfg.getProperty("gl.required", "4.3"));
        ArfVersion glPref = ArfVersion.parse(cfg.getProperty("gl.preferred", "4.5"));
        boolean preferred = false;
        Optional<String> gl = env.glVersion();
        if (gl.isEmpty()) {
            problems.add("No OpenGL context available");
        } else {
            ArfVersion have = ArfVersion.parse(gl.get());
            if (!have.atLeast(glReq)) problems.add("OpenGL " + gl.get() + " is below required " + cfg.getProperty("gl.required", "4.3"));
            preferred = have.atLeast(glPref);
        }

        String exts = cfg.getProperty("gl.extensions", "").trim();
        if (!exts.isEmpty()) {
            for (String e : exts.split("\\s*,\\s*")) {
                if (!env.glExtensions().contains(e)) problems.add("Missing OpenGL extension " + e);
            }
        }

        return new Result(problems.isEmpty() ? State.ACTIVE : State.INERT, List.copyOf(problems), preferred);
    }
}
