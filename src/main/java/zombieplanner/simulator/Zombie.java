package zombieplanner.simulator;

import robotutils.data.IntCoord;

public class Zombie {

	private static int NEXT_ID = 0;

	private int id;
	private IntCoord position;
	private boolean isAlive;
	private double speed;

	public Zombie(IntCoord position, double speed) {
		this.id = NEXT_ID++;
		this.position = position;
		this.isAlive = true;
		this.speed = speed;
	}

	public int getId() {
		return id;
	}

	public IntCoord getPosition() {
		return position;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public double getSpeed() {
		return speed;
	}
}
