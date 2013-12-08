package zombieplanner.simulator;

import robotutils.data.GridMap;

public interface ZombieMap extends GridMap {

	public enum CellType {
		CLEAR, OBSTACLE, BUILDING;
	}

	public CellType typeOf(int... idx);

	public void set(CellType type, int... idx);

}
