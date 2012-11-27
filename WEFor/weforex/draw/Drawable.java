package draw;

import java.awt.Graphics;

/**
 * Describes an object which can be drawn in a chart.
 * 
 * @author Dennis Ekstrom
 *
 */
public interface Drawable {
	/**
	 * Draw on given graphics.
	 * 
	 * Invoke this method with the graphics of a DrawPanel layer.
	 * 
	 * @param g Graphics to draw onto.
	 */
	public void paintFinal(Graphics g);

	/**
	 * Performs the user driven drawing process.
	 * 
	 * Invoke this method with the graphics of a DrawingPanel.DragLayer.
	 * 
	 * @param g Graphics to draw onto.
	 */
	public void paintWhileDrawing(Graphics g);

	/**
	 * Returns true if the ChartDrawable is drawn, otherwise false.
	 * 
	 * @return true if the ChartDrawable is drawn, otherwise false
	 */
	public boolean isDrawn();

	/**
	 * Returns the DrawingListener used for user driven drawing of this.
	 * 
	 * @return the DrawingListener used for user driven drawing of this
	 */
	public DrawingListener getDrawingListener();
}