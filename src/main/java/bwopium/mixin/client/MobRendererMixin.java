package bwopium.mixin.client;

import bwopium.Pallor;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

import java.util.List;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin {
	@Shadow @Final private List<StaticEntityModel> setupModels;
	@Shadow protected abstract float getRenderAlpha(Mob e, float pt);

	@Inject(method = "render(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;DDDFF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/MobRenderer;renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/Mob;F)V"))
	private void bwo$pale(TessellatorGeneral t, Mob e, double x, double y, double z, float yaw, float pt, CallbackInfo ci) {

		if (!(e instanceof Player p) || setupModels.isEmpty() || getRenderAlpha(e, pt) < 1) return;
		Pallor.draw(p, setupModels.get(0));
	}
}
