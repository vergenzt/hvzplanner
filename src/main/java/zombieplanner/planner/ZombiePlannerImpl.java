package zombieplanner.planner;

import java.util.List;

import robotutils.data.IntCoord;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.ZombieMap;
import zombieplanner.simulator.ZombieSimulator.MoveAction;

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

	@Override
	public Action getAction(IntCoord from, Multiset<IntCoord> visibleZombies) {
		if (planner == null) {
			planner = new ProbabilisticGridDStar(map, probDist, from, goal);
		}

		// if there are no zombies in sight
//		if (visibleZombies.isEmpty()) {
//			planner.setStart(from);
			List<IntCoord> plan = planner.plan();
			if (plan.isEmpty())
				return null;
			IntCoord next = plan.get(1);
			if (next.get(0) < from.get(0)) return MoveAction.LEFT;
			if (next.get(0) > from.get(0)) return MoveAction.RIGHT;
			if (next.get(1) < from.get(1)) return MoveAction.UP;
			if (next.get(1) > from.get(1)) return MoveAction.DOWN;
//		}
//		else {
//		}
		return null;
	}

	@Override
	public void setGoal(IntCoord goal) {
		this.goal = goal;
	}

}
