package bwopium.mixin.client;

import bwopium.ClientFx;
import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Mixin(MobRendererPlayer.class)
public abstract class MobRendererPlayerMixin {
	@Inject(method = "setupAnimations(Lnet/minecraft/core/entity/player/Player;Lorg/useless/dragonfly/models/entity/StaticEntityModel;FI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;", at = @At("RETURN"))
	private void bwo$nod(Player p, StaticEntityModel model, float partial, int layer, CallbackInfoReturnable<StaticEntityModel> cir) {
		StaticEntityModel m = cir.getReturnValue();
		Opium o = OpiumHolder.of(p);
		if (m != null && o != null) ClientFx.pose(p, o, m, partial);
	}
}
