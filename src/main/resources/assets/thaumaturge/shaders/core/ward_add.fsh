#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    vec4 color = vec4(clamp(tex.rgb + vertexColor.rgb, 0.0, 1.0), tex.a * vertexColor.a);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
