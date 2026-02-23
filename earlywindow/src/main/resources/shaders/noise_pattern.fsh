#version 330

layout(std140) uniform Scene {
    mat4 uProjectionMatrix;
    mat4 uInverseProjectionMatrix;
    mat4 uViewMatrix;
    mat4 uInverseViewMatrix;
    float uTime;
};

in vec2 texCoord;
out vec4 fragColor;

// noises: https://zhuanlan.zhihu.com/p/560229938
vec3 random3(vec3 c) {
    float j = 4096.0*sin(dot(c,vec3(17.0, 59.4, 15.0)));
    vec3 r;
    r.z = fract(512.0*j);
    j *= .125;
    r.x = fract(512.0*j);
    j *= .125;
    r.y = fract(512.0*j);
    return r-0.5;
}

const float F3 =  0.3333333;
const float G3 =  0.16666;

float simplex3d(vec3 p) {
    vec3 s = floor(p + dot(p, vec3(F3, F3, F3)));
    vec3 x = p - s + dot(s, vec3(G3, G3, G3));

    vec3 e = step(vec3(0,0,0), x - x.yzx);
    vec3 i1 = e*(1.0 - e.zxy);
    vec3 i2 = 1.0 - e.zxy*(1.0 - e);

    vec3 x1 = x - i1 + G3;
    vec3 x2 = x - i2 + 2.0*G3;
    vec3 x3 = x - 1.0 + 3.0*G3;

    vec4 w, d;

    w.x = dot(x, x);
    w.y = dot(x1, x1);
    w.z = dot(x2, x2);
    w.w = dot(x3, x3);

    w = max(0.6 - w, 0.0);

    d.x = dot(random3(s), x);
    d.y = dot(random3(s + i1), x1);
    d.z = dot(random3(s + i2), x2);
    d.w = dot(random3(s + 1.0), x3);

    w *= w;
    w *= w;
    d *= w;

    return dot(d, vec4(52.0, 52.0, 52.0, 52.0));
}

vec2 hash22(vec2 p) {
    p = vec2( dot(p,vec2(127.1,311.7)),
            dot(p,vec2(269.5,183.3)));
    return -1.0 + 2.0 * fract(sin(p)*43758.5453123);
}

vec2 hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yzx+19.19);
    return -1. + 2.*fract((p3.xx+p3.yz)*p3.zy);
}

float perlin_noise(vec2 p) {
    vec2 pi = floor(p);
    vec2 pf = p - pi;

    vec2 w = pf * pf * (3.0 - 2.0 * pf);

    return mix(mix(dot(hash22(pi + vec2(0.0, 0.0)), pf - vec2(0.0, 0.0)),
            dot(hash22(pi + vec2(1.0, 0.0)), pf - vec2(1.0, 0.0)), w.x),
            mix(dot(hash22(pi + vec2(0.0, 1.0)), pf - vec2(0.0, 1.0)),
                    dot(hash22(pi + vec2(1.0, 1.0)), pf - vec2(1.0, 1.0)), w.x),
            w.y);
}


float hash21(vec2 p) {
    float h = dot(p,vec2(127.1,311.7));
    return -1.0 + 2.0 * fract(sin(h)*43758.5453123);
}

float value_noise(vec2 p) {
    vec2 pi = floor(p);
    vec2 pf = p - pi;
    vec2 w = pf * pf * (3.0 - 2.0 * pf);

    return mix(mix(hash21(pi + vec2(0.0, 0.0)), hash21(pi + vec2(1.0, 0.0)), w.x),
            mix(hash21(pi + vec2(0.0, 1.0)), hash21(pi + vec2(1.0, 1.0)), w.x),
            w.y);
}

vec2 rad2dir(float r) {
    return vec2(cos(r), sin(r));
}

float sdSine(vec2 p, float A, float w, float t) {
    return p.y - A * sin(w * p.x + t);
}

float sdCircle( vec2 p, float r ) {
    return length(p) - r;
}

void main() {
    vec2 uv         = texCoord - vec2(0.0, 0.1);
    vec2 p          = abs(uv * 2.0 - 1.0) * 1.25;

    float n         = value_noise(vec2(uTime));
    float time      = uTime + n * 0.35;

    float noise1    = simplex3d(vec3(p * 2.96, time));
    float noise2    = simplex3d(vec3(p * 94.3, time * 1.2)) * 0.5 + 0.5;
    vec2 dir        = rad2dir(noise1 * 3.14 * 0.5) * noise2;

    vec2 pos        = p + dir * (0.25 + n * 0.15);

    float snoise    = perlin_noise(vec2(pos.x, time)) * 0.5 + 0.45;
    float cnoise1   = perlin_noise(vec2(pos.y * 1.2 - 0.1, time * 0.2));
    float cnoise2   = perlin_noise(vec2(pos.y + 0.1, time * 0.5));

    // waves
    float wl        = 4.9;
    float amp1      = 0.28 + cnoise1 * 0.12;
    float amp2      = 0.36 + cnoise2 * 0.23;

    float n1        = value_noise(vec2(uTime - 94.3, p.y));
    float n2        = value_noise(vec2(p.x, uTime));
    vec2 p1         = p + dir * (0.31 + n1 * 0.27);
    vec2 p2         = p + dir * (0.27 + n2 * 0.18);

    float wave1     = sdSine(p1 - vec2(0.0, 0.55), amp1, wl, p.y * 0.7 + 0.8);
    float wave2     = sdSine(p2 - vec2(0.0, 0.55), amp2, wl, p.x * 0.65 - 1.943);

    float w1r       = smoothstep(0.03 + snoise * 0.05, 0.0, abs(wave1));
    float w2r       = smoothstep(0.12 + snoise * 0.08, 0.0, abs(wave2));

    float weight    = max(w1r, w2r) * smoothstep(4.2, 1.2, dot(pos, pos));

    vec3 col        = vec3(mix(0.943, 0.90 + snoise * noise1 * 0.15, weight));
    fragColor       = vec4(col,1.0);
}