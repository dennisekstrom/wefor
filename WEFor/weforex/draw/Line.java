package draw;

import java.awt.Graphics;

import chart.ChartDirection;
import chart.ChartPoint;
import chart.DrawingPanel;

import forex.ForexException;

/**
 * Attachment point not allowed to be set unless all attachment points of lower
 * indices are set.
 * 
 * @author Dennis Ekstrom
 */
public abstract class Line {

	private ChartPoint[] attachmentPoints;

	protected Line(ChartPoint... attachmentPoints) {
		this.attachmentPoints = attachmentPoints;
		
		verifyAttachmentPointOrder();
	}

	private void verifyAttachmentPointOrder() {
		boolean earlierWasNull = false;

		for (ChartPoint cp : attachmentPoints) {
			if (earlierWasNull && cp != null)
				throw new ForexException(
						"illegal order of attaching points: attachmentPoints[0] is null while attachmentPoints[1] is non-null");

			if (cp == null)
				earlierWasNull = true;
		}
	}

	/**
	 * Returns the direction of this line.
	 * 
	 * @return the direction of this line
	 */
	public abstract ChartDirection getDirection();

	/**
	 * Returns true if all parameters of the line are determined, otherwise
	 * false.
	 * 
	 * @return true if all parameters of the line are determined, otherwise
	 *         false
	 */
	public abstract boolean isDetermined();

	/**
	 * Draws this line through the given DrawingPanel and on the given graphics.
	 * Nothing is drawn if this line is not determined (isDetermined() returns
	 * false).
	 * 
	 * @param drawingPanel DrawingPanel to draw line through
	 * @param g Graphics to draw on
	 */
	public abstract void drawOnDrawingPanel(DrawingPanel drawingPanel, Graphics g);

	/**
	 * Returns a clone of this line.
	 * 
	 * @return a clone of this line
	 */
	@Override
	public abstract Line clone();

	/**
	 * Returns all attachment points of this line.
	 * 
	 * @return all attachment points of this line
	 */
	public final ChartPoint[] getAttachmentPoints() {
		return attachmentPoints;
	}

	/**
	 * Returns the attachment point at specified index.
	 * 
	 * @return the attachment point at specified index
	 */
	public final ChartPoint getAttachmentPoint(int index) {
		return attachmentPoints[index];
	}

	/**
	 * Sets the attachment point at given index.
	 * 
	 * @param attachmentPoint the attachment point to set
	 * @param index the index at which to set the attachment point
	 */
	public final void setAttachmentPoint(ChartPoint attachmentPoint, int index) {
		attachmentPoints[index] = attachmentPoint;
		verifyAttachmentPointOrder();
	}

//	/**
//	 * @return the color of the line
//	 */
//	public final Color getColor() {
//		return color;
//	}
//
//	/**
//	 * @param color the color of the line to set
//	 */
//	public final void setColor(Color color) {
//		this.color = color;
//	}

	/**
	 * Returns the direction between the two given ChartPoints.
	 * 
	 * @param cp1 the first ChartPoint
	 * @param cp2 the second ChartPoint
	 * @return the direction between the two given ChartPoints
	 */
	public static final ChartDirection getDirectionBetween(ChartPoint cp1, ChartPoint cp2) {
		return new ChartDirection(cp2.time - cp1.time, cp2.rate - cp1.rate);
	}
}