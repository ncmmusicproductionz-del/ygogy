package dev.atomics.arf.core;

import dev.atomics.arf.camera.CameraRig;
import dev.atomics.arf.content.MaterialRegistry;
import dev.atomics.arf.content.MeshResolver;
import dev.atomics.arf.pack.Builtin;
import dev.atomics.arf.pack.PackManager;
import dev.atomics.arf.snapshot.SnapshotBuffer;
import dev.atomics.arf.snapshot.WorldSnapshot;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Process-wide ARF state, driven by the thin ZombieBuddy patch classes. Nothing here throws into the game:
 * every entry point swallows failures and falls back to inert (vanilla isometric). Hooks are a no-op until
 * {@link #bootstrap} passes, so a failed check can never leave the game half-patched.
 */
public final class ArfRuntime {
    private static volatile ArfBootstrap.Result result;
    private static volatile boolean broken;
    private static long frame;

    public static final SnapshotBuffer SNAPSHOTS = new SnapshotBuffer();
    public static final CameraRig CAMERA = new CameraRig();
    public static final MeshResolver MESHES = new MeshResolver();
    public static final MaterialRegistry MATERIALS = new MaterialRegistry();
    private static volatile PackManager packs;

    private ArfRuntime() {}

    public static boolean active() { return result != null && result.active() && !broken; }
    public static List<String> problems() { return result == null ? List.of("not bootstrapped") : result.problems(); }
    public static PackManager packs() { return packs; }

    /** Idempotent. Call on the render thread once the GL context exists (Display.create has returned). */
    public static synchronized ArfBootstrap.Result bootstrap(ArfBootstrap.Environment env) {
        if (result != null) return result;
        try {
            result = ArfBootstrap.run(env, loadConfig());
            if (result.active()) packs = Builtin.newManager();
        } catch (Throwable t) {
            result = new ArfBootstrap.Result(ArfBootstrap.State.INERT, List.of("bootstrap failed: " + t), false);
        }
        log(result.active() ? "active" + (result.glPreferredMet() ? "" : " (GL below preferred 4.5)")
                            : "INERT - vanilla isometric. " + String.join("; ", result.problems()));
        return result;
    }

    /** Main thread, once per game update tick (after PZ's own update). */
    public static void onMainUpdate() {
        if (!active()) return;
        try {
            // TODO(M1): copy visible chunks/objects/lights/weather from live PZ state. Needs verified PZ accessors.
            WorldSnapshot prev = SNAPSHOTS.latest();
            SNAPSHOTS.publish(new WorldSnapshot(++frame, prev.gameTimeHours(), prev.rain(), prev.snow(), prev.fogDensity(),
                prev.cloudCover(), prev.wind(), prev.playerLevel(), prev.indoors(), prev.lights(), prev.drawables()));
        } catch (Throwable t) { fail(t); }
    }

    /** Render thread, at the end of the world frame (just before PZ draws its UI). */
    public static void onRenderFrameEnd() {
        if (!active()) return;
        try {
            if (CAMERA.mode().handsControlToVanilla()) return;   // isometric: vanilla owns the frame
            // TODO(M2): run the deferred frame graph from SNAPSHOTS.latest() through the OpenGL RenderDevice.
        } catch (Throwable t) { fail(t); }
    }

    private static void fail(Throwable t) {
        broken = true;                                            // one failure disables ARF for the session
        log("disabled after error: " + t);
        t.printStackTrace();
    }

    private static Properties loadConfig() throws Exception {
        Properties p = new Properties();
        try (InputStream in = ArfRuntime.class.getResourceAsStream("/arf.properties")) {
            if (in != null) p.load(in);
        }
        return p;
    }

    private static void log(String s) { System.out.println("[ARF] " + s); }

    /** Test hook. */
    static synchronized void resetForTests() { result = null; broken = false; packs = null; frame = 0; }
}
