package dev.atomics.arf.camera;

/** Camera placement. yaw 0 faces +x (east), increasing clockwise seen from above; pitch positive looks up. */
public record Pose(Vec3 position, float yaw, float pitch) {
    public Vec3 forward() {
        float cp = (float) Math.cos(pitch);
        return new Vec3(cp * (float) Math.cos(yaw), cp * (float) Math.sin(yaw), (float) Math.sin(pitch));
    }
    /** Horizontal right-hand vector (south when facing east, since y points south). */
    public Vec3 right() { return new Vec3(-(float) Math.sin(yaw), (float) Math.cos(yaw), 0); }

    public Pose blend(Pose to, float t) {
        float s = t * t * (3 - 2 * t); // smoothstep
        return new Pose(position.lerp(to.position, s), lerpAngle(yaw, to.yaw, s), pitch + (to.pitch - pitch) * s);
    }

    static float lerpAngle(float a, float b, float t) {
        float d = (float) ((b - a + Math.PI) % (2 * Math.PI));
        if (d < 0) d += 2 * Math.PI;
        d -= Math.PI;
        return a + d * t;
    }

    public float[] viewMatrix() { return Mat4.lookAt(position, position.add(forward()), new Vec3(0, 0, 1)); }
}
