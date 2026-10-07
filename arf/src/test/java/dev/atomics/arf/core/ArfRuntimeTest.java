package dev.atomics.arf.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArfRuntimeTest {
    private static ArfBootstrap.Environment env(String zb, String gl) {
        return new ArfBootstrap.Environment() {
            public Optional<String> zombieBuddyVersion() { return Optional.ofNullable(zb); }
            public Optional<String> glVersion() { return Optional.ofNullable(gl); }
            public Set<String> glExtensions() {
                return Set.of("GL_ARB_compute_shader", "GL_ARB_shader_storage_buffer_object", "GL_ARB_texture_cube_map_array");
            }
        };
    }

    @BeforeEach void reset() { ArfRuntime.resetForTests(); }

    @Test void hooksAreNoOpsBeforeBootstrap() {
        long before = ArfRuntime.SNAPSHOTS.latest().frameId();
        ArfRuntime.onMainUpdate();
        ArfRuntime.onRenderFrameEnd();
        assertEquals(before, ArfRuntime.SNAPSHOTS.latest().frameId());
        assertFalse(ArfRuntime.active());
    }

    @Test void inertWhenGlTooOld() {
        assertFalse(ArfRuntime.bootstrap(env("1.0", "4.1 Metal")).active());
        long before = ArfRuntime.SNAPSHOTS.latest().frameId();
        ArfRuntime.onMainUpdate();
        assertEquals(before, ArfRuntime.SNAPSHOTS.latest().frameId());
        assertNull(ArfRuntime.packs());
    }

    @Test void activeRunsHooksAndLoadsDefaultPack() {
        assertTrue(ArfRuntime.bootstrap(env("1.0", "4.6")).active());
        assertNotNull(ArfRuntime.packs());
        long before = ArfRuntime.SNAPSHOTS.latest().frameId();
        ArfRuntime.onMainUpdate();
        assertEquals(before + 1, ArfRuntime.SNAPSHOTS.latest().frameId());
        ArfRuntime.onRenderFrameEnd();   // isometric by default: must not throw
    }

    @Test void bootstrapIsIdempotent() {
        var first = ArfRuntime.bootstrap(env("1.0", "4.6"));
        assertSame(first, ArfRuntime.bootstrap(env(null, null)));
    }
}
