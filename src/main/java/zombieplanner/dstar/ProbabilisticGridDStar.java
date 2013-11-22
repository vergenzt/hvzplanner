package zombieplanner.dstar;

import robotutils.data.GridMap;
import robotutils.data.IntCoord;
import robotutils.planning.GridDStar;

/**
 * An implementation of D*-lite that takes a probability distribution of some
 * Really Bad Event happening and a grid of obstacles, and plans a path that
 * balances path length with path risk, according to parameters.
 * 
 * @author Tim Vergenz
 */
public class ProbabilisticGridDStar extends GridDStar {
	
	// cost = (movement cost) + -alpha * log(1 - P(bad stuff in destination))
	private ProbabilityMap probDist;
	private double alpha = 5.0;

	public ProbabilisticGridDStar(
			GridMap map, ProbabilityMap probDist, IntCoord start, IntCoord goal) {
		super(map, start, goal);
		this.probDist = probDist;
	}
	
	@Override
	protected double c(IntCoord a, IntCoord b) {
		return super.c(a,b) + alpha*probDist.get(b.getInts());
	}

}
