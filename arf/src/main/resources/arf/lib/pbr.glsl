// arf/pbr.glsl - shared PBR helpers (ARF API 1)
const float ARF_PI = 3.14159265359;

vec2 arfOctEncode(vec3 n) {
    n /= (abs(n.x) + abs(n.y) + abs(n.z));
    vec2 e = n.xy;
    if (n.z < 0.0) e = (1.0 - abs(e.yx)) * vec2(e.x >= 0.0 ? 1.0 : -1.0, e.y >= 0.0 ? 1.0 : -1.0);
    return e * 0.5 + 0.5;
}

vec3 arfOctDecode(vec2 f) {
    f = f * 2.0 - 1.0;
    vec3 n = vec3(f.x, f.y, 1.0 - abs(f.x) - abs(f.y));
    float t = clamp(-n.z, 0.0, 1.0);
    n.xy += vec2(n.x >= 0.0 ? -t : t, n.y >= 0.0 ? -t : t);
    return normalize(n);
}

// Narkowicz ACES fit
vec3 arfAces(vec3 x) {
    return clamp((x * (2.51 * x + 0.03)) / (x * (2.43 * x + 0.59) + 0.14), 0.0, 1.0);
}

// Cheap AgX-style sigmoid; swap for the full transform in a pack that wants it.
vec3 arfAgx(vec3 x) {
    x = max(x, 0.0);
    return clamp(x / (x + 0.155) * 1.019, 0.0, 1.0);
}
