#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
varying vec4 v_color;
varying vec2 v_texCoord;

uniform bool u_drawShadow;
uniform float u_smoothing;
uniform vec2 u_shadowOffset;
uniform float u_shadowSmoothing;
uniform vec4 u_shadowColor;

void main() {
    // Glyph
    float distance = texture2D(u_texture, v_texCoord).a;
    float alpha = smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, distance);
    vec4 glyph = vec4(v_color.rgb, v_color.a * alpha);

    // Shadow
    if (u_drawShadow && alpha < 0.1) {
        float shadowDistance = texture2D(u_texture, v_texCoord - u_shadowOffset).a;
        float shadowAlpha = smoothstep(0.5 - u_shadowSmoothing, 0.5 + u_shadowSmoothing, shadowDistance);
        vec4 shadow = vec4(u_shadowColor.rgb, u_shadowColor.a * shadowAlpha);
        gl_FragColor = mix(shadow, glyph, glyph.a);
    } else {
        gl_FragColor = glyph;
    }
}