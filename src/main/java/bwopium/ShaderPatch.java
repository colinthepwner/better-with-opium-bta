package bwopium;

public final class ShaderPatch {
	static final String SAT_LINE = "color = adjustSaturation(color, saturation);";
	static final String MAIN = "void main()";

	static final String GLSL = """
		uniform float bwoCrave;

		vec3 bwoHsv(vec3 c) {
		    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
		    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
		    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
		    float d = q.x - min(q.w, q.y);
		    float e = 1.0e-10;
		    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
		}

		float bwoMask(vec3 c) {
		    vec3 h = bwoHsv(clamp(c, 0.0, 1.0));
		    float d = abs(h.x - 0.855);
		    d = min(d, 1.0 - d);
		    return (1.0 - smoothstep(0.035, 0.075, d)) * smoothstep(0.10, 0.24, h.y) * smoothstep(0.08, 0.20, h.z);
		}

		vec3 bwoSaturate(vec3 color, float s) {
		    vec3 graded = adjustSaturation(color, s);
		    if (bwoCrave <= 0.001) return graded;
		    float pulse = 0.88 + 0.12 * sin(frameTimeCounter * 1.7);
		    float m = bwoMask(color) * bwoCrave * pulse;
		    vec3 vivid = adjustSaturation(color, max(s, 0.0) + 0.25) * (1.0 + 0.06 * pulse);
		    return mix(graded, vivid, m);
		}

		""";

	public static String apply(String src) {
		if (src == null || !src.contains(SAT_LINE) || !src.contains(MAIN) || src.contains("bwoCrave")) return src;
		return src.replace(SAT_LINE, "color = bwoSaturate(color, saturation);")
			.replace(MAIN, GLSL + MAIN);
	}
}
