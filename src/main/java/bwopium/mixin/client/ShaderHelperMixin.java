package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.client.render.shader.ShaderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderHelper.class)
public abstract class ShaderHelperMixin {

	@Inject(method = "setUniforms", at = @At("TAIL"))
	private static void bwo$crave(Minecraft mc, Shader shader, float partial, CallbackInfo ci) {
		if (shader.isEnabled()) shader.uniformFloat("bwoCrave", ClientFx.crave());
	}
}
