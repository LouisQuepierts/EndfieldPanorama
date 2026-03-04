#version 330
// blit_screen

uniform sampler2D uDiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor   = texture(uDiffuseSampler, texCoord);
}
