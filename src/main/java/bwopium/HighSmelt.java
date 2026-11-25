package bwopium;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;

public final class HighSmelt {

	public static float nearby_high(World w, TilePos pos) {
		float best = 0;
		for (Player p : w.players) {
			double dx = p.x - pos.x - 0.5, dy = p.y - pos.y, dz = p.z - pos.z - 0.5;
			if (dx * dx + dy * dy + dz * dz > 64) continue;
			Opium o = OpiumHolder.of(p);
			if (o != null) best = Math.max(best, o.high);
		}
		return best;
	}

	public static float rate(float high) { return 0.25f * high; }
}
