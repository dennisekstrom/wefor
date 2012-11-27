package draw;

import chart.ChartBounds;
import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

/**
 * Class for drawings attached at one point in a chart.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public abstract class OnePointDrawing extends Drawing implements FiniteDrawing {

	/**
	 * Position on chart when done drawing.
	 */
	private ChartPoint chartPosition;

	/**
	 * Pixel position during drawing.
	 */
	protected PixelPoint pixelPosition;

	/**
	 * Create a one point drawing.
	 * 
	 * @param host the hosting DrawingPanel
	 */
	protected OnePointDrawing(DrawingPanel host) {
		super(host);
	}

	/**
	 * Returns the chart position.
	 * 
	 * @return The chart position.
	 */
	public ChartPoint getChartPosition() {
		return chartPosition;
	}

	/**
	 * Returns the chart position corresponding to the given pixel point on
	 * hosting ChartPanel.
	 * 
	 * @param p Point for which to calculate chart position for.
	 * @return The chart position corresponding to the given pixel point on the
	 *         most closely related ChartPanel being a parent to this.
	 */
	public ChartPoint getChartPosition(PixelPoint p) {
		return host.getChartPoint(p);
	}
	
	/**
	 * Set the position on chart when done drawing. Can only be changed isDrawn
	 * is false.
	 * 
	 * @param chartPosition The chart position to set.
	 * @throws IllegalArgumentException if chartPosition is null.
	 */
	protected void setFinalChartPosition(ChartPoint chartPosition) {
		if (chartPosition == null)
			throw new IllegalArgumentException("chartPosition=" + chartPosition);

		this.chartPosition = chartPosition;
		
		doneDrawing();
	}
	
	@Override
	public ChartBounds getChartBounds() {
		return new ChartBounds(chartPosition, chartPosition);
	}
}