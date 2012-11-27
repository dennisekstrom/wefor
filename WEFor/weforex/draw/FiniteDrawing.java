package draw;

import chart.ChartBounds;

public interface FiniteDrawing {

	/**
	 * Returns the smallest ChartBounds containing this drawing, null if such
	 * ChartBounds could not be determined.
	 * 
	 * @return the smallest ChartBounds containing this drawing
	 */
	public ChartBounds getChartBounds();

}