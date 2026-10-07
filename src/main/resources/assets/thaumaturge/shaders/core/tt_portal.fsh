#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in float vertexDistance;

const vec3[] COLORS = vec3[](
    vec3(0.100000, 0.100000, 0.100000),
    vec3(0.006317, 0.050960, 0.047539),
    vec3(0.014739, 0.054234, 0.053507),
    vec3(0.024945, 0.058866, 0.061520),
    vec3(0.034947, 0.063375, 0.052333),
    vec3(0.034537, 0.047068, 0.066975),
    vec3(0.046264, 0.061088, 0.090753),
    vec3(0.053619, 0.084766, 0.050085),
    vec3(0.058973, 0.072858, 0.108439),
    vec3(0.054968, 0.061981, 0.105316),
    vec3(0.076295, 0.079016, 0.084904),
    vec3(0.040837, 0.141944, 0.137545),
    vec3(0.118059, 0.085740, 0.128817),
    vec3(0.029551, 0.197086, 0.201231),
    vec3(0.136450, 0.260007, 0.201378),
    vec3(0.060716, 0.236115, 0.496118)
);

const float[] SCALES = float[](0.125, 0.5, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625, 0.0625);

out vec4 fragColor;

vec2 layer_uv(vec2 base, int i) {
    float ang = radians(float(i * i * 4321 + i * 9) * 2.0);
    float c = cos(ang);
    float s = sin(ang);
    vec2 uv = mat2(c, s, -s, c) * (base - 0.5) + 0.5;
    uv *= SCALES[i];
    uv.y += GameTime * 4.8;
    return uv;
}

const float UV_SHARPNESS = 3.0;

void main() {
    vec2 base = texCoord0 * UV_SHARPNESS;
    vec3 color = texture(Sampler0, layer_uv(base, 0)).rgb * COLORS[0];
    for (int i = 1; i < 16; i++) {
        vec4 tex = texture(Sampler1, layer_uv(base, i));
        color += tex.rgb * tex.a * COLORS[i];
    }
    fragColor = linear_fog(vec4(color, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
