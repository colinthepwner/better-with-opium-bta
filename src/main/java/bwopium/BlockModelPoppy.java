package bwopium;

import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelPoppy extends BlockModelGeneric<BlockLogicPoppy> {
	final StaticBlockModel[] lower = new StaticBlockModel[4], upper = new StaticBlockModel[4];

	public BlockModelPoppy(@NotNull Block<BlockLogicPoppy> block, @NotNull String base) {

		super(block, BlockModelDispatcher.loadDataModel(base + "upper_0"));
		for (int i = 0; i < 4; i++) {
			lower[i] = BlockModelDispatcher.loadDataModel(base + "lower_" + i).asModel();
			upper[i] = BlockModelDispatcher.loadDataModel(base + "upper_" + i).asModel();
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		int n = Math.min(3, BlockLogicPoppy.heads(data) - 1);
		return BlockLogicPoppy.upper(data) ? upper[n] : lower[n];
	}
}
