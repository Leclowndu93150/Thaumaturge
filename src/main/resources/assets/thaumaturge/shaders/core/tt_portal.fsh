#version 150

#moj_import <fog.glsl>

uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec3 viewRay;
in float vertexDistance;

out vec4 fragColor;

const float TAU = 6.28318530718;
const int STAR_LAYERS = 6;
const float STAR_GRID[STAR_LAYERS] = float[](12.0, 19.0, 30.0, 46.0, 70.0, 104.0);
const float STAR_DENSITY[STAR_LAYERS] = float[](0.30, 0.30, 0.28, 0.26, 0.24, 0.22);
const float STAR_GAIN[STAR_LAYERS] = float[](1.7, 1.4, 1.15, 0.95, 0.75, 0.6);
const float STAR_TURNS[STAR_LAYERS] = float[](6.0, -5.0, 4.0, -3.0, 2.0, -1.0);
const vec3 STAR_AXIS[STAR_LAYERS] = vec3[](vec3(0.27, 0.93, 0.25), vec3(-0.6, 0.72, 0.35), vec3(0.15, 0.85, -0.5), vec3(0.7, 0.6, -0.39), vec3(-0.3, 0.9, -0.31),
        vec3(0.0, 1.0, 0.0));
const float STAR_MARGIN = 0.2;
const float NEBULA_SCALE = 2.6;
const float NEBULA_GAIN = 0.85;
const vec3 NEBULA_AXIS = vec3(0.36, 0.86, 0.36);
const float VEIL_SCALE = 5.5;
const vec3 VEIL_AXIS = vec3(-0.5, 0.7, 0.5);

const vec3 VOID_BASE = vec3(0.008, 0.004, 0.02);
const vec3 NEBULA_INDIGO = vec3(0.07, 0.04, 0.24);
const vec3 NEBULA_VIOLET = vec3(0.34, 0.08, 0.52);
const vec3 NEBULA_TEAL = vec3(0.03, 0.30, 0.36);
const vec3 NEBULA_ROSE = vec3(0.72, 0.18, 0.55);
const vec3 VEIL_GLOW = vec3(0.42, 0.20, 0.85);

vec3 mix3(uvec3 v) {
    v = v * 1664525u + 1013904223u;
    v.x += v.y * v.z;
    v.y += v.z * v.x;
    v.z += v.x * v.y;
    v ^= v >> 16u;
    v.x += v.y * v.z;
    v.y += v.z * v.x;
    v.z += v.x * v.y;
    return vec3(v) * (1.0 / 4294967295.0);
}

vec3 hashCell(ivec2 cell, uint salt) {
    return mix3(uvec3(uint(cell.x), uint(cell.y), salt));
}

float hashPoint(ivec3 cell, uint salt) {
    return mix3(uvec3(uint(cell.x), uint(cell.y), uint(cell.z) ^ (salt * 2654435769u))).x;
}

float valueNoise(vec3 p, uint salt) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 s = f * f * (3.0 - 2.0 * f);
    ivec3 c = ivec3(i);
    float x00 = mix(hashPoint(c, salt), hashPoint(c + ivec3(1, 0, 0), salt), s.x);
    float x10 = mix(hashPoint(c + ivec3(0, 1, 0), salt), hashPoint(c + ivec3(1, 1, 0), salt), s.x);
    float x01 = mix(hashPoint(c + ivec3(0, 0, 1), salt), hashPoint(c + ivec3(1, 0, 1), salt), s.x);
    float x11 = mix(hashPoint(c + ivec3(0, 1, 1), salt), hashPoint(c + ivec3(1, 1, 1), salt), s.x);
    return mix(mix(x00, x10, s.y), mix(x01, x11, s.y), s.z);
}

float fbm(vec3 p, uint salt, int octaves) {
    float sum = 0.0;
    float amplitude = 0.5;
    for (int octave = 0; octave < octaves; octave++) {
        sum += amplitude * valueNoise(p, salt + uint(octave) * 7u);
        p = p.yzx * 2.03 + vec3(1.7, 9.2, 4.1);
        amplitude *= 0.5;
    }
    return sum;
}

vec3 turn(vec3 v, vec3 axis, float angle) {
    vec3 a = normalize(axis);
    float c = cos(angle);
    float s = sin(angle);
    return v * c + cross(a, v) * s + a * dot(a, v) * (1.0 - c);
}

vec4 cubeFace(vec3 dir) {
    vec3 a = abs(dir);
    if (a.x >= a.y && a.x >= a.z) {
        return vec4(dir.zy / a.x, dir.x > 0.0 ? 0.0 : 1.0, a.x);
    }
    if (a.y >= a.z) {
        return vec4(dir.xz / a.y, dir.y > 0.0 ? 2.0 : 3.0, a.y);
    }
    return vec4(dir.xy / a.z, dir.z > 0.0 ? 4.0 : 5.0, a.z);
}

vec3 starTint(float pick) {
    if (pick < 0.45) {
        return vec3(0.82, 0.86, 1.0);
    }
    if (pick < 0.7) {
        return vec3(0.62, 0.72, 1.0);
    }
    if (pick < 0.88) {
        return vec3(0.86, 0.6, 1.0);
    }
    return vec3(1.0, 0.82, 0.62);
}

vec3 starLayer(vec3 look, int layer, float day, float pixelAngle) {
    vec3 dir = turn(look, STAR_AXIS[layer], TAU * STAR_TURNS[layer] * day);
    vec4 face = cubeFace(dir);
    float grid = STAR_GRID[layer];
    vec2 p = (face.xy * 0.5 + 0.5) * grid;
    uint salt = uint(layer) * 101u + uint(face.z) * 7919u;
    ivec2 cell = ivec2(floor(p));
    vec3 roll = hashCell(cell, salt + 11u);
    if (roll.x > STAR_DENSITY[layer]) {
        return vec3(0.0);
    }
    vec3 place = hashCell(cell, salt + 53u);
    vec2 offset = fract(p) - (STAR_MARGIN + (1.0 - 2.0 * STAR_MARGIN) * place.xy);
    float dist = length(offset);
    float size = mix(0.035, 0.11, roll.y * roll.y);
    float blur = max(pixelAngle * grid * 0.5 / face.w, 1e-4);
    float radius = max(size, blur);
    float core = smoothstep(radius, 0.0, dist) * (size / radius);
    float halo = exp(-dist * dist / (size * size * 5.0)) * 0.3;
    float sparkle = 0.0;
    if (roll.y > 0.8) {
        vec2 a = abs(offset);
        sparkle = (exp(-a.x / (size * 0.3)) * exp(-a.y / (size * 3.5)) + exp(-a.y / (size * 0.3)) * exp(-a.x / (size * 3.5))) * 0.45;
    }
    float fence = smoothstep(STAR_MARGIN, STAR_MARGIN * 0.4, dist);
    float twinkle = 0.55 + 0.45 * sin(TAU * floor(180.0 + 420.0 * place.z) * day + roll.z * TAU);
    return starTint(roll.z) * (core + (halo + sparkle) * fence) * twinkle * STAR_GAIN[layer];
}

vec3 nebula(vec3 look, float day) {
    vec3 p = turn(look, NEBULA_AXIS, TAU * day) * NEBULA_SCALE;
    vec3 drift = turn(vec3(0.8, 0.0, 0.0), VEIL_AXIS, TAU * 2.0 * day);
    vec3 warp = vec3(fbm(p + drift, 3u, 4), fbm(p + vec3(5.2, 1.3, 2.7) - drift.yzx, 9u, 4), fbm(p + vec3(2.1, 7.7, 0.4), 13u, 4));
    float cloud = fbm(p + 2.4 * warp, 17u, 5);
    float wisp = fbm(p * 2.2 - 1.6 * warp + vec3(7.1, 3.4, 5.9), 29u, 5);
    vec3 color = mix(VOID_BASE, NEBULA_INDIGO, smoothstep(0.25, 0.65, cloud));
    color = mix(color, NEBULA_VIOLET, smoothstep(0.48, 0.82, cloud) * 0.85);
    color = mix(color, NEBULA_TEAL, smoothstep(0.55, 0.85, wisp) * smoothstep(0.35, 0.6, cloud) * 0.55);
    color += NEBULA_ROSE * pow(smoothstep(0.62, 0.95, cloud * wisp * 1.9), 2.0) * 0.45;
    return color * NEBULA_GAIN;
}

float veil(vec3 look, float day) {
    vec3 p = turn(look, VEIL_AXIS, TAU * 8.0 * day) * VEIL_SCALE;
    float ridge = 1.0 - abs(2.0 * fbm(p, 41u, 4) - 1.0);
    float pulse = 0.65 + 0.35 * sin(TAU * 90.0 * day);
    return pow(ridge, 6.0) * pulse;
}

void main() {
    vec3 look = normalize(viewRay);
    float pixelAngle = length(fwidth(look));
    float day = GameTime;

    vec3 color = nebula(look, day);
    for (int layer = STAR_LAYERS - 1; layer >= 0; layer--) {
        color += starLayer(look, layer, day, pixelAngle);
    }
    color += VEIL_GLOW * veil(look, day) * 0.18;

    color = color / (1.0 + color * 0.35);
    fragColor = linear_fog(vec4(color, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
