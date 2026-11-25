package bwopium.mixin;

import bwopium.Bwopium;
import bwopium.high.Opium;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockLogic.class)
public abstract class BlockLogicMixin {
	@Shadow @Final public Block<?> block;
	@Unique private static final Random bwo$rng = new Random();

	@Shadow public abstract ItemStack[] getBreakResult(World world, EnumDropCause cause, TilePosc pos, int data, TileEntity te);

	@Unique
	private boolean bwo$eligible(ItemStack[] drops) {
		if (drops == null || drops.length == 0) return false;
		if (block == Bwopium.poppy || block == Bwopium.crop) return false;
		for (ItemStack d : drops) if (d == null || d.getItem() instanceof ItemBlock<?> || d.itemID == block.id()) return false;
		return true;
	}

	@Inject(method = "dropWithCause", at = @At("HEAD"), cancellable = true)
	private void bwo$unlucky(World w, EnumDropCause cause, TilePosc pos, int data, TileEntity te, Player p, CallbackInfo ci) {
		if (w.isClientSide || p == null || cause != EnumDropCause.PROPER_TOOL) return;
		float luck = Opium.luck_of(p);
		if (luck >= 0 || bwo$rng.nextFloat() >= -luck * 0.25f) return;
		if (bwo$eligible(getBreakResult(w, cause, pos, data, te))) ci.cancel();
	}

	@Inject(method = "dropWithCause", at = @At("TAIL"))
	private void bwo$lucky(World w, EnumDropCause cause, TilePosc pos, int data, TileEntity te, Player p, CallbackInfo ci) {
		if (w.isClientSide || p == null || cause != EnumDropCause.PROPER_TOOL) return;
		float luck = Opium.luck_of(p);
		if (luck <= 0 || bwo$rng.nextFloat() >= luck * 0.35f) return;
		ItemStack[] extra = getBreakResult(w, cause, pos, data, te);
		if (!bwo$eligible(extra)) return;
		for (ItemStack d : extra) w.dropItem(pos, d.copy());
	}
}
