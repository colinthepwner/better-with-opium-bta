package bwopium;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemPoppyPod extends Item {
	public static final float BITE = 0.5f, EXTRACT = 1.2f;
	final int kind;

	public ItemPoppyPod(String key, String ns, int id, int kind) {
		super(key, ns, id);
		this.kind = kind;
	}

	@Override
	public ItemStack onUse(@NotNull ItemStack self, @NotNull World world, @NotNull Player p) {
		if (!self.consumeItem(p)) return self;

		if (kind == Opium.BITE && p.getGamemode().hasBlockConsumption() && !world.isClientSide && Bwopium.pod_half != null) {
			ItemStack rest = new ItemStack(Bwopium.pod_half, 1);
			p.inventory.insertItem(rest, true);
			if (rest.stackSize > 0) p.dropPlayerItem(rest);
		}
		world.playSoundAtEntity(p, p, "random.bite", 0.5f + (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1f, 0.85f + (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1f);
		if (!world.isClientSide) {
			Opium o = OpiumHolder.of(p);
			if (o != null) o.eat(p, world.getWorldTime(), kind == Opium.EXTRACT ? EXTRACT : BITE, kind);
		}
		return self;
	}
}
