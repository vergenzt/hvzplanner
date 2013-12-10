package zombieplanner.simulator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import robotutils.data.GridMapUtils;
import robotutils.data.IntCoord;
import robotutils.gui.MapPanel;
import zombieplanner.planner.ZombiePlanner;
import zombieplanner.planner.ZombiePlannerImpl;
import zombieplanner.simulator.ZombieMap.CellType;
import zombieplanner.simulator.impl.GTMapGenerator;

public class ZombieSimulatorUI implements ActionListener {

	private final ZombieSimulator sim;
	private final MapPanel mp;

	private final JButton initialize, step, run, stop;

	public ZombieSimulatorUI(ZombieSimulator sim) throws IOException {
		this.sim = sim;
		final ZombieMap map = sim.getMap();
		final Rectangle2D mapBounds = new Rectangle2D.Double(0.0, 0.0, map.size(0), map.size(1));

        this.mp = new MapPanel() {
            @Override
            public final void onClick(double x, double y, int button, int numClicks) {
                int row = (int)x;
                int col = (int)y;

                // Ignore clicks outside the map
                if (row < 0 || row >= mapBounds.getWidth()
                        || col < 0 || col >= mapBounds.getHeight())
                    return;

                mapClicked(row, col, button, numClicks);
            }
        };

        Image mapImage = GridMapUtils.toImage(map);
        Image probDistImage = sim.getProbDist().getHeatMap();
        mp.setIcon("map", mapImage, mapBounds);
        mp.setIcon("probDist", probDistImage, mapBounds);
        mp.setPreferredSize(new Dimension(2*map.size(0), 2*map.size(1)));

        JFrame jf = new JFrame("Map");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(mp, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel(
                "<html>" +
                "<font color=red>(LEFT CLICK)</font> Set start / " +
                "<font color=red>(RIGHT CLICK)</font> Set goal <br/>" +
                "Start and goal are fixed once zombies are initialized. <br>" +
                "<font color=blue>(MOUSE DRAG)</font> Pan around map <br/> " +
                "<font color=blue>(MOUSE WHEEL)</font> Zoom in/out of map" +
                "</html>"
                ), BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
    	initialize = new JButton("Initialize Zombies");
        step = new JButton("Step");
        run = new JButton("Run");
        stop = new JButton("Stop");
        for (JButton button : Arrays.asList(initialize, step, run, stop)) {
        	button.setEnabled(false);
        	button.addActionListener(this);
        	buttonPanel.add(button);
        }
        topPanel.add(buttonPanel, BorderLayout.EAST);

        jf.getContentPane().add(topPanel, BorderLayout.NORTH);
        jf.pack();
        jf.setVisible(true);

        mp.setView(mapBounds);
	}

    public static Shape human = new RoundRectangle2D.Double(-0.5, -0.5, 1.0, 1.0, 0.5, 0.5);
//    public static Stroke humanStroke = new BasicStroke(3.5f);
    public static int r = ZombieSimulator.VIEW_RADIUS;
    public static Shape humanView = new Ellipse2D.Double(-r, -r, 2*r, 2*r);
    public static Shape goalView = new Ellipse2D.Double(-r/2, -r/2, r, r);
//    public static Shape humanView = new RoundRectangle2D.Double(-r, -r, 2*r, 2*r, 2*r, 2*r);
//    public static Stroke humanViewStroke = new BasicStroke(2.0f);

    public void mapClicked(int x, int y, int button, int numClicks) {
    	if (sim.zombies != null)
    		return;
    	if (button == MouseEvent.BUTTON1) {
    		if (sim.getMap().typeOf(x, y) == CellType.OBSTACLE)
    			return;
    		sim.setHumanPosition(new IntCoord(x, y));
    		AffineTransform xform = AffineTransform.getTranslateInstance(x+0.5, y+0.5);
			mp.setShape("human", human, xform, Color.BLUE.darker());
    		mp.setShape("humanView", humanView, xform, new Color(0,0,150,50));
    		mp.repaint();
    	}
    	else if (button == MouseEvent.BUTTON3) {
    		if (sim.getMap().typeOf(x, y) == CellType.OBSTACLE)
    			return;
    		sim.setGoalPosition(new IntCoord(x, y));
    		AffineTransform xform = AffineTransform.getTranslateInstance(x+0.5, y+0.5);
			mp.setShape("goal", human, xform, Color.GREEN.darker());
    		mp.setShape("goalView", goalView, xform, new Color(0,150,0,80));
    		mp.repaint();
    	}
    	if (sim.hasHumanAndGoal())
    		initialize.setEnabled(true);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
//		System.out.println(cmd);

		if (cmd.equals("Initialize Zombies")) {
			sim.initializeZombies();
			int i=0;
			for (IntCoord zombie : sim.zombies) {
				mp.setShape("zombie" + i, human,
						AffineTransform.getTranslateInstance(zombie.get(0)+0.5, zombie.get(1)+0.5),
						Color.ORANGE.darker());
				mp.setShape("zombieView" + i, human,
						AffineTransform.getTranslateInstance(zombie.get(0)+0.5, zombie.get(1)+0.5),
						Color.ORANGE.darker());
				i++;
			}
			initialize.setEnabled(false);

			sim.planner.initialize(sim.map, sim.probDist);
			step.setEnabled(true);
//			run.setEnabled(true);
//			stop.setEnabled(true);
		}
		else if (cmd.equals("Step")) {
			boolean gameOver = sim.stepOnce();
			int i=0;
			for (IntCoord zombie : sim.zombies) {
				mp.setShape("zombie" + i, human,
						AffineTransform.getTranslateInstance(zombie.get(0)+0.5, zombie.get(1)+0.5),
						Color.ORANGE.darker());
				mp.setShape("zombieView" + i, human,
						AffineTransform.getTranslateInstance(zombie.get(0)+0.5, zombie.get(1)+0.5),
						Color.ORANGE.darker());
				i++;
			}

			if (gameOver) {
				step.setEnabled(false);
			}
		}
		mp.repaint();
	}

	public static void main(String[] args) throws IOException {

		ZombieMap map = GTMapGenerator.loadGTMap();
		ProbabilityMap probDist = GTMapGenerator.loadGTZombieProbabilities(0.1);
		ZombiePlanner planner = new ZombiePlannerImpl();
		ZombieSimulator sim = new ZombieSimulator(map, probDist, planner);

		ZombieSimulatorUI ui = new ZombieSimulatorUI(sim);

	}

}
