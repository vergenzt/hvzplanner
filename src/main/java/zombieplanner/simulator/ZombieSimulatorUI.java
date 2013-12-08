package zombieplanner.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import robotutils.gui.MapPanel;
import zombieplanner.ResourceLoader;
import zombieplanner.planner.ZombiePlanner;
import zombieplanner.planner.ZombiePlannerImpl;
import zombieplanner.simulator.impl.GTMapGenerator;

public class ZombieSimulatorUI implements ActionListener {

	private final ZombieSimulator sim;
	private final MapPanel mp;

	public ZombieSimulatorUI(ZombieSimulator sim) throws IOException {
		// TODO handle UI exception properly
		this.sim = sim;
		final ZombieMap map = sim.getMap();
		final Rectangle2D mapBounds = new Rectangle2D.Double(0.0, 0.0, map.size(0), map.size(1));

        this.mp = new MapPanel() {
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
                } else if (button == MouseEvent.BUTTON3) {
                }
            }
        };

        BufferedImage img = ImageIO.read(ResourceLoader.getInputStream("gatech_map.png"));
//        BufferedImage img = GridMapUtils.toImage(map);
        mp.setIcon("map", img, mapBounds);
        mp.setPreferredSize(new Dimension(2*map.size(0), 2*map.size(1)));

        JFrame jf = new JFrame("Map");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(mp, BorderLayout.CENTER);
        jf.getContentPane().add(new JLabel(
                "<html>" +
                "<font color=blue>(MOUSE DRAG)</font> Pan around map / " +
                "<font color=blue>(MOUSE WHEEL)</font> Zoom in/out of map" +
                "</html>"
                ), BorderLayout.NORTH);

        JPanel panel = new JPanel();
        List<JButton> buttons = Arrays.asList(
        	new JButton("Step Once"),
        	new JButton("Play")
		);
        for (JButton button : buttons) {
        	button.addActionListener(this);
        	panel.add(button);
        }
        jf.getContentPane().add(panel, BorderLayout.SOUTH);

        jf.pack();
        jf.setVisible(true);

        mp.setView(mapBounds);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("Step Once")) {
			sim.stepOnce();
			mp.repaint();
		}
	}

	public static void main(String[] args) throws IOException {

		ZombieMap map = GTMapGenerator.loadGTMap();
		ProbabilityMap probDist = GTMapGenerator.loadGTZombieProbabilities();
		ZombiePlanner planner = new ZombiePlannerImpl();
		ZombieSimulator sim = new ZombieSimulator(map, probDist, planner);

		ZombieSimulatorUI ui = new ZombieSimulatorUI(sim);

	}

}
