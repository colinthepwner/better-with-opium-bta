package bwopium;

import bwopium.high.DeathCauseOverdose;
import bwopium.high.Opium;
import bwopium.high.OpiumLook;
import bwopium.high.OpiumSync;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogicFlowerStackable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.Items;
import net.minecraft.core.net.command.CommandManager;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryPlacement;
import turniplabs.halplibe.helper.creativeInventory.CreativeInventoryRegistry;
import java.util.stream.IntStream;
import net.minecraft.core.block.tag.BlockTags;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Item;
import net.minecraft.core.sound.BlockSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.helper.BlockBuilder;
import turniplabs.halplibe.helper.RecipeBuilder;
import turniplabs.halplibe.helper.network.NetworkHandler;
import turniplabs.halplibe.util.TomlConfigHandler;
import turniplabs.halplibe.util.deathcause.DeathCauseRegistry;
import turniplabs.halplibe.util.dependency.Key;
import turniplabs.halplibe.util.toml.Toml;

public class Bwopium implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("bwopium", true);
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	public static TomlConfigHandler cfg;
	public static boolean debug, frame_pacing, input_delay;

	public static Block<BlockLogicPoppy> poppy;
	public static Block<BlockLogicPoppyCrop> crop;
	public static Item pod, pod_half, seeds, raw;

	public static final String[] COLOR_NAMES = {"lilac", "red", "white", "purple"};
	@SuppressWarnings("unchecked")
	public static final Block<BlockLogicFlowerStackable>[] flowers = new Block[COLOR_NAMES.length];

	public static boolean self_dmg;

	public static volatile boolean client_naive;

	@Override
	public void onInitialize() {
		Toml def = new Toml("Better with Opium");
		def.addEntry("poppy_block_id", "change if it clashes with another mod", 2400);
		def.addEntry("poppy_pod_item_id", "change if it clashes with another mod", 23400);
		def.addEntry("poppy_pod_half_item_id", "change if it clashes with another mod", 23401);
		def.addEntry("poppy_crop_block_id", "change if it clashes with another mod", 2401);
		def.addEntry("poppy_seeds_item_id", "change if it clashes with another mod", 23402);
		def.addEntry("poppy_flower_block_id", "first of 4 ids in a row (one per color). change if it clashes with another mod", 2402);
		def.addEntry("raw_opium_item_id", "change if it clashes with another mod", 23403);
		def.addEntry("frame_pacing", "lets the high/withdrawal mess with your framerate a little. off = never touches fps", true);
		def.addEntry("input_delay", "a tiny bit of mouse lag sober, none when high, a touch more when sick. off = never touches input", true);
		def.addEntry("debug", "prints the numbers above the hotbar", false);
		cfg = new TomlConfigHandler(MOD_ID, def);
		debug = cfg.getBoolean("debug");
		frame_pacing = cfg.getBoolean("frame_pacing");
		input_delay = cfg.getBoolean("input_delay");

		DeathCauseRegistry.getInstance().register(MOD_ID, "overdose", DeathCauseOverdose::new);
		NetworkHandler.registerNetworkMessage(OpiumSync::new);
		NetworkHandler.registerNetworkMessage(OpiumLook::new);
		CommandManager.registerCommand(new OpiumCommand());
		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::reg);
		CommonEvents.RECIPES_NAMESPACE_INIT.listen(Key.of(MOD_ID), () -> RecipeBuilder.initNameSpace(MOD_ID));
		CommonEvents.RECIPES_READY.listen(Key.of(MOD_ID), this::recipes);
	}

	void reg() {
		poppy = new BlockBuilder(MOD_ID)
			.setHardness(0f)
			.setBlockSound(BlockSounds.GRASS)
			.setTicking(true)
			.build("opium_poppy", "opium_poppy", cfg.getInt("poppy_block_id"), BlockLogicPoppy::new);
		pod = new ItemPoppyPod(MOD_ID + ".poppy_pod", MOD_ID + ":item/poppy_pod", cfg.getInt("poppy_pod_item_id"), Opium.BITE);
		pod_half = new ItemPoppyPod(MOD_ID + ".poppy_pod_half", MOD_ID + ":item/poppy_pod_half", cfg.getInt("poppy_pod_half_item_id"), Opium.HALF);
		raw = new ItemPoppyPod(MOD_ID + ".raw_opium", MOD_ID + ":item/raw_opium", cfg.getInt("raw_opium_item_id"), Opium.EXTRACT);
		crop = new BlockBuilder(MOD_ID)
			.setHardness(0f)
			.setBlockSound(BlockSounds.GRASS)
			.setTicking(true)
			.setTags(BlockTags.BROKEN_BY_FLUIDS)
			.build("poppy_crop", "poppy_crop", cfg.getInt("poppy_crop_block_id"), BlockLogicPoppyCrop::new);
		seeds = new ItemPoppySeeds(MOD_ID + ".poppy_seeds", MOD_ID + ":item/poppy_seeds", cfg.getInt("poppy_seeds_item_id"));
		for (int c = 0; c < flowers.length; c++) {
			String n = "poppy_flower_" + COLOR_NAMES[c];
			flowers[c] = new BlockBuilder(MOD_ID)
				.setHardness(0f)
				.setBlockSound(BlockSounds.GRASS)
				.setTicking(true)
				.setTags(BlockTags.BROKEN_BY_FLUIDS, BlockTags.PLANTABLE_IN_JAR, BlockTags.SHEARS_DO_SILK_TOUCH)
				.build(n, n, cfg.getInt("poppy_flower_block_id") + c, BlockLogicFlowerStackable::new);
		}

		CreativeInventoryRegistry inv = CreativeInventoryRegistry.INSTANCE;
		inv.register(poppy, new CreativeInventoryPlacement.After(() -> Blocks.FLOWER_RED)
			.setCustomSupplier(() -> IntStream.range(0, BlockLogicPoppy.COLORS).mapToObj(c -> new ItemStack(poppy, 1, BlockLogicPoppy.UPPER | c << 3)).toList()));
		for (int c = flowers.length - 1; c >= 0; c--) inv.register(flowers[c], new CreativeInventoryPlacement.After(() -> poppy));
		for (Item i : new Item[]{seeds, pod, pod_half, raw}) inv.register(i, new CreativeInventoryPlacement.After(() -> Items.SEEDS_PUMPKIN));
	}

	void recipes() {
		RecipeBuilder.Furnace(MOD_ID).setInput(pod).create("poppy_seeds", new ItemStack(seeds, 1));

		int[] dye = {13, 1, 15, 5};
		for (int c = 0; c < flowers.length; c++)
			RecipeBuilder.Shapeless(MOD_ID).addInput(flowers[c]).create("poppy_flower_" + COLOR_NAMES[c] + "_to_dye", new ItemStack(Items.DYE, 2, dye[c]));
	}
}
