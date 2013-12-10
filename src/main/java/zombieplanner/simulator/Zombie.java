package zombieplanner.simulator;

import java.util.Map;
import java.util.Random;

import robotutils.data.IntCoord;
import zombieplanner.simulator.ZombieMap.CellType;

public class Zombie {

	private static int NEXT_ID = 0;

	private int id;
	private IntCoord position;
	private boolean isAlive;
	private double speed;

	public static final double MIN_SPEED = 0.5;
	public static final double MAX_SPEED = 1.5;

	public Zombie(IntCoord position) {
		this(position, new Random().nextDouble() * (MAX_SPEED-MIN_SPEED) + MIN_SPEED);
	}

	public Zombie(IntCoord position, double speed) {
		this.id = NEXT_ID++;
		this.position = position;
		this.isAlive = true;
		this.speed = speed;
	}

	/**
	 * Process one turn's worth of movement (taking speed into account)
	 */
	public void doTurn(Map<IntCoord,IntCoord> prevs, ZombieMap map) {
		assert (MIN_SPEED <= speed) && (speed <= MAX_SPEED);
		Random rand = new Random();
		int numMoves = 0;
		if (speed < 1.0) {
			if (rand.nextDouble() < speed)
				numMoves = 1;
			else numMoves = 0;
		}
		else {
			if (rand.nextDouble() < speed-1.0)
				numMoves = 2;
			else numMoves = 1;
		}
		while (numMoves-- > 0)
			doMove(prevs, map);
	}

	/**
	 * Process one movement for this zombie.
	 */
	public void doMove(Map<IntCoord,IntCoord> prevs, ZombieMap map) {
		IntCoord zpos = this.getPosition();
		if (prevs.containsKey(zpos)) {
			if (prevs.get(zpos) != null)
				this.setPosition(prevs.get(zpos));
		}
		else {
			IntCoord[] moves = new IntCoord[] {
				new IntCoord((int)zpos.get(0), (int)zpos.get(1)),
				new IntCoord((int)zpos.get(0)+1, (int)zpos.get(1)),
				new IntCoord((int)zpos.get(0)-1, (int)zpos.get(1)),
				new IntCoord((int)zpos.get(0), (int)zpos.get(1)+1),
				new IntCoord((int)zpos.get(0), (int)zpos.get(1)-1),
			};
			IntCoord moved = moves[new Random().nextInt(5)];
			if (map.typeOf(moved.getInts()) == CellType.CLEAR)
				this.setPosition(moved);
		}
	}

	public int getId() {
		return id;
	}

	public IntCoord getPosition() {
		return position;
	}
	public void setPosition(IntCoord position) {
		this.position = position;
	}

	public boolean isAlive() {
		return isAlive;
	}
	public void setAlive(boolean alive) {
		isAlive = alive;
	}

	public double getSpeed() {
		return speed;
	}
}
