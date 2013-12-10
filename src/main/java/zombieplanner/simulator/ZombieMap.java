package zombieplanner.simulator;

import robotutils.data.GridMap;

public interface ZombieMap extends GridMap {

	public enum CellType {
		CLEAR((byte)255), OBSTACLE((byte)0), BUILDING((byte)128);

		private byte val;
		private CellType(byte val) {
			this.val = val;
		}

		public byte getByteVal() {
			return val;
		}

		public static CellType fromByteVal(byte val) {
			for (CellType t : values())
				if (t.getByteVal() == val)
					return t;
			return null;
		}
	}

	public CellType typeOf(int... idx);

	public void set(CellType type, int... idx);

}
