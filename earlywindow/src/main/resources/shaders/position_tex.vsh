#version 330

layout(std140) uniform Scene {
    mat4    uProjectionMatrix;
    mat4    uInverseProjectionMatrix;
    mat4    uViewMatrix;
    mat4    uInverseViewMatrix;
    float   uTime;
};

uniform mat4 uModelMatrix;

in vec3     Position;
in vec2     UV;
in int      Group;

out vec2    texCoord;

void main()
{
    texCoord = UV;
    vec4 pos = vec4(Position, 1.0);
    gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * pos;
}