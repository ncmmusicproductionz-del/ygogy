package dev.atomics.arf.content;

/** 8-bit class written to the G-buffer; packs branch on it the way Iris packs branch on block IDs. */
public enum MaterialClass {
    TERRAIN, GRASS, WATER, GLASS, METAL, WOOD, FOLIAGE, BLOOD, SKIN, EMISSIVE, DEFAULT;

    public static MaterialClass parse(String s) {
        try { return valueOf(s.toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return DEFAULT; }
    }
    public int id() { return ordinal(); }
}
