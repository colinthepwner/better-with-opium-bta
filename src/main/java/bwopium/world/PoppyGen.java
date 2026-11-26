package bwopium.world;

import bwopium.Bwopium;
import bwopium.BlockLogicPoppy;
import bwopium.BlockLogicPoppyCrop;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.pos.TilePos;

import java.util.Random;

public class PoppyGen {

	static int odds(Biome b) {
		if (b == Biomes.OVERWORLD_MEADOW) return 28;
		if (b == Biomes.OVERWORLD_PLAINS || b == Biomes.OVERWORLD_LEGACY_PLAINS) return 40;
		if (b == Biomes.OVERWORLD_GRASSLANDS) return 36;
		if (b == Biomes.OVERWORLD_SHRUBLAND || b == Biomes.OVERWORLD_LEGACY_SHRUBLAND) return 40;
		if (b == Biomes.OVERWORLD_CAATINGA_PLAINS) return 45;
		if (b == Biomes.OVERWORLD_OUTBACK_GRASSY || b == Biomes.OVERWORLD_LEGACY_SAVANNA) return 50;
		if (b == Biomes.OVERWORLD_SEASONAL_FOREST) return 70;
		return 80;
	}

	public static int pick_color(Random r) {
		int n = r.nextInt(20);
		return n < 7 ? 0 : n < 13 ? 1 : n < 17 ? 2 : 3;
	}

	public static void gen(World w, int chunk_x, int chunk_z) {
		if (Bwopium.poppy == null) return;
		Random r = new Random(w.getRandomSeed() ^ (chunk_x * 341873128712L + chunk_z * 132897987541L) ^ 0x6f7069756dL);
		int cx = chunk_x * 16 + 8 + r.nextInt(16), cz = chunk_z * 16 + 8 + r.nextInt(16);
		int cy = w.getHeightValue(cx, cz);
		int n = odds(w.getBlockBiome(new TilePos(cx, cy, cz)));
		if (n <= 0 || r.nextInt(n) != 0) return;

		int id = Bwopium.poppy.id();

		int col = pick_color(r);

		int tries = 6 + r.nextInt(9);
		for (int i = 0; i < tries; i++) {

			int x = cx + r.nextInt(4) - r.nextInt(4), z = cz + r.nextInt(4) - r.nextInt(4);
			int y = w.getHeightValue(x, z);

			if (!w.isAirBlock(x, y, z) || !w.isAirBlock(x, y + 1, z)) continue;
			if (!BlockLogicPoppyCrop.good_ground(w.getBlockType(new TilePos(x, y - 1, z)))) continue;
			int stack = r.nextInt(10) < 6 ? 0 : r.nextInt(3) == 0 ? 2 + r.nextInt(2) : 1;
			boolean pods = r.nextInt(3) != 0;
			int c = r.nextInt(10) == 0 ? pick_color(r) : col;

			w.setBlockAndMetadata(x, y + 1, z, id, BlockLogicPoppy.data(true, stack, pods, c));
			w.setBlockAndMetadata(x, y, z, id, BlockLogicPoppy.data(false, stack, pods, c));
		}
	}
}
