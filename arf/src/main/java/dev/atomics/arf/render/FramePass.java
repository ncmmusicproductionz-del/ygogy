package dev.atomics.arf.render;

import java.util.List;

/**
 * The deferred frame, in order. Every pass is the default pack; a shader pack may replace or add programs
 * (via {@link #stages}) and the engine guarantees each stage's inputs and attachments.
 */
public enum FramePass {
    SHADOW("shadow"),                                   // 4 cascades @ 2048^2 + cube shadows for <= 8 point lights
    GBUFFER("gbuffer_terrain", "gbuffer_object", "gbuffer_character", "gbuffer_foliage"),
    SSAO("composite0"),                                 // SSAO/GTAO, half-res, bilateral upsample
    LIGHTING("deferred_lighting"),                      // Cook-Torrance GGX, clustered 16x9x24
    TRANSLUCENT("translucent_water"),                   // glass, water, foliage cards, particles, blood decals
    VOLUMETRICS("composite1"),                          // fog and rain driven from ClimateManager
    POST("composite2", "composite3", "composite4", "composite5", "composite6", "composite7", "final"),
    UI("");                                             // vanilla PZ UI, unchanged

    /** Shader program names (without .vsh/.fsh) a pack can supply for this pass. */
    public final List<String> stages;

    FramePass(String... stages) { this.stages = List.of(stages); }

    public static final int CASCADES = 4;
    public static final int CASCADE_RESOLUTION = 2048;
    public static final int MAX_POINT_SHADOWS = 8;
    public static final int[] CLUSTER_GRID = {16, 9, 24};
}
