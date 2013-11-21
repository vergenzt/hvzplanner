package zombieplanner;

import robotutils.data.GridMap;
import robotutils.data.GridMapGenerator;
import zombieplanner.dstar.ProbabilityMap;

/**
 * A {@link GridMap} implementation of 
 * @author Tim Vergenz
 */
public final class GTMapGenerator {
	
	public static final int WIDTH = 401, HEIGHT = 294;
	
	private static GridMap gtMap;
	private static ProbabilityMap probMap;
	
	/**
	 * Get a {@link GridMap} with data from the map of GT's campus.
	 * @return a filled-in {@link GridMap}
	 */
	public static GridMap loadGTMap() {
		if (gtMap == null) {
			// TODO load GT map from file
			gtMap = GridMapGenerator.createRandomMazeMap2D(WIDTH, HEIGHT);
		}
		return gtMap;
	}
	
	public static ProbabilityMap loadGTZombieProbabilities() {
		if (probMap == null) {
			probMap = new ProbabilityMap(false, WIDTH, HEIGHT);
			for (int i=0; i<probMap.getData().length; i++)
				probMap.getData()[i] = 1.0;
			probMap.normalize();
			// TODO load zombie probabilities
		}
		return probMap;
	}

}
