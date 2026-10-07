package dev.atomics.arf.content;

/** Texture paths may be null; missing maps fall back to defaults so sprite-only content still lights correctly. */
public record Material(String albedo, String normal, String orm, String emissive, MaterialClass materialClass) {
    public static final float DEFAULT_ROUGHNESS = 0.8f;
    public static final float DEFAULT_METAL = 0f;
    /** Flat tangent-space normal used when no normal map exists. */
    public static final float[] FLAT_NORMAL = {0.5f, 0.5f, 1f};

    public boolean hasNormal() { return normal != null; }
    public boolean hasOrm() { return orm != null; }
}
