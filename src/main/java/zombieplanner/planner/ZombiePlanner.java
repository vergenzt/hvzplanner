package zombieplanner.planner;

import java.util.Set;

import robotutils.data.IntCoord;
import zombieplanner.simulator.Action;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.Zombie;
import zombieplanner.simulator.ZombieMap;

public interface ZombiePlanner {

	public void initialize(ZombieMap map, ProbabilityMap probMap);

	public void setGoal(IntCoord goal);

	public Action getAction(IntCoord from, Set<Zombie> visibleZombies);

}
