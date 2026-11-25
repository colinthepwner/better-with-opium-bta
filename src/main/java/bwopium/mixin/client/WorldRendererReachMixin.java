package bwopium.mixin.client;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererReachMixin {
	@ModifyExpressionValue(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/gamemode/Gamemode;getBlockReachDistance()F"))
	private float bwo$block_reach(float r) { return r + bwo$add(); }

	@ModifyExpressionValue(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/gamemode/Gamemode;getEntityReachDistance()F"))
	private float bwo$entity_reach(float r) { return r + bwo$add(); }

	@Unique
	private static float bwo$add() {
		Opium o = OpiumHolder.of(Minecraft.getMinecraft().thePlayer);
		return o == null ? 0 : o.reach_add();
	}
}
