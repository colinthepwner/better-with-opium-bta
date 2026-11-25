package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Renderer;endRenderGame(F)V", shift = At.Shift.AFTER))
	private void bwo$frame(CallbackInfo ci) {
		ClientFx.frame();
	}
}
