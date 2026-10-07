package dev.atomics.arf.pack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A parsed pack: manifest, option definitions and profiles. Shader text is read lazily from the source. */
public final class ShaderPack {
    public final PackSource source;
    public final PackManifest manifest;
    public final List<OptionDef> options;
    public final Map<String, Map<String, Object>> profiles;

    private ShaderPack(PackSource source, PackManifest manifest, List<OptionDef> options, Map<String, Map<String, Object>> profiles) {
        this.source = source;
        this.manifest = manifest;
        this.options = options;
        this.profiles = profiles;
    }

    @SuppressWarnings("unchecked")
    public static ShaderPack load(PackSource src) throws PackException {
        String packJson = src.read("pack.json").orElseThrow(() -> new PackException("pack.json missing"));
        try {
            PackManifest manifest = PackManifest.from(Json.object(packJson));
            if (manifest.minApi() > ArfUniforms.API)
                throw new PackException(manifest.name() + " needs ARF API " + manifest.minApi() + " (this is " + ArfUniforms.API + ")");

            List<OptionDef> options = new ArrayList<>();
            if (src.read("options.json").isPresent()) {
                Object list = Json.object(src.read("options.json").get()).get("options");
                if (list instanceof List<?> l) for (Object o : l) options.add(OptionDef.from((Map<String, Object>) o));
            }

            Map<String, Map<String, Object>> profiles = new LinkedHashMap<>();
            if (src.read("profiles.json").isPresent())
                Json.object(src.read("profiles.json").get()).forEach((k, v) -> profiles.put(k, (Map<String, Object>) v));

            return new ShaderPack(src, manifest, List.copyOf(options), profiles);
        } catch (IllegalArgumentException e) {
            throw new PackException("invalid pack: " + e.getMessage());
        }
    }

    /** Defaults, then the named profile, then the user's overrides. Result is NAME to GLSL token. */
    public Map<String, String> defines(String profile, Map<String, Object> overrides) {
        Map<String, Object> prof = profiles.getOrDefault(profile, Map.of());
        Map<String, String> out = new LinkedHashMap<>();
        for (OptionDef o : options) {
            Object v = overrides.containsKey(o.name()) ? overrides.get(o.name()) : prof.get(o.name());
            out.put(o.name(), o.glslValue(v));
        }
        return out;
    }
}
