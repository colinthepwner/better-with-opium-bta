package bwopium.mixin;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import bwopium.high.OpiumLook;
import bwopium.high.OpiumSync;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumSleepStatus;
import net.minecraft.core.world.pos.TilePos;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import turniplabs.halplibe.helper.EnvironmentHelper;
import turniplabs.halplibe.helper.network.NetworkHandler;

@Mixin(Player.class)
public abstract class PlayerMixin implements OpiumHolder {
	@Unique
	private final Opium bwo$o = new Opium();
	@Unique
	private boolean bwo$look_live;

	@Override
	public Opium bwo$opium() { return bwo$o; }

	@Unique
	private Player bwo$self() { return (Player) (Object) this; }

	@Inject(method = "tick", at = @At("TAIL"))
	private void bwo$tick(CallbackInfo ci) {
		Player p = bwo$self();
		if (p.world == null || p.world.isClientSide) return;
		bwo$o.tick(p);
		if (EnvironmentHelper.isMultiplayerServer() && p.tickCount % 10 == 0) {
			if (NetworkHandler.canReceiveNativePackets(p)) NetworkHandler.sendToPlayer(p, new OpiumSync(bwo$o));

			boolean live = bwo$o.high > 0.01f || bwo$o.nod > 0.01f;
			if (live || bwo$look_live) NetworkHandler.sendToAllAround(p.x, p.y, p.z, 64, p.world.dimension.id, new OpiumLook(p, bwo$o));
			bwo$look_live = live;
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void bwo$save(CompoundTag tag, CallbackInfo ci) {
		tag.putCompound("bwopium", bwo$o.save());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void bwo$load(CompoundTag tag, CallbackInfo ci) {
		if (tag.containsKey("bwopium")) bwo$o.load(tag.getCompound("bwopium"));
	}

	@Inject(method = "onDeath", at = @At("HEAD"))
	private void bwo$death(Entity killer, CallbackInfo ci) {
		if (!bwo$self().world.isClientSide) bwo$o.on_death(bwo$self());
	}

	@Inject(method = "getCurrentPlayerStrVsBlock", at = @At("RETURN"), cancellable = true)
	private void bwo$mine(Block<?> block, CallbackInfoReturnable<Float> cir) {
		float v = cir.getReturnValue();
		if (v > 0) cir.setReturnValue(v * bwo$o.mine_mul());
	}

	@Inject(method = "jump", at = @At("HEAD"), cancellable = true)
	private void bwo$jump(CallbackInfo ci) {
		if (bwo$o.locked) ci.cancel();
	}

	@Inject(method = "jump", at = @At("TAIL"))
	private void bwo$jump_boost(CallbackInfo ci) {
		Player p = bwo$self();
		if (p.yd > 0) p.yd *= bwo$o.jump_mul();
	}

	@Inject(method = "wakeUpPlayer", at = @At("HEAD"))
	private void bwo$wake(boolean a, boolean b, CallbackInfo ci) {
		Player p = bwo$self();
		if (!p.world.isClientSide && p.isPlayerSleeping() && p.world.isDaytime()) bwo$o.wake();
	}

	@ModifyExpressionValue(method = {"sleepInBedAt", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/World;isDaytime()Z"))
	private boolean bwo$day(boolean day) {
		return day && !(bwo$o.can_nap() && bwo$nap_ok());
	}

	@Unique
	private boolean bwo$nap_ok() {
		Player p = bwo$self();
		if (p.world.isClientSide) return true;
		for (Player q : p.world.players) {
			if (q == p || q.isPlayerSleeping()) continue;
			Opium o = OpiumHolder.of(q);
			if (o == null || !o.can_nap()) return false;
		}
		return true;
	}

	@Inject(method = "sleepInBedAt", at = @At("HEAD"), cancellable = true)
	private void bwo$sleep(int x, int y, int z, CallbackInfoReturnable<EnumSleepStatus> cir) {
		Player p = bwo$self();
		if (p.world.isClientSide || bwo$o.wd < 0.45f || p.isPlayerSleeping()) return;

		if (p.world.players.size() > 1) return;
		TilePos bed = new TilePos(x, y, z);
		if (!bed.equals(p.getPlayerSpawnPoint())) {
			p.sendStatusMessageTranslated("messages.bed.setSpawn");
			p.setPlayerSpawnPoint(bed);
		}
		cir.setReturnValue(EnumSleepStatus.OTHER_PROBLEM);
	}
}
