package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.render.FogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogManager.class)
public abstract class FogManagerMixin {

	@Inject(method = "setupFog", at = @At("RETURN"))
	private void bwo$fog(int mode, float far, float partial, FogManager.FogState dest, CallbackInfoReturnable<FogManager.FogState> cir) {
		if (mode == -1 || dest.fogMode != FogManager.FogState.Mode.LINEAR) return;
		ClientFx.fog(dest, far);
	}
}
