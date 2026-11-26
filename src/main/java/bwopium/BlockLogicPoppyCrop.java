package bwopium;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.season.Season;
import net.minecraft.core.world.season.Seasons;
import bwopium.world.PoppyGen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicPoppyCrop extends BlockLogic {
	static final int STAGE_ODDS = 175;
	static final float FAIL = 0.3f;

	public BlockLogicPoppyCrop(@NotNull Block<?> block) {
		super(block, Materials.PLANT);
		block.setTicking(true);
		setBlockBounds(0.2, 0.0, 0.2, 0.8, 0.6, 0.8);
	}

	public static boolean good_ground(Block<?> b) { return b.hasTag(BlockTags.GROWS_FLOWERS) || b == Blocks.FARMLAND_DIRT || b == Blocks.SAND; }

	@Override
	public boolean canStay(@NotNull World w, @NotNull TilePosc pos) {
		return good_ground(w.getBlockType(pos.down(new TilePos())));
	}

	@Override
	public void onNeighborChanged(@NotNull World w, @NotNull TilePosc pos, @NotNull Block<?> changed) {
		if (!canStay(w, pos)) {
			dropWithCause(w, EnumDropCause.WORLD, pos, w.getBlockData(pos), null, null);
			w.setBlockTypeNotify(pos, Blocks.AIR);
		}
	}

	@Override
	public void updateTick(@NotNull World w, @NotNull TilePosc pos, @NotNull Random rand, boolean random_tick) {
		if (!random_tick || w.isClientSide) return;
		Season s = w.getSeasonManager().getCurrentSeason();

		if (s != null && s.killFlowers) {
			if (rand.nextInt(6) == 0) w.setBlockTypeNotify(pos, Blocks.AIR);
			return;
		}
		boolean season_ok = s == null || s == Seasons.OVERWORLD_SPRING || s == Seasons.OVERWORLD_SUMMER;
		if (!season_ok || !w.canBlockSeeTheSky(pos.x(), pos.y() + 1, pos.z())) return;
		if (rand.nextInt(STAGE_ODDS) != 0) return;

		int stage = w.getBlockData(pos) & 3;
		if (stage < 3) { w.setBlockDataNotify(pos, stage + 1); return; }

		TilePos up = pos.up(new TilePos());
		if (!w.isAirBlock(up)) return;
		if (rand.nextFloat() < FAIL) { w.setBlockTypeNotify(pos, Blocks.AIR); return; }

		int stack = rand.nextInt(5) == 0 ? 1 : 0;

		int col = PoppyGen.pick_color(new Random(w.getRandomSeed() ^ (pos.x() >> 4) * 341873128712L ^ (pos.z() >> 4) * 132897987541L));
		w.setBlockTypeDataNotify(up, Bwopium.poppy, BlockLogicPoppy.data(true, stack, false, col));
		w.setBlockTypeDataNotify(pos, Bwopium.poppy, BlockLogicPoppy.data(false, stack, false, col));
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World w, @NotNull EnumDropCause cause, int data, @Nullable TileEntity te) {
		if (cause == EnumDropCause.PICK_BLOCK) return new ItemStack[]{new ItemStack(Bwopium.seeds, 1)};
		return (data & 3) == 0 ? new ItemStack[]{new ItemStack(Bwopium.seeds, 1)} : null;
	}

	@Nullable
	@Override
	public AABBdc getCollisionAABB(@NotNull WorldSource src, @NotNull TilePosc pos) { return null; }

	@NotNull
	@Override
	public ISupport getSupport(@NotNull World w, @NotNull TilePosc pos, @NotNull Side side) { return PartialSupport.INSTANCE; }

	@Override
	public boolean isSolidRender() { return false; }

	@Override
	public boolean isCubeShaped() { return false; }
}
