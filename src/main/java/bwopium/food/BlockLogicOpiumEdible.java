package bwopium.food;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicEdible;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.function.Supplier;

public class BlockLogicOpiumEdible extends BlockLogicEdible {
	final boolean pie;
	final int heal;
	final float amt;

	public BlockLogicOpiumEdible(@NotNull Block<?> block, boolean pie, @NotNull Supplier<Item> item, int heal, float amt) {
		super(block, pie ? 4 : 6, item);
		this.pie = pie;
		this.heal = heal;
		this.amt = amt;
	}

	@Override
	public int getHealAmount(@NotNull World w, @NotNull TilePosc pos) { return heal; }

	@Override
	public boolean onInteracted(@NotNull World w, @NotNull TilePosc pos, @NotNull Player p, @Nullable Side side, double x_hit, double y_hit) {
		if (p.getHealth() < p.getMaxHealth()) p.heal(heal);
		Infused.munch(p, w, Infused.crumb());
		Infused.dose(p, w, amt);
		int d = w.getBlockData(pos) + 1;
		if (d >= maxBites) w.setBlockTypeNotify(pos, Blocks.AIR);
		else {
			w.setBlockDataNotify(pos, d);
			w.markBlockDirty(pos);
		}
		return true;
	}

	@NotNull
	@Override
	public AABBdc getBoundsFromState(@NotNull WorldSource src, @NotNull TilePosc pos) {
		int d = src.getBlockData(pos);
		if (pie) return new AABBd(d >= 2 ? 0.5 : 0.0625, 0, d >= 3 ? 0.5 : 0.0625, 0.9375, 0.375, 0.9375);
		return new AABBd((1 + d * 2) / 16.0, 0, 0.0625, 0.9375, 0.5, 0.9375);
	}
}
