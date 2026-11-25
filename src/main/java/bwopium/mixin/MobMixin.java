package bwopium.mixin;

import bwopium.Bwopium;
import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.entity.projectile.ProjectileArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Unique
	private static final Random bwo$rng = new Random();

	@ModifyArg(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Mob;moveRelative(FFF)V"), index = 2)
	private float bwo$speed(float f) {
		Opium o = OpiumHolder.of(this);
		return o == null ? f : f * o.speed_mul();
	}

	@Unique private double bwo$mx, bwo$mz;

	@Inject(method = "moveEntityWithHeading", at = @At("HEAD"))
	private void bwo$move_pre(float strafe, float fwd, CallbackInfo ci) {
		Mob self = (Mob) (Object) this;
		bwo$mx = self.xd; bwo$mz = self.zd;
	}

	@Inject(method = "moveEntityWithHeading", at = @At("TAIL"))
	private void bwo$move_post(float strafe, float fwd, CallbackInfo ci) {
		Opium o = OpiumHolder.of(this);
		if (o == null) return;
		float g = o.glide();
		Mob self = (Mob) (Object) this;
		if (g < 0.01f || self.horizontalCollision) return;
		self.xd += (bwo$mx * 0.985 - self.xd) * g * 0.7;
		self.zd += (bwo$mz * 0.985 - self.zd) * g * 0.7;
	}

	@ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private int bwo$hurt(int dmg, @Local(argsOnly = true) Entity attacker) {
		if (Bwopium.self_dmg || dmg <= 0) return dmg;
		Mob self = (Mob) (Object) this;
		if (self.world == null || self.world.isClientSide) return dmg;
		float m = 1f;
		Opium me = OpiumHolder.of(self);
		if (me != null) m *= me.dmg_in_mul();
		Entity src = attacker instanceof ProjectileArrow a && a.owner != null ? a.owner : attacker;
		if (src instanceof Player pl && src != self) {
			Opium them = OpiumHolder.of(pl);
			if (them != null) m *= them.dmg_out_mul();
		}
		return m == 1f ? dmg : Opium.scale_int(dmg, m, bwo$rng);
	}

	@Unique private double bwo$kx, bwo$ky, bwo$kz;

	@Inject(method = "knockBack", at = @At("HEAD"))
	private void bwo$knock_pre(Entity src, int dmg, double dx, double dz, CallbackInfo ci) {
		Mob self = (Mob) (Object) this;
		bwo$kx = self.xd; bwo$ky = self.yd; bwo$kz = self.zd;
	}

	@Inject(method = "knockBack", at = @At("TAIL"))
	private void bwo$knock_post(Entity src, int dmg, double dx, double dz, CallbackInfo ci) {
		Opium o = OpiumHolder.of(this);
		if (o == null) return;
		float k = o.knock_mul();
		if (k == 1f) return;
		Mob self = (Mob) (Object) this;
		self.xd = bwo$kx + (self.xd - bwo$kx) * k;
		self.yd = bwo$ky + (self.yd - bwo$ky) * k;
		self.zd = bwo$kz + (self.zd - bwo$kz) * k;
	}

	@Unique private Player bwo$killer;
	@Unique private boolean bwo$rerolling;

	@Shadow protected abstract void dropDeathItems();

	@Inject(method = "onDeath", at = @At("HEAD"))
	private void bwo$death(Entity killer, CallbackInfo ci) {
		Entity src = killer instanceof ProjectileArrow a && a.owner != null ? a.owner : killer;
		bwo$killer = src instanceof Player p && !((Object) this instanceof Player) ? p : null;
	}

	@Inject(method = "dropDeathItems", at = @At("HEAD"), cancellable = true)
	private void bwo$loot_pre(CallbackInfo ci) {
		if (bwo$rerolling || bwo$killer == null || ((Mob) (Object) this).world.isClientSide) return;
		float luck = Opium.luck_of(bwo$killer);
		if (luck < 0 && bwo$rng.nextFloat() < -luck * 0.4f) ci.cancel();
	}

	@Inject(method = "dropDeathItems", at = @At("TAIL"))
	private void bwo$loot_post(CallbackInfo ci) {
		if (bwo$rerolling || bwo$killer == null || ((Mob) (Object) this).world.isClientSide) return;
		float luck = Opium.luck_of(bwo$killer);
		if (luck <= 0 || bwo$rng.nextFloat() >= luck * 0.6f) return;
		bwo$rerolling = true;
		try { dropDeathItems(); }
		finally { bwo$rerolling = false; }
	}

	@ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private int bwo$heal(int amt) {
		Mob self = (Mob) (Object) this;
		if (amt <= 0 || self.world == null || self.world.isClientSide) return amt;
		Opium o = OpiumHolder.of(self);
		return o == null ? amt : Opium.scale_int(amt, o.food_mul(), bwo$rng);
	}
}
