package bwopium.mixin.client;

import bwopium.ClientFx;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
	private void bwo$fov(float partial, boolean by_fov, CallbackInfoReturnable<Float> cir) {
		if (by_fov) cir.setReturnValue(cir.getReturnValue() * ClientFx.fov_mul());
	}

	@ModifyConstant(method = "hurtCameraEffect", constant = @Constant(floatValue = 14.0F))
	private float bwo$tilt(float deg) {
		return ClientFx.hurt_tilt();
	}

	@ModifyArgs(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/PlayerLocal;interpolateTurn(FF)V"))
	private void bwo$turn(Args args) {
		args.set(0, ClientFx.turn(args.<Float>get(0), true));
		args.set(1, ClientFx.turn(args.<Float>get(1), false));
	}
}
