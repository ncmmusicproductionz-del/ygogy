#version 430 core
#include "arf/pbr.glsl"
in vec3 vNormal;
in vec2 vUv;
uniform sampler2D arf_Albedo;
uniform int arf_MaterialClass;   // MaterialClass.id(), default 0 for this stage
layout(location = 0) out vec4 oAlbedo;     // RGBA8
layout(location = 1) out vec2 oNormal;     // RG16 octahedral
layout(location = 2) out vec4 oMaterial;   // R metal, G rough, B AO, A emissive
void main() {
    vec4 c = texture(arf_Albedo, vUv);
    if (c.a < 0.5) discard;
    oAlbedo = vec4(c.rgb, 1.0);
    oNormal = arfOctEncode(normalize(vNormal));
    oMaterial = vec4(0.0, 0.8, 1.0, 0.0);
}
