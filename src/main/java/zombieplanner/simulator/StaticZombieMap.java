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
	public void set(CellType val, int... idx) {
		set(val.getByteVal(), idx);
	}

	@Override
	public CellType typeOf(int... idx) {
		return CellType.fromByteVal(get(idx));
	}

}
