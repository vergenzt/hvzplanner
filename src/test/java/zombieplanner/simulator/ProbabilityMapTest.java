package zombieplanner.simulator;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zombieplanner.simulator.ProbabilityMap;

public class ProbabilityMapTest {

	ProbabilityMap map;

	/* This stuff isn't strictly necessary. You can leave out this
	 * and the variable above, and just instantiate stuff in the
	 * method annotated with @Test. When you run JUnit, any methods
	 * with that annotation will run and it'll give you statistics.
	 */

	@Before public void setUp() { map = new ProbabilityMap(2, 2); }
	@After public void tearDown() { map = null; }

	@Test
	public void testProbabilityMap() {
		double eps = 0.0001;

		assertArrayEquals(new int[] {2,2}, map.sizes());
		assertEquals(map.get(0, 0), 0.0, eps);
		assertEquals(map.get(1, 0), 0.0, eps);
		assertEquals(map.get(0, 1), 0.0, eps);
		assertEquals(map.get(1, 1), 0.0, eps);
		assertFalse(map.isGuaranteedNormal());

		map.set(1.0, 0,0);
		assertEquals(map.get(0, 0), 1.0, eps);
		assertEquals(map.get(1, 0), 0.0, eps);
		assertEquals(map.get(0, 1), 0.0, eps);
		assertEquals(map.get(1, 1), 0.0, eps);
		assertFalse(map.isGuaranteedNormal());
		map.normalize();
		assertTrue(map.isGuaranteedNormal());

		map.set(2.0, 1,0);
		map.set(3.0, 0,1);
		map.set(4.0, 1,1);
		assertFalse(map.isGuaranteedNormal());
		map.normalize();
		assertTrue(map.isGuaranteedNormal());

		assertEquals(map.get(0, 0), 0.1, eps);
		assertEquals(map.get(1, 0), 0.2, eps);
		assertEquals(map.get(0, 1), 0.3, eps);
		assertEquals(map.get(1, 1), 0.4, eps);
	}
}
