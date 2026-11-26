package bwopium.food;

import bwopium.Bwopium;
import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.joml.Vector3dc;

import java.util.Random;

public final class Infused {
	static final Random rnd = new Random();

	public static void dose(Player p, World w, float amt) {
		if (w.isClientSide) return;
		Opium o = OpiumHolder.of(p);
		if (o != null) o.eat(p, w.getWorldTime(), amt, Opium.EXTRACT);
	}

	public static void munch(Player p, World w, int crumb_id) {
		w.playSoundAtEntity(p, p, "random.bite_extended", 0.5f + (rnd.nextFloat() - rnd.nextFloat()) * 0.1f, 0.9f + (rnd.nextFloat() - rnd.nextFloat()) * 0.1f);
		if (!w.isClientSide) return;
		p.swingItem();
		Vector3dc v = p.getViewVector(1);
		double x = p.x + v.x() * 0.35, y = p.y + p.getHeadHeight() - 0.15 + v.y() * 0.35, z = p.z + v.z() * 0.35;
		for (int i = 0; i < 8; i++)
			w.spawnParticle("item", x, y, z, (rnd.nextDouble() - 0.5) * 0.1, rnd.nextDouble() * 0.1, (rnd.nextDouble() - 0.5) * 0.1, crumb_id, false);
	}

	public static int crumb() { return Bwopium.raw.id; }
}
