package zombieplanner.simulator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import robotutils.data.IntCoord;
import zombieplanner.planner.ZombiePlanner;
import zombieplanner.simulator.ZombieMap.CellType;
import zombieplanner.simulator.impl.GTMapGenerator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * Simulates a Zombie Survival situation according to our model.
 *
 * @author Tim Vergenz <vergenzt@gmail.com>
 */
public class ZombieSimulator {

	protected ZombieMap map;
	protected ProbabilityMap probDist;
	protected IntCoord human;
	protected IntCoord goal;
	protected Multiset<IntCoord> zombies;
	protected ZombiePlanner planner;

	public ZombieSimulator(ZombieMap map, ProbabilityMap probDist, ZombiePlanner planner) {
		this.map = map;
		this.probDist = probDist;
		GTMapGenerator.filterAndNormProbabilityMap(probDist, map);
		this.planner = planner;
	}

	protected void setHumanPosition(IntCoord position) {
		this.human = position;
	}

	protected void setGoalPosition(IntCoord position) {
		this.goal = position;
		planner.setGoal(goal);
	}

	public boolean hasHumanAndGoal() {
		return (human != null) && (goal != null);
	}

	public ZombieMap getMap() {
		return map;
	}

	public ProbabilityMap getProbDist() {
		return probDist;
	}

	public enum MoveAction implements Action {
		NONE(0,0), LEFT(-1,0), RIGHT(1,0), UP(0,-1), DOWN(0,1);

		private final int dx, dy;

		private MoveAction(int dx, int dy) {
			this.dx = dx; this.dy = dy;
		}

		@Override
		public void execute(ZombieSimulator sim) {
			IntCoord curr = sim.human;
			int nx = (int)curr.get(0) + dx;
			int ny = (int)curr.get(1) + dy;
			IntCoord next = new IntCoord(nx, ny);

			if (sim.map.typeOf(next.getInts()) == CellType.OBSTACLE) {
				throw new IllegalArgumentException(
					"Action " + toString() + " collides with obstacle!"
				);
			}
			sim.human = next;
		}
	}

	/**
	 * The Manhattan distance view radius for the human. (i.e. zombies
	 * whose Manhattan distance is <= VIEW_RADIUS are visible)
	 */
	public static final int VIEW_RADIUS = 12;
	public static final int ZOMBIE_VIEW_RADIUS = 12;

	/**
	 * Get whether the given position is visible to the human
	 * @param position
	 * @return true if it is within the viewing radius
	 */
	public boolean isVisible(IntCoord position) {
		assert human.dims() == position.dims();
		int[] humanCoords = human.getInts();
		int[] otherCoords = position.getInts();
		int sum = 0;
		for (int i=0; i<humanCoords.length; i++)
			sum += Math.abs(humanCoords[i] - otherCoords[i]);
		return sum <= VIEW_RADIUS;
	}

	/**
	 * Get adjacent zombie-walkable neighbors of a position.
	 * @param pos
	 */
	public Collection<IntCoord> getNeighbors(IntCoord pos) {
		return Collections2.filter(Arrays.asList(
				new IntCoord((int)pos.get(0)-1,(int)pos.get(1)),
				new IntCoord((int)pos.get(0)+1,(int)pos.get(1)),
				new IntCoord((int)pos.get(0),(int)pos.get(1)-1),
				new IntCoord((int)pos.get(0),(int)pos.get(1)+1)
			), new Predicate<IntCoord>() {
				@Override
				public boolean apply(IntCoord _pos) {
					return map.typeOf(_pos.getInts()) == CellType.CLEAR;
				}
			});
	}

	public static final int NUM_ZOMBIES = 30;

	public void initializeZombies() {
		zombies = HashMultiset.create();
		Random rand = new Random();
		for (int n=0; n<NUM_ZOMBIES; n++) {
			//
			double x = rand.nextDouble();
			double sum = 0.0;
			IntCoord zombie = null;
			for (int i=0; i<map.size(0); i++) {
				for (int j=0; j<map.size(1); j++) {
					sum += probDist.get(i, j);
					if (x <= sum) {
						zombie = new IntCoord(i,j);
					}
					if (zombie != null)
						break;
				}
				if (zombie != null)
					break;
			}
			zombies.add(zombie);
		}
	}

	/**
	 * Step the simulation once, and return whether the game is over.
	 * @return
	 */
	public boolean stepOnce() {
		boolean gameOver = false;

		// get the zombies within the player's view radius
		Multiset<IntCoord> visibleZombies
			= Multisets.filter(zombies, new Predicate<IntCoord>() {
				@Override public boolean apply(IntCoord zombie) {
					return isVisible(zombie);
				}
			});

		Action action = planner.getAction(human, visibleZombies);
		if (action == null) {
			return true;
		}

		action.execute(this);

		// run BFS from human to move zombies toward
		Queue<IntCoord> queue = Lists.newLinkedList();
		Map<IntCoord,IntCoord> prevs = Maps.newHashMap();
		Map<IntCoord,Integer> depths = Maps.newHashMap();
		queue.add(human);
		prevs.put(human, null);
		depths.put(human, 0);
		while (!queue.isEmpty()) {
			IntCoord pos = queue.poll();
			Integer d = depths.get(pos);
			if (d.intValue() < ZOMBIE_VIEW_RADIUS) {
				for (IntCoord neighbor : getNeighbors(pos)) {
					if (!prevs.containsKey(neighbor)) {
						prevs.put(neighbor, pos);
						depths.put(neighbor, d + 1);
						queue.add(neighbor);
					}
				}
			}
		}

		Multiset<IntCoord> movedZombies = HashMultiset.create();
		for (IntCoord zombie : zombies) {
			if (prevs.containsKey(zombie)) {
				movedZombies.add(prevs.get(zombie));
			}
			else {
				IntCoord[] moves = new IntCoord[] {
					new IntCoord((int)zombie.get(0), (int)zombie.get(1)),
					new IntCoord((int)zombie.get(0)+1, (int)zombie.get(1)),
					new IntCoord((int)zombie.get(0)-1, (int)zombie.get(1)),
					new IntCoord((int)zombie.get(0), (int)zombie.get(1)+1),
					new IntCoord((int)zombie.get(0), (int)zombie.get(1)-1),
				};
				IntCoord moved = moves[new Random().nextInt(5)];
				if (map.typeOf(moved.getInts()) == CellType.CLEAR)
					movedZombies.add(moved);
				else
					movedZombies.add(zombie);
			}
		}
		zombies = movedZombies;

		for (IntCoord zombie : zombies)
			if (zombie.equals(human))
				gameOver = true;

		return gameOver;
	}
}
