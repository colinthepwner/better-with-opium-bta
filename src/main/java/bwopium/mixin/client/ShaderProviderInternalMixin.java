package bwopium.mixin.client;

import bwopium.ShaderPatch;
import net.minecraft.client.render.shader.ShaderProviderInternal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderProviderInternal.class)
public abstract class ShaderProviderInternalMixin {
	@Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
	private void bwo$patch(String name, CallbackInfoReturnable<String> cir) {
		if ("post/post.fsh".equals(name)) cir.setReturnValue(ShaderPatch.apply(cir.getReturnValue()));
	}
}
