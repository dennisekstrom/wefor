package chart;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

/**
 * This class implements an axis on a chart. Children of this class are either
 * vertical or horizontal, which one is determined by the alignment parameter
 * chosen at construction.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public abstract class ChartAxis extends ChartView {

	// Font size 10
	public static final Font FONT = new Font(null, Font.PLAIN, 10);
	// Indent of text, from left edge
	public static final int INDENT = 5;

	// grid that should be adjusted according to this axis
	protected ChartGrid correspondingGrid;
	protected int width; // width perpendicular to alignment
	protected Alignment alignment;

	public static enum Alignment {
		HORIZONTAL, VERTICAL
	}

	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent evt) {
			setDisplayPosition(null);
			repaint();
		}
	};

	private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
		@Override
		public void mouseMoved(MouseEvent evt) {
			// setDisplayPosition will cause repainting automatically since
			// it causes changing bounds of the display if argument isn't null
			if (alignment == Alignment.HORIZONTAL && isOnAxis(evt.getPoint().x)) {
				setDisplayPosition(evt.getPoint().x);
			} else if (alignment == Alignment.VERTICAL && isOnAxis(evt.getPoint().y)) {
				setDisplayPosition(evt.getPoint().y);
			} else {
				setDisplayPosition(null);
				repaint();
			}
		}
	};

	/**
	 * Create a chart axis with given alignment.
	 * 
	 * @param controller the controller of this axis
	 * @param correspondingGrid the grid to adjust to this axis
	 * @param width the width in pixels perpendicular to the alignment
	 * @param alignment axis along which this axis will be aligned
	 */
	protected ChartAxis(ChartController controller, ChartGrid correspondingGrid,
			int width, Alignment alignment) {
		super(controller);

		this.correspondingGrid = correspondingGrid;

		// add listeners
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseMotionListener);

		// adjust size according to width and alignment
		switch (alignment) {
		case HORIZONTAL:
			this.setPreferredSize(new Dimension(0, width));
			break;
		case VERTICAL:
			this.setPreferredSize(new Dimension(width, 0));
			break;
		}

		// set values
		this.width = width;
		this.alignment = alignment;
	}

	/**
	 * Sets the position of the center of the display on an axis.
	 * 
	 * @pixelPosAlongAxis the position
	 */
	protected abstract void setDisplayPosition(Integer pixelPosAlongAxis);

	/**
	 * Updates the display of the axis.
	 */
	protected abstract void updateDisplay();

	/**
	 * Returns true if the given position is on the axis pixel range, otherwise
	 * false.
	 * 
	 * @param pixelPosAlongAxis the position
	 * @return true if the given position is on the axis pixel range, otherwise
	 *         false
	 */
	public abstract boolean isOnAxis(int pixelPosAlongAxis);
}