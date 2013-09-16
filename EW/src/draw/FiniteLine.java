package draw;

import java.awt.Graphics;

import chart.ChartBounds;
import chart.ChartDirection;
import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

public class FiniteLine extends Line implements FiniteDrawing {

	public FiniteLine(ChartPoint firstAttachmentPoint, ChartPoint secondAttachmentPoint) {
		super(firstAttachmentPoint, secondAttachmentPoint);
	}

	/**
	 * Sets the first attachment point of this line.
	 * 
	 * @param the attachment point to set
	 */
	public void setFirstAttachment(ChartPoint firstAttachmentPoint) {
		setAttachmentPoint(firstAttachmentPoint, 0);
	}

	/**
	 * Returns the first attachment point of this line.
	 * 
	 * @return the first attachment point of this line
	 */
	public ChartPoint getFirstAttachment() {
		return getAttachmentPoint(0);
	}

	/**
	 * Sets the second attachment point of this line.
	 * 
	 * @param the attachment point to set
	 */
	public void setSecondAttachment(ChartPoint secondAttachmentPoint) {
		setAttachmentPoint(secondAttachmentPoint, 1);
	}

	/**
	 * Returns the second attachment point of this line.
	 * 
	 * @return the second attachment point of this line
	 */
	public ChartPoint getSecondAttachment() {
		return getAttachmentPoint(1);
	}

	/**
	 * Returns the lowest rate this line covers.
	 * 
	 * @return the lowest rate this line covers
	 */
	public double getLowRate() {
		return Math.min(getAttachmentPoint(0).rate, getAttachmentPoint(1).rate);
	}

	/**
	 * Returns the highest rate this line covers.
	 * 
	 * @return the highestrate this line covers
	 */
	public double getHighRate() {
		return Math.max(getAttachmentPoint(0).rate, getAttachmentPoint(1).rate);
	}

	/**
	 * Returns the start time of this line.
	 * 
	 * @return the start time of this line
	 */
	public double getStartTime() {
		return Math.min(getAttachmentPoint(0).time, getAttachmentPoint(1).time);
	}

	/**
	 * Returns the end time of this line.
	 * 
	 * @return the end time of this line
	 */
	public double getEndTime() {
		return Math.max(getAttachmentPoint(0).time, getAttachmentPoint(1).time);
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public ChartDirection getDirection() {
		return Line.getDirectionBetween(getAttachmentPoint(0), getAttachmentPoint(1));
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public void drawOnDrawingPanel(DrawingPanel drawingPanel, Graphics g) {
		if (!isDetermined())
			return;

		PixelPoint p1 = drawingPanel.getPixelPoint(getAttachmentPoint(0));
		PixelPoint p2 = drawingPanel.getPixelPoint(getAttachmentPoint(1));

		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * Returns true if neither first attachment point or second attachment point
	 * is null, otherwise false.
	 * 
	 * @return true if neither first attachment point or second attachment point
	 *         is null, otherwise false
	 */
	@Override
	public boolean isDetermined() {
		return getAttachmentPoint(0) != null && getAttachmentPoint(1) != null;
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public FiniteLine clone() {
		return new FiniteLine(getAttachmentPoint(0), getAttachmentPoint(1));
	}

	/**
	 * {@inheritDoc FiniteDrawing}
	 */
	@Override
	public ChartBounds getChartBounds() {
		if (!isDetermined())
			return null;

		return new ChartBounds(getAttachmentPoint(0), getAttachmentPoint(1));
	}
}