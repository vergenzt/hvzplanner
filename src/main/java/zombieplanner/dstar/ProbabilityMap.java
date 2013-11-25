package zombieplanner.dstar;

import java.util.Arrays;

import robotutils.data.GridMap;
import robotutils.data.StaticMap;

/**
 * A grid map that stores a double at each location in the grid instead of a
 * byte, with the restriction that total probabilities add to 1.
 *
 * @see GridMap
 * @see StaticMap
 * @author Tim Vergenz
 */
public class ProbabilityMap {

	// TODO make GridMap generic?
	// TODO test ProbabilityMap

    double[] _map = null;
    int[] _sizes = new int[0];
    int[] _cumSizes = new int[0];
    int _length = 0;

    boolean isNormal;

    /**
     * Instantiate a ProbabilityMap
     */
    public ProbabilityMap() {
    	isNormal = false;
    }

    public ProbabilityMap(int... sizes) {
        resize(sizes);
        isNormal = false;
    }

    /**
     * Scale the probabilities so the total distribution sums to 1.
     */
    public void normalize() {
    	double sum = 0.0;
    	for (int i=0; i<_map.length; i++)
    		sum += _map[i];
    	if (sum > 0) {
	    	for (int i=0; i<_map.length; i++)
	    		_map[i] = _map[i]/sum;
    	}
    	isNormal = true;
    }

    /**
     * Gets whether this map is definitely normal (probabilities sum to 1).
     * Runs in constant time. May return false when the data actually is
     * normal, but should never return true when it is not.
     * @return true if probabilities sum to 1
     */
    public boolean isGuaranteedNormal() {
    	return isNormal;
    }

    /**
     * @see StaticMap#resize(int...)
     */
    public void resize(int... sizes) {
        _sizes = Arrays.copyOf(sizes, sizes.length);
        _cumSizes = new int[_sizes.length];

        _cumSizes[0] = 1;
        for (int i = 1; i < _sizes.length; i++) {
            _cumSizes[i] = _cumSizes[i-1] * _sizes[i-1];
        }

        _length = _cumSizes[_sizes.length - 1] * _sizes[_sizes.length - 1];
        _map = new double[_length];
        isNormal = false;
    }

    /**
     * @see StaticMap#index(int[])
     */
    protected int index(int[] idx) {
        int linIdx = 0;

        for (int i = 0; i < _sizes.length; i++) {
            if (idx[i] < 0) return -1;
            if (idx[i] >= _sizes[i]) return -1;

            linIdx += _cumSizes[i]*idx[i];
        }

        return linIdx;
    }

    /**
     * @see StaticMap#get(int...)
     */
    public double get(int... idx) {
        int i = index(idx);
        return (i >= 0) ? _map[i] : 0;
    }

    /**
     * @see StaticMap#set(byte, int...)
     */
    public void set(double val, int... idx) {
        int i = index(idx);
        if (_map[i] != val)
        	isNormal = false;
        if (i >= 0) _map[i] = val;
    }

    /**
     * @see StaticMap#length()
     */
    public int length() {
        return _length;
    }

    /**
     * @see StaticMap#size(int)
     */
    public int size(int dim) {
        return _sizes[dim];
    }

    /**
     * @see StaticMap#sizes()
     */
    public int[] sizes() {
        return Arrays.copyOf(_sizes, _sizes.length);
    }

    /**
     * @see StaticMap#dims()
     */
    public int dims() {
        return _sizes.length;
    }

    /**
     * @see StaticMap#getData()
     */
    public double[] getData() {
        return _map;
    }

}
