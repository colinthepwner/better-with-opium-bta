package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.render.PostProcessingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PostProcessingManager.class)
public abstract class PostProcessingManagerMixin {
	@Shadow @Final public PostProcessingManager.PostProcessingConfig lerped;

	@Inject(method = "render", at = @At("TAIL"))
	private void bwo$grade(float partial, CallbackInfo ci) {
		ClientFx.grade(lerped, partial);
	}
}
