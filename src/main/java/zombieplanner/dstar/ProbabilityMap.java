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
    
    boolean enforceNormalization = true;
    boolean normalizationRequired = true;
    
    /**
     * Instantiate a ProbabilityMap
     */
    public ProbabilityMap() {
    	this(true);
    }

    /**
     * Instantiate a ProbabilityMap that enforces normalization. That is,
     * every time {@link #get(int...)} is called, the data is normalized if it
     * has been changed since the last get.
     * @param enforceNormalization
     */
    public ProbabilityMap(boolean enforceNormalization) {    
    	this.enforceNormalization = enforceNormalization;
    }

    public ProbabilityMap(boolean enforceNormalization, int... sizes) {
    	this(enforceNormalization);
        resize(sizes);
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
    	normalizationRequired = false;
    }
    
    /**
     * Gets whether this map is normal (probabilities sum to 1). Runs in
     * constant time.
     * @return true if probabilities sum to 1
     */
    public boolean isNormal() {
    	return !normalizationRequired;
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
        normalizationRequired = true;
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
    	if (enforceNormalization && normalizationRequired)
    		normalize();
        int i = index(idx);
        return (i >= 0) ? _map[i] : 0;
    }

    /**
     * @see StaticMap#set(byte, int...)
     */
    public void set(double val, int... idx) {
        int i = index(idx);
        if (i >= 0) _map[i] = val;
        normalizationRequired = true;
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
