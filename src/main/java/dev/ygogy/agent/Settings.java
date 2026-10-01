package dev.ygogy.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live, thread-safe knobs shared by the injected hooks and the control menu.
 *
 * Hook advice is inlined into game classes, so it only talks to this class through
 * static methods with primitive/String arguments.
 */
public final class Settings {
    private static final Map<String, Double> VALUES = new ConcurrentHashMap<>();

    private Settings() {}

    public static double get(String key, double fallback) {
        return VALUES.getOrDefault(key, fallback);
    }

    public static void set(String key, double value) {
        VALUES.put(key, value);
    }

    public static Map<String, Double> snapshot() {
        return Map.copyOf(VALUES);
    }

    /** Multiplier applied to a hooked method's return value. Key is the hook id ("pkg.Class.method"). */
    public static float scale(String hookId, float original) {
        return (float) (original * get("scale:" + hookId, 1.0));
    }

    public static double scale(String hookId, double original) {
        return original * get("scale:" + hookId, 1.0);
    }

    /** Additive offset applied after scaling. */
    public static float offset(String hookId, float original) {
        return (float) (original + get("offset:" + hookId, 0.0));
    }

    public static double offset(String hookId, double original) {
        return original + get("offset:" + hookId, 0.0);
    }
}
