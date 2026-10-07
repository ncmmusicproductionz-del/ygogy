package dev.atomics.arf.camera;

/** World axes follow PZ: x east, y south, z up (floor level). */
public record Vec3(float x, float y, float z) {
    public static final Vec3 ZERO = new Vec3(0, 0, 0);
    public Vec3 add(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
    public Vec3 sub(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
    public Vec3 mul(float k) { return new Vec3(x * k, y * k, z * k); }
    public float dot(Vec3 o) { return x * o.x + y * o.y + z * o.z; }
    public Vec3 cross(Vec3 o) { return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x); }
    public float length() { return (float) Math.sqrt(dot(this)); }
    public Vec3 normalize() { float l = length(); return l == 0 ? this : mul(1f / l); }
    public Vec3 lerp(Vec3 o, float t) { return add(o.sub(this).mul(t)); }
}
