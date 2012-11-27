package chart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 * This class implements a cross shown on top of the graph in the chart view.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class MousePositionCross extends JPanel {
	private static final Color lineColor = Color.GRAY;

	private PixelPoint intersection;

	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent evt) {
			setCross(null);

			repaint();
		}
	};

	private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
		@Override
		public void mouseMoved(MouseEvent evt) {
			setCross(new PixelPoint(evt.getPoint()));

			repaint();
		}
	};

	/**
	 * Create a mouse position cross.
	 */
	MousePositionCross() {

		// add listeners
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseMotionListener);

		// make transparent
		this.setOpaque(false);
	}

	/**
	 * Set the intersection point of the cross.
	 * 
	 * @param intersection point where the lines should intersect
	 */
	public void setCross(PixelPoint intersection) {
		this.intersection = intersection;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (intersection == null)
			return;

		g.setColor(lineColor);

		g.drawLine(intersection.x, 0, intersection.x, this.getHeight());
		g.drawLine(0, intersection.y, this.getWidth(), intersection.y);
	}
}