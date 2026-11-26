package bwopium;

import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelSlices<T extends BlockLogic> extends BlockModelGeneric<T> {
	final StaticBlockModel[] models;

	public BlockModelSlices(@NotNull Block<T> block, @NotNull String base, int n) {
		super(block, BlockModelDispatcher.loadDataModel(base + "0"));
		models = new StaticBlockModel[n];
		for (int i = 0; i < n; i++) models[i] = BlockModelDispatcher.loadDataModel(base + i).asModel();
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) { return models[Math.floorMod(data, models.length)]; }
}
