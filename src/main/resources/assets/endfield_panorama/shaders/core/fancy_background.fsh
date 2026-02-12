#version 150

uniform sampler2D uMaskSampler;
uniform sampler2D uBackgroundSampler;

uniform mat4 uInverseViewMatrix;
uniform mat4 uInverseProjectionMatrix;

uniform float uTime;

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

float rectSDF(vec2 p, vec2 b) {
    vec2 d = abs(p) - b;
    return max(d.x, d.y);
}

float aastep(float threshold, float value) {
    float afwidth = fwidth(value);
    return smoothstep(threshold - afwidth * 0.5, threshold + afwidth * 0.5, value);
}

vec3 renderPattern(vec2 uv) {
    float iTime = uTime;
    float noise0 = simple_noise(uv + vec2(iTime * 0.1, 0.0), 3.0);
    float noise1 = simple_noise(uv + iTime * 0.01, 32.0) * noise0;
    float m = step(0.97, fract(triangular_wave(noise1) * 2.));

    vec3 bgColor            = vec3(0.95);
    vec3 patternColor       = vec3(0.85);

    return mix(bgColor, patternColor, m);
}

vec3 renderBackground()
{

    // ---------------------
    // Convert texCoord to NDC
    // ---------------------
    vec2 ndc = texCoord * 2.0 - 1.0;
    ndc.y *= -1.0;

    // ---------------------
    // Ray in world space
    // ---------------------
    mat4 invProj = uInverseProjectionMatrix;
    mat4 invView = uInverseViewMatrix;

    // NDC -> view space
    vec4 viewPos = invProj * vec4(ndc, -1.0, 1.0);
    viewPos /= viewPos.w;

    // View space -> world space
    vec3 rayDir         = normalize((invView * vec4(viewPos.xyz, 0.0)).xyz);
    vec3 rayOrigin      = (invView * vec4(0.0, 0.0, 0.0, 1.0)).xyz;

    // ---------------------
    // Plane
    // ---------------------
    float planeY        = -1.0;
    float t             = (planeY - rayOrigin.y) / rayDir.y;
    vec3 hit            = rayOrigin + rayDir * t;
    float heightMask    = step(t, 0.0);

    // ---------------------
    // Grid cell
    // ---------------------
    vec2 cellId         = floor(hit.xz);
    vec3 center         = vec3(cellId.x + 0.5, planeY, cellId.y + 0.5);


    // ---------------------
    // Billboard basis
    // ---------------------
    vec3 N              = normalize(rayOrigin - center);
    vec3 worldUp        = vec3(0.0, 1.0, 0.0);

    if (abs(dot(N, worldUp)) > 0.99)
        worldUp = vec3(0.0,0.0,1.0);

    vec3 R              = normalize(cross(worldUp, N));
    vec3 U              = cross(N, R);


    vec3 local          = hit - center;
    vec2 p              = vec2(dot(local, R), dot(local, U));


    // ---------------------
    // Shape SDF
    // ---------------------
    float size          = 0.03;
    float d             = rectSDF(p, vec2(size)); // rect

    float mask          = 1.0 - aastep(0.0, d);

    // ---------------------
    // Background Sky
    // ---------------------
    float time          = uTime;
    float disturb       = simple_noise(texCoord + vec2(time * 0.1, time * -0.1), 5.0);
    float lineNoise     = linear_noise((texCoord.y + time * 0.01 + disturb * 0.1) * 532.3);
    float line          = max(0.0, triangular_wave(lineNoise));

    // ---------------------
    // Color with fade by distance
    // ---------------------
    vec3 bgColor        = vec3( mix(0.95, 0.92, line) );
    vec3 groundColor    = renderPattern(hit.xz * 0.02);
    vec3 shapeColor     = vec3(0.65);


    float depthFade     = 1.0 - clamp(t * -0.0625, 0.0, 1.0);
    float bgWeight      = smoothstep(heightMask, 0.0, 0.05);
    vec3 col            = mix(
                            bgColor,
                            mix(groundColor, shapeColor, mask * heightMask),
                            bgWeight
                        );

    return col;
}

void main() {
    const vec3 white    = vec3(0.96);

    float baseGlitch1   = triangular_wave(simple_noise(texCoord.y + uTime * 128.0, 532.0)) * 0.5;
    float baseGlitch2   = square_wave(simple_noise(texCoord.y + uTime * 0.1, 68.0) * 2.0);
    float noise1        = simple_noise(uTime, 100.0);

    float strength      = step(noise1, 0.3);
    float glitch        = (baseGlitch1 + baseGlitch2) * strength * 0.002;

    vec2 glitchCoord    = texCoord + vec2(glitch, 0.0);

    float maskColor     = texture(uMaskSampler, glitchCoord).a;
    float mask          = step(0.1, maskColor);
    vec3 front          = texture(uBackgroundSampler, texCoord).rgb;
    vec3 back           = renderBackground();
    vec3 finalColor     = back;

    fragColor           = vec4(finalColor, 1.0);
}