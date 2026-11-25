package bwopium.mixin.server;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PacketHandlerServer.class)
public abstract class PacketHandlerServerReachMixin {
	@Shadow private PlayerServer playerEntity;

	@ModifyExpressionValue(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/gamemode/Gamemode;getBlockReachDistance()F"), require = 0)
	private float bwo$block_reach(float r) { return r + bwo$add(); }

	@ModifyExpressionValue(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/player/gamemode/Gamemode;getEntityReachDistance()F"), require = 0)
	private float bwo$entity_reach(float r) { return r + bwo$add(); }

	@Unique
	private float bwo$add() {
		Opium o = OpiumHolder.of(playerEntity);
		return o == null ? 0 : o.reach_add();
	}
}
