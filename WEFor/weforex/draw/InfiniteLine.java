package draw;

import java.awt.Graphics;

import chart.ChartDirection;
import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

/**
 * This class describes a line in a chart. As a line is not a static image at
 * one position in the chart, its position has to be continuously calculated.
 * Linear algebraic line calculations come in handy.
 * 
 * @author Dennis Ekstrom
 */
public class InfiniteLine extends Line {

	private ChartDirection direction;

	public InfiniteLine(ChartPoint attachment, ChartDirection direction) {
		super(attachment);

		this.direction = direction;
	}

	/**
	 * @return the attachment point
	 */
	public ChartPoint getAttachment() {
		return getAttachmentPoint(0);
	}

	/**
	 * @param attachment the attachment to set
	 */
	public void setAttachment(ChartPoint attachment) {
		setAttachmentPoint(attachment, 0);
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public ChartDirection getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(ChartDirection direction) {
		this.direction = direction;
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public void drawOnDrawingPanel(DrawingPanel drawingPanel, Graphics g) {
		if (!isDetermined())
			return;

		PixelPoint p1;
		PixelPoint p2;

		if (!direction.isDetermined()) { // draw horizontal line
			p1 = drawingPanel.getPixelPoint(new ChartPoint(drawingPanel.getStartTime(),
					getAttachmentPoint(0).rate));
			p2 = drawingPanel.getPixelPoint(new ChartPoint(drawingPanel.getEndTime(),
					getAttachmentPoint(0).rate));
		} else if (direction.timeDiff == 0) { // draw vertical line
			p1 = drawingPanel.getPixelPoint(new ChartPoint(getAttachmentPoint(0).time,
					drawingPanel.getHighRate()));
			p2 = drawingPanel.getPixelPoint(new ChartPoint(getAttachmentPoint(0).time,
					drawingPanel.getLowRate()));
		} else {
			// point = getAttachmentPoint(0) + t * diff => t = (point -
			// getAttachmentPoint(0)) / diff;
			double t1 = (double) (drawingPanel.getStartTime() - getAttachmentPoint(0).time)
					/ direction.timeDiff;
			double t2 = (double) (drawingPanel.getEndTime() - getAttachmentPoint(0).time)
					/ direction.timeDiff;

			double startRate = getAttachmentPoint(0).rate + t1 * direction.rateDiff;
			double endRate = getAttachmentPoint(0).rate + t2 * direction.rateDiff;

			p1 = drawingPanel.getPixelPoint(new ChartPoint(drawingPanel.getStartTime(),
					startRate));
			p2 = drawingPanel.getPixelPoint(new ChartPoint(drawingPanel.getEndTime(),
					endRate));
		}

		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	/**
	 * Returns true if neither attachment or direction is null,
	 * otherwise false.
	 * 
	 * @return true if neither attachment or direction is null,
	 *         otherwise false
	 */
	@Override
	public boolean isDetermined() {
		return getAttachmentPoint(0) != null && direction != null;
	}

	/**
	 * {@inheritDoc Line}
	 */
	@Override
	public InfiniteLine clone() {
		return new InfiniteLine(getAttachmentPoint(0), direction);
	}
}