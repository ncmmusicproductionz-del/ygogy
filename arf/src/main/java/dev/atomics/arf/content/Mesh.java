package dev.atomics.arf.content;

public record Mesh(Source source, String name) {
    public enum Source { EXPLICIT, PROCEDURAL, BILLBOARD, FALLBACK_BOX }
}
