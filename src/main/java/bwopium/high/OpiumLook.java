package bwopium.high;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class OpiumLook implements NetworkMessage {
	int id;
	float high, nod, od;

	public OpiumLook() {}

	public OpiumLook(Player p, Opium o) { id = p.id; high = o.high; nod = o.nod; od = o.od; }

	@Override
	public void encodeToUniversalPacket(UniversalPacket buf) {
		buf.writeInt(id);
		buf.writeInt(Float.floatToIntBits(high));
		buf.writeInt(Float.floatToIntBits(nod));
		buf.writeInt(Float.floatToIntBits(od));
	}

	@Override
	public void decodeFromUniversalPacket(UniversalPacket buf) {
		id = buf.readInt();
		high = Float.intBitsToFloat(buf.readInt());
		nod = Float.intBitsToFloat(buf.readInt());
		od = Float.intBitsToFloat(buf.readInt());
	}

	@Override
	public void handle(NetworkContext ctx) {
		if (ctx.player == null || !ctx.player.world.isClientSide) return;
		Entity e = ctx.player.world.getEntityByID(id);
		if (e == ctx.player || !(e instanceof Player)) return;
		Opium o = OpiumHolder.of(e);
		if (o == null) return;
		o.high = high; o.nod = nod; o.od = od;
	}
}
