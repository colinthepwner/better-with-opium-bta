package bwopium;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.model.ItemModelStandard;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.helper.ParticleHelper;
import turniplabs.halplibe.util.dependency.Key;

public class BwopiumClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleHelper.createParticle("bwopium.mote", (w, x, y, z, mx, my, mz, data) -> new ParticlePoppyMote(w, x, y, z));
		ClientEvents.BLOCK_MODEL_RELOAD.listen(Key.of(Bwopium.MOD_ID), d -> {
			d.addDispatch(new BlockModelPoppy(Bwopium.poppy, "bwopium:block/opium_poppy/").render3D(false));
			d.addDispatch(new BlockModelCrop(Bwopium.crop, "bwopium:block/poppy_crop/").render3D(false));
		});
		ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(Bwopium.MOD_ID), d -> {
			d.addDispatch(new ItemModelStandard(Bwopium.pod));
			d.addDispatch(new ItemModelStandard(Bwopium.pod_half));
			d.addDispatch(new ItemModelStandard(Bwopium.seeds));
			d.addDispatch(new ItemModelStandard(Bwopium.raw));
		});
	}
}
