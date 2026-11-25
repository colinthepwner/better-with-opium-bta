package bwopium;

import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelCrop extends BlockModelGeneric<BlockLogicPoppyCrop> {
	final StaticBlockModel[] stages = new StaticBlockModel[4];

	public BlockModelCrop(@NotNull Block<BlockLogicPoppyCrop> block, @NotNull String base) {
		super(block, BlockModelDispatcher.loadDataModel(base + "3"));
		for (int i = 0; i < 4; i++) stages[i] = BlockModelDispatcher.loadDataModel(base + i).asModel();
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) { return stages[data & 3]; }
}
