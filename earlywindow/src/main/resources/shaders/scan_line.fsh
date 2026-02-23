#version 330

uniform sampler2D uDiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float overlay   = max(0.0, sin(texCoord.y * 943.0f * 1.6f));

    vec3  lineColor = vec3(0.08);
    vec3  diffuse   = texture(uDiffuseSampler, texCoord).rgb;

    fragColor = vec4(diffuse - overlay * lineColor, 1.0);
}