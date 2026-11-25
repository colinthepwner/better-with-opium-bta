package bwopium.high;

import turniplabs.halplibe.helper.network.NetworkMessage;
import turniplabs.halplibe.helper.network.UniversalPacket;

public class OpiumSync implements NetworkMessage {
	float high, wd, od, pen, tol, dep, hist, rush, life;
	float nod;

	public OpiumSync() {}

	public OpiumSync(Opium o) {
		high = o.high; wd = o.wd; od = o.od; pen = o.pen; tol = o.tol; dep = o.dep; hist = o.hist; nod = o.nod; rush = o.rush; life = o.life;
	}

	@Override
	public void encodeToUniversalPacket(UniversalPacket buf) {
		for (float f : new float[]{high, wd, od, pen, tol, dep, hist, nod, rush, life}) buf.writeInt(Float.floatToIntBits(f));
	}

	@Override
	public void decodeFromUniversalPacket(UniversalPacket buf) {
		high = rd(buf); wd = rd(buf); od = rd(buf); pen = rd(buf); tol = rd(buf); dep = rd(buf); hist = rd(buf); nod = rd(buf); rush = rd(buf); life = rd(buf);
	}

	static float rd(UniversalPacket b) { return Float.intBitsToFloat(b.readInt()); }

	@Override
	public void handle(NetworkContext ctx) {

		if (ctx.player == null || !ctx.player.world.isClientSide) return;
		Opium o = OpiumHolder.of(ctx.player);
		if (o == null) return;
		o.high = high; o.wd = wd; o.od = od; o.pen = pen; o.tol = tol; o.dep = dep; o.hist = hist; o.nod = nod; o.rush = rush; o.life = life; o.locked = nod > 0.5f;
		o.synced = true;
	}
}
