package zombieplanner.simulator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import robotutils.data.CoordUtils;
import robotutils.data.IntCoord;
import zombieplanner.planner.RiskAverseZombiePlanner;
import zombieplanner.planner.SimpleZombiePlanner;
import zombieplanner.planner.ZombiePlanner;
import zombieplanner.simulator.ZombieMap.CellType;
import zombieplanner.simulator.impl.GTMapGenerator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Simulates a Zombie Survival situation according to our model.
 *
 * @author Tim Vergenz <vergenzt@gmail.com>
 */
public class ZombieSimulator {

	private final static Logger log = Logger.getLogger(ZombieSimulator.class.getName());

	protected ZombieMap map;
	protected ProbabilityMap probDist;
	protected IntCoord human;
	protected IntCoord goal;
	protected Set<Zombie> zombies;
	protected ZombiePlanner planner;

	protected int totalSteps = 0;

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

	public static final int MAX_STUN_DISTANCE = 4;

	public static class StunAction implements Action {

		private final Zombie target;

		public StunAction(Zombie target) {
			this.target = target;
		}

		@Override
		public void execute(ZombieSimulator sim) {
			assert sim.zombies.contains(target);

			Random rand = new Random();
			double dist = CoordUtils.mdist(sim.human, target.getPosition());

			if (dist > MAX_STUN_DISTANCE) return;

			double p = 0.65 - (0.1)*(dist);

			if (rand.nextDouble() < p)
				target.setAlive(false);
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

	public static int NUM_ZOMBIES = 50;

	public void initializeZombies() {
		zombies = Sets.newHashSet();
		Random rand = new Random();
		for (int n=0; n<NUM_ZOMBIES; n++) {
			double x = rand.nextDouble();
			double sum = 0.0;
			IntCoord pos = null;
			for (int i=0; i<map.size(0); i++) {
				for (int j=0; j<map.size(1); j++) {
					sum += probDist.get(i, j);
					if (x <= sum) {
						pos = new IntCoord(i,j);
					}
					if (pos != null)
						break;
				}
				if (pos != null)
					break;
			}
			zombies.add(new Zombie(pos));
		}

		planner.initialize(map, probDist);
		setState(GameState.ACTIVE);
	}

	public enum GameState {
		SETUP, ACTIVE, SUCCESS, FAILURE;
	}

	private GameState state = GameState.SETUP;

	public void setState(GameState state) {
		this.state = state;
	}

	public GameState getState() {
		return state;
	}

	/**
	 * Step the simulation once.
	 */
	public void stepOnce() {
		if (state != GameState.ACTIVE)
			return;

		// get the zombies within the player's view radius
		Set<Zombie> visibleZombies
			= Sets.filter(zombies, new Predicate<Zombie>() {
				@Override public boolean apply(Zombie zombie) {
					return isVisible(zombie.getPosition()) && zombie.isAlive();
				}
			});

		Action action = planner.getAction(human, visibleZombies);
		if (action == null) {
			state = GameState.FAILURE;
			return;
		}

		action.execute(this);

		this.totalSteps++;

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

		for (Zombie zombie : zombies)
			if (zombie.isAlive() && zombie.getPosition().equals(human)) {
				state = GameState.FAILURE;
				return;
			}

		// process zombie movement
		for (Zombie zombie : zombies) {
			if (!zombie.isAlive())
				continue;
			zombie.doTurn(prevs, map);
		}

		for (Zombie zombie : zombies)
			if (zombie.isAlive() && zombie.getPosition().equals(human)) {
				state = GameState.FAILURE;
				return;
			}

		if (human.equals(goal)) {
			state = GameState.SUCCESS;
		}
	}

	public static final int NUM_TRIALS = 200;

	/**
	 * Run a simulation without the user interface.
	 * @param args
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		// TODO implement non-UI main method

		String fname = new SimpleDateFormat("'log/'yyyy-MM-dd HH:mm:ss.SSS'.txt'").format(new Date());
		FileHandler fh = new FileHandler(fname);
		fh.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + "\n";
			}
		});
		log.addHandler(fh);
		log.setUseParentHandlers(false);
		ConsoleHandler h = new ConsoleHandler();
		h.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + "\n";
			}
		});
		log.addHandler(h);

		ZombieMap map = GTMapGenerator.loadGTMap();
		ProbabilityMap probDist = GTMapGenerator.loadGTZombieProbabilities(0.1);

		for (NUM_ZOMBIES = 40; NUM_ZOMBIES <= 70; NUM_ZOMBIES+=10) {
			log.info("=== NUM_ZOMBIES: " + NUM_ZOMBIES + " ===");
			log.info("");

			Map<String,ZombiePlanner> planners = Maps.newLinkedHashMap();
			planners.put("Risk Averse Planner", new RiskAverseZombiePlanner());
			planners.put("Simple Planner", new SimpleZombiePlanner());

			Map<ZombiePlanner,Integer> successes = Maps.newLinkedHashMap();
			Map<ZombiePlanner,Integer> totalSteps = Maps.newLinkedHashMap();

			// freshmen dorms
			IntCoord start = new IntCoord(377, 275);
			// clough building
			IntCoord goal = new IntCoord(274, 204);
			log.info("From: " + start);
			log.info("To: " + goal);
			log.info("");

			long startTime = System.nanoTime();
			for (int i=0; i<NUM_TRIALS; i++) {
				for (Entry<String,ZombiePlanner> e : planners.entrySet()) {
					ZombiePlanner planner = e.getValue();

					ZombieSimulator sim = new ZombieSimulator(map, probDist, e.getValue());
					sim.setHumanPosition(start);
					sim.setGoalPosition(goal);
					sim.initializeZombies();

					while (sim.getState() == GameState.ACTIVE) {
						sim.stepOnce();
					}

					if (!successes.containsKey(planner))
						successes.put(planner, 0);
					if (!totalSteps.containsKey(planner))
						totalSteps.put(planner, 0);
					successes.put(planner, successes.get(planner) + (sim.getState() == GameState.SUCCESS ? 1 : 0));
					totalSteps.put(planner, totalSteps.get(planner) + sim.totalSteps);
				}
			}
			long endTime = System.nanoTime();
			log.info("Total time: " + (endTime - startTime)/1000000000 + "s");
			log.info("");

			for (Entry<String,ZombiePlanner> e : planners.entrySet()) {
				log.info("Planner: " + e.getKey());
				log.info("Average steps:" + ((double)totalSteps.get(e.getValue())/NUM_TRIALS));
				log.info("% success: " + ((double)successes.get(e.getValue())/NUM_TRIALS)); System.out.println();
			}
			log.info("");
			log.info("");
		}
	}
}
