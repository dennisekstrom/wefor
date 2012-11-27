package graph;

import io.ForexDataIO;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import chart.ChartController;
import chart.ChartPoint;
import chart.PixelPoint;
import chart.RateAxis;
import chart.TimeAxis;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IBarFeedListener;

import feed.TimeRelativeFeed;
import feed.Provider;
import feed.BarRequester;
import feed.ITickFeedListener;

/**
 * This class implements a graph displaying a single line.
 * 
 * @author Dennis Ekstrom
 * @version 2012-01-30
 */
@SuppressWarnings("serial")
public class SingleLineGraph extends LineGraph implements BarRequester,
		ITickFeedListener, IBarFeedListener {

	// currently displayed points
	private ArrayList<ChartPoint> displayingPoints;

	private OfferSide offerSide;

	private Provider provider;

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
	public SingleLineGraph(ChartController controller, TimeAxis correspondingTimeAxis,
			RateAxis correspondingRateAxis, TimeRelativeFeed feed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis, feed);

		this.offerSide = controller.getOfferSide();

		this.provider = new Provider(this);
		this.displayingPoints = new ArrayList<ChartPoint>();
	}

	@Override
	public void setProviderHistory(IHistory history) {
		provider.setHistory(history);
	}

	@Override
	public long getUpperTimeLimit() {
		return this.getCurrentTime();
	}

	@Override
	public Instrument getInstrument() {
		return controller.getInstrument();
	}

	@Override
	public Period getPeriod() {
		return period;
	}

	@Override
	public OfferSide getOfferSide() {
		return offerSide;
	}

	@Override
	protected Double getHighestRate() {
		if (displayingPoints.isEmpty())
			return null;

		double high = displayingPoints.get(0).rate;
		for (ChartPoint cp : displayingPoints)
			if (cp.rate > high)
				high = cp.rate;

		return high;
	}

	@Override
	protected Double getLowestRate() {
		if (displayingPoints.isEmpty())
			return null;

		double low = displayingPoints.get(0).rate;
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
		if (!this.period.equals(Period.TICK))
			return;

		if (inTimeRange(tick.getTime())) {

			addToDisplayingRates(tick);

			repaint();
		}

		setTimeOfFront(tick.getTime());
	}

	private void addToDisplayingRates(ITick tick) {
		double rate = offerSide.equals(OfferSide.ASK) ? tick.getAsk() : tick.getBid();
		displayingPoints.add(new ChartPoint(tick.getTime(), rate));
	}

	/**
	 * Registers that a bar has been supplied.
	 */
	@Override
	public void onBar(Instrument instrument, Period period, OfferSide offerSide, IBar bar) {
		// return if incorrect offer side or period, this also excludes doing
		// stuff if this.period == Period.TICK
		if (!this.offerSide.equals(offerSide) || !this.period.equals(period))
			return;

		if (inTimeRange(bar.getTime() + period.getInterval())) {

			addToDisplayingRates(bar);

			repaint();
		}

		setTimeOfFront(bar.getTime() + period.getInterval());
	}

	private void addToDisplayingRates(IBar bar) {
		long time = bar.getTime() + period.getInterval();
		displayingPoints.add(new ChartPoint(time, bar.getClose()));
	}

	@Override
	protected final void updateDisplayingElements() {
		displayingPoints.clear();

		long startTime = getStartTime();
		long endTime = getEndTime();

		if (period.equals(Period.TICK)) {

			List<ITick> ticks = provider.getTicks(startTime, endTime);

			if (!ticks.isEmpty()) {

				if (ticks.get(0).getTime() > startTime) {
					ITick previous = provider.getPreviousTick(startTime);
					if (previous != null)
						addToDisplayingRates(previous);
				}

				for (ITick tick : ticks) {
					addToDisplayingRates(tick);
				}

				if (ticks.get(ticks.size() - 1).getTime() < endTime) {
					ITick upcoming = provider.getUpcomingTick(endTime);
					if (upcoming != null)
						addToDisplayingRates(upcoming);
				}

			} else {

				ITick previous = provider.getPreviousTick(startTime);
				ITick upcoming = provider.getUpcomingTick(endTime);

				if (previous != null && upcoming != null) {
					addToDisplayingRates(previous);
					addToDisplayingRates(upcoming);
				}
			}

		} else {

			List<IBar> bars = provider.getBars(startTime - period.getInterval(), endTime);

			for (IBar bar : bars) {
				addToDisplayingRates(bar);
			}
		}
	}

	@Override
	protected final void drawDisplayingElements(Graphics g) {
		if (displayingPoints.size() <= 1) // no line if only one point
			return;

		ChartPoint cp1 = displayingPoints.get(0), cp2;
		for (int i = 0; i < displayingPoints.size(); i++) {
			cp2 = displayingPoints.get(i);
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
		return new PixelPoint(timeToXPixelPos(cp.time), rateToYPixelPos(cp.rate));
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		super.modelPropertyChange(evt);

		if (evt.getPropertyName().equals(ChartController.PERIOD_PROPERTY)) {

			this.period = (Period) evt.getNewValue();

			focusAboutTime(getDefaultTimeRange(), getCurrentTime());

			if (period.equals(Period.TICK)) {

				ITick frontTick = provider.getPreviousTick(this.getCurrentTime());

				if (frontTick != null)
					setTimeOfFront(frontTick.getTime());

			} else {

				setTimeOfFront(ForexDataIO.getBarStart(period, this.getCurrentTime()));
			}

			repaint();

		} else if (evt.getPropertyName().equals(ChartController.OFFER_SIDE_PROPERTY)) {

			this.offerSide = (OfferSide) evt.getNewValue();

			repaint();
		}
	}
}