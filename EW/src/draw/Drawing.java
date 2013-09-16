package draw;

import java.awt.Graphics;

import javax.swing.JPanel;

import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

/**
 * This class describes a ChartDrawing.
 * 
 * Any drawing to be drawn on the chart must extend this class.
 * 
 * The methods inherited from ChartDrawable enables user driven drawing of any
 * children classes. A ChartDrawing can not override paintComponent() as it's
 * declared final by this class, and should instead implement the inherited
 * methods paintFinal() and paintWhileDrawing(). Neither can isDrawn() from
 * ChartDrawable be overridden as it's declared final in this class, instead
 * should doneDrawing() be invoked when user driven process is done and
 * isDrawn() should return false.
 * 
 * If isDrawn() return true, paintFinal() will be invoked upon repaint.
 * Otherwise paintWhilwDrawing() will be invoked.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public abstract class Drawing extends JPanel implements Drawable {

	/**
	 * An array with all types of drawings.
	 */
	@SuppressWarnings("unchecked")
	// @formatter:off
	public static final Class<? extends Drawing>[] types = new Class[] {
			UpSignalArrow.class, 
			DownSignalArrow.class, 
			SingleLineDrawing.class,
			ChanelLines.class };
	// @formatter:on

	/**
	 * Property which events fired due to changes of isDrawn will have.
	 */
	public static final String IS_DRAWN_PROPERTY = "IsDrawn";

	/**
	 * When isDrawn is true, paintFinal(Graphics) will be invoked when
	 * repainting.
	 * 
	 * When isDrawn is false, paintWhileDrawing(Graphics) will be invoked when
	 * repainting.
	 * 
	 * Initial value of false, can be set to true once, cannot reset to false.
	 */
	private boolean isDrawn = false;

	protected final DrawingPanel host;

	/**
	 * Create a chart drawing.
	 * 
	 * @param parent he hosting DrawingPanel
	 * @throws IllegalArgumentException if host is null
	 */
	protected Drawing(DrawingPanel host) {
		if (host == null)
			throw new IllegalArgumentException("ChartDrawing must have a host. host="
					+ host);

		// make transparent
		this.setOpaque(false);

		this.host = host;
	}

	/**
	 * Checks if this drawing is drawn.
	 * 
	 * @return true, is this drawing is drawn, otherwise false.
	 */
	@Override
	public final boolean isDrawn() {
		return isDrawn;
	}

	/**
	 * Invoke this when user-driven drawing is done. isDrawn() will always
	 * return true after this method is invoked.
	 */
	protected void doneDrawing() {
		isDrawn = true;

		firePropertyChange(IS_DRAWN_PROPERTY, false, true);
	}

	/**
	 * Returns the pixel position (pixels from top left corner) corresponding to
	 * the given chart point in the most closely related ChartPanel being a
	 * parent to this. Returns null if no parent is a ChartPanel.
	 * 
	 * @param cp ChartPoint for which to calculate pixel position for.
	 * @return The pixel position (pixels from top left corner) corresponding to
	 *         the given chart point in the most closely related ChartPanel
	 *         being a parent to this.
	 */
	protected PixelPoint getPixelPosition(ChartPoint cp) {
		return host.getPixelPoint(cp);
	}

	@Override
	protected final void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (isDrawn)
			paintFinal(g);
		else
			paintWhileDrawing(g);
	}
}