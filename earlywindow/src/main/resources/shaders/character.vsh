#version 150

#define MAX_BONES 32

layout(std140) uniform Scene {
    mat4    uProjectionMatrix;
    mat4    uInverseProjectionMatrix;
    mat4    uViewMatrix;
    mat4    uInverseViewMatrix;
    float   uTime;
};

layout(std140) uniform AnimationSkeleton {
    mat4    uBoneTransforms[MAX_BONES];
};

uniform mat4 uModelMatrix;

in vec3     Position;
in vec2     UV;
in int      Group;

out vec2    texCoord;

void main() {
    vec4 position = vec4(Position, 1.0);
    position = uBoneTransforms[Group] * position;
    gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * position;
    texCoord = UV;
}
