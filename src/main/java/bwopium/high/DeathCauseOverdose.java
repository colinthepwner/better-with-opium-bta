package bwopium.high;

import net.minecraft.core.entity.Mob;
import turniplabs.halplibe.util.deathcause.DeathCause;

public class DeathCauseOverdose extends DeathCause {
	public DeathCauseOverdose() {}

	public DeathCauseOverdose(Mob victim) { super(victim); }

	@Override
	protected String getTranslationKeyShard() { return "bwopium.overdose"; }
}
