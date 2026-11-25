package bwopium;

import net.minecraft.client.render.particle.ParticleFirefly;
import net.minecraft.core.entity.animal.MobFireflyCluster.FireflyColor;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class ParticlePoppyMote extends ParticleFirefly {
	final float ph, spin, rad, rise;

	public ParticlePoppyMote(@NotNull World w, double x, double y, double z) {
		super(w, x, y, z, 0, 0, 0, FireflyColor.RED.getId());
		noPhysics = true;
		xd = yd = zd = 0;
		lifetime = 80 + random.nextInt(50);
		ph = random.nextFloat() * 6.283f;
		spin = (random.nextInt(2) == 0 ? 1 : -1) * (0.03f + random.nextFloat() * 0.015f);
		rad = 0.05f + random.nextFloat() * 0.04f;
		rise = 0.0015f + random.nextFloat() * 0.0015f;
	}

	@Override
	public void tick() {
		cachedLightmapCoord = calcLightIndex(1);
		xo = x; yo = y; zo = z;
		if (age++ >= lifetime) { remove(); return; }

		float a = ph + age * spin;
		xd = -Math.sin(a) * rad * spin;
		zd = Math.cos(a) * rad * spin;
		yd = rise + Math.sin(ph + age * 0.045f) * 0.0025;
		move(xd, yd, zd);
	}

	@Override
	public float getInternalBrightness(float pt) {
		float t = Math.min(1, (age + pt) / lifetime);
		float env = (float) Math.sin(Math.PI * t);
		return env * env * (0.85f + 0.15f * (float) Math.sin(ph + (age + pt) * 0.07f));
	}
}
