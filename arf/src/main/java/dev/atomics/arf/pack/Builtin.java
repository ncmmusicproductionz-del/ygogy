package dev.atomics.arf.pack;

/** The default pack and the {@code arf/*.glsl} standard library, both shipped inside the jar. */
public final class Builtin {
    private Builtin() {}

    public static PackSource defaultPackSource() { return PackSource.classpath("/arf/defaultpack"); }

    /** Resolves {@code #include "arf/pbr.glsl"} to the jar's {@code /arf/lib/pbr.glsl}. */
    public static PackSource stdlib() {
        PackSource lib = PackSource.classpath("/arf/lib");
        return path -> path.startsWith("arf/") ? lib.read(path.substring(4)) : java.util.Optional.empty();
    }

    public static PackManager newManager() throws PackException {
        return new PackManager(ShaderPack.load(defaultPackSource()), stdlib());
    }
}
