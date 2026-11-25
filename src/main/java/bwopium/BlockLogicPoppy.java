package bwopium;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.BlockLogicFlowerStackable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumBlockSoundEffectType;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.tool.ItemToolShears;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBdc;

import java.util.Random;

public class BlockLogicPoppy extends BlockLogic {
	public static final int UPPER = 1;

	public static final int LANCED = 2;

	static final int LANCE_HEAL = 30;

	public BlockLogicPoppy(@NotNull Block<?> block) {
		super(block, Materials.PLANT);
		block.setTicking(true);
		setBlockBounds(0.25, 0.0, 0.25, 0.75, 1.0, 0.75);
	}

	public static boolean upper(int data) { return (data & UPPER) != 0; }

	public static boolean lanced(int data) { return (data & LANCED) != 0; }

	public static int heads(int data) { return BlockLogicFlowerStackable.getStackCount(data) + 1; }

	public static int data(boolean upper, int stack) { return BlockLogicFlowerStackable.setStackCount(upper ? UPPER : 0, stack); }

	boolean is_half(World w, TilePosc pos, boolean want_upper) {
		return w.getBlockType(pos) == block && upper(w.getBlockData(pos)) == want_upper;
	}

	@Override
	public boolean canStay(@NotNull World w, @NotNull TilePosc pos) {
		if (upper(w.getBlockData(pos))) return is_half(w, pos.down(new TilePos()), false);
		return BlockLogicPoppyCrop.good_ground(w.getBlockType(pos.down(new TilePos()))) && is_half(w, pos.up(new TilePos()), true);
	}

	@Override
	public boolean canPlaceAt(@NotNull World w, @NotNull TilePosc pos) {
		return super.canPlaceAt(w, pos)
			&& BlockLogicPoppyCrop.good_ground(w.getBlockType(pos.down(new TilePos())))
			&& w.isAirBlock(pos.up(new TilePos()));
	}

	@Override
	public void onPlacedByMob(@NotNull World w, @NotNull TilePosc pos, @NotNull Side side, @NotNull Mob mob, double x_hit, double y_hit) {
		int stack = BlockLogicFlowerStackable.getStackCount(w.getBlockData(pos));
		w.setBlockTypeDataNotify(pos, block, data(false, stack));
		w.setBlockTypeDataNotify(pos.up(new TilePos()), block, data(true, stack));
	}

	@Override
	public void onNeighborChanged(@NotNull World w, @NotNull TilePosc pos, @NotNull Block<?> changed) {
		if (canStay(w, pos)) return;
		int d = w.getBlockData(pos);
		boolean partner = upper(d) ? is_half(w, pos.down(new TilePos()), false) : is_half(w, pos.up(new TilePos()), true);
		if (partner) dropWithCause(w, EnumDropCause.WORLD, pos, d, null, null);
		w.setBlockTypeNotify(pos, Blocks.AIR);
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World w, @NotNull EnumDropCause cause, int data, @Nullable TileEntity te) {

		if (cause == EnumDropCause.PICK_BLOCK) return new ItemStack[]{new ItemStack(this, 1, UPPER)};

		return new ItemStack[]{new ItemStack(Bwopium.pod, heads(data))};
	}

	@Override
	public boolean onInteracted(@NotNull World w, @NotNull TilePosc pos, @NotNull Player p, @Nullable Side side, double x_hit, double y_hit) {
		ItemStack held = p.getHeldItem();
		if (held == null || !(held.getItem() instanceof ItemToolShears)) return false;
		TilePos up = new TilePos(pos);
		if (!upper(w.getBlockData(pos))) pos.up(up);
		if (!is_half(w, up, true)) return false;
		int d = w.getBlockData(up);
		if (lanced(d)) return true;
		if (!w.isClientSide) {
			w.dropItem(up, new ItemStack(Bwopium.raw, heads(d) >= 3 ? 2 : 1));
			w.setBlockDataNotify(up, d | LANCED);
			held.damageItem(1, p);
			w.playBlockSoundEffect(p, up.x + 0.5f, up.y + 0.5f, up.z + 0.5f, block, EnumBlockSoundEffectType.DIG);
		}
		return true;
	}

	@Override
	public void updateTick(@NotNull World w, @NotNull TilePosc pos, @NotNull Random rand, boolean random_tick) {
		if (!random_tick || w.isClientSide) return;
		int d = w.getBlockData(pos);
		if (upper(d) && lanced(d) && rand.nextInt(LANCE_HEAL) == 0) w.setBlockDataNotify(pos, d & ~LANCED);
	}

	@Override
	public void animationTick(@NotNull World w, @NotNull TilePosc pos, @NotNull Random rand) {

		if (!Bwopium.client_naive || !upper(w.getBlockData(pos)) || rand.nextInt(30) != 0) return;
		w.spawnParticle("bwopium.mote", pos.x() + 0.3 + rand.nextDouble() * 0.4, pos.y() + 0.35 + rand.nextDouble() * 0.35, pos.z() + 0.3 + rand.nextDouble() * 0.4,
			0, 0, 0, 0, false);
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
