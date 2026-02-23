#version 330

layout(std140) uniform Scene {
    mat4 uProjectionMatrix;
    mat4 uInverseProjectionMatrix;
    mat4 uViewMatrix;
    mat4 uInverseViewMatrix;
    float uTime;
};

uniform sampler2D uMaskSampler;
uniform sampler2D uBackgroundSampler;

in vec2 texCoord;
out vec4 fragColor;

float random_value(float v) {
    return fract(sin(v) * 43758.5453123);
}

float noise_interpolate(float a, float b, float t) {
    return (1.0-t)*a + (t*b);
}

float linear_noise(float v) {
    float i = floor(v);
    float f = fract(v);
    f = f * f * (3.0 - 2.0 * f);
    float p = abs(fract(v) - 0.5);

    float r0 = random_value(i);
    float r1 = random_value(i + 1.0);

    return noise_interpolate(r0, r1, f);
}

float simple_noise(float v, float scale) {
    float t0 = linear_noise(v * scale) * 0.125;
    float t1 = linear_noise(v * scale * 0.5) * 0.25;
    float t2 = linear_noise(v * scale * 0.25) * 0.5;

    return t0 + t1 + t2;
}

float random_value(vec2 v) {
    return fract(sin(dot(v, vec2(12.9898, 78.233)))*43758.5453);
}

float linear_noise(vec2 v) {
    vec2 i = floor(v);
    vec2 f = fract(v);
    f = f * f * (3.0 - 2.0 * f);

    vec2 c0 = i + vec2(0.0, 0.0);
    vec2 c1 = i + vec2(1.0, 0.0);
    vec2 c2 = i + vec2(0.0, 1.0);
    vec2 c3 = i + vec2(1.0, 1.0);

    float r0 = random_value(c0);
    float r1 = random_value(c1);
    float r2 = random_value(c2);
    float r3 = random_value(c3);

    return noise_interpolate(
            noise_interpolate(r0, r1, f.x),
            noise_interpolate(r2, r3, f.x),
            f.y
    );
}

float simple_noise(vec2 v, float scale) {
    float t0 = linear_noise(v * scale) * 0.125;
    float t1 = linear_noise(v * scale * 0.5) * 0.25;
    float t2 = linear_noise(v * scale * 0.25) * 0.5;

    return t0 + t1 + t2;
}

float triangular_wave(float v) {
    return 2.0 * abs( 2.0 * (v - floor(0.5 + v)) ) - 1.0;
}

float square_wave(float v) {
    return 1.0 - 2.0 * round(fract(v));
}

void main() {
    float baseGlitch1   = triangular_wave(simple_noise(texCoord.y + uTime * 128.0, 532.0)) * 0.5;
    float baseGlitch2   = square_wave(simple_noise(texCoord.y + uTime * 0.1, 68.0) * 2.0);
    float noise1        = simple_noise(uTime, 100.0);

    float strength      = step(noise1, 0.3);
    float glitch        = (baseGlitch1 + baseGlitch2) * strength * 0.002;

    vec2 glitchCoord    = texCoord + vec2(glitch, 0.0);

    float maskColor     = texture(uMaskSampler, glitchCoord).a;
    float mask          = step(0.1, maskColor);

    if (mask < 0.1) {
        discard;
    }

    vec3 finalColor     = texture(uBackgroundSampler, texCoord).rgb;
    fragColor           = vec4(finalColor, 1.0);
}