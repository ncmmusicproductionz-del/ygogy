package dev.atomics.arf.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/** Where a pack's files come from: a folder, the classpath (default pack) or memory (tests). */
public interface PackSource {
    /** @param path forward-slash path relative to the pack root, e.g. {@code shaders/final.fsh}. */
    Optional<String> read(String path);

    static PackSource directory(Path root) {
        return path -> {
            Path p = root.resolve(path).normalize();
            if (!p.startsWith(root.normalize())) return Optional.empty();   // no escaping the pack folder
            try { return Files.isRegularFile(p) ? Optional.of(Files.readString(p, StandardCharsets.UTF_8)) : Optional.empty(); }
            catch (IOException e) { return Optional.empty(); }
        };
    }

    static PackSource classpath(String prefix) {
        return path -> {
            if (path.contains("..")) return Optional.empty();
            try (InputStream in = PackSource.class.getResourceAsStream(prefix + "/" + path)) {
                return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) { return Optional.empty(); }
        };
    }

    static PackSource memory(Map<String, String> files) { return path -> Optional.ofNullable(files.get(path)); }
}
