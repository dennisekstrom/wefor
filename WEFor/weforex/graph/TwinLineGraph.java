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

import feed.Provider;
import feed.BarRequester;
import feed.TimeRelativeFeed;
import feed.ITickFeedListener;
import forex.ForexException;

/**
 * This class implements a graph displaying two lines.
 * 
 * @author Dennis Ekstrom
 * @version 2012-01-30
 */
@SuppressWarnings("serial")
public class TwinLineGraph extends LineGraph implements ITickFeedListener,
		IBarFeedListener {

	private class BarReq implements BarRequester {

		final TwinLineGraph graph;
		final OfferSide offerSide;

		BarReq(TwinLineGraph graph, OfferSide offerSide) {
			this.graph = graph;
			this.offerSide = offerSide;
		}

		@Override
		public long getUpperTimeLimit() {
			return graph.getCurrentTime();
		}

		@Override
		public Instrument getInstrument() {
			return graph.controller.getInstrument();
		}

		@Override
		public Period getPeriod() {
			return graph.controller.getPeriod();
		}

		@Override
		public OfferSide getOfferSide() {
			return offerSide;
		}
	};

	private static final Color ASK_COLOR = Color.green;
	private static final Color BID_COLOR = Color.red;

	// currently displayed points
	private ArrayList<ChartPoint> displayingAskPoints;
	private ArrayList<ChartPoint> displayingBidPoints;

	private Provider askProvider, bidProvider;

	private BarSupplyHandler barSupplyHandler;

	/**
	 * Create a twin line graph.
	 * 
	 * @param controller the controller of this graph
	 * @param correspondingTimeAxis the time axis to adjust or adjust to
	 * @param correspondingRateAxis the rate axis to adjust
	 * @param feed the feed providing the graph with ticks and bars
	 * @param period the initial period of the graph
	 * @param offerSide the initial offer side of the graph
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	public TwinLineGraph(ChartController controller, TimeAxis correspondingTimeAxis,
			RateAxis correspondingRateAxis, TimeRelativeFeed feed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis, feed);

		this.askProvider = new Provider(new BarReq(this, OfferSide.ASK));
		this.bidProvider = new Provider(new BarReq(this, OfferSide.BID));

		this.displayingAskPoints = new ArrayList<ChartPoint>();
		this.displayingBidPoints = new ArrayList<ChartPoint>();

		this.barSupplyHandler = new BarSupplyHandler();
	}

	@Override
	public void setProviderHistory(IHistory history) {
		askProvider.setHistory(history);
		bidProvider.setHistory(history);
	}

	@Override
	protected Double getHighestRate() {
		// highest rate must be among ask rates

		checkAskAndBidPointsCoherency();

		if (displayingAskPoints.isEmpty())
			return null;

		double high = displayingAskPoints.get(0).rate;
		for (ChartPoint cp : displayingAskPoints)
			if (cp.rate > high)
				high = cp.rate;

		return high;
	}

	@Override
	protected Double getLowestRate() {
		// lowest rate must be among ask rates

		checkAskAndBidPointsCoherency();

		if (displayingBidPoints.isEmpty())
			return null;

		double low = displayingBidPoints.get(0).rate;
		for (ChartPoint cp : displayingBidPoints)
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
		displayingAskPoints.add(new ChartPoint(tick.getTime(), tick.getAsk()));
		displayingBidPoints.add(new ChartPoint(tick.getTime(), tick.getBid()));

		checkAskAndBidPointsCoherency();
	}

	private class BarSupplyHandler {

		Instrument instrument;
		Period period;
		IBar askBar, bidBar;

		void setBar(Instrument instrument, Period period, OfferSide offerSide, IBar bar) {

			// reset if instrument or period changed
			if (!instrument.equals(this.instrument) || !period.equals(this.period)) {
				this.instrument = instrument;
				this.period = period;
				askBar = null;
				bidBar = null;
			}

			if (offerSide.equals(OfferSide.ASK)) {

				askBar = bar;

				if (bidBar != null && askBar.getTime() != bidBar.getTime())
					bidBar = null;

			} else {

				bidBar = bar;

				if (askBar != null && askBar.getTime() != bidBar.getTime())
					askBar = null;
			}
		}

		boolean isFull() {
			return askBar != null && bidBar != null;
		}

		Long timeOfFront() {
			if (!isFull())
				return null;

			return askBar.getTime() + period.getInterval();
		}
	}

	/**
	 * Registers that a bar has been supplied.
	 */
	@Override
	public void onBar(Instrument instrument, Period period, OfferSide offerSide, IBar bar) {
		// return if incorrect offer side or period, this also excludes doing
		// stuff if this.period == Period.TICK
		if (!this.period.equals(period))
			return;

		barSupplyHandler.setBar(instrument, period, offerSide, bar);

		if (barSupplyHandler.isFull()) {

			if (inTimeRange(barSupplyHandler.timeOfFront())) {

				addToDisplayingRates(barSupplyHandler.askBar, barSupplyHandler.bidBar);

				repaint();
			}

			setTimeOfFront(barSupplyHandler.timeOfFront());
		}
	}

	private void addToDisplayingRates(IBar askBar, IBar bidBar) {
		long time = askBar.getTime() + period.getInterval();

		displayingAskPoints.add(new ChartPoint(time, askBar.getClose()));
		displayingBidPoints.add(new ChartPoint(time, bidBar.getClose()));

		checkAskAndBidPointsCoherency();
	}

	@Override
	protected final void updateDisplayingElements() {
		displayingAskPoints.clear();
		displayingBidPoints.clear();

		long startTime = getStartTime();
		long endTime = getEndTime();

		if (period.equals(Period.TICK)) {

			// use any of the providers to load ticks
			List<ITick> ticks = askProvider.getTicks(startTime, endTime);

			if (!ticks.isEmpty()) {

				if (ticks.get(0).getTime() > startTime) {
					ITick previous = askProvider.getPreviousTick(startTime);
					if (previous != null)
						addToDisplayingRates(previous);
				}

				for (ITick tick : ticks) {
					addToDisplayingRates(tick);
				}

				if (ticks.get(ticks.size() - 1).getTime() < endTime) {
					ITick upcoming = askProvider.getUpcomingTick(endTime);
					if (upcoming != null)
						addToDisplayingRates(upcoming);
				}

			} else {

				ITick previous = askProvider.getPreviousTick(startTime);
				ITick upcoming = askProvider.getUpcomingTick(endTime);

				if (previous != null && upcoming != null) {
					addToDisplayingRates(previous);
					addToDisplayingRates(upcoming);
				}
			}

		} else {

			ArrayList<IBar> askBars = askProvider.getBars(
					startTime - period.getInterval(), endTime);
			ArrayList<IBar> bidBars = bidProvider.getBars(
					startTime - period.getInterval(), endTime);

			for (int i = 0; i < askBars.size(); i++)
				addToDisplayingRates(askBars.get(i), bidBars.get(i));
		}
	}

	@Override
	protected final void drawDisplayingElements(Graphics g) {
		checkAskAndBidPointsCoherency();

		if (displayingAskPoints.size() <= 1) // no line if only one point
			return;

		// draw ask lines
		g.setColor(ASK_COLOR);
		ChartPoint cp1 = displayingAskPoints.get(0), cp2;
		for (int i = 0; i < displayingAskPoints.size(); i++) {
			cp2 = displayingAskPoints.get(i);
			drawLine(cp1, cp2, g);
			cp1 = cp2;
		}

		// draw ask lines
		g.setColor(BID_COLOR);
		cp1 = displayingBidPoints.get(0);
		for (int i = 0; i < displayingBidPoints.size(); i++) {
			cp2 = displayingBidPoints.get(i);
			drawLine(cp1, cp2, g);
			cp1 = cp2;
		}
	}

	private void drawLine(ChartPoint cp1, ChartPoint cp2, Graphics g) {
		PixelPoint p1 = chartPointToPixelPoint(cp1);
		PixelPoint p2 = chartPointToPixelPoint(cp2);

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

				ITick frontTick = askProvider.getPreviousTick(this.getCurrentTime());

				if (frontTick != null)
					setTimeOfFront(frontTick.getTime());

			} else {

				setTimeOfFront(ForexDataIO.getBarStart(period, this.getCurrentTime()));
			}

			repaint();
		}
	}

	private void checkAskAndBidPointsCoherency() {
		if (displayingAskPoints.isEmpty() && displayingBidPoints.isEmpty())
			return;

		if (displayingAskPoints.size() != displayingBidPoints.size()
				|| displayingAskPoints.get(0).time != displayingBidPoints.get(0).time
				|| displayingAskPoints.get(displayingAskPoints.size() - 1).time != displayingBidPoints
						.get(displayingBidPoints.size() - 1).time) {

			throw new ForexException(
					this.getClass().getName()
							+ "Incoherency detected between displayingAskPoints and displayingBidPoints");
		}
	}
}