#version 330

in  vec2    texCoord;
in  vec4    color;

out vec4    fragColor;

uniform sampler2D   uDiffuseSampler;
uniform vec4        uColor;
uniform int         uRenderType;

#define TYPE_REGULAR 0
#define TYPE_TEXTURE 1
#define TYPE_CUTOUT  2
#define TYPE_SINGLE  3

void main() {
    vec4 color    = uColor;

    if (uRenderType == TYPE_TEXTURE) {
        vec4 tex    = texture(uDiffuseSampler, texCoord);
        color       *= tex;
    }

    if (uRenderType == TYPE_CUTOUT) {
        if (color.a < 0.1) {
            discard;
        }
    }

    fragColor       = color;
}