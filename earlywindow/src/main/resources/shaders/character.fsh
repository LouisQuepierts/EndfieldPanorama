#version 330

uniform sampler2D uTexture;

in  vec2    texCoord;
out vec4    fragColor;

void main() {
    vec4 color = texture(uTexture, texCoord);

    if (color.a < 0.1) {
        discard;
    }

    fragColor = color;
}