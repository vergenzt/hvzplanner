package zombieplanner.planner;

import java.util.List;
import java.util.Set;

import robotutils.data.CoordUtils;
import robotutils.data.IntCoord;
import robotutils.planning.GridDStar;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.Zombie;
import zombieplanner.simulator.ZombieMap;
import zombieplanner.simulator.ZombieSimulator;
import zombieplanner.simulator.ZombieSimulator.MoveAction;
import zombieplanner.simulator.ZombieSimulator.StunAction;


public class RiskAverseZombiePlanner implements ZombiePlanner {

	protected ZombieMap map;
	protected ProbabilityMap probDist;
	protected IntCoord goal;

	protected ProbabilityMap dynProbDist;

	protected GridDStar planner;

	@Override
	public void initialize(ZombieMap map, ProbabilityMap probMap) {
		this.map = map;
		this.probDist = probMap;
		this.plan = null;
		this.planner = null;
	}

	private List<IntCoord> plan;
	public List<IntCoord> getPlan() {
		return plan;
	}

	protected GridDStar getPlanner(IntCoord start) {
		return new ProbabilisticGridDStar(map, probDist, 10000, start, goal);
	}

	@Override
	public Action getAction(IntCoord from, Set<Zombie> visibleZombies) {
		if (planner == null) {
			planner = getPlanner(from);
		}

		// if there are zombies in sight
		if (!visibleZombies.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			Zombie target = null;
			for (Zombie zombie : visibleZombies) {
				IntCoord zpos = zombie.getPosition();
				if (CoordUtils.mdist(from, zpos) < min) {
					min = CoordUtils.mdist(from, zpos);
					target = zombie;
				}
			}

			if (min <= ZombieSimulator.MAX_STUN_DISTANCE && target != null) {
				return new StunAction(target);
			}
		}

		planner.updateStart(from);
		plan = planner.plan();
		if (plan.isEmpty())
			return null;
		plan = plan.subList(1, plan.size());
		if (plan.isEmpty())
			return null;
		IntCoord next = plan.get(0);
		if (next.get(0) < from.get(0)) return MoveAction.LEFT;
		if (next.get(0) > from.get(0)) return MoveAction.RIGHT;
		if (next.get(1) < from.get(1)) return MoveAction.UP;
		if (next.get(1) > from.get(1)) return MoveAction.DOWN;
		return MoveAction.NONE;
	}

	@Override
	public void setGoal(IntCoord goal) {
		this.goal = goal;
	}

}
