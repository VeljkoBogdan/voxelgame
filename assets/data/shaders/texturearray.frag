#ifdef GL_ES
precision mediump float;
precision mediump sampler2DArray;
#endif

uniform sampler2DArray u_textureArray;

in vec3 v_texCoords;

out vec4 fragColor;

void main() {
    fragColor = texture(u_textureArray, v_texCoords);

    //gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0); // test with color
    //gl_FragColor = texture(u_textureArray, vec3(v_texCoords.xy, 0.0)); // test with fixed layer
}
