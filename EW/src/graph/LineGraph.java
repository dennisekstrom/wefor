package graph;

import chart.ChartController;
import chart.RateAxis;
import chart.TimeAxis;

import util.Period;

import feed.OfferFeed;
import forex.ForexConstants;

/**
 * This class implements a graph displaying line(s).
 * 
 * @author Dennis Ekstrom
 * @version 2012-01-30
 */
@SuppressWarnings("serial")
public abstract class LineGraph extends Graph {

	/**
	 * Create a line graph.
	 * 
	 * @param controller the controller of this graph
	 * @param correspondingTimeAxis the time axis to adjust or adjust to
	 * @param correspondingRateAxis the rate axis to adjust
	 * @param feed the feed providing the graph with ticks and bars
	 * @param period the initial period of the graph
	 * @param offerSide the initial offer side of the graph
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	public LineGraph(ChartController controller,
			TimeAxis correspondingTimeAxis, RateAxis correspondingRateAxis,
			OfferFeed offerFeed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis, offerFeed);
	}

	@Override
	protected final long getDefaultTimeRange() {
		if (getPeriod().equals(Period.TICK))
			return ForexConstants.DEFAULT_TICK_TIME_RANGE;
		else
			return getPeriod().getInterval()
					* ForexConstants.DEFAULT_NO_BARS_IN_RANGE;
	}

	@Override
	protected final long getMaxTimeRange() {
		if (getPeriod().equals(Period.TICK))
			return ForexConstants.MAX_TICK_TIME_RANGE;
		else
			return getPeriod().getInterval()
					* ForexConstants.MAX_NO_BARS_IN_RANGE;
	}

	@Override
	protected final long getMinTimeRange() {
		if (getPeriod().equals(Period.TICK))
			return ForexConstants.MIN_TICK_TIME_RANGE;
		else
			return getPeriod().getInterval()
					* ForexConstants.MIN_NO_BARS_IN_RANGE;
	}
}