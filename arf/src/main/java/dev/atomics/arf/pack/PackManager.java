package dev.atomics.arf.pack;

import dev.atomics.arf.render.FramePass;
import dev.atomics.arf.render.RenderDevice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads and hot-reloads shader packs. Compile on a worker thread ({@link #prepare}), link on the render
 * thread ({@link #commit}). On any error the previous pack stays active and the error (with file and line)
 * is kept for the in-game log. Any stage a pack omits falls back to the default pack.
 */
public final class PackManager {
    /** All shader sources for one pack, ready to hand to the device. */
    public record Prepared(ShaderPack pack, Map<String, Map<String, String>> programs) {}

    private final ShaderPack defaultPack;
    private final PackSource stdlib;
    private Prepared active;
    private String lastError;

    public PackManager(ShaderPack defaultPack, PackSource stdlib) {
        this.defaultPack = defaultPack;
        this.stdlib = stdlib;
    }

    public Optional<Prepared> active() { return Optional.ofNullable(active); }
    public Optional<String> lastError() { return Optional.ofNullable(lastError); }

    /** Worker thread: preprocess every stage the frame graph can use. */
    public Prepared prepare(ShaderPack pack, String profile, Map<String, Object> overrides) throws PackException {
        Map<String, String> defines = pack.defines(profile, overrides);
        ShaderPreprocessor userPre = new ShaderPreprocessor(pack.source, stdlib);
        ShaderPreprocessor defPre = new ShaderPreprocessor(defaultPack.source, stdlib);
        Map<String, String> defDefines = defaultPack.defines(profile, Map.of());

        Map<String, Map<String, String>> programs = new LinkedHashMap<>();
        for (FramePass pass : FramePass.values()) {
            for (String stage : pass.stages) {
                if (stage.isEmpty()) continue;
                Map<String, String> src = new LinkedHashMap<>();
                String fsh = "shaders/" + stage + ".fsh";
                if (pack.source.read(fsh).isPresent()) src.put("frag", userPre.process(fsh, defines).source());
                else if (defaultPack.source.read(fsh).isPresent()) src.put("frag", defPre.process(fsh, defDefines).source());
                else continue;                                           // nobody supplies this stage: skip it

                String vsh = "shaders/" + stage + ".vsh";
                if (pack.source.read(vsh).isPresent()) src.put("vert", userPre.process(vsh, defines).source());
                else if (defaultPack.source.read(vsh).isPresent()) src.put("vert", defPre.process(vsh, defDefines).source());
                else src.put("vert", defPre.process("shaders/fullscreen.vsh", defDefines).source());
                programs.put(stage, src);
            }
        }
        return new Prepared(pack, programs);
    }

    /** Render thread: link everything. All-or-nothing; on failure the old pack keeps rendering. */
    public boolean commit(RenderDevice device, Prepared next) {
        java.util.List<Integer> created = new java.util.ArrayList<>();
        try {
            for (var e : next.programs().entrySet()) created.add(device.createPipeline(e.getKey(), e.getValue()));
        } catch (RenderDevice.CompileException ex) {
            created.forEach(device::destroy);
            lastError = ex.getMessage();
            return false;
        }
        active = next;
        lastError = null;
        return true;
    }

    /** Convenience for F8 reload: prepare then commit, recording any failure. */
    public boolean reload(RenderDevice device, ShaderPack pack, String profile, Map<String, Object> overrides) {
        try {
            return commit(device, prepare(pack, profile, overrides));
        } catch (PackException e) {
            lastError = e.getMessage();
            return false;
        }
    }
}
