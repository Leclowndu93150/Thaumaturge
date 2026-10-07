#version 150

uniform sampler2D Sampler0;

uniform mat4 ModelViewMat;
uniform float GameTime;

in vec3 eyePos;

out vec4 fragColor;

const float TAU = 6.283185307179586;
const float PI = 3.141592653589793;
const int LAYER_COUNT = 16;
const vec3 SPACE_TINT = vec3(0.044, 0.036, 0.063);
const float SCROLL_RATE = 0.00006;

float layerHash(float seed) {
    return fract(sin(seed * 12.9898) * 43758.5453);
}

vec3 spin(vec3 v, float yaw, float tilt) {
    float cy = cos(yaw);
    float sy = sin(yaw);
    v = vec3(v.x * cy - v.z * sy, v.y, v.x * sy + v.z * cy);
    float ct = cos(tilt);
    float st = sin(tilt);
    return vec3(v.x, v.y * ct - v.z * st, v.y * st + v.z * ct);
}

vec3 starfield(vec3 look, float scroll) {
    vec3 col = SPACE_TINT;
    for (int layer = 0; layer < LAYER_COUNT; layer++) {
        float seed = float(layer) * 7.31 + 1.0;
        vec3 ray = spin(look, layerHash(seed) * TAU, layerHash(seed + 17.0) * TAU);

        float around = atan(ray.z, ray.x) / TAU + 0.5;
        float band = asin(clamp(ray.y, -1.0, 1.0)) / PI + 0.5;

        float zoom = 2.75 + 0.5 * float(LAYER_COUNT - layer);
        float star = texture(Sampler0, vec2(around * zoom, (band + scroll) * zoom * 0.6)).r;

        float depth = 1.0 / float(LAYER_COUNT - layer);
        float poleFade = 1.0 - smoothstep(0.15, 0.48, abs(band - 0.5));
        float glow = star * (0.05 + depth * 0.65) * poleFade;

        vec3 tint = vec3(0.1 + 0.5 * layerHash(seed + 3.0),
                         0.4 + 0.5 * layerHash(seed + 5.0),
                         0.5 + 0.5 * layerHash(seed + 7.0));
        col = mix(col, tint, glow);
    }
    return col;
}

void main() {
    vec3 look = normalize(transpose(mat3(ModelViewMat)) * normalize(-eyePos));
    fragColor = vec4(starfield(look, GameTime * 24000.0 * SCROLL_RATE), 1.0);
}
