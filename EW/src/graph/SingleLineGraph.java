package graph;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import chart.ChartController;
import chart.ChartPoint;
import chart.PixelPoint;
import chart.RateAxis;
import chart.TimeAxis;

import forex.IBar;
import forex.ITick;
import util.ForexUtils;
import util.Instrument;
import util.OfferSide;
import util.Period;

import feed.CachingProvider;
import feed.OfferFeed;

/**
 * This class implements a graph displaying a single line.
 * 
 * @author Dennis Ekstrom
 * @version 2012-01-30
 */
@SuppressWarnings("serial")
public class SingleLineGraph extends LineGraph {

	private NavigableSet<ChartPoint> displayingPoints;
	private CachingProvider provider;

	/**
	 * Create a single line graph.
	 * 
	 * @param controller the controller of this graph
	 * @param correspondingTimeAxis the time axis to adjust or adjust to
	 * @param correspondingRateAxis the rate axis to adjust
	 * @param feed the feed providing the graph with ticks and bars
	 * @param period the initial period of the graph
	 * @param offerSide the initial offer side of the graph
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	public SingleLineGraph(ChartController controller,
			TimeAxis correspondingTimeAxis, RateAxis correspondingRateAxis,
			OfferFeed offerFeed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis, offerFeed);

		this.provider = new CachingProvider(getInstrument(), getOfferSide(),
				getPeriod());

		this.displayingPoints = new TreeSet<ChartPoint>(
				ForexUtils.hasTimeComparator);
	}

	@Override
	protected Double getHighestRate() {
		if (displayingPoints.isEmpty())
			return null;

		double high = displayingPoints.first().rate;
		for (ChartPoint cp : displayingPoints)
			if (cp.rate > high)
				high = cp.rate;

		return high;
	}

	@Override
	protected Double getLowestRate() {
		if (displayingPoints.isEmpty())
			return null;

		double low = displayingPoints.first().rate;
		for (ChartPoint cp : displayingPoints)
			if (cp.rate < low)
				low = cp.rate;

		return low;
	}

	/**
	 * Updates the front graph upon tick arrival.
	 */
	@Override
	public void onTick(Instrument instrument, ITick tick) {
		if (!this.getPeriod().equals(Period.TICK))
			return;

		if (inTimeRange(tick.getTime())) {

			addTickToDisplayingRates(tick);

			repaint();
		}

		setTimeOfFront(tick.getTime());
	}

	private void addTickToDisplayingRates(ITick tick) {
		double rate = getOfferSide().equals(OfferSide.ASK) ? tick.getAsk()
				: tick.getBid();
		displayingPoints.add(new ChartPoint(tick.getTime(), rate));
	}

	private void addTicksToDisplayingRates(Collection<ITick> ticks) {
		for (ITick tick : ticks)
			addTickToDisplayingRates(tick);
	}

	/**
	 * Registers that a bar has been supplied.
	 */
	@Override
	public void onBar(Instrument instrument, Period period,
			OfferSide offerSide, IBar bar) {
		// return if incorrect offer side or period, this also excludes doing
		// stuff if this.period == Period.TICK
		if (!this.getOfferSide().equals(offerSide)
				|| !this.getPeriod().equals(period))
			return;

		if (inTimeRange(bar.getTime() + period.getInterval())) {

			addBarToDisplayingRates(bar);

			repaint();
		}

		setTimeOfFront(bar.getTime() + period.getInterval());
	}

	private void addBarToDisplayingRates(IBar bar) {
		long time = bar.getTime() + getPeriod().getInterval();
		displayingPoints.add(new ChartPoint(time, bar.getClose()));
	}

	private void addBarsToDisplayingRates(Collection<IBar> bars) {
		for (IBar bar : bars)
			addBarToDisplayingRates(bar);
	}

	@Override
	protected final void updateDisplayingElements() {
		long startTime = getStartTime();
		long endTime = getEndTime();

		ChartPoint start = new ChartPoint(startTime, 0.0);
		ChartPoint end = new ChartPoint(endTime, 0.0);
		NavigableSet<ChartPoint> displayingPointsInRange = displayingPoints
				.subSet(start, true, end, true);

		displayingPoints.clear();
		displayingPoints.addAll(displayingPointsInRange);

		provider.load(startTime, endTime, true, true, this);
		provider.loadPrevious(startTime);
		provider.loadUpcoming(endTime);
	}

	@Override
	protected final void drawDisplayingElements(Graphics g) {
		if (displayingPoints.size() <= 1) // no line if only one point
			return;

		ChartPoint cp1 = null;
		for (ChartPoint cp2 : displayingPoints) {
			if (cp1 != null)
				drawLine(cp1, cp2, g);
			cp1 = cp2;
		}
	}

	private void drawLine(ChartPoint cp1, ChartPoint cp2, Graphics g) {
		Color color;

		if (cp2.rate > cp1.rate)
			color = getBullColor();
		else if (cp2.rate < cp1.rate)
			color = getBearColor();
		else
			color = getNeutralColor();

		PixelPoint p1 = chartPointToPixelPoint(cp1);
		PixelPoint p2 = chartPointToPixelPoint(cp2);

		// set color
		g.setColor(color);

		// draw the line
		g.drawLine(p1.x, p1.y, p2.x, p2.y);

	}

	private PixelPoint chartPointToPixelPoint(ChartPoint cp) {
		return new PixelPoint(timeToXPixelPos(cp.time),
				rateToYPixelPos(cp.rate));
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		super.modelPropertyChange(evt);

		if (evt.getPropertyName().equals(ChartController.PERIOD_PROPERTY)) {

			provider = new CachingProvider(getInstrument(), getOfferSide(),
					getPeriod());

		} else if (evt.getPropertyName()
				.equals(ChartController.PERIOD_PROPERTY)) {

			provider = new CachingProvider(getInstrument(), getOfferSide(),
					getPeriod());

			focusAboutTime(getDefaultTimeRange(), getCenterTime());

			repaint();

		} else if (evt.getPropertyName().equals(
				ChartController.OFFER_SIDE_PROPERTY)) {

			provider = new CachingProvider(getInstrument(), getOfferSide(),
					getPeriod());

			repaint();
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {

		if (event.getPropertyName().equals(BARS_LOADED_PROPERTY)) {

			CachingProvider.Data data = (CachingProvider.Data) event
					.getNewValue();
			if (!sameProperties(data.instrument, data.offerSide, data.period))
				return;

			@SuppressWarnings("unchecked")
			NavigableSet<IBar> bars = ((NavigableSet<IBar>) data.data);
			addBarsToDisplayingRates(bars);

			repaint();

		} else if (event.getPropertyName().equals(TICKS_LOADED_PROPERTY)) {

			CachingProvider.Data data = (CachingProvider.Data) event
					.getNewValue();
			if (!sameProperties(data.instrument, data.offerSide, data.period))
				return;

			@SuppressWarnings("unchecked")
			NavigableSet<ITick> ticks = ((NavigableSet<ITick>) data.data);
			addTicksToDisplayingRates(ticks);

			repaint();
		}
	}
}