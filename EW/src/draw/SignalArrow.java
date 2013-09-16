package draw;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.IOException;

import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

/**
 * Super class for up and down signal arrows. Implements properties which both
 * types of signal arrows have incommon.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public abstract class SignalArrow extends OnePointDrawing {

	private PixelPoint arrowTipPoint;
	private DrawingListener drawingListener = new DrawingListener() {

		@Override
		public void mousePressed(MouseEvent e) {
			setFinalChartPosition(getChartPosition(new PixelPoint(e.getPoint())));

			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			pixelPosition = new PixelPoint(e.getPoint());

			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			pixelPosition = null;

			repaint();
		}
	};

	/**
	 * Create an indicator arrow that is not yet drawn.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param arrowTipPoint the point of the arrow tip relative upper left
	 *            corner of this drawing
	 * @param arrowType Constants for arrowType defined in this class
	 */
	protected SignalArrow(DrawingPanel host, PixelPoint arrowTipPoint) throws IOException {
		super(host);

		this.arrowTipPoint = arrowTipPoint;
	}

	/**
	 * Create an indicator arrow on a specific chart position.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param chartPosition The position of the tip of the arrow.
	 * @param arrowTipPoint the point of the arrow tip relative upper left
	 *            corner of this drawing
	 * @param arrowType Constants for arrowType defined in this class
	 */
	protected SignalArrow(DrawingPanel host, ChartPoint chartPosition,
			PixelPoint arrowTipPoint) throws IOException {
		super(host);

		this.arrowTipPoint = arrowTipPoint;

		setFinalChartPosition(chartPosition);
	}

	protected abstract Image getImage();

	/**
	 * Returns the pixel position of the upper left corner for arrow to be
	 * pointing at pp.
	 * 
	 * @param pp The pixel point on which the arrow should point.
	 * @return The pixel position of the upper left corner for arrow to be
	 *         pointing at pp.
	 */
	protected PixelPoint getPixelPosition(PixelPoint pp) {
		return new PixelPoint(pp.x - arrowTipPoint.x, pp.y - arrowTipPoint.y);
	}

	@Override
	protected PixelPoint getPixelPosition(ChartPoint cp) {
		PixelPoint upperLeftPoint = super.getPixelPosition(cp);

		return new PixelPoint(upperLeftPoint.x - arrowTipPoint.x, upperLeftPoint.y
				- arrowTipPoint.y);
	}

	@Override
	public void paintFinal(Graphics g) {
		PixelPoint p = getPixelPosition(getChartPosition());

		g.drawImage(getImage(), p.x, p.y, this);
	}

	@Override
	public void paintWhileDrawing(Graphics g) {
		if (pixelPosition == null)
			return;

		PixelPoint p = getPixelPosition(pixelPosition);

		g.drawImage(getImage(), p.x, p.y, this);
	}

	@Override
	public DrawingListener getDrawingListener() {
		return drawingListener;
	}
}