package zombieplanner.planner;

import robotutils.data.IntCoord;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.ZombieMap;

import com.google.common.collect.Multiset;

public interface ZombiePlanner {

	public void initialize(ZombieMap map, ProbabilityMap probMap);

	public void setGoal(IntCoord goal);

	public Action getAction(IntCoord from, Multiset<IntCoord> visibleZombies);

}
