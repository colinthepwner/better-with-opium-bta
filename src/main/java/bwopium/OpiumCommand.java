package bwopium;

import bwopium.high.Opium;
import bwopium.high.OpiumHolder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeFloat;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager.CommandRegistry;
import net.minecraft.core.net.command.CommandSource;

public class OpiumCommand implements CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> d) {
		d.register(ArgumentBuilderLiteral.<CommandSource>literal("opium")
			.requires(CommandSource::hasAdmin)
			.executes(c -> run(c, (p, o) -> {}))
			.then(ArgumentBuilderLiteral.<CommandSource>literal("dose")
				.then(ArgumentBuilderRequired.<CommandSource, Float>argument("pods", ArgumentTypeFloat.floatArg(0.05f, 20f))
					.executes(c -> run(c, (p, o) -> {
						o.eat(p, p.world.getWorldTime(), ArgumentTypeFloat.getFloat(c, "pods"), Opium.BITE, 1f);
						o.half_str = 0;
					}))))
			.then(ArgumentBuilderLiteral.<CommandSource>literal("extract")
				.then(ArgumentBuilderRequired.<CommandSource, Float>argument("lumps", ArgumentTypeFloat.floatArg(0.05f, 20f))
					.executes(c -> run(c, (p, o) -> o.eat(p, p.world.getWorldTime(), ArgumentTypeFloat.getFloat(c, "lumps") * ItemPoppyPod.EXTRACT, Opium.EXTRACT, 1f)))))
			.then(ArgumentBuilderLiteral.<CommandSource>literal("skip")
				.then(ArgumentBuilderRequired.<CommandSource, Float>argument("hours", ArgumentTypeFloat.floatArg(0f, 24f * 30))
					.executes(c -> run(c, (p, o) -> o.skip((long) (ArgumentTypeFloat.getFloat(c, "hours") * 1000))))))
			.then(ArgumentBuilderLiteral.<CommandSource>literal("set")
				.then(ArgumentBuilderRequired.<CommandSource, String>argument("field", ArgumentTypeString.word())
					.then(ArgumentBuilderRequired.<CommandSource, Float>argument("value", ArgumentTypeFloat.floatArg())
						.executes(c -> run(c, (p, o) -> set(c, o, ArgumentTypeString.getString(c, "field"), ArgumentTypeFloat.getFloat(c, "value")))))))
			.then(ArgumentBuilderLiteral.<CommandSource>literal("clear")
				.executes(c -> run(c, (p, o) -> o.clear()))));
	}

	interface Act { void go(Player p, Opium o); }

	static int run(CommandContext<CommandSource> c, Act act) {
		CommandSource src = c.getSource();
		Player p = src.getSender();
		Opium o = OpiumHolder.of(p);
		if (p == null || o == null) { src.sendMessage("players only"); return 0; }
		act.go(p, o);

		o.tick(p);
		src.sendMessage(o.status());
		return 1;
	}

	static void set(CommandContext<CommandSource> c, Opium o, String f, float v) {
		switch (f) {
			case "tol" -> o.tol = Math.max(0, v);
			case "dep" -> o.dep = Math.max(0, Math.min(1, v));
			case "hist" -> o.hist = Math.max(0, v);
			case "life" -> o.life = Math.max(0, v);
			case "dry" -> o.dry = Math.max(0, v);
			case "morning" -> o.morning = Math.max(0, v);
			case "rebound" -> o.rebound = Math.max(0, v);
			default -> c.getSource().sendMessage("fields: tol dep hist life dry morning rebound");
		}
	}
}
