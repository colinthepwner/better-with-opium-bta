package bwopium.food;

import bwopium.Bwopium;
import bwopium.ItemPoppyPod;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumBlockSoundEffectType;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemOpiumJar extends Item {
	public ItemOpiumJar(String key, String ns, int id) {
		super(key, ns, id);
		setMaxStackSize(1);
		setMaxDamage(2);
		setContainerItem(Items.JAR);
	}

	@Override
	public ItemStack onUse(@NotNull ItemStack self, @NotNull World w, @NotNull Player p) { return eat(self, w, p); }

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack self, @NotNull World w, @Nullable Player p, @NotNull TilePosc pos, @NotNull Side side, double x_hit, double y_hit) {
		if (p == null) return false;
		if (!p.isSneaking()) {
			p.setHeldItem(eat(self, w, p));
			return true;
		}

		TilePos at = new TilePos(pos);
		if (!w.getBlockType(pos).hasTag(BlockTags.PLACE_OVERWRITES)) pos.add(side, at);
		if (!Bwopium.jar_block.getLogic().canPlaceAt(w, at)) return false;
		if (!w.isClientSide) {
			w.setBlockTypeDataNotify(at, Bwopium.jar_block, self.getMetadata() & 1);
			w.playBlockSoundEffect(p, at.x + 0.5f, at.y + 0.5f, at.z + 0.5f, Bwopium.jar_block, EnumBlockSoundEffectType.PLACE);
		}
		self.consumeItem(p);
		return true;
	}

	static ItemStack eat(ItemStack self, World w, Player p) {
		Infused.munch(p, w, Infused.crumb());
		Infused.dose(p, w, ItemPoppyPod.EXTRACT);
		if (!p.getGamemode().hasBlockConsumption()) return self;
		if (self.getMetadata() == 0) { self.setMetadata(1); return self; }
		return new ItemStack(Items.JAR);
	}
}
