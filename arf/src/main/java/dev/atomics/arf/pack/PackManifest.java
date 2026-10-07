package dev.atomics.arf.pack;

import java.util.List;
import java.util.Map;

public record PackManifest(String name, String version, String author, int minApi, List<String> stages) {
    public static PackManifest from(Map<String, Object> m) {
        Object api = m.get("minApi");
        @SuppressWarnings("unchecked")
        List<String> stages = m.get("stages") instanceof List<?> l ? (List<String>) l : List.of();
        return new PackManifest(
            (String) m.getOrDefault("name", "unnamed"),
            (String) m.getOrDefault("version", "0"),
            (String) m.getOrDefault("author", "unknown"),
            api instanceof Double d ? d.intValue() : 1,
            List.copyOf(stages));
    }
}
