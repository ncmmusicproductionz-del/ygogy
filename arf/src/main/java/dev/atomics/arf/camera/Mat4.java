package dev.atomics.arf.camera;

/** Column-major 4x4 float matrices, the layout OpenGL uniforms expect. */
public final class Mat4 {
    private Mat4() {}

    public static float[] perspective(float fovYDeg, float aspect, float near, float far) {
        float f = (float) (1.0 / Math.tan(Math.toRadians(fovYDeg) / 2.0));
        float[] m = new float[16];
        m[0] = f / aspect;
        m[5] = f;
        m[10] = (far + near) / (near - far);
        m[11] = -1f;
        m[14] = 2f * far * near / (near - far);
        return m;
    }

    /** Right-handed view matrix with +z up. */
    public static float[] lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        Vec3 f = target.sub(eye).normalize();
        Vec3 s = f.cross(up).normalize();
        Vec3 u = s.cross(f);
        return new float[] {
            s.x(), u.x(), -f.x(), 0,
            s.y(), u.y(), -f.y(), 0,
            s.z(), u.z(), -f.z(), 0,
            -s.dot(eye), -u.dot(eye), f.dot(eye), 1
        };
    }

    public static float[] multiply(float[] a, float[] b) {
        float[] r = new float[16];
        for (int c = 0; c < 4; c++)
            for (int row = 0; row < 4; row++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) sum += a[k * 4 + row] * b[c * 4 + k];
                r[c * 4 + row] = sum;
            }
        return r;
    }
}
