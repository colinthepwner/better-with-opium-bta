package bwopium.mixin;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
	@Shadow protected abstract void wakeUpAllPlayers();

	@Inject(method = "updateSleepingPlayers", at = @At("HEAD"), cancellable = true)
	private void bwo$nap(CallbackInfo ci) {
		World w = (World) (Object) this;

		if (!w.isDaytime() || w.getCurrentWeather() == Weathers.OVERWORLD_STORM || !w.areEnoughPlayersFullyAsleep()) return;

		for (Player p : w.players) if (!p.isPlayerSleeping()) return;
		w.setWorldTime(w.getWorldTime() + 6000);
		wakeUpAllPlayers();
		ci.cancel();
	}
}
