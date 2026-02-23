#version 330

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

layout(location = 0) in vec3     Position;
layout(location = 1) in vec2     UV;
layout(location = 2) in int      Group;

out vec2    texCoord;

void main() {
    vec4 position = vec4(Position, 1.0);
    mat4 bone = uBoneTransforms[Group];
    gl_Position = uProjectionMatrix * uViewMatrix * uModelMatrix * bone * position;
    texCoord = UV;
}
