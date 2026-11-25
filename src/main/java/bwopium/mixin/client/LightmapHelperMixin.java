package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.LightmapHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapHelper.class)
public abstract class LightmapHelperMixin {
	@Shadow @Final public Minecraft mc;

	@Inject(method = "setLightmapTextureData", at = @At("HEAD"))
	private void bwo$dark(int[] data, CallbackInfo ci) {
		if (!mc.fullbright && mc.currentWorld != null) ClientFx.lift_dark(data);
	}
}
