package bwopium;

import net.minecraft.core.block.Block;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumBlockSoundEffectType;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPoppySeeds extends Item {
	public ItemPoppySeeds(String key, String ns, int id) {
		super(key, ns, id);
	}

	@Override
	public boolean onUseOnBlock(@NotNull ItemStack self, @NotNull World w, @Nullable Player p, @NotNull TilePosc clicked, @NotNull Side side, double x_hit, double y_hit) {
		TilePosc pos = clicked;
		if (!w.canPlaceInsideBlock(pos)) pos = pos.add(side.direction(), new TilePos());
		Block<?> crop = Bwopium.crop;
		if (BlockLogicPoppyCrop.good_ground(w.getBlockType(pos.down(new TilePos()))) && w.canPlaceInsideBlock(pos) && w.setBlockTypeNotify(pos, crop)) {
			w.playBlockSoundEffect(p, pos.x() + 0.5f, pos.y() + 0.5f, pos.z() + 0.5f, crop, EnumBlockSoundEffectType.PLACE);
			self.consumeItem(p);
			return true;
		}
		return false;
	}
}
