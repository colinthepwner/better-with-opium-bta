package bwopium;

import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemModelOpiumJar extends ItemModelStandard {
	static IconCoordinate half;

	public ItemModelOpiumJar(@NotNull Item item) { super(item); }

	@NotNull
	@Override
	public IconCoordinate getIcon(@Nullable Entity e, @NotNull ItemStack stack) {
		if (stack.getMetadata() == 0) return super.getIcon(e, stack);
		if (half == null) half = TextureRegistry.getTexture("bwopium:item/opium_jar_half");
		return half;
	}
}
