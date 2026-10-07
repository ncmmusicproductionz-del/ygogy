package dev.atomics.arf.pack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prepares a pack shader for compilation: keeps the {@code #version} line first, injects
 * {@code #define ARF_OPT_<NAME> <value>} for every option, and resolves {@code #include}. Includes resolve
 * inside the pack ({@code include/} then pack root) or the ARF standard library ({@code arf/...}). {@code #line}
 * markers carry a file index so driver errors map back to a file and line ({@link Result#files()}).
 */
public final class ShaderPreprocessor {
    private static final Pattern INCLUDE = Pattern.compile("^\\s*#\\s*include\\s+\"([^\"]+)\"\\s*$");
    private static final int MAX_DEPTH = 16;

    public record Result(String source, List<String> files) {}

    private final PackSource pack;
    private final PackSource stdlib;

    public ShaderPreprocessor(PackSource pack, PackSource stdlib) {
        this.pack = pack;
        this.stdlib = stdlib;
    }

    public Result process(String path, Map<String, String> optionDefines) throws PackException {
        List<String> files = new ArrayList<>();
        StringBuilder out = new StringBuilder();
        String text = pack.read(path).orElseThrow(() -> new PackException("missing shader " + path));

        String version = "#version 430 core";
        String body = text;
        String[] first = text.split("\n", 2);
        if (first[0].trim().startsWith("#version")) {
            version = first[0].trim();
            body = first.length > 1 ? first[1] : "";
        }
        out.append(version).append('\n');
        optionDefines.forEach((k, v) -> out.append("#define ARF_OPT_").append(k).append(' ').append(v).append('\n'));

        Deque<String> chain = new ArrayDeque<>();
        emit(path, body, version.equals(first[0].trim()) ? 2 : 1, out, files, chain);
        return new Result(out.toString(), files);
    }

    private void emit(String path, String text, int firstLine, StringBuilder out, List<String> files, Deque<String> chain) throws PackException {
        if (chain.size() >= MAX_DEPTH) throw new PackException("#include nested too deeply at " + path);
        if (chain.contains(path)) throw new PackException("circular #include: " + String.join(" -> ", chain) + " -> " + path);
        chain.push(path);
        int idx = files.indexOf(path);
        if (idx < 0) { files.add(path); idx = files.size() - 1; }

        String[] lines = text.split("\n", -1);
        out.append("#line ").append(firstLine).append(' ').append(idx).append('\n');
        for (int n = 0; n < lines.length; n++) {
            Matcher m = INCLUDE.matcher(lines[n]);
            if (m.matches()) {
                String target = m.group(1);
                Resolved r = resolve(target, path).orElseThrow(() -> new PackException(path + ": cannot find #include \"" + target + "\""));
                emit(r.path, r.text, 1, out, files, chain);
                out.append("#line ").append(firstLine + n + 1).append(' ').append(files.indexOf(path)).append('\n');
            } else {
                out.append(lines[n]).append('\n');
            }
        }
        chain.pop();
    }

    private record Resolved(String path, String text) {}

    private Optional<Resolved> resolve(String target, String from) {
        if (target.startsWith("arf/")) return stdlib.read(target).map(t -> new Resolved(target, t));
        for (String candidate : new String[] {"include/" + target, "shaders/" + target, target}) {
            Optional<String> t = pack.read(candidate);
            if (t.isPresent()) return Optional.of(new Resolved(candidate, t.get()));
        }
        return Optional.empty();
    }
}
