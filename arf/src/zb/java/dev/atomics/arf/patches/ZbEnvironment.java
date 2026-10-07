package dev.atomics.arf.patches;

import dev.atomics.arf.core.ArfBootstrap;
import me.zed_0xff.zombie_buddy.ZombieBuddy;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** Reads versions from ZombieBuddy and the current GL context. GL is reached by reflection (LWJGL3, falling back to lwjglx) so ARF has no hard LWJGL compile dependency. */
final class ZbEnvironment implements ArfBootstrap.Environment {
    private static final int GL_EXTENSIONS = 0x1F03, GL_VERSION = 0x1F02, GL_NUM_EXTENSIONS = 0x821D;

    @Override public Optional<String> zombieBuddyVersion() {
        try { return Optional.ofNullable(ZombieBuddy.getVersion()); } catch (Throwable t) { return Optional.empty(); }
    }

    @Override public Optional<String> glVersion() {
        try {
            Method m = gl("GL11").getMethod("glGetString", int.class);
            return Optional.ofNullable((String) m.invoke(null, GL_VERSION));
        } catch (Throwable t) { return Optional.empty(); }
    }

    @Override public Set<String> glExtensions() {
        Set<String> out = new HashSet<>();
        try {
            int n = (Integer) gl("GL11").getMethod("glGetInteger", int.class).invoke(null, GL_NUM_EXTENSIONS);
            Method s = gl("GL30").getMethod("glGetStringi", int.class, int.class);
            for (int i = 0; i < n; i++) out.add((String) s.invoke(null, GL_EXTENSIONS, i));
        } catch (Throwable ignored) { /* empty set: any required extension is then reported missing */ }
        return out;
    }

    private static Class<?> gl(String name) throws ClassNotFoundException {
        try { return Class.forName("org.lwjgl.opengl." + name); }
        catch (ClassNotFoundException e) { return Class.forName("org.lwjglx.opengl." + name); }
    }
}
