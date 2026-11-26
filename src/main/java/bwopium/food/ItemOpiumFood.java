package bwopium.food;

import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemFood;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemOpiumFood extends ItemFood {
	final float amt;
	final boolean bowl;

	public ItemOpiumFood(String key, String ns, int id, int heal, int ticks_per_heal, int stack, float amt, boolean bowl) {
		super(key, ns, id, heal, ticks_per_heal, false, stack);
		this.amt = amt;
		this.bowl = bowl;
	}

	@Override
	public ItemStack onUse(@NotNull ItemStack self, @NotNull World w, @NotNull Player p) {
		boolean hungry = p.getHealth() < p.getMaxHealth() && p.getHealth() + p.getTotalHealingRemaining() < p.getMaxHealth();
		if (!self.consumeItem(p)) return self;
		if (hungry) p.eatFood(self);
		Infused.munch(p, w, Infused.crumb());
		Infused.dose(p, w, amt);
		return bowl && self.stackSize <= 0 ? new ItemStack(Items.BOWL) : self;
	}
}
