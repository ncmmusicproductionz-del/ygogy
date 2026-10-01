package dev.ygogy.agent;

import net.bytebuddy.asm.Advice;

/**
 * Advice templates. Byte Buddy copies these method bodies into the target method, so they must
 * only reference classes the game's classloader can see (Settings is on the system classpath
 * because the agent jar is).
 */
public final class Hooks {
    private Hooks() {}

    /** Scales + offsets a float return value, e.g. camera zoom / FOV / render scale. */
    public static final class FloatValue {
        @Advice.OnMethodExit
        public static void exit(@Advice.Origin("#t.#m") String id,
                                @Advice.Return(readOnly = false) float result) {
            result = Settings.offset(id, Settings.scale(id, result));
        }
    }

    /** Same as {@link FloatValue} for double return values. */
    public static final class DoubleValue {
        @Advice.OnMethodExit
        public static void exit(@Advice.Origin("#t.#m") String id,
                                @Advice.Return(readOnly = false) double result) {
            result = Settings.offset(id, Settings.scale(id, result));
        }
    }

    /** Runs a static callback after the target method, e.g. to draw the menu / run a shader pass. */
    public static final class AfterCall {
        @Advice.OnMethodExit
        public static void exit(@Advice.Origin("#t.#m") String id) {
            Settings.set("calls:" + id, Settings.get("calls:" + id, 0) + 1);
        }
    }
}
