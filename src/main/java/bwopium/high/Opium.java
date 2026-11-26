package bwopium.high;

import bwopium.Bwopium;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import turniplabs.halplibe.util.deathcause.DeathCauseMixinInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Opium {
	static final double LN2 = Math.log(2);
	static final double NORM = 1.4;

	static final double TOL_HL = 96000;

	static final double DEP_HL_ACUTE = 84000, DEP_HL_TAIL = 150000;

	static final double HIST_HL = 480000, LIFE_HL = 720000;

	static final double DRY_PEAK = 36000;

	static final Map<UUID, CompoundTag> stash = new HashMap<>();

	public float tol, dep, life, dry, morning, hist, rebound;

	public float hx, hy, last_pot = 1;
	public long last_dose_t = -1;
	boolean od_killed;
	public long last_t = -1;
	public final ArrayList<Dose> doses = new ArrayList<>();
	public boolean loaded;

	public boolean ready;

	public float half_str;
	public boolean extract_done;

	public static final int BITE = 0, HALF = 1, EXTRACT = 2, EDIBLE = 3;

	static final float[][] PROFILE = {
		{1, 1, 1, 1, 1},
		{1, 1, 1, 1, 1},
		{0.5f, 0.85f, 1.2f, 1.6f, 1.6f},
		{2.5f, 1.5f, 0.85f, 0.6f, 0.6f},
	};

	public boolean synced;

	public float lvl, high, wd, od, pen, rush;
	public boolean locked;

	public float nod;

	double heal_acc, od_acc, wd_acc;
	int dbg_cd, nod_t, wake_t;
	final Random rng = new Random();

	public static class Dose {
		long t0; float amt, ka, ke; long full_at = -1;

		float heal, healed;

		float pot = 1;

		float rlen = 1;

		float hook = 1;
	}

	float heal_sens() { return (float) Math.pow(1 + 3 * tol, -1.5) * emax(); }

	float adapt() { return clamp(0.55f * tol + 0.6f * dep, 0, 1); }

	float emax() { return (float) (1 - 0.3 * (1 - Math.exp(-life / 40.0))); }

	double dep_slow() { return 1 + Math.min(2.0, 0.05 * hist); }

	double tol_slow() { return 1 + Math.min(1.5, 0.035 * hist); }

	float ec50() { return 0.4f * (1 + 1.8f * tol); }

	float od_limit() { return 2.0f * (1 + 1.8f * tol); }

	float need() { return 0.35f * (1 + 2.0f * tol); }

	static double part(Dose d, double t) {
		if (t <= 0) return 0;
		double ka = d.ka, ke = d.ke, a = d.amt;
		if (Math.abs(ka - ke) < 1e-9) return a * NORM * ka * t * Math.exp(-ka * t);
		return a * NORM * ka / (ka - ke) * (Math.exp(-ke * t) - Math.exp(-ka * t));
	}

	double level_at(long now) {
		double sum = 0;
		for (Dose d : doses) sum += part(d, now - d.t0);
		return sum;
	}

	double hook_at(long now) {
		double s = 0, w = 0;
		for (Dose d : doses) { double c = part(d, now - d.t0); s += c * d.hook; w += c; }
		return w > 1e-6 ? s / w : 1;
	}

	float rush_at(long now) {
		float r = 0;
		for (Dose d : doses) {
			if (d.pot <= 1.001f) continue;
			double t = now - d.t0;
			if (t <= 0) continue;
			double tp = 0.6 * d.rlen * Math.log(d.ka / d.ke) / (d.ka - d.ke);
			r = Math.max(r, (float) ((d.pot - 1) * 1.2 * (t / tp) * Math.exp(1 - t / tp)));
		}
		return r;
	}

	float potency(long now) {
		long gap = last_dose_t < 0 ? 1_000_000 : now - last_dose_t;

		if (gap < 600) return last_pot;
		float gap_b = 0.15f * smooth((gap - 4000) / 14000f);
		float routine_b = 0;
		if (gap >= 6000) {
			double ang = (Math.floorMod(now, 24000L) / 24000.0) * Math.PI * 2;
			float cons = (float) Math.hypot(hx, hy);
			if (cons > 0.01f) {
				double diff = Math.abs(Math.atan2(Math.sin(ang - Math.atan2(hy, hx)), Math.cos(ang - Math.atan2(hy, hx))));
				float off_ticks = (float) (diff / (Math.PI * 2) * 24000);
				routine_b = 0.10f * smooth((cons - 0.3f) / 0.6f) * (1 - smooth((off_ticks - 800) / 1500f));
			}
			hx = hx * 0.7f + (float) Math.cos(ang) * 0.3f;
			hy = hy * 0.7f + (float) Math.sin(ang) * 0.3f;
		}
		return 1 + gap_b + routine_b;
	}

	float effect(double l) {
		double x = l / ec50();
		double xh = Math.pow(x, 1.6);
		return (float) (emax() * xh / (xh + 1));
	}

	float strength_roll() {
		float v = (float) Math.exp(rng.nextGaussian() * 0.22);
		if (rng.nextFloat() < 0.03f) v *= 1.6f;
		return clamp(v, 0.55f, 2.4f);
	}

	public void eat(Player p, long now, float amt, int kind) { eat(p, now, amt, kind, 0); }

	public void eat(Player p, long now, float amt, int kind, float force) {
		update(p, now);
		float str;
		if (force > 0) str = force;
		else if (kind == HALF && half_str > 0) { str = half_str; half_str = 0; }
		else str = strength_roll();
		if (kind == BITE) half_str = str;
		float[] pf = PROFILE[kind];
		amt *= str * pf[2];

		float x = lvl / ec50();
		float stack = 1 - 0.75f * smooth((x - 0.3f) / 1.5f);
		float eff = amt * stack;
		float a = adapt();
		double abs_hl = lerp(900, 450, a) * pf[0], elim_hl = lerp(3200, 1100, a) * pf[1];

		boolean first = life < 0.01f && last_dose_t < 0;

		boolean first_extract = kind == EXTRACT && !extract_done;
		if (kind == EXTRACT) extract_done = true;
		Dose d = new Dose();
		d.t0 = now; d.amt = eff; d.hook = pf[4];
		d.pot = last_pot = first ? 1.6f : first_extract ? 1.45f : potency(now);
		if (first_extract && !first) d.rlen = 0.6f;
		last_dose_t = now;
		d.ka = (float) (LN2 / abs_hl); d.ke = (float) (LN2 / elim_hl);

		float sens = heal_sens();
		d.heal = 8f * amt * sens;

		d.healed = d.heal * (0.4f + 0.4f * (1 - stack) / 0.75f);
		heal_acc += d.healed;

		double chance = 0.3 * Math.pow(sens, 1.5) * (0.4 + 0.6 * Math.min(1, amt));
		if (first || first_extract || rng.nextDouble() < chance) {
			double tmax = Math.log(d.ka / d.ke) / (d.ka - d.ke);
			d.full_at = now + (long) tmax;
		}
		doses.add(d);

		tol += 0.10f * amt * pf[3] * (1 - tol / 2.2f);
		life += amt * (1 - life / 60f);
	}

	public void tick(Player p) {
		long now = p.world.getWorldTime();
		if (!loaded && !stash.isEmpty() && p.uuid != null) {
			CompoundTag t = stash.remove(p.uuid);
			if (t != null) load(t);
			loaded = true;
		}
		update(p, now);
		ready = true;
		apply(p);
	}

	void update(Player p, long now) {
		if (last_t < 0 || now < last_t) { last_t = now; derive(now); return; }
		long dt_all = Math.min(now - last_t, 24000L * 4);
		long t = now - dt_all;
		while (dt_all > 0) {
			long dt = Math.min(dt_all, 200);
			step(t + dt / 2, dt);
			t += dt; dt_all -= dt;
		}
		last_t = now;
		derive(now);

		Iterator<Dose> it = doses.iterator();
		while (it.hasNext()) {
			Dose d = it.next();

			if (d.healed < d.heal && now > d.t0) {
				float due = (float) (d.heal * (1 - Math.exp(-d.ka * (now - d.t0)))) - d.healed;
				if (due > 0) {
					d.healed += due;
					if (od < 0.9f && p.getHealth() > 0) heal_acc += due;
				}
			}
			if (d.full_at >= 0 && now >= d.full_at) {
				d.full_at = -1;
				if (p.getHealth() > 0 && od < 0.9f) p.setHealthRaw(p.getMaxHealth());
			}
			if (now - d.t0 > 12 * LN2 / d.ke) it.remove();
		}
	}

	void step(long mid, long dt) {
		double l = level_at(mid);
		float n = need();
		float deficit = clamp(1 - (float) (l / n), 0, 1);

		tol *= (float) Math.exp(-LN2 * dt / (TOL_HL * tol_slow()));
		life *= (float) Math.exp(-LN2 * dt / LIFE_HL);
		hist += dep * dt / 24000f;
		hist *= (float) Math.exp(-LN2 * dt / HIST_HL);

		double pres = Math.min(1, l / 0.3);
		double drive = Math.pow(Math.max(0, tol - 0.15), 1.2);
		dep += (float) (dt * 1.2e-5 * pres * drive * hook_at(mid) * (1 - dep));
		double hl = (dep > 0.3f ? DEP_HL_ACUTE : DEP_HL_TAIL) * dep_slow();
		dep *= (float) Math.exp(-LN2 * dt / hl * deficit);

		if (l < n * 0.5) dry += dt;
		else dry = Math.max(0, dry - dt * 4);
		dry = Math.min(dry, 200000);

		morning *= (float) Math.exp(-LN2 * dt / 1500.0);
		rebound *= (float) Math.exp(-LN2 * dt / 1200.0);
	}

	public void wake() {
		if (dep > 0.08f) morning = Math.max(morning, 0.45f * (float) Math.pow(dep, 0.8));
	}

	void derive(long now) {
		lvl = (float) level_at(now);
		high = effect(lvl);
		rush = rush_at(now);
		float n = need();
		float deficit = clamp(1 - lvl / n, 0, 1);
		float ramp = 0.25f + 0.75f * smooth(dry / (float) DRY_PEAK);
		wd = clamp((float) Math.pow(dep, 1.1) * deficit * ramp, 0, 1);
		wd = clamp(wd + morning * deficit * (1 - wd), 0, 1);
		wd = clamp(wd + rebound * deficit * (1 - wd), 0, 1);
		od = lvl / od_limit();

		pen = 0.035f * Math.min(tol, 2f) + 0.15f * (float) Math.pow(dep, 1.5);
	}

	void apply(Player p) {
		if (p.getHealth() <= 0) return;

		if (heal_acc >= 1) {
			int n = (int) heal_acc;
			heal_acc -= n;
			heal_raw(p, n);
		}

		float x = clamp((od - 0.8f) / 0.4f, 0, 1);
		if (nod_t > 0) {
			if (--nod_t == 0) wake_t = (int) lerp(260, 140, x) + rng.nextInt(100);
		} else if (wake_t > 0) wake_t--;
		else if (od > 0.8f && rng.nextFloat() < 0.004f + 0.02f * x)
			nod_t = (int) lerp(120, 220, clamp((od - 0.8f) / 0.6f, 0, 1)) + rng.nextInt((int) lerp(80, 140, x) + 1);
		if (nod_t > 0) nod = Math.min(1, nod + 1 / 60f);
		else nod = Math.max(0, nod - 1 / 100f);
		locked = nod > 0.5f;

		if (od > 1.15f) {
			double rate = od > 1.7f ? 1 / 20.0 : od > 1.4f ? 1 / 45.0 : 1 / 110.0;
			od_acc += rate;
			if (od_acc >= 1) {
				od_acc -= 1;
				int amt = od > 1.7f ? 2 : 1;
				if (p.getHealth() > amt) {

					p.setHealthRaw(p.getHealth() - amt);
				} else {

					new DeathCauseOverdose(p).bind(p);
					od_killed = true;
					Bwopium.self_dmg = true;
					try { p.hurt(null, amt, DamageType.DROWN); }
					finally { Bwopium.self_dmg = false; }
					if (p.getHealth() > 0) { ((DeathCauseMixinInterface) p).halplibe$setDeathCause(null); od_killed = false; }
				}
			}
		} else od_acc = 0;

		if (wd > 0.55f && p.getHealth() > 2) {
			wd_acc += (wd - 0.5f) / 1100.0;
			if (wd_acc >= 1) { wd_acc -= 1; p.setHealthRaw(p.getHealth() - 1); }
		} else wd_acc = 0;

		if (Bwopium.debug && --dbg_cd <= 0) {
			dbg_cd = 20;
			p.sendStatusMessage(String.format("lvl %.2f high %.2f wd %.2f od %.2f | tol %.2f dep %.2f pen %.3f dry %.0f life %.1f hist %.1f",
				lvl, high, wd, od, tol, dep, pen, dry, life, hist));
		}
	}

	static void heal_raw(Player p, int amt) {
		p.setHealthRaw(Math.min(p.getMaxHealth(), p.getHealth() + amt));
	}

	float sedation() { return 0.6f * clamp((od - 0.5f) / 0.5f, 0, 1); }

	float nod_eff() { return smooth(nod); }

	public float glide() { return Math.max(smooth((od - 0.45f) / 0.8f), nod_eff()) * 0.85f; }

	public float nov() { return 1f / (1 + 2.5f * tol); }

	public float speed_mul() {
		return Math.max(0.3f, 1 + (0.07f + 0.13f * nov()) * high - pen - 0.08f * wd - sedation()) * (1 - nod_eff());
	}

	public float mine_mul() {
		float base = Math.max(0.4f, 1 + (0.12f + 0.25f * nov()) * high - pen - 0.18f * wd - 0.5f * sedation());
		return base + (0.08f - base) * nod_eff();
	}

	public float dmg_out_mul() {
		float base = Math.max(0.5f, 1 + (0.10f + 0.15f * nov()) * high - pen - 0.15f * wd - 0.5f * sedation());
		return base + (0.3f - base) * nod_eff();
	}

	public float dmg_in_mul() { return Math.max(0.5f, 1 - (0.30f + 0.15f * nov()) * high + 0.6f * pen + 0.20f * wd); }

	public float reach_add() { return 0.4f * nov() * high - 0.3f * wd - 0.5f * nod_eff(); }

	public float notice_mul() { return clamp(1 - 0.4f * high * (0.5f + 0.5f * nov()) + 0.25f * wd, 0.55f, 1.25f); }

	public boolean can_nap() { return high > 0.35f; }

	public float jump_mul() { return 1 + Math.min(0.08f, 0.11f * nov() * high) * (1 - nod_eff()); }

	public float luck() { return clamp(0.5f * high - 0.45f * wd, -0.45f, 0.5f); }

	public static float luck_of(Object e) { Opium o = OpiumHolder.of(e); return o == null ? 0 : o.luck(); }

	public float food_mul() { return clamp(1 - 0.12f * dep - 0.5f * wd, 0.25f, 1); }

	public float knock_mul() { return clamp(1 - 0.45f * high + 0.2f * wd, 0.5f, 1.25f); }

	public static int scale_int(int v, float mul, Random r) {
		double d = v * mul;
		int f = (int) Math.floor(d);
		return f + (r.nextDouble() < d - f ? 1 : 0);
	}

	public void on_death(Player p) {
		doses.clear();
		nod_t = 0; wake_t = 0; nod = 0; locked = false; morning = 0;

		if (od_killed) { rebound = 0.4f; od_killed = false; }
		derive(last_t < 0 ? 0 : last_t);
		if (p.uuid != null) stash.put(p.uuid, save());
	}

	public CompoundTag save() {
		CompoundTag t = new CompoundTag();
		t.putFloat("tol", tol);
		t.putFloat("dep", dep);
		t.putFloat("life", life);
		t.putFloat("hist", hist);

		t.putInt("nod_t", nod_t);
		t.putInt("wake_t", wake_t);
		t.putFloat("nod", nod);
		t.putDouble("heal_acc", heal_acc);
		t.putDouble("od_acc", od_acc);
		t.putDouble("wd_acc", wd_acc);
		t.putFloat("dry", dry);
		t.putFloat("morning", morning);
		t.putFloat("rebound", rebound);
		t.putFloat("hx", hx);
		t.putFloat("hy", hy);
		t.putFloat("last_pot", last_pot);
		t.putFloat("half_str", half_str);
		t.putBoolean("extract_done", extract_done);
		t.putLong("last_dose_t", last_dose_t);
		t.putLong("last_t", last_t);
		t.putInt("n", doses.size());
		for (int i = 0; i < doses.size(); i++) {
			Dose d = doses.get(i);
			CompoundTag dt = new CompoundTag();
			dt.putLong("t0", d.t0);
			dt.putFloat("amt", d.amt);
			dt.putFloat("ka", d.ka);
			dt.putFloat("ke", d.ke);
			dt.putLong("full", d.full_at);
			dt.putFloat("heal", d.heal);
			dt.putFloat("healed", d.healed);
			dt.putFloat("pot", d.pot);
			dt.putFloat("rlen", d.rlen);
			dt.putFloat("hook", d.hook);
			t.putCompound("d" + i, dt);
		}
		return t;
	}

	public void load(CompoundTag t) {
		tol = t.getFloat("tol");
		dep = t.getFloat("dep");
		life = t.getFloat("life");
		hist = t.getFloat("hist");
		nod_t = t.getInteger("nod_t");
		wake_t = t.getInteger("wake_t");
		nod = t.getFloat("nod");
		locked = nod > 0.5f;
		heal_acc = t.getDouble("heal_acc");
		od_acc = t.getDouble("od_acc");
		wd_acc = t.getDouble("wd_acc");
		dry = t.getFloat("dry");
		morning = t.getFloat("morning");
		rebound = t.getFloat("rebound");
		hx = t.getFloat("hx");
		hy = t.getFloat("hy");
		last_pot = t.containsKey("last_pot") ? t.getFloat("last_pot") : 1;
		half_str = t.getFloat("half_str");
		extract_done = t.getBoolean("extract_done");
		last_dose_t = t.containsKey("last_dose_t") ? t.getLong("last_dose_t") : -1;
		last_t = t.containsKey("last_t") ? t.getLong("last_t") : -1;
		doses.clear();
		int n = t.getInteger("n");
		for (int i = 0; i < n; i++) {
			if (!t.containsKey("d" + i)) continue;
			CompoundTag dt = t.getCompound("d" + i);
			Dose d = new Dose();
			d.t0 = dt.getLong("t0");
			d.amt = dt.getFloat("amt");
			d.ka = dt.getFloat("ka");
			d.ke = dt.getFloat("ke");
			d.full_at = dt.getLong("full");
			d.heal = dt.getFloat("heal");
			d.healed = dt.getFloat("healed");
			d.pot = dt.containsKey("pot") ? dt.getFloat("pot") : 1;
			d.rlen = dt.containsKey("rlen") ? dt.getFloat("rlen") : 1;
			d.hook = dt.containsKey("hook") ? dt.getFloat("hook") : 1;
			doses.add(d);
		}
		loaded = true;

		derive(last_t < 0 ? 0 : last_t);
		ready = true;
	}

	public void skip(long ticks) {
		if (last_t >= 0) last_t -= ticks;
		if (last_dose_t >= 0) last_dose_t -= ticks;
		for (Dose d : doses) { d.t0 -= ticks; if (d.full_at >= 0) d.full_at -= ticks; }
	}

	public void clear() {
		doses.clear();
		tol = dep = life = dry = morning = hist = rebound = 0;
		hx = hy = 0; last_pot = 1; last_dose_t = -1; half_str = 0; extract_done = false;
		nod_t = wake_t = 0; nod = 0; locked = false;
		heal_acc = od_acc = wd_acc = 0;
		derive(last_t < 0 ? 0 : last_t);
	}

	public String status() {
		return String.format("lvl %.2f high %.2f rush %.2f wd %.2f od %.2f nod %.2f | tol %.2f dep %.2f hist %.1f life %.1f | pen %.3f luck %.2f nov %.2f | dry %.0f morning %.2f rebound %.2f | doses %d",
			lvl, high, rush, wd, od, nod, tol, dep, hist, life, pen, luck(), nov(), dry, morning, rebound, doses.size());
	}

	static float clamp(float v, float lo, float hi) { return v < lo ? lo : v > hi ? hi : v; }

	static double lerp(double a, double b, double t) { return a + (b - a) * t; }

	static double sq(double v) { return v * v; }

	static float smooth(float x) { x = clamp(x, 0, 1); return x * x * (3 - 2 * x); }
}
