package dev.atomics.arf.content;

/** What the patch layer knows about a sprite on a tile. {@code kind} is walls/floors/stairs/fences/roofs etc. */
public record TileObject(String sprite, Kind kind, int wallDirection, int heightLevel, float roofSlope, boolean billboardHint) {
    public enum Kind { WALL, FLOOR, STAIRS, FENCE, ROOF, FURNITURE, PLANT, OTHER }

    public static TileObject of(String sprite, Kind kind) { return new TileObject(sprite, kind, 0, 0, 0f, false); }
}
