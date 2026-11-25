package bwopium;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.PostProcessingManager;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.type.tag.WorldTypeTags;
import org.lwjgl.glfw.GLFW;
import org.joml.Vector3dc;
import org.lwjgl.glfw.GLFWVidMode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public final class ClientFx {
	static float v_high, v_wd, v_dep, v_od, v_lock, v_tol, v_hist, v_rush;
	static float v_music = 1;
	static Opium last_o;
	static boolean snap_pending;
	static long last_frame;
	static final Random rng = new Random();

	static long pace_end;
	static double nat_ns = 1e9 / 60;
	static int refresh_hz = -1;
	static long pace_last;
	static double pace_fps = -1;

	static float nod_of(Opium o) { return o.nod * o.nod * (3 - 2 * o.nod); }

	static Opium state() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || mc.thePlayer == null || mc.currentWorld == null) return null;
		return OpiumHolder.of(mc.thePlayer);
	}

	public static void frame() {
		long now = System.nanoTime();
		double dt = last_frame == 0 ? 0 : Math.min(0.25, (now - last_frame) / 1e9);
		last_frame = now;
		Opium o = state();
		float th = 0, tw = 0, td = 0, to = 0, tl = 0, tt = 0, ts = 0, tr = 0;
		if (o != null) { th = o.high; tw = o.wd; td = o.dep; to = o.od; tl = nod_of(o); tt = o.tol; ts = o.hist; tr = o.rush; }

		if (o != last_o) { last_o = o; snap_pending = o != null; }
		boolean snap = false;

		if (snap_pending && o != null && (o.synced || (o.ready && !Minecraft.getMinecraft().isMultiplayerWorld()))) {
			snap = true; snap_pending = false;
			pace_fps = -1;
		}

		float a = snap ? 1 : (float) (1 - Math.exp(-dt / 1.2));
		v_high += (th - v_high) * a;
		v_rush += (tr - v_rush) * a;
		v_wd += (tw - v_wd) * a;
		v_dep += (td - v_dep) * a;
		v_od += (to - v_od) * a;
		v_tol += (tt - v_tol) * a;
		v_hist += (ts - v_hist) * a;
		v_lock += (tl - v_lock) * (snap ? 1 : (float) (1 - Math.exp(-dt / 0.25)));

		v_music += (music_target(o != null) - v_music) * (snap ? 1 : (float) (1 - Math.exp(-dt / 3.0)));
		if (o != null) cue_tick((float) dt);
		boolean naive = o != null && o.life < 0.01f && o.hist < 0.01f && o.high < 0.01f;
		Bwopium.client_naive = naive;
		v_allure += ((naive ? 1f : 0f) - v_allure) * (snap ? 1 : (float) (1 - Math.exp(-dt / 2.0)));
		first_music(o);
		pace(o != null);
	}

	static float over() { return clamp((v_od - 0.5f) / 0.6f, 0, 1); }

	public static void grade(PostProcessingManager.PostProcessingConfig c, float partial) {

		float e = clamp(v_high + v_rush, 0, 1), w = v_wd, d = v_dep, ov = over(), lk = v_lock;
		if (e < 0.001f && w < 0.001f && d < 0.001f && ov < 0.001f && lk < 0.001f) return;

		float nov = 1f / (1 + 2.5f * v_tol);
		c.saturation += (0.42f + 0.2f * nov) * e - 0.78f * w - 0.12f * d - 0.35f * ov;
		c.rMod *= 1 + 0.07f * e - 0.04f * w;
		c.gMod *= 1 + 0.025f * e + 0.012f * w;
		c.bMod *= 1 - 0.07f * e + 0.035f * w;
		c.contrast += 0.05f * e - 0.08f * w - 0.15f * ov;
		c.brightness += 0.015f * e - 0.025f * w + 0.10f * ov + 0.06f * lk;
		c.exposure += 0.55f * ov - 0.04f * w + 0.35f * lk;

		float n = w > 0.01f ? night(partial) * w : 0;
		if (n > 0) {
			c.brightness -= 0.03f * n;
			c.exposure -= 0.15f * n;
			c.saturation -= 0.1f * n;
			c.rMod *= 1 - 0.03f * n;
			c.bMod *= 1 + 0.05f * n;
		}
		c.saturation = Math.max(c.saturation, -0.82f);
	}

	public static void fog(net.minecraft.client.render.FogManager.FogState f, float far) {
		float clear = 0.8f * smooth(v_high / 0.6f), close = 0.25f * v_wd * (1 - clear);
		if (clear < 0.001f && close < 0.001f) return;
		f.fogEnd += (far - f.fogEnd) * clear;
		f.fogStart += (f.fogEnd * 0.85f - f.fogStart) * clear;
		f.fogStart *= 1 - close;
		f.fogEnd *= 1 - close * 0.4f;
	}

	public static void lift_dark(int[] data) {
		float lift = 0.07f * smooth(v_high / 0.6f);
		if (lift < 0.002f) return;
		for (int i = 0; i < data.length; i++) {
			int c = data[i];
			int r = lift_ch(c >> 16 & 0xFF, lift), g = lift_ch(c >> 8 & 0xFF, lift), b = lift_ch(c & 0xFF, lift);
			data[i] = (c & 0xFF000000) | r << 16 | g << 8 | b;
		}
	}

	static int lift_ch(int v, float lift) {
		float x = v / 255f, inv = 1 - x;
		return Math.min(255, Math.round((x + lift * inv * inv * inv * inv) * 255));
	}

	public static void pose(Player p, Opium o, StaticEntityModel m, float partial) {
		float h = o.high, n = smooth(o.nod), sed = clamp((o.od - 0.5f) / 0.5f, 0, 1);
		if (h < 0.02f && n < 0.01f) return;
		BoneTransform head = m.getTransform("head");
		if (head == null) return;
		double t = (p.tickCount + partial) / 20.0 + (p.id % 97) * 1.7;
		double dip = 1.5 * h * h + 3 * sed;
		double wave = Math.sin(t * 0.42) * 0.6 + Math.sin(t * 0.17 + 1.3) * 0.4;
		double nods = Math.pow(Math.max(0, wave), 3) * (2 + 8 * h * h + 10 * sed);
		double pitch = Math.min(30, (dip + nods) * (1 - n) + 25 * n);
		double roll = Math.sin(t * 0.63) * (0.5 + 1.5 * h) * h;
		double yaw = Math.sin(t * 0.37 + 0.8) * (0.8 + 2 * h) * h;
		head.rotX += Math.toRadians(pitch);
		head.rotZ += Math.toRadians(roll);
		head.rotY += Math.toRadians(yaw);
	}

	static float night(float partial) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentWorld == null || !mc.currentWorld.getWorldType().hasTag(WorldTypeTags.OVERWORLD)) return 0;
		float ang = mc.currentWorld.getCelestialAngle(partial);
		return smooth((ang - 0.22f) / 0.08f) * (1 - smooth((ang - 0.70f) / 0.08f));
	}

	public static float crave() { return Math.max(Math.max(smooth((v_wd - 0.05f) / 0.4f), cue()), 0.22f * v_allure); }

	static float v_allure;

	static Opium music_for;
	static void first_music(Opium o) {
		if (o == null || o == music_for || v_rush < 0.4f || v_music < 0.5f) return;
		music_for = o;
		try {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.sndManager != null) mc.sndManager.ticksBeforeMusic = 0;
		} catch (Throwable ignored) {}
	}

	static float cue_peak, cue_t = -1, cue_cd, look_cd, sober_t;
	static boolean was_looking, looking;

	static void cue_tick(float dt) {

		float memory = smooth((v_hist - 1.5f) / 10f);
		sober_t = v_high < 0.08f && v_wd < 0.3f ? sober_t + dt : 0;
		if (cue_cd > 0) cue_cd -= dt;
		look_cd -= dt;
		if (look_cd <= 0) {
			look_cd = 0.1f;
			looking = memory > 0.01f && sober_t > 120 && v_lock < 0.1f && sees_poppy();
		}
		if (looking && !was_looking && cue_t < 0 && cue_cd <= 0) {
			if (rng.nextFloat() < 0.3f) {
				cue_t = 0;
				cue_peak = memory * (0.6f + 0.4f * rng.nextFloat());
				cue_cd = 90 + rng.nextFloat() * 150;
			} else cue_cd = 6 + rng.nextFloat() * 6;
		}
		was_looking = looking;
		if (cue_t >= 0 && (cue_t += dt) > 12.5f) cue_t = -1;
	}

	static float cue() {
		if (cue_t < 0) return 0;
		float env = cue_t < 2.5f ? smooth(cue_t / 2.5f) : cue_t < 5.5f ? 1 : 1 - smooth((cue_t - 5.5f) / 7f);
		return cue_peak * env;
	}

	static boolean sees_poppy() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null || mc.currentWorld == null || Bwopium.poppy == null) return false;
		World w = mc.currentWorld;
		Vector3dc v = mc.thePlayer.getViewVector(1f);
		double ox = mc.thePlayer.x, oy = mc.thePlayer.y + mc.thePlayer.getHeadHeight(), oz = mc.thePlayer.z;
		int pid = Bwopium.poppy.id();
		for (double d = 1; d < 24; d += 0.4) {
			int bx = (int) Math.floor(ox + v.x() * d), by = (int) Math.floor(oy + v.y() * d), bz = (int) Math.floor(oz + v.z() * d);
			int id = w.getBlockId(bx, by, bz);
			if (id != 0 && id != pid) {
				Block<?> b = Blocks.blocksList[id];
				if (b != null && b.getLogic().isSolidRender()) return false;
			}

			int r = d > 6 ? 1 : 0;
			for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) for (int dy = -1; dy <= 0; dy++)
				if (w.getBlockId(bx + dx, by + dy, bz + dz) == pid) return true;
		}
		return false;
	}

	public static float fov_mul() {
		return 1 + 0.035f * v_high - 0.03f * v_wd - 0.05f * v_lock;
	}

	public static float hurt_tilt() {
		return 14f * clamp(1 - 0.75f * v_high + 0.35f * v_wd, 0.2f, 1.4f);
	}

	static float rate_x, rate_y, turn_dt = 1 / 60f;
	static long turn_last;

	static float cam_glide() { return Math.max(smooth((v_od - 0.45f) / 0.8f), v_lock); }

	public static float turn(float delta, boolean yaw) {
		if (yaw) {
			long now = System.nanoTime();
			turn_dt = clamp(turn_last == 0 ? 1 / 60f : (now - turn_last) / 1e9f, 0.001f, 0.1f);
			turn_last = now;
		}
		delta = lag(yaw ? lag_x : lag_y, delta);
		float sed = clamp((v_od - 0.5f) / 0.5f, 0, 1);
		float m = (1 - 0.15f * v_wd) * (1 - 0.35f * sed) * (1 - 0.55f * v_lock);
		float shake = v_wd > 0.05f ? (rng.nextFloat() - 0.5f) * v_wd * (yaw ? 1.2f : 0.8f) : 0;
		float d = delta * m;
		float g = cam_glide();
		if (g < 0.01f) { rate_x = rate_y = 0; return d + shake; }

		float tau = 0.04f + 0.85f * (float) Math.pow(g, 1.4);
		float k = (float) (1 - Math.exp(-turn_dt / tau));
		float in = d / turn_dt;
		if (yaw) { rate_x += (in - rate_x) * k; return rate_x * turn_dt + shake; }
		rate_y += (in - rate_y) * k;
		return rate_y * turn_dt + shake;
	}

	static final ArrayDeque<long[]> lag_x = new ArrayDeque<>(), lag_y = new ArrayDeque<>();

	static float lag_ms() {
		if (!Bwopium.input_delay || state() == null) return 0;
		return 12f * (1 - smooth(v_high / 0.3f)) + 18f * v_wd;
	}

	static float lag(ArrayDeque<long[]> q, float delta) {
		long now = System.nanoTime();
		long cut = now - (long) (lag_ms() * 1_000_000L);
		if (delta != 0) q.addLast(new long[]{now, Float.floatToIntBits(delta)});
		float out = 0;
		while (!q.isEmpty() && (q.peekFirst()[0] <= cut || q.size() > 256)) out += Float.intBitsToFloat((int) q.pollFirst()[1]);
		return out;
	}

	static boolean skip(SoundCategory c) { return c == SoundCategory.GUI_SOUNDS || c == SoundCategory.MUSIC; }

	public static float snd_vol(float v, SoundCategory c) {
		if (skip(c)) return v;
		return v * (1 - 0.10f * v_high - 0.12f * v_rush) * (1 - 0.16f * v_wd) * (1 - 0.35f * v_lock);
	}

	public static float snd_pitch(float p, SoundCategory c) {
		if (skip(c)) return p;
		float detune = v_wd > 0.05f ? (rng.nextFloat() * 2 - 1) * 0.03f * v_wd : 0;
		return p * (1 - 0.035f * v_high - 0.02f * v_rush) * (1 + detune);
	}

	static float music_target(boolean in_world) {
		if (!in_world) return 1;
		float mute = smooth((v_wd - 0.12f) / 0.33f);
		float cap = 1 - 0.5f * smooth(v_tol / 2.2f);
		float lift = smooth(v_high / 0.35f);
		return Math.min(cap, Math.max(1 - mute, lift * cap));
	}

	public static float music_mul() { return v_music; }

	static float bg_base = -1, rec_base = -1;
	static boolean bg_is_music;
	static float bg_applied = -1, rec_applied = -1;

	static MethodHandle get_sys, set_vol, playing;

	public static void bg_started(String name, float base) {
		bg_base = base;
		bg_is_music = name == null || !name.contains("cave");
		bg_applied = -1;
	}

	public static void rec_started(float base) { rec_base = base; rec_applied = -1; }

	public static float bg_mul() { return bg_is_music ? v_music : 1; }

	public static float bg_options(float raw) {
		bg_base = raw;
		bg_applied = bg_is_music ? v_music : 1;
		return raw * bg_mul();
	}

	public static void music_tick() {
		try {
			if (get_sys == null) get_sys = MethodHandles.publicLookup().unreflect(SoundEngine.class.getMethod("getSoundSystem"));
			Object sys = get_sys.invoke();
			if (sys == null) return;
			if (set_vol == null) {
				MethodHandles.Lookup l = MethodHandles.publicLookup();
				set_vol = l.findVirtual(sys.getClass(), "setVolume", MethodType.methodType(void.class, String.class, float.class));
				playing = l.findVirtual(sys.getClass(), "playing", MethodType.methodType(boolean.class, String.class));
			}
			if (bg_base >= 0 && bg_is_music && Math.abs(v_music - bg_applied) > 0.004f && (boolean) playing.invoke(sys, "BgMusic")) {
				set_vol.invoke(sys, "BgMusic", bg_base * v_music);
				bg_applied = v_music;
			}
			if (rec_base >= 0 && Math.abs(v_music - rec_applied) > 0.004f && (boolean) playing.invoke(sys, "Streaming")) {
				set_vol.invoke(sys, "Streaming", rec_base * v_music);
				rec_applied = v_music;
			}
		} catch (Throwable ignored) {}
	}

	static void pace(boolean in_world) {

		if (Bwopium.frame_pacing && GameSettings.CUSTOM_FPS_LIMIT.value != GameSettings.CUSTOM_FPS_LIMIT.highest) {
			GameSettings.CUSTOM_FPS_LIMIT.set(GameSettings.CUSTOM_FPS_LIMIT.highest);
			GameSettings.CUSTOM_FPS_LIMIT.onUpdate();
		}
		long now = System.nanoTime();
		long work = pace_end == 0 ? 0 : now - pace_end;
		long prev = pace_last;
		pace_last = now;
		if (work > 0 && work < 250_000_000L) nat_ns += (work - nat_ns) * 0.05;
		if (Bwopium.frame_pacing && in_world && work > 0) {
			if (refresh_hz < 0) refresh_hz = probe_hz();
			double nat_fps = 1e9 / Math.max(nat_ns, 1e5);
			double ref = Math.min(nat_fps, refresh_hz);
			double f = 0.87 + 0.30 * v_high - 0.32 * v_wd - 0.05 * v_dep - 0.12 * over();

			f += (0.33 - f) * v_lock;

			double want = f <= 1 ? ref * Math.max(f, 0.3) : ref + (nat_fps - ref) * smooth((float) ((f - 1) / 0.17));

			double fdt = prev == 0 ? 0 : Math.min(0.25, (now - prev) / 1e9);
			pace_fps = pace_fps <= 0 ? want : pace_fps + (want - pace_fps) * (1 - Math.exp(-fdt / 2.5));
			if (pace_fps < nat_fps * 0.98) {
				long target = (long) (1e9 / pace_fps);
				if (target > work) nap(target - work);
			}

			if (v_wd > 0.05f && rng.nextFloat() < v_wd * 0.006f) nap((25 + rng.nextInt(45)) * 1_000_000L);
		}
		pace_end = System.nanoTime();
	}

	static int probe_hz() {
		try {
			GLFWVidMode m = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			if (m != null && m.refreshRate() > 0) return m.refreshRate();
		} catch (Throwable ignored) {}
		return 60;
	}

	static void nap(long ns) {
		long end = System.nanoTime() + ns;
		while (true) {
			long left = end - System.nanoTime();
			if (left <= 0) return;
			if (left > 2_000_000L) LockSupport.parkNanos(left - 1_500_000L);
			else Thread.onSpinWait();
		}
	}

	static float clamp(float v, float lo, float hi) { return v < lo ? lo : v > hi ? hi : v; }

	static float smooth(float x) { x = clamp(x, 0, 1); return x * x * (3 - 2 * x); }
}
