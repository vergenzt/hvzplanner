package zombieplanner.planner;

import java.util.List;

import robotutils.data.CoordUtils;
import robotutils.data.IntCoord;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.ZombieMap;
import zombieplanner.simulator.ZombieSimulator;
import zombieplanner.simulator.ZombieSimulator.MoveAction;
import zombieplanner.simulator.ZombieSimulator.StunAction;

import com.google.common.collect.Multiset;


public class ZombiePlannerImpl implements ZombiePlanner {

	private ZombieMap map;
	private ProbabilityMap probDist;
	private IntCoord goal;

	private ProbabilityMap dynProbDist;

	private ProbabilisticGridDStar planner;

	@Override
	public void initialize(ZombieMap map, ProbabilityMap probMap) {
		this.map = map;
		this.probDist = probMap;
	}

	private List<IntCoord> plan;
	public List<IntCoord> getPlan() {
		return plan;
	}

	@Override
	public Action getAction(IntCoord from, Multiset<IntCoord> visibleZombies) {
		if (planner == null) {
			planner = new ProbabilisticGridDStar(map, probDist, 10000, from, goal);
		}

		// if there are zombies in sight
		if (!visibleZombies.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			IntCoord target = null;
			for (IntCoord zombie : visibleZombies) {
				if (CoordUtils.mdist(from, zombie) < min) {
					min = CoordUtils.mdist(from, zombie);
					target = zombie;
				}
			}

			if (min <= ZombieSimulator.MAX_STUN_DISTANCE && target != null) {
				return new StunAction(target);
			}
		}

		planner.updateStart(from);
		plan = planner.plan();
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
