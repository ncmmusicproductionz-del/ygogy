package dev.atomics.arf.pack;

import java.util.List;
import java.util.Map;

/**
 * Uniforms the engine guarantees (ARF API 1). Versioned: only ever ADD uniforms, never rename, so packs keep
 * working.
 */
public final class ArfUniforms {
    public static final int API = 1;

    public static final Map<String, List<String>> GROUPS = Map.of(
        "Camera", List.of("arf_View", "arf_Proj", "arf_PrevViewProj", "arf_CameraPos", "arf_Jitter"),
        "Time and world", List.of("arf_GameTime", "arf_DayFactor", "arf_SunDir", "arf_MoonDir", "arf_Season"),
        "Weather", List.of("arf_Rain", "arf_Snow", "arf_FogDensity", "arf_Wind", "arf_CloudCover"),
        "Player state", List.of("arf_IsIndoors", "arf_Health", "arf_Panic", "arf_Drunk", "arf_IsAiming"),
        "Buffers", List.of("arf_GAlbedo", "arf_GNormal", "arf_GMaterial", "arf_Depth", "arf_ShadowCascades"));

    private ArfUniforms() {}
}
