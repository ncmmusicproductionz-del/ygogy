#version 430 core
#include "arf/pbr.glsl"
in vec2 vUv;
uniform sampler2D arf_HdrColour;
out vec4 oColour;
void main() {
    vec3 hdr = texture(arf_HdrColour, vUv).rgb * float(ARF_OPT_EXPOSURE);
    vec3 ldr = (ARF_OPT_TONEMAP == 1) ? arfAgx(hdr) : arfAces(hdr);
    oColour = vec4(pow(ldr, vec3(1.0 / 2.2)), 1.0);
}
