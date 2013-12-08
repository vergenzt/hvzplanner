package zombieplanner.simulator;

import robotutils.data.StaticMap;

public class StaticZombieMap extends StaticMap implements ZombieMap {

	public StaticZombieMap() {
		super();
	}

	public StaticZombieMap(int... sizes) {
		super(sizes);
	}

	@Override
	public void set(byte val, int... idx) {
		if (0 <= val && val <= CellType.values().length)
			super.set(val, idx);
		else
			throw new IllegalArgumentException("Not an allowable CellType: " + val);
	}

	public void set(CellType val, int... idx) {
		set((byte)val.ordinal(), idx);
	}

	@Override
	public CellType typeOf(int... idx) {
		return CellType.values()[get(idx)];
	}

}
