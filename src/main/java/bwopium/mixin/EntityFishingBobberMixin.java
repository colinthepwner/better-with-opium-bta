package bwopium.mixin;

import bwopium.high.Opium;
import net.minecraft.core.entity.EntityFishingBobber;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(EntityFishingBobber.class)
public abstract class EntityFishingBobberMixin {
	@Shadow private int ticksCatchable;
	@Unique private int bwo$before;
	@Unique private boolean bwo$fish;
	@Unique private static final Random bwo$rng = new Random();

	@Inject(method = "tick", at = @At("HEAD"))
	private void bwo$pre(CallbackInfo ci) { bwo$before = ticksCatchable; }

	@Inject(method = "tick", at = @At("TAIL"))
	private void bwo$post(CallbackInfo ci) {
		EntityFishingBobber self = (EntityFishingBobber) (Object) this;
		if (self.world.isClientSide || self.removed || self.owner == null || self.hookedEntity != null) return;
		float luck = Opium.luck_of(self.owner);
		if (luck < 0 && bwo$before == 0 && ticksCatchable > 0 && bwo$rng.nextFloat() < -luck * 0.8f) {
			ticksCatchable = 0;
		} else if (luck > 0 && ticksCatchable == 0 && self.isInWater() && bwo$rng.nextFloat() < luck * 0.003f) {

			ticksCatchable = bwo$rng.nextInt(30) + 10;
			self.yd -= 0.2;
			self.world.playSoundAtEntity(null, self, "random.splash", 0.25f, 1.0f + (bwo$rng.nextFloat() - bwo$rng.nextFloat()) * 0.4f);
		}
	}

	@Inject(method = "yoink", at = @At("HEAD"))
	private void bwo$yoink_pre(CallbackInfoReturnable<Integer> cir) {
		EntityFishingBobber self = (EntityFishingBobber) (Object) this;
		bwo$fish = !self.world.isClientSide && self.hookedEntity == null && ticksCatchable > 0 && self.owner != null;
	}

	@Inject(method = "yoink", at = @At("RETURN"))
	private void bwo$yoink_post(CallbackInfoReturnable<Integer> cir) {
		if (!bwo$fish) return;
		bwo$fish = false;
		EntityFishingBobber self = (EntityFishingBobber) (Object) this;
		float luck = Opium.luck_of(self.owner);
		if (luck <= 0 || bwo$rng.nextFloat() >= luck * 0.6f) return;

		EntityItem e = new EntityItem(self.world, self.x, self.y, self.z, new ItemStack(Items.FOOD_FISH_RAW));
		double dx = self.owner.x - self.x, dy = self.owner.y - self.y, dz = self.owner.z - self.z;
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
		e.xd = dx * 0.1;
		e.yd = dy * 0.1 + Math.sqrt(dist) * 0.08;
		e.zd = dz * 0.1;
		self.world.entityJoinedWorld(e);
	}
}
