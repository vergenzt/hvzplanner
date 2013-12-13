package zombieplanner.planner;

import robotutils.data.CoordUtils;
import robotutils.data.GridMap;
import robotutils.data.IntCoord;
import robotutils.planning.GridDStar;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.ZombieMap;
import zombieplanner.simulator.ZombieMap.CellType;

/**
 * An implementation of D*-lite that takes a probability distribution of some
 * Really Bad Event happening and a grid of obstacles, and plans a path that
 * balances path length with path risk, according to parameters.
 *
 * @author Tim Vergenz
 */
public class ProbabilisticGridDStar extends GridDStar {

	// cost = (movement cost) + -alpha * log(1 - P(bad stuff in destination))
	private GridMap map;
	private ProbabilityMap probDist;
	private double alpha = 5.0;

	public ProbabilisticGridDStar(
			GridMap map, ProbabilityMap probDist, IntCoord start, IntCoord goal) {
		super(map, start, goal);
		this.map = map;
		this.probDist = probDist;
	}

	public ProbabilisticGridDStar(
			GridMap map, ProbabilityMap probDist, double alpha, IntCoord start, IntCoord goal) {
		super(map, start, goal);
		this.map = map;
		this.probDist = probDist;
		this.alpha = alpha;
	}

	/**
	 * The constant addition factor that allows the algorithm to
	 * take length of a path into consideration.
	 */
	public static final double CONSTANT = 1.0;

	@Override
	protected double c(IntCoord a, IntCoord b) {
        if (CoordUtils.mdist(a, b) != 1)
            return Double.POSITIVE_INFINITY;
		CellType ca = ((ZombieMap)map).typeOf(a.getInts());
		CellType cb = ((ZombieMap)map).typeOf(b.getInts());
		if (ca == CellType.OBSTACLE || cb == CellType.OBSTACLE)
			return Double.POSITIVE_INFINITY;
		else
			return CONSTANT - alpha*Math.log(1.0 - probDist.get(b.getInts()));
	}

}
