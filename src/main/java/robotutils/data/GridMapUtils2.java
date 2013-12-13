package robotutils.data;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import zombieplanner.simulator.ZombieMap;

/**
 * Temporary class used until bugfix for
 * {@link GridMapUtils#fillImage(java.awt.image.BufferedImage, GridMap)}
 * is released.
 * @author tim
 *
 */
public class GridMapUtils2 {

    public static BufferedImage toImage(GridMap map) {
        if (map.dims() != 2)
            throw new IllegalArgumentException("Cannot display " + map.dims() + "-D map as image.");

        BufferedImage image = new BufferedImage(map.size(0), map.size(1), BufferedImage.TYPE_BYTE_GRAY);
        fillImage(image, map);

        return image;
    }

	public static void fillImage(BufferedImage image, GridMap map) {
        int width = Math.min(image.getWidth(), map.size(0));
        int height = Math.min(image.getHeight(), map.size(1));
        fillImage(image, map, 0, 0, width, height);
    }

	public static void fillImage(BufferedImage image, GridMap map, int x, int y, int width, int height) {
	    WritableRaster wr = image.getRaster();
	    for (int i = y; i < height; i++) {
	        for (int j = x; j < width; j++) {
	           wr.setSample(j, i, 0, map.get(j, i));
	        }
	    }
	}

}
