package dev.atomics.arf.content;

import dev.atomics.arf.pack.Json;

import java.util.HashMap;
import java.util.Map;

/**
 * Sprite name to material. Mods register through Lua ({@code arf.registerMaterial(name, {...})}, which the
 * patch layer forwards here) or a {@code materials.json} in their mod folder. Unregistered sprites get an
 * albedo-only default material, so nothing is ever unlit or invisible.
 */
public final class MaterialRegistry {
    private final Map<String, Material> materials = new HashMap<>();

    public void register(String sprite, Material m) { materials.put(sprite, m); }

    public Material lookup(String sprite) {
        return materials.getOrDefault(sprite, new Material(sprite, null, null, null, MaterialClass.DEFAULT));
    }

    @SuppressWarnings("unchecked")
    public int loadJson(String json) {
        int n = 0;
        for (var e : Json.object(json).entrySet()) {
            Map<String, Object> m = (Map<String, Object>) e.getValue();
            register(e.getKey(), new Material(
                (String) m.getOrDefault("albedo", e.getKey()), (String) m.get("normal"), (String) m.get("orm"),
                (String) m.get("emissive"), MaterialClass.parse((String) m.getOrDefault("class", "default"))));
            n++;
        }
        return n;
    }
}
