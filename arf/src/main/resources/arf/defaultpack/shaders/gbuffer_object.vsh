#version 430 core
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aUv;
uniform mat4 arf_View;
uniform mat4 arf_Proj;
out vec3 vNormal;
out vec2 vUv;
void main() {
    vNormal = mat3(arf_View) * aNormal;
    vUv = aUv;
    gl_Position = arf_Proj * arf_View * vec4(aPos, 1.0);
}
