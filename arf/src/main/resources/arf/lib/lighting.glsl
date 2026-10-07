// arf/lighting.glsl - Cook-Torrance GGX (ARF API 1)
#include "arf/pbr.glsl"

float arfDistGGX(float nh, float rough) {
    float a = rough * rough, a2 = a * a;
    float d = nh * nh * (a2 - 1.0) + 1.0;
    return a2 / (ARF_PI * d * d);
}

float arfGeomSmith(float nv, float nl, float rough) {
    float k = (rough + 1.0) * (rough + 1.0) / 8.0;
    return (nv / (nv * (1.0 - k) + k)) * (nl / (nl * (1.0 - k) + k));
}

vec3 arfFresnel(float vh, vec3 f0) { return f0 + (1.0 - f0) * pow(1.0 - vh, 5.0); }

vec3 arfDirectLight(vec3 albedo, vec3 n, vec3 v, vec3 l, vec3 radiance, float metal, float rough) {
    vec3 h = normalize(v + l);
    float nl = max(dot(n, l), 0.0), nv = max(dot(n, v), 1e-4), nh = max(dot(n, h), 0.0), vh = max(dot(v, h), 0.0);
    vec3 f0 = mix(vec3(0.04), albedo, metal);
    vec3 f = arfFresnel(vh, f0);
    vec3 spec = arfDistGGX(nh, rough) * arfGeomSmith(nv, nl, rough) * f / (4.0 * nv * nl + 1e-4);
    vec3 diff = (1.0 - f) * (1.0 - metal) * albedo / ARF_PI;
    return (diff + spec) * radiance * nl;
}

// Stand-in for the sky probe: hemisphere gradient scaled by time of day.
vec3 arfAmbient(vec3 n, float dayFactor) {
    vec3 sky = mix(vec3(0.02, 0.03, 0.06), vec3(0.45, 0.6, 0.9), dayFactor);
    vec3 ground = sky * 0.35;
    return mix(ground, sky, n.z * 0.5 + 0.5);
}
