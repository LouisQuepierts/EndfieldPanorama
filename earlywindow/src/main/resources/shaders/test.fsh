#version 150

layout(std140) uniform Scene {
    mat4 uProjectionMatrix;
    mat4 uInverseProjectionMatrix;
    mat4 uViewMatrix;
    mat4 uInverseViewMatrix;
    float uTime;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(texCoord, fract(uTime), 1.0);
}