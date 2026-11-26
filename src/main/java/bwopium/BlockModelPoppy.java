package bwopium;

import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.core.block.Block;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.models.block.StaticBlockModel;

public class BlockModelPoppy extends BlockModelGeneric<BlockLogicPoppy> {
	static final String[] COLS = {"lilac", "red", "white", "purple"};
	final StaticBlockModel[] lower = new StaticBlockModel[4];

	final StaticBlockModel[][][] upper = new StaticBlockModel[COLS.length][4][2];

	public BlockModelPoppy(@NotNull Block<BlockLogicPoppy> block, @NotNull String base) {

		super(block, BlockModelDispatcher.loadDataModel(base + "lilac_0"));
		for (int i = 0; i < 4; i++) {
			lower[i] = BlockModelDispatcher.loadDataModel(base + "lower_" + i).asModel();
			for (int c = 0; c < COLS.length; c++) {
				upper[c][i][0] = BlockModelDispatcher.loadDataModel(base + COLS[c] + "_" + i).asModel();
				upper[c][i][1] = BlockModelDispatcher.loadDataModel(base + COLS[c] + "_" + i + "_pods").asModel();
			}
		}
	}

	@NotNull
	@Override
	public StaticBlockModel getModelFromData(int data) {
		int n = Math.min(3, BlockLogicPoppy.heads(data) - 1);
		return BlockLogicPoppy.upper(data) ? upper[BlockLogicPoppy.color(data)][n][BlockLogicPoppy.has_pods(data) ? 1 : 0] : lower[n];
	}
}
