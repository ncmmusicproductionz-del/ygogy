#version 430 core
layout(location = 0) in vec3 aPos;
uniform mat4 arf_CascadeViewProj;
void main() { gl_Position = arf_CascadeViewProj * vec4(aPos, 1.0); }
