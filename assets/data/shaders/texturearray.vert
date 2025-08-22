in vec3 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;
in vec2 a_texCoord1;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

out vec3 v_texCoords;

void main() {
    v_texCoords = vec3(a_texCoord0, a_texCoord1.x);
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1);

    //gl_Position = u_projTrans * vec4(a_position, 1.0); // test with no world translation
}
