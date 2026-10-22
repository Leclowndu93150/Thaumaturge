#version 150

#moj_import <fog.glsl>

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec3 viewRay;
out float vertexDistance;

void main() {
    vec4 clip = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clip;

    float scaleX = length(vec3(ProjMat[0][0], ProjMat[1][0], ProjMat[2][0]));
    float scaleY = length(vec3(ProjMat[0][1], ProjMat[1][1], ProjMat[2][1]));
    vec3 steadyEye = vec3(clip.x / scaleX, clip.y / scaleY, -clip.w);
    viewRay = transpose(mat3(ModelViewMat)) * steadyEye;
    vertexDistance = fog_distance(Position, FogShape);
}
