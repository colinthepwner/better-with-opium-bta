package bwopium.mixin;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobMonster;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobMonster.class)
public abstract class MobMonsterMixin {
	@ModifyArg(method = "findPlayerToAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;getClosestPlayerToEntity(Lnet/minecraft/core/entity/Entity;D)Lnet/minecraft/core/entity/player/Player;"), index = 1)
	private double bwo$wider(double r) { return r * 1.25; }

	@Inject(method = "findPlayerToAttack", at = @At("RETURN"), cancellable = true)
	private void bwo$aura(CallbackInfoReturnable<Entity> cir) {
		if (!(cir.getReturnValue() instanceof Player p)) return;
		Opium o = OpiumHolder.of(p);
		float range = 16f * (o == null ? 1 : o.notice_mul());
		Entity self = (Entity) (Object) this;
		double dx = p.x - self.x, dy = p.y - self.y, dz = p.z - self.z;
		if (dx * dx + dy * dy + dz * dz > range * range) cir.setReturnValue(null);
	}
}
