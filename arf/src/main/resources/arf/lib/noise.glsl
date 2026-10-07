// arf/noise.glsl
float arfHash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float arfValueNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(arfHash(i), arfHash(i + vec2(1, 0)), f.x),
               mix(arfHash(i + vec2(0, 1)), arfHash(i + vec2(1, 1)), f.x), f.y);
}
