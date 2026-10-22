#version 150

#moj_import <fog.glsl>

uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 surfaceCoord;
in vec3 viewRay;
in float vertexDistance;

out vec4 fragColor;

const float TAU = 6.28318530718;
const float MIN_INCIDENCE = 0.07;
const int STAR_LAYERS = 6;
const float STAR_DEPTH[STAR_LAYERS] = float[](0.3, 0.75, 1.5, 2.7, 4.6, 8.0);
const float STAR_CELL[STAR_LAYERS] = float[](0.62, 0.48, 0.38, 0.3, 0.24, 0.19);
const float STAR_DENSITY[STAR_LAYERS] = float[](0.10, 0.16, 0.24, 0.32, 0.40, 0.48);
const float STAR_GAIN[STAR_LAYERS] = float[](1.6, 1.35, 1.1, 0.85, 0.65, 0.5);
const float STAR_ORBIT[STAR_LAYERS] = float[](1.0, 1.3, 1.6, 2.0, 2.6, 3.2);
const float STAR_ORBIT_TURNS[STAR_LAYERS] = float[](12.0, -9.0, 8.0, -6.0, 5.0, -4.0);
const float STAR_MARGIN = 0.2;
const float NEBULA_DEPTH = 12.0;
const float NEBULA_SCALE = 0.16;
const float NEBULA_GAIN = 0.8;
const float VEIL_SCALE = 0.55;

const vec3 VOID_BASE = vec3(0.008, 0.004, 0.02);
const vec3 NEBULA_INDIGO = vec3(0.07, 0.04, 0.24);
const vec3 NEBULA_VIOLET = vec3(0.34, 0.08, 0.52);
const vec3 NEBULA_TEAL = vec3(0.03, 0.30, 0.36);
const vec3 NEBULA_ROSE = vec3(0.72, 0.18, 0.55);
const vec3 VEIL_GLOW = vec3(0.42, 0.20, 0.85);
const vec3 RIM_GLOW = vec3(0.30, 0.12, 0.55);

vec3 hash3(ivec2 cell, uint salt) {
    uvec3 v = uvec3(uint(cell.x), uint(cell.y), salt) * 1664525u + 1013904223u;
    v.x += v.y * v.z;
    v.y += v.z * v.x;
    v.z += v.x * v.y;
    v ^= v >> 16u;
    v.x += v.y * v.z;
    v.y += v.z * v.x;
    v.z += v.x * v.y;
    return vec3(v) * (1.0 / 4294967295.0);
}

float valueNoise(vec2 p, uint salt) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 s = f * f * (3.0 - 2.0 * f);
    ivec2 c = ivec2(i);
    float a = hash3(c, salt).x;
    float b = hash3(c + ivec2(1, 0), salt).x;
    float d = hash3(c + ivec2(0, 1), salt).x;
    float e = hash3(c + ivec2(1, 1), salt).x;
    return mix(mix(a, b, s.x), mix(d, e, s.x), s.y);
}

float fbm(vec2 p, uint salt) {
    const mat2 TURN = mat2(0.8, 0.6, -0.6, 0.8);
    float sum = 0.0;
    float amplitude = 0.5;
    for (int octave = 0; octave < 5; octave++) {
        sum += amplitude * valueNoise(p, salt + uint(octave) * 7u);
        p = TURN * p * 2.03 + vec2(1.7, 9.2);
        amplitude *= 0.5;
    }
    return sum;
}

vec2 orbit(float day, float turns, float radius, float phase) {
    float angle = TAU * turns * day + phase;
    return vec2(cos(angle), sin(angle)) * radius;
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

vec3 starLayer(vec2 uv, int layer, float day) {
    float cellSize = STAR_CELL[layer];
    vec2 p = uv / cellSize;
    vec2 footprint = fwidth(p) * cellSize;
    ivec2 cell = ivec2(floor(p));
    vec3 roll = hash3(cell, uint(layer) * 101u + 11u);
    if (roll.x > STAR_DENSITY[layer]) {
        return vec3(0.0);
    }
    vec3 place = hash3(cell, uint(layer) * 101u + 53u);
    vec2 offset = (fract(p) - (STAR_MARGIN + (1.0 - 2.0 * STAR_MARGIN) * place.xy)) * cellSize;
    float dist = length(offset);
    float edge = STAR_MARGIN * cellSize;
    float size = cellSize * mix(0.012, 0.045, roll.y * roll.y);
    float blur = max(length(footprint) * 0.75, 1e-4);
    float radius = max(size, blur);
    float core = smoothstep(radius, 0.0, dist) * (size / radius);
    float halo = exp(-dist * dist / (size * size * 5.0)) * 0.3;
    float sparkle = 0.0;
    if (roll.y > 0.8) {
        vec2 a = abs(offset);
        sparkle = (exp(-a.x / (size * 0.3)) * exp(-a.y / (size * 3.5)) + exp(-a.y / (size * 0.3)) * exp(-a.x / (size * 3.5))) * 0.45;
    }
    float fence = smoothstep(edge, edge * 0.4, dist);
    halo *= fence;
    sparkle *= fence;
    float twinkle = 0.55 + 0.45 * sin(TAU * floor(180.0 + 420.0 * place.z) * day + roll.z * TAU);
    return starTint(roll.z) * (core + halo + sparkle) * twinkle * STAR_GAIN[layer];
}

vec3 nebula(vec2 uv, float day) {
    vec2 p = uv * NEBULA_SCALE;
    vec2 drift = orbit(day, 2.0, 0.9, 0.0);
    vec2 warp = vec2(fbm(p + drift, 3u), fbm(p + vec2(5.2, 1.3) - drift.yx, 9u));
    float cloud = fbm(p + 2.4 * warp + orbit(day, -1.0, 0.6, 1.3), 17u);
    float wisp = fbm(p * 2.2 - 1.6 * warp + vec2(7.1, 3.4), 29u);
    vec3 color = mix(VOID_BASE, NEBULA_INDIGO, smoothstep(0.25, 0.65, cloud));
    color = mix(color, NEBULA_VIOLET, smoothstep(0.48, 0.82, cloud) * 0.85);
    color = mix(color, NEBULA_TEAL, smoothstep(0.55, 0.85, wisp) * smoothstep(0.35, 0.6, cloud) * 0.55);
    color += NEBULA_ROSE * pow(smoothstep(0.62, 0.95, cloud * wisp * 1.9), 2.0) * 0.45;
    return color * NEBULA_GAIN;
}

float veil(vec2 uv, float day) {
    vec2 p = uv * VEIL_SCALE + orbit(day, 4.0, 0.7, 0.4);
    float ridge = 1.0 - abs(2.0 * fbm(p, 41u) - 1.0);
    float pulse = 0.65 + 0.35 * sin(TAU * 90.0 * day);
    return pow(ridge, 6.0) * pulse;
}

void main() {
    vec3 normal = abs(cross(dFdx(viewRay), dFdy(viewRay)));
    vec3 look = normalize(viewRay);
    vec2 slide;
    float incidence;
    if (normal.y >= normal.x && normal.y >= normal.z) {
        slide = look.xz;
        incidence = abs(look.y);
    } else if (normal.x >= normal.z) {
        slide = look.zy;
        incidence = abs(look.x);
    } else {
        slide = look.xy;
        incidence = abs(look.z);
    }
    vec2 parallax = slide / max(incidence, MIN_INCIDENCE);
    float day = GameTime;

    vec3 color = nebula(surfaceCoord + parallax * NEBULA_DEPTH, day);
    for (int layer = STAR_LAYERS - 1; layer >= 0; layer--) {
        float phase = float(layer) * 1.7;
        vec2 uv = surfaceCoord + parallax * STAR_DEPTH[layer] + orbit(day, STAR_ORBIT_TURNS[layer], STAR_ORBIT[layer], phase);
        float depthFade = 1.0 - float(layer) / float(STAR_LAYERS) * 0.45;
        color += starLayer(uv, layer, day) * depthFade;
    }
    color += VEIL_GLOW * veil(surfaceCoord + parallax * 0.12, day) * 0.22;
    color += RIM_GLOW * pow(1.0 - clamp(incidence, 0.0, 1.0), 4.0) * 0.35;

    color = color / (1.0 + color * 0.35);
    fragColor = linear_fog(vec4(color, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
