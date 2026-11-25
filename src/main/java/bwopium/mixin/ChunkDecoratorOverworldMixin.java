package bwopium.mixin;

import bwopium.world.PoppyGen;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.perlin.overworld.ChunkDecoratorOverworld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDecoratorOverworld.class)
public abstract class ChunkDecoratorOverworldMixin {
	@Shadow @Final private World world;

	@Inject(method = "decorate", at = @At("TAIL"))
	private void bwo$poppies(Chunk chunk, CallbackInfo ci) {
		try {
			PoppyGen.gen(world, chunk.pos.x, chunk.pos.z);
		} catch (Throwable t) {

		}
	}
}
