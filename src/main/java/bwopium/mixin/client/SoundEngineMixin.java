package bwopium.mixin.client;

import bwopium.ClientFx;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.core.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
	private static final String PLAY = "playSoundWithID(Lnet/minecraft/client/sound/SoundEntry;Lnet/minecraft/core/sound/SoundCategory;FFLjava/lang/String;Z)V";
	private static final String PLAY_AT = "playSoundWithIdAtPos(Lnet/minecraft/client/sound/SoundEntry;Lnet/minecraft/core/sound/SoundCategory;FFFFFLjava/lang/String;Z)V";
	private static final String SET_VOL = "Lpaulscode/sound/SoundSystem;setVolume(Ljava/lang/String;F)V";
	private static final String SET_PITCH = "Lpaulscode/sound/SoundSystem;setPitch(Ljava/lang/String;F)V";

	@ModifyArg(method = {PLAY, PLAY_AT}, at = @At(value = "INVOKE", target = SET_VOL), index = 1)
	private float bwo$vol(float v, @Local(argsOnly = true) SoundCategory cat) {
		return ClientFx.snd_vol(v, cat);
	}

	@ModifyArg(method = {PLAY, PLAY_AT}, at = @At(value = "INVOKE", target = SET_PITCH), index = 1)
	private float bwo$pitch(float p, @Local(argsOnly = true) SoundCategory cat) {
		return ClientFx.snd_pitch(p, cat);
	}

	@ModifyArg(method = "playBackgroundSound", at = @At(value = "INVOKE", target = SET_VOL), index = 1)
	private float bwo$bg(float v, @Local(argsOnly = true) SoundEntry entry) {
		ClientFx.bg_started(entry == null ? null : entry.name, v);
		return v * ClientFx.bg_mul();
	}

	@ModifyArg(method = "updateOptions", at = @At(value = "INVOKE", target = SET_VOL), index = 1)
	private float bwo$opts(float v) {
		return ClientFx.bg_options(v);
	}

	@ModifyArg(method = "playMusic(Lnet/minecraft/client/sound/SoundEntry;FFFFF)V", at = @At(value = "INVOKE", target = SET_VOL), index = 1)
	private float bwo$record(float v) {
		ClientFx.rec_started(v);
		return v * ClientFx.music_mul();
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void bwo$tick(CallbackInfo ci) {
		ClientFx.music_tick();
	}
}
