package dev.atomics.arf.camera;

/** Sphere-cast against walls and roofs, supplied by the patch layer from PZ's tile data. */
@FunctionalInterface
public interface CollisionProbe {
    /** Fraction in [0,1] of the way from {@code from} to {@code to} a sphere of {@code radius} can travel. */
    float sweep(Vec3 from, Vec3 to, float radius);

    CollisionProbe NONE = (from, to, radius) -> 1f;
}
