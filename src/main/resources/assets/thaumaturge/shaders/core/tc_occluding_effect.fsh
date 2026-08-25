#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

in float vertexDistance;
in vec4 vertexColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

const float EDGE_ALPHA_CUTOFF = 0.05;
const float OPAQUE_ALPHA_CUTOFF = 0.4;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < EDGE_ALPHA_CUTOFF) {
        discard;
    }
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color.a = smoothstep(EDGE_ALPHA_CUTOFF, OPAQUE_ALPHA_CUTOFF, color.a);
    fragColor = color * linear_fog_fade(vertexDistance, FogStart, FogEnd);
}
