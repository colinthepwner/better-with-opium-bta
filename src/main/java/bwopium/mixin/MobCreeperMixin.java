package bwopium.mixin;

import bwopium.high.Opium;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.monster.MobCreeper;
import net.minecraft.core.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(MobCreeper.class)
public abstract class MobCreeperMixin {
	@Shadow int timeSinceIgnited;
	@Unique private static final Random bwo$rng = new Random();

	@Inject(method = "attackEntity", at = @At("TAIL"))
	private void bwo$fuse(Entity target, float dist, CallbackInfo ci) {
		MobCreeper self = (MobCreeper) (Object) this;
		if (self.world.isClientSide || self.removed || !(target instanceof Player p)) return;
		if (timeSinceIgnited <= 0 || timeSinceIgnited >= 30) return;
		float luck = Opium.luck_of(p);
		if (luck > 0 && bwo$rng.nextFloat() < luck * 0.8f) timeSinceIgnited--;

		else if (luck < 0 && bwo$rng.nextFloat() < -luck * 0.6f && !bwo$crowd(self, p)) timeSinceIgnited++;
	}

	@Unique
	private static boolean bwo$crowd(MobCreeper c, Player p) {
		for (Player q : c.world.players) if (q != p && q.distanceToSqr(c) < 64) return true;
		return false;
	}
}
