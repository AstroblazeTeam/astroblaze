#ifdef GL_ES
precision mediump float;
#endif

uniform float alpha;
varying vec2 v_texCoords;

void main() {
    gl_FragColor = vec4(0.0, 0.0, 0.0, alpha);
}
