package zombieplanner.planner;

import robotutils.data.IntCoord;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.ZombieMap;

import com.google.common.collect.Multiset;


public class ZombiePlannerImpl implements ZombiePlanner {

	@Override
	public void initialize(ZombieMap map, ProbabilityMap probMap) {

	}

	@Override
	public Action getAction(IntCoord from, Multiset<IntCoord> visibleZombies) {
		return null;
	}

}
