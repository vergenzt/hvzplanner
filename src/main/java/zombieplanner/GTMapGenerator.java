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
			probMap = getUniformProbabilityMap(WIDTH, HEIGHT);
			// TODO load zombie probabilities
		}
		return probMap;
	}

	/**
	 * Get a ProbabilityMap where every cell has equal probability.
	 * @param width
	 * @param height
	 * @return a normalized equal-weighted probability map
	 */
	public static ProbabilityMap getUniformProbabilityMap(int width, int height) {
		ProbabilityMap probMap = new ProbabilityMap(false, WIDTH, HEIGHT);
		for (int i=0; i<probMap.getData().length; i++)
			probMap.getData()[i] = 1.0;
		probMap.normalize();
		return probMap;
	}

	/**
	 * Get a ProbabilityMap where probability is proportional to Manhatten distance
	 * from the center point.
	 * @param width
	 * @param height
	 * @return a normalized centralized probability map
	 */
	public static ProbabilityMap getCentralizedProbabilityMap(int width, int height) {
		ProbabilityMap probMap = new ProbabilityMap(false, WIDTH, HEIGHT);
		int ci = (width/2), cj = (height/2);
		for (int i=0; i<probMap.size(0); i++)
			for (int j=0; j<probMap.size(1); j++)
				probMap.set(Math.abs(i-ci) + Math.abs(j-cj), i, j);
		probMap.normalize();
		return probMap;
	}

}
