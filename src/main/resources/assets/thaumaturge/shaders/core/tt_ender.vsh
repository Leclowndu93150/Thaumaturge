#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 eyePos;

void main() {
    vec4 eye = ModelViewMat * vec4(Position, 1.0);
    eyePos = eye.xyz;
    gl_Position = ProjMat * eye;
}
