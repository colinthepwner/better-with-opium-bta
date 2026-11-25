package bwopium;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.PlayerSkinParser;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.renderer.CompareFunc;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.texture.TextureBuffered;
import net.minecraft.client.render.texture.TextureDownloaded;
import net.minecraft.core.entity.player.Player;
import org.useless.dragonfly.models.entity.StaticEntityModel;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class Pallor {
	static final Map<String, TextureBuffered> masks = new HashMap<>();
	static final Map<Player, float[]> eased = new WeakHashMap<>();

	static float target(Opium o) { return 0.6f * smooth((o.od - 0.45f) / 0.7f); }

	public static void draw(Player p, StaticEntityModel m) {
		Opium o = OpiumHolder.of(p);
		if (o == null || m == null) return;

		float[] e = eased.computeIfAbsent(p, k -> new float[]{0, System.nanoTime()});
		long now = System.nanoTime();
		float dt = Math.min(0.5f, (now - (long) e[1]) / 1e9f);
		e[1] = now;
		e[0] += (target(o) - e[0]) * (float) (1 - Math.exp(-dt / 1.2));
		float amt = e[0];
		if (amt < 0.003f) return;

		TextureBuffered t = mask_for(p);
		if (t == null || !t.bind()) return;
		float at = GLRenderer.getAlphaTest();
		GLRenderer.setAlphaTest(0.001f);
		GLRenderer.setDepthFunc(CompareFunc.EQUAL);
		GLRenderer.setDepthMask(false);
		GLRenderer.setColor4f(1, 1, 1, amt);
		m.render();
		GLRenderer.setColor4f(1, 1, 1, 1);
		GLRenderer.setDepthMask(true);
		GLRenderer.setDepthFunc(CompareFunc.LESS_EQUAL);
		GLRenderer.setAlphaTest(at);
	}

	static TextureBuffered mask_for(Player p) {
		TextureManager tm = Minecraft.getMinecraft().textureManager;
		String key = null;
		BufferedImage src = null;
		if (p.skinURL != null) {
			TextureDownloaded d = tm.downloadedTextures.get(p.skinURL);
			if (d != null && d.isGenerated() && d.image != null) { key = p.skinURL; src = d.image; }
		}
		if (src == null) key = p.getEntityTexture();
		if (key == null) return null;
		TextureBuffered t = masks.get(key);
		if (t != null) return t;
		if (src == null) {
			src = tm.getImage(key);
			if (src == null) return null;
			if (src.getHeight() == 32) src = (p.slimModel ? PlayerSkinParser.instanceAlex : PlayerSkinParser.instanceSteve).parseImage(src);
		}
		t = tm.loadBufferedTexture(bake(src));
		masks.put(key, t);
		return t;
	}

	static BufferedImage bake(BufferedImage src) {
		int w = src.getWidth(), h = src.getHeight();
		float sx = w / 64f, sy = h / 64f;
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
			int c = src.getRGB(x, y);
			if ((c >>> 24) < 128 || outer((int) (x / sx), (int) (y / sy))) { out.setRGB(x, y, 0); continue; }
			float r = (c >> 16 & 255) / 255f, g = (c >> 8 & 255) / 255f, b = (c & 255) / 255f;
			float mx = Math.max(r, Math.max(g, b)), mn = Math.min(r, Math.min(g, b));
			float s = mx > 0 ? (mx - mn) / mx : 0, hue = hue(r, g, b, mx, mn);

			float hw = 1 - smooth((Math.abs(hue - 22) - 20) / 12);
			float sw = smooth((s - 0.08f) / 0.1f) * (1 - smooth((s - 0.72f) / 0.15f));
			float vw = smooth((mx - 0.18f) / 0.14f);
			float wt = hw * sw * vw;
			if (wt < 0.02f) { out.setRGB(x, y, 0); continue; }

			float l = 0.299f * r + 0.587f * g + 0.114f * b;
			float pr = mix(r, l * 0.96f + 0.06f, 0.65f), pg = mix(g, l * 1.0f + 0.06f, 0.65f), pb = mix(b, l * 1.07f + 0.07f, 0.65f);
			out.setRGB(x, y, (int) (wt * 255) << 24 | ch(pr) << 16 | ch(pg) << 8 | ch(pb));
		}
		return out;
	}

	static boolean outer(int x, int y) {
		if (y < 16) return x >= 32;
		if (y >= 32 && y < 48) return x < 56;
		if (y >= 48) return x < 16 || x >= 48;
		return false;
	}

	static float hue(float r, float g, float b, float mx, float mn) {
		float d = mx - mn;
		if (d <= 0) return 0;
		float hh;
		if (mx == r) hh = (g - b) / d;
		else if (mx == g) hh = (b - r) / d + 2;
		else hh = (r - g) / d + 4;
		hh *= 60;
		return hh > 300 ? hh - 360 : hh;
	}

	static int ch(float v) { return Math.max(0, Math.min(255, (int) (v * 255 + 0.5f))); }
	static float mix(float a, float b, float t) { return a + (b - a) * t; }
	static float smooth(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }
}
