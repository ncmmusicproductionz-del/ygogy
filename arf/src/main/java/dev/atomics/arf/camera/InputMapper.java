package dev.atomics.arf.camera;

import java.util.HashSet;
import java.util.Set;

/**
 * Turns camera-relative input into the world-space movement PZ's own movement code already understands, so
 * collision and pathing stay vanilla. Never latches state across mode switches: {@link #clearHeld()} runs on
 * every switch (the pzopt inputLatch walk-forever bug is the cautionary tale).
 */
public final class InputMapper {
    private final Set<Integer> held = new HashSet<>();

    public void press(int key) { held.add(key); }
    public void release(int key) { held.remove(key); }
    public boolean isHeld(int key) { return held.contains(key); }
    public void clearHeld() { held.clear(); }

    /**
     * @param forward  +1 W, -1 S
     * @param strafe   +1 D, -1 A
     * @return world-space (x, y) direction, normalised unless idle
     */
    public static float[] remap(float forward, float strafe, float yaw) {
        float fx = (float) Math.cos(yaw), fy = (float) Math.sin(yaw);
        float rx = -fy, ry = fx;
        float x = fx * forward + rx * strafe, y = fy * forward + ry * strafe;
        float len = (float) Math.sqrt(x * x + y * y);
        return len > 1f ? new float[] {x / len, y / len} : new float[] {x, y};
    }
}
