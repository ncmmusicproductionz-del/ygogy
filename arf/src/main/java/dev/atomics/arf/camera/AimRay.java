package dev.atomics.arf.camera;

import java.util.Optional;

/** Crosshair ray used for aiming and context menus; the hit is converted back to a tile for PZ's own code. */
public final class AimRay {
    private AimRay() {}

    /** Intersection with the horizontal plane at {@code planeZ}, or empty if the ray points away or parallel. */
    public static Optional<Vec3> groundHit(Pose cam, float planeZ) {
        Vec3 d = cam.forward();
        if (Math.abs(d.z()) < 1e-6f) return Optional.empty();
        float t = (planeZ - cam.position().z()) / d.z();
        if (t < 0) return Optional.empty();
        return Optional.of(cam.position().add(d.mul(t)));
    }

    public static int[] toTile(Vec3 hit) { return new int[] {(int) Math.floor(hit.x()), (int) Math.floor(hit.y())}; }
}
