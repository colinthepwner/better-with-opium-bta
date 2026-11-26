package bwopium.food;

import bwopium.Bwopium;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.block.support.ISupport;
import net.minecraft.core.block.support.ISupportable;
import net.minecraft.core.block.support.PartialSupport;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

public class BlockLogicOpiumJar extends BlockLogic implements ISupportable {
	public BlockLogicOpiumJar(@NotNull Block<?> block) {
		super(block, Materials.GLASS);
		setBlockBounds(0.3125, 0.0, 0.3125, 0.6875, 0.375, 0.6875);
	}

	static ItemStack item(int data) { return new ItemStack(Bwopium.jar, 1, data & 1); }

	@Override
	public boolean canPlaceAt(@NotNull World w, @NotNull TilePosc pos) { return isSupported(w, pos, Side.BOTTOM) && super.canPlaceAt(w, pos); }

	@Override
	public boolean onInteracted(@NotNull World w, @NotNull TilePosc pos, @NotNull Player p, @Nullable Side side, double x_hit, double y_hit) {
		if (p.getHeldItem() != null) return false;
		int d = w.getBlockData(pos);
		w.setBlockTypeNotify(pos, Blocks.AIR);
		w.playSoundAtEntity(p, p, "item.pickup", 1.0f, 1.0f);
		if (!w.isClientSide) w.dropItem(pos, item(d));
		return true;
	}

	@Override
	public ItemStack[] getBreakResult(@NotNull World w, @NotNull EnumDropCause cause, int data, @Nullable TileEntity te) { return new ItemStack[]{item(data)}; }

	@NotNull
	@Override
	public AABBdc getBoundsFromState(@NotNull WorldSource src, @NotNull TilePosc pos) { return new AABBd(0.3125, 0.0, 0.3125, 0.6875, 0.375, 0.6875); }

	@NotNull
	@Override
	public ISupport getSupport(@NotNull World w, @NotNull TilePosc pos, @NotNull Side side) { return side == Side.BOTTOM ? PartialSupport.INSTANCE.center() : PartialSupport.INSTANCE; }

	@NotNull
	@Override
	public ISupport getSupportConstraint(@NotNull World w, @NotNull TilePosc pos, @NotNull Side side) { return PartialSupport.INSTANCE.center(); }

	@Override
	public boolean isCubeShaped() { return false; }

	@Override
	public boolean isSolidRender() { return false; }

	@Override
	public int getPistonPushReaction(@NotNull World w, @NotNull TilePosc pos) { return 1; }
}
