#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out float vertexDistance;
out vec4 vertexColor;
out vec4 overlayColor;
out vec2 texCoord0;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    vertexDistance = length(viewPosition.xyz);
    // Match main's NO_CARDINAL_LIGHTING and NO_OVERLAY flat pipelines.
    vertexColor = Color;
    overlayColor = vec4(0.0, 0.0, 0.0, 1.0);
    texCoord0 = UV0;
}
