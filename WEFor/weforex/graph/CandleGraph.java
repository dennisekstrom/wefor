package graph;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import chart.ChartController;
import chart.RateAxis;
import chart.TimeAxis;
import chart.TimeRange;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IBarFeedListener;

import feed.BarBuilder;
import feed.TimeRelativeFeed;
import feed.ITickFeedListener;
import feed.Provider;
import feed.BarRequester;
import forex.ForexConstants;
import forex.ForexException;

/**
 * This class implements a graph displaying bars.
 * 
 * TODO small bug: built candles are not consistently identical to those being
 * loaded
 * 
 * TODO ingen skillnad mellan offer sides
 * 
 * @author Dennis Ekstrom
 * @version 2012-01-30
 */
@SuppressWarnings("serial")
public class CandleGraph extends Graph implements BarRequester, ITickFeedListener,
		IBarFeedListener {

	// pixels to cut of the side of each candle stick to create space between
	// them
	private static final int HORIZONTAL_CANDLE_STICK_EDGE_CUT = 1;

	// currently displayed bars
	private ArrayList<IBar> displayingBars;
	private BarBuilder buildingBar; // currently building bar

	private OfferSide offerSide;
	private Period period;

	private Provider provider;

	/**
	 * Create a candle graph.
	 * 
	 * @param controller the controller of this graph
	 * @param correspondingTimeAxis the time axis to adjust or adjust to
	 * @param correspondingRateAxis the rate axis to adjust
	 * @param feed the feed providing the graph with ticks and bars
	 * @param period the initial period of the graph
	 * @param offerSide the initial offer side of the graph
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	public CandleGraph(ChartController controller, TimeAxis correspondingTimeAxis,
			RateAxis correspondingRateAxis, TimeRelativeFeed feed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis, feed);

		this.period = controller.getPeriod();
		this.offerSide = controller.getOfferSide();

		this.provider = new Provider(this);
		this.displayingBars = new ArrayList<IBar>();
	}

	@Override
	public void setProviderHistory(IHistory history) {
		provider.setHistory(history);
	}

	@Override
	protected long getDefaultTimeRange() {
		return period.getInterval() * ForexConstants.DEFAULT_NO_BARS_IN_RANGE;
	}

	@Override
	protected long getMaxTimeRange() {
		return period.getInterval() * ForexConstants.MAX_NO_BARS_IN_RANGE;
	}

	@Override
	protected long getMinTimeRange() {
		return period.getInterval() * ForexConstants.MIN_NO_BARS_IN_RANGE;
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

		Double high = null;

		if (!displayingBars.isEmpty()) {

			high = displayingBars.get(0).getHigh();

			for (IBar bar : displayingBars)
				if (bar.getHigh() > high)
					high = bar.getHigh();
		}

		if (isBuildingBarOpenAndVisible()
				&& (high == null || buildingBar.getHigh() > high))
			high = buildingBar.getHigh();

		return high;
	}

	@Override
	protected Double getLowestRate() {

		Double low = null;

		if (!displayingBars.isEmpty()) {

			low = displayingBars.get(0).getLow();
			for (IBar bar : displayingBars)
				if (bar.getLow() < low)
					low = bar.getLow();
		}

		if (isBuildingBarOpenAndVisible() && (low == null || buildingBar.getLow() < low))
			low = buildingBar.getLow();

		return low;
	}

	/**
	 * Updates the front bar upon tick arrival.
	 */
	@Override
	public void onTick(Instrument instrument, ITick tick) {

		if (buildingBar == null) {

			updateBuildingBar();

		} else {

			buildingBar.addTick(tick);
		}

		// repaint if building bar is visible
		if (isBuildingBarOpenAndVisible())
			repaint();
	}

	private void updateBuildingBar() {
		buildingBar = provider.getBuildingBar(this.getUpperTimeLimit());

		if (buildingBar != null)
			setTimeOfFront(buildingBar.getTime() + period.getInterval());
	}

	/**
	 * Registers that a bar has been supplied and starts building a new bar.
	 */
	@Override
	public void onBar(Instrument instrument, Period period, OfferSide offerSide, IBar bar) {
		// ignore if wrong offer side or period
		if (!this.offerSide.equals(offerSide) || !this.period.equals(period))
			return;

		buildingBar = new BarBuilder(offerSide, bar.getTime() + period.getInterval());

		if (isBarVisible(bar)) {
			displayingBars.add(bar);
			repaint();
		}

		setTimeOfFront(buildingBar.getTime() + period.getInterval());
	}

	@Override
	protected final void updateDisplayingElements() {
		displayingBars = provider.getBars(getStartTime() - period.getInterval(),
				getEndTime());
	}

	private boolean isBarVisible(IBar bar) {
		return bar != null
				&& this.getTimeRange()
						.overlaps(
								new TimeRange(bar.getTime(), bar.getTime()
										+ period.getInterval()));
	}

	private boolean isBuildingBarOpenAndVisible() {
		return buildingBar != null && buildingBar.isOpen() && isBarVisible(buildingBar);
	}

	@Override
	protected final void drawDisplayingElements(Graphics g) {
		for (IBar bar : displayingBars)
			drawCandleStick(bar, g);

		if (isBuildingBarOpenAndVisible())
			drawCandleStick(buildingBar, g);
	}

	/**
	 * Draws a candle stick of the given characteristics on the given graphics.
	 */
	private void drawCandleStick(IBar bar, Graphics g) {

		// y-positions
		int open = rateToYPixelPos(bar.getOpen());
		int close = rateToYPixelPos(bar.getClose());
		int high = rateToYPixelPos(bar.getHigh());
		int low = rateToYPixelPos(bar.getLow());

		// x-positions
		int start = timeToXPixelPos(bar.getTime());
		int end = timeToXPixelPos(bar.getTime() + period.getInterval());

		Color color;
		if (bar.getClose() > bar.getOpen()) {
			color = getBullColor();
		} else if (bar.getClose() < bar.getOpen()) {
			color = getBearColor();
		} else {
			color = getNeutralColor();
		}

		// adjust for horizontal edge cut
		start += HORIZONTAL_CANDLE_STICK_EDGE_CUT;
		end -= HORIZONTAL_CANDLE_STICK_EDGE_CUT;

		int centerX = (start + end) / 2;

		// draw edges
		g.setColor(Color.BLACK);
		g.drawRect(start, Math.min(open, close), end - start, Math.abs(open - close));
		g.drawLine(centerX, Math.min(open, close), centerX, high);
		g.drawLine(centerX, Math.max(open, close), centerX, low);

		// fill
		g.setColor(color);
		g.fillRect(start + 1, Math.min(open, close) + 1, end - start - 1,
				Math.abs(open - close) - 1);
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		super.modelPropertyChange(evt);

		if (evt.getPropertyName().equals(ChartController.PERIOD_PROPERTY)) {

			Period p = (Period) evt.getNewValue();

			if (p.equals(Period.TICK))
				throw new ForexException("CandleGraph unable to work with Period.TICK");

			this.period = p;

			focusAboutTime(getDefaultTimeRange(), getCurrentTime());

			updateBuildingBar();

			repaint();

		} else if (evt.getPropertyName().equals(ChartController.OFFER_SIDE_PROPERTY)) {

			this.offerSide = (OfferSide) evt.getNewValue();

			updateBuildingBar();

			repaint();
		}
	}
}