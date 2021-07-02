#ifdef GL_ES
precision highp float;
#endif

varying vec2 v_texCoords;

uniform float time;
uniform sampler2D u_texture;

void main() {
    vec2 uvs = vec2(v_texCoords.x + time, v_texCoords.y);
    vec4 color = vec4(0.0, 1.0, 0.0, 1.0);
    gl_FragColor = texture2D(u_texture, uvs) * color;
}
