package bwopium.mixin;

import bwopium.HighSmelt;
import net.minecraft.core.block.entity.TileEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityFurnace.class)
public abstract class TileEntityFurnaceMixin {
	@Unique private float bwo$extra, bwo$high;
	@Unique private int bwo$scan;

	@Inject(method = "tick", at = @At("TAIL"))
	private void bwo$tick(CallbackInfo ci) {
		TileEntityFurnace self = (TileEntityFurnace) (Object) this;
		if (self.worldObj == null || self.worldObj.isClientSide || self.tilePos == null) return;
		if (self.currentCookTime <= 0 || !self.isBurning()) { bwo$extra = 0; return; }
		if (--bwo$scan <= 0) { bwo$scan = 20; bwo$high = HighSmelt.nearby_high(self.worldObj, self.tilePos); }
		bwo$extra += HighSmelt.rate(bwo$high);

		if (bwo$extra >= 1 && self.currentCookTime < self.maxCookTime - 1) {
			bwo$extra -= 1;
			self.currentCookTime++;
		}
	}
}
