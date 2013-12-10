package zombieplanner.simulator.impl;

import robotutils.data.GridMap;
import zombieplanner.ResourceLoader;
import zombieplanner.simulator.ProbabilityMap;
import zombieplanner.simulator.StaticZombieMap;
import zombieplanner.simulator.ZombieMap;
import zombieplanner.simulator.ZombieMap.CellType;
import zombieplanner.simulator.ZombieSimulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A class to generate {@link GridMap}s and {@link ProbabilityMap}s for
 * {@link ZombieSimulator}.
 * @author Tim Vergenz
 */
public final class GTMapGenerator {

	public static final int WIDTH = 401, HEIGHT = 294;

	public static final String OBSTACLE_MAP = "gt_obstacle_map.csv";
	public static final String BUILDING_MAP = "gt_building_map.csv";
	public static final String CLEAR_MAP = "gt_clear_map.csv";
	public static final String ZOMBIE_DENSITY_MAP = "zombie_density_map.csv";

	private static ZombieMap gtMap;
	private static ProbabilityMap probMap;

	/**
	 * Get a {@link GridMap} with data from the map of GT's campus.
	 * @return a filled-in {@link GridMap}
	 */
	public static ZombieMap loadGTMap() {
		if (gtMap == null) {
			//gtMap = GridMapGenerator.createRandomMazeMap2D(WIDTH, HEIGHT);

			gtMap = new StaticZombieMap(WIDTH, HEIGHT);

			updateGTMap(gtMap, CellType.OBSTACLE, OBSTACLE_MAP);
			updateGTMap(gtMap, CellType.BUILDING, BUILDING_MAP);
			updateGTMap(gtMap, CellType.CLEAR, CLEAR_MAP);
		}
		return gtMap;
	}

	public static ProbabilityMap loadGTZombieProbabilities(double baseline) {
		if (probMap == null) {
			//probMap = getCentralizedProbabilityMap(WIDTH, HEIGHT);

			probMap = new ProbabilityMap(WIDTH, HEIGHT);

			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			String line = "";
			String delimiter = ",";

			try {
				is = ResourceLoader.getInputStream(ZOMBIE_DENSITY_MAP);
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);

				int xInd = 0;
				int yInd = 0;
				while ((line = br.readLine()) != null) {
					String[] rowEntries = line.split(delimiter);

					xInd = 0;
					for (String entry : rowEntries) {
						probMap.set(Double.parseDouble(entry) + baseline, xInd, yInd);
						xInd++;
					}
					yInd++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
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
		ProbabilityMap probMap = new ProbabilityMap(WIDTH, HEIGHT);
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
		ProbabilityMap probMap = new ProbabilityMap(WIDTH, HEIGHT);
		int ci = (width/2), cj = (height/2);
		for (int i=0; i<probMap.size(0); i++)
			for (int j=0; j<probMap.size(1); j++) {
				probMap.set(Math.abs(i-ci) + Math.abs(j-cj), i, j);
			}
		probMap.normalize();
		System.out.println(probMap.get(ci, cj));
		return probMap;
	}

	/**
	 * Reduce positions in ProbabilityMap which coincide with buildings/safezones to
	 * value of '0'. Normalize updated ProbabilityMap
	 * @param probMap
	 * @param zombieMap
	 */
	public static void filterAndNormProbabilityMap(ProbabilityMap probMap, ZombieMap zombieMap) {
		for (int xInd = 0; xInd < WIDTH; xInd++) {
			for (int yInd = 0; yInd < HEIGHT; yInd++) {
				if (zombieMap.typeOf(xInd, yInd) != CellType.CLEAR) {
					probMap.set(0, xInd, yInd);
				}
			}
		}

		probMap.normalize();
	}

	/**
	 * Update ZombieMap positions, indicated by file, to be of specified CellType
	 * @param gtMap
	 * @param layer
	 * @param filename
	 */
	private static void updateGTMap(ZombieMap gtMap, CellType layer, String filename) {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String line = "";
		String delimiter = ",";

		try {

			is = ResourceLoader.getInputStream(filename);
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			int xInd = 0;
			int yInd = 0;
			while ((line = br.readLine()) != null) {
				String[] rowEntries = line.split(delimiter);

				xInd = 0;
				for (String entry : rowEntries) {
					if (Integer.parseInt(entry) == 1) {
						gtMap.set(layer, xInd, yInd);
					}
					xInd++;
				}
				yInd++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
