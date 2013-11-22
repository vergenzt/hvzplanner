package zombieplanner;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;

import robotutils.data.Coordinate;
import robotutils.data.GridMap;
import robotutils.data.GridMapUtils;
import robotutils.data.IntCoord;
import robotutils.gui.MapPanel;
import zombieplanner.dstar.ProbabilisticGridDStar;
import zombieplanner.dstar.ProbabilityMap;

/**
 * Creates a randomized 2D map and solves a path between two random locations
 * using D* search.  Since D* is incremental, the GUI is configured to toggle
 * obstacles on left clicks and change the start location on a right click.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class ZombiePlanning {
    public static Random rnd = new Random();
    public static Shape dot = new RoundRectangle2D.Double(-0.25, -0.25, 0.5, 0.5, 0.25, 0.25);
    public static Stroke dotStroke = new BasicStroke(0.5f);
    public static AtomicBoolean needToReplan = new AtomicBoolean(true);
    private static int oldPathSize = 0;

    public static void main(String args[]) {

        // Generate a random blocky map (using cellular automata rules)
        final GridMap map = GTMapGenerator.loadGTMap();
        final ProbabilityMap probDist = GTMapGenerator.loadGTZombieProbabilities();
        final Rectangle2D mapBounds = new Rectangle2D.Double(0.0, 0.0, map.size(0), map.size(1));

        // Find an unoccupied start location
        int[] start = new int[map.dims()];
        while (map.get(start) < 0) {
            for (int i = 0; i < map.dims(); i++) {
                start[i] = rnd.nextInt(map.size(i));
            }
        }

        // Find an unoccupied goal location (that isn't the same as the start)
        int[] goal = new int[map.dims()];
        while (map.get(goal) < 0 || Arrays.equals(start, goal)) {
            for (int i = 0; i < map.dims(); i++) {
                goal[i] = rnd.nextInt(map.size(i));
            }
        }

        // Initialize D* search
        final ProbabilisticGridDStar dstar = new ProbabilisticGridDStar(
        		map, probDist, new IntCoord(start), new IntCoord(goal));

        // Create a display panel to draw the results
        final MapPanel mp = new MapPanel() {
            @Override
            public final void onClick(double x, double y, int button, int numClicks) {
                // Find the map cell that was clicked
                int row = (int)x;
                int col = (int)y;

                // Ignore clicks outside the map
                if (row < 0 || row >= mapBounds.getWidth()
                        || col < 0 || col >= mapBounds.getHeight())
                    return;

                // Determine if click was left (BUTTON1) or right (BUTTON3)
                if (button == MouseEvent.BUTTON1) {
                    // When clicked, toggle a map obstacle
                    synchronized(dstar) {
                        if (map.get(row, col) == 0) {
                            dstar.setCost(new IntCoord(row, col), (byte)255);
                        } else {
                            dstar.setCost(new IntCoord(row, col), (byte)0);
                        }
                    }

                    setIcon("map", GridMapUtils.toImage(map), mapBounds);
                } else if (button == MouseEvent.BUTTON3) {
                    // When clicked, change the start location
                    synchronized(dstar) {
                        dstar.setStart(new IntCoord(row, col));
                    }

                    setShape("Start", dot, AffineTransform.getTranslateInstance(
                        (double)row + 0.5, (double)col + 0.5), Color.GREEN, dotStroke);
                }

                needToReplan.set(true);
            }
        };
        mp.setIcon("map", GridMapUtils.toImage(map), mapBounds);
        mp.setPreferredSize(new Dimension(600,600));

        JFrame jf = new JFrame("Map");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(mp, BorderLayout.CENTER);
        jf.getContentPane().add(new JLabel(
                "<html>" +
                "<font color=red>(LEFT CLICK)</font> Toggle Obstacle / " +
                "<font color=red>(RIGHT-CLICK)</font> Change start location <p>" +
                "<font color=blue>(MOUSE DRAG)</font> Pan around map / " +
                "<font color=blue>(MOUSE WHEEL)</font> Zoom in/out of map" +
                "</html>"
                ), BorderLayout.NORTH);
        jf.pack();
        jf.setVisible(true);

        int bound = Math.max(map.size(0), map.size(1));
        mp.setView(0, bound, 0, bound);

        // Print and display start and goal locations
        System.out.println("Picked endpoints: " + Arrays.toString(start) + "->" + Arrays.toString(goal));
        mp.setShape("Start", dot, AffineTransform.getTranslateInstance(
                (double)start[0] + 0.5, (double)start[1] + 0.5), Color.GREEN, dotStroke);
        mp.setShape("Goal", dot, AffineTransform.getTranslateInstance(
                (double)goal[0] + 0.5, (double)goal[1] + 0.5), Color.RED, dotStroke);

        // Execute D* search FOREVER
        while(true) {
            if (needToReplan.getAndSet(false)) {
                synchronized(dstar) {
                    List<? extends Coordinate> path = dstar.plan();
                    drawPath(mp, path);
                }
            }
            Thread.yield();
        }
    }

    public static void drawPath(MapPanel mp, List<? extends Coordinate> path) {
        // Print and display resulting lowest cost path
        if (path.isEmpty()) {
            System.out.println("No path found!");

            for (int i = 1; i < oldPathSize - 1; i++)
                mp.removeShape("p" + i);

            oldPathSize = 0;
        } else {
            System.out.println("Solution path: " + path);

            for (int i = 1; i < path.size() - 1; i++) {
                Coordinate c = path.get(i);
                mp.setShape("p" + i, dot, AffineTransform.getTranslateInstance(
                    c.get(0) + 0.5, c.get(1) + 0.5), Color.CYAN, dotStroke);
            }

            for (int i = path.size() - 1; i < oldPathSize - 1; i++)
                mp.removeShape("p" + i);

            oldPathSize = path.size();
        }
    }
}
