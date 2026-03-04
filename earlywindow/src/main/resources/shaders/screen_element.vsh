#version 330

layout(location = 0) in vec2    Position;
layout(location = 1) in vec2    UV;
layout(location = 2) in vec4    Color;

out vec2 texCoord;
out vec4 color;

uniform mat4 uProjectionViewMatrix;

void main() {
    texCoord    = UV;
    color       = Color;
    gl_Position = uProjectionViewMatrix * vec4(Position, 0.0, 1.0);
}