package dev.atomics.arf.camera;

/**
 * Owns the view and projection matrices. Modes are strategies that write a target pose into the rig, so the
 * renderer never knows which mode is active. Switching blends over {@link #blendSeconds}.
 */
public final class CameraRig {
    public static final float EYE_FORWARD = 0.08f;       // metres in front of the head bone
    public static final float MAX_PITCH = (float) Math.toRadians(85);

    public final InputMapper input = new InputMapper();

    public float blendSeconds = 0.25f;
    public float fovDeg = 75f;                            // slider range 60..110
    public float thirdPersonDistance = 3.0f;              // changed by scroll
    public float shoulderSide = 1f;                       // +1 right, -1 left
    public float sensitivity = 0.0025f;                   // radians per mouse count

    private CameraMode mode = CameraMode.ISOMETRIC;
    private float yaw, pitch;
    private Pose current = new Pose(Vec3.ZERO, 0, 0);
    private Pose blendFrom;
    private float blendT = 1f;

    public CameraMode mode() { return mode; }
    public Pose pose() { return current; }
    public float yaw() { return yaw; }

    public void setFov(float deg) { fovDeg = Math.max(60f, Math.min(110f, deg)); }
    public void zoom(float scrollSteps) { thirdPersonDistance = Math.max(1f, Math.min(8f, thirdPersonDistance - scrollSteps * 0.25f)); }
    public void swapShoulder() { shoulderSide = -shoulderSide; }

    public void setMode(CameraMode next) {
        if (next == mode) return;
        blendFrom = current;
        blendT = 0f;
        mode = next;
        input.clearHeld();                                // every switch clears held state
    }

    /** Mouse look sets the yaw that becomes the player's facing. Ignored in isometric and free-fly input pause. */
    public void look(float dx, float dy) {
        if (mode.handsControlToVanilla()) return;
        yaw += dx * sensitivity;
        pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitch - dy * sensitivity));
    }

    /** @param head player head-bone world position. Returns the pose to render this frame. */
    public Pose update(float dt, Vec3 head, CollisionProbe probe) {
        Pose target = switch (mode) {
            case FIRST_PERSON -> {
                Pose p = new Pose(head, yaw, pitch);
                yield new Pose(head.add(p.forward().mul(EYE_FORWARD)), yaw, pitch);
            }
            case THIRD_PERSON -> {
                Pose look = new Pose(head, yaw, pitch);
                Vec3 pivot = head.add(look.right().mul(0.4f * shoulderSide)).add(new Vec3(0, 0, 0.3f));
                Vec3 desired = pivot.sub(look.forward().mul(thirdPersonDistance));
                float f = probe.sweep(pivot, desired, 0.2f);
                yield new Pose(pivot.lerp(desired, Math.max(0f, Math.min(1f, f))), yaw, pitch);
            }
            case FREE, ISOMETRIC -> current;              // detached / vanilla: leave the pose alone
        };

        if (blendT < 1f) {
            blendT = Math.min(1f, blendT + dt / Math.max(1e-4f, blendSeconds));
            current = blendFrom.blend(target, blendT);
        } else {
            current = target;
        }
        return current;
    }

    public float[] projection(float aspect) { return Mat4.perspective(fovDeg, aspect, 0.05f, 400f); }

    /** Indoor auto-cutaway: in third person, levels above the player fade when the camera would clip them. */
    public static boolean fadeLevel(CameraMode mode, int level, int playerLevel, boolean cameraBelowCeiling) {
        return mode == CameraMode.THIRD_PERSON && level > playerLevel && cameraBelowCeiling;
    }
}
