#version 430 core
#include "arf/lighting.glsl"
in vec2 vUv;
uniform sampler2D arf_GAlbedo;
uniform sampler2D arf_GNormal;
uniform sampler2D arf_GMaterial;
uniform sampler2D arf_Depth;
uniform sampler2DArrayShadow arf_ShadowCascades;   // 4 cascades
uniform mat4 arf_InvProj;
uniform vec3 arf_SunDir;
uniform float arf_DayFactor;
out vec4 oColour;
void main() {
    vec4 albedo = texture(arf_GAlbedo, vUv);
    vec3 n = arfOctDecode(texture(arf_GNormal, vUv).rg);
    vec4 mat = texture(arf_GMaterial, vUv);
    vec3 v = vec3(0.0, 0.0, 1.0);
    vec3 sun = mix(vec3(0.05, 0.07, 0.15), vec3(1.0, 0.95, 0.85), arf_DayFactor);
    vec3 col = arfDirectLight(albedo.rgb, n, v, normalize(arf_SunDir), sun, mat.r, mat.g);
    col += albedo.rgb * arfAmbient(n, arf_DayFactor) * mat.b;
#if ARF_OPT_SHADOWS
    // cascade sampling hooks in here; bias = ARF_OPT_SHADOW_BIAS
    col *= 1.0 - 0.0 * float(ARF_OPT_SHADOW_BIAS);
#endif
    oColour = vec4(col, 1.0);
}
