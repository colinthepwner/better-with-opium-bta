package bwopium.high;

public interface OpiumHolder {
	Opium bwo$opium();

	static Opium of(Object p) { return p instanceof OpiumHolder h ? h.bwo$opium() : null; }
}
