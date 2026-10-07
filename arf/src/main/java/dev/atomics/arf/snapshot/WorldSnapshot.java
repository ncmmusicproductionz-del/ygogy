package dev.atomics.arf.snapshot;

import java.util.List;

/**
 * Immutable copy of everything the renderer needs for one frame, taken on the main thread after PZ's update.
 * The render thread never touches live game objects (see SnapshotBuffer).
 */
public record WorldSnapshot(
        long frameId,
        float gameTimeHours,
        float rain, float snow, float fogDensity, float cloudCover, float wind,
        int playerLevel,
        boolean indoors,
        List<Light> lights,
        List<Drawable> drawables) {

    public record Light(float x, float y, float z, float r, float g, float b, float radius, boolean spot) {}

    /** A tile object or character; {@code boneMatrices} is null for static objects (16 floats per bone). */
    public record Drawable(String sprite, float x, float y, float z, int level, float[] boneMatrices) {}

    public static WorldSnapshot empty() {
        return new WorldSnapshot(0, 12f, 0, 0, 0, 0, 0, 0, false, List.of(), List.of());
    }

    public WorldSnapshot {
        lights = List.copyOf(lights);
        drawables = List.copyOf(drawables);
    }

    /** 0 at midnight .. 1 at noon, the shape the {@code arf_DayFactor} uniform exposes. */
    public float dayFactor() {
        float t = (gameTimeHours % 24f) / 24f;
        return 0.5f - 0.5f * (float) Math.cos(t * 2.0 * Math.PI);
    }
}
