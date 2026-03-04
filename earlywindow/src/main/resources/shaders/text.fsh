#version 330

in  vec2    texCoord;
in  vec4    color;

out vec4    fragColor;

uniform sampler2D   uDiffuseSampler;

void main() {
    float c         = texture(uDiffuseSampler, texCoord).r;
    vec4 diffuse    = vec4(c) * color;
    fragColor       = diffuse;
}