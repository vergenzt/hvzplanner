package zombieplanner.planner;

import robotutils.data.IntCoord;
import robotutils.planning.GridDStar;

public class SimpleZombiePlanner extends RiskAverseZombiePlanner {

	@Override
	protected GridDStar getPlanner(IntCoord start) {
		return new ProbabilisticGridDStar(map, probDist, start, goal) {
			@Override
			protected double c(IntCoord a, IntCoord b) {
				double s = super.c(a, b);
				if (s < Double.POSITIVE_INFINITY) {
					return 1.0;
				}
				else {
					return s;
				}
			}
		};
	}

}
