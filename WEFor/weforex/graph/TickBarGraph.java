//package graph;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.beans.PropertyChangeEvent;
//import java.util.ArrayList;
//
//import chart.ChartController;
//import chart.RateAxis;
//import chart.TimeAxis;
//import chart.TimeRange;
//
//import com.dukascopy.api.ITick;
//import com.dukascopy.api.Instrument;
//import com.dukascopy.api.OfferSide;
//import com.dukascopy.api.TickBarSize;
//import com.dukascopy.api.feed.ITickBar;
//import com.dukascopy.api.feed.ITickBarFeedListener;
//
//import feed.Provider;
//import feed.ForexDataFeed;
//import feed.ITickFeedListener;
//import feed.TickBarBuilder;
//import feed.TickBarRequester;
//import forex.ForexConstants;
//
///**
// * This class implements a graph displaying bars.
// * 
// * @author Dennis Ekstrom
// * @version 2012-01-30
// */
//@SuppressWarnings("serial")
//public class TickBarGraph extends Graph implements TickBarRequester, ITickFeedListener,
//		ITickBarFeedListener {
//
//	// pixels to cut of the side of each tick bar to create space between them
//	private static final int HORIZONTAL_TICK_BAR_EDGE_CUT = 1;
//
//	// currently displayed bars
//	private ArrayList<ITickBar> displayingTickBars;
//	private TickBarBuilder buildingTickBar;
//
//	private OfferSide offerSide;
//	private TickBarSize tickBarSize;
//
//	private Provider provider;
//
//	private ITick lastTick;
//
//	/**
//	 * Create a tick bar graph.
//	 * 
//	 * @param controller the controller of this graph
//	 * @param correspondingTimeAxis the time axis to adjust or adjust to
//	 * @param correspondingRateAxis the rate axis to adjust
//	 * @param feed the feed providing the graph with ticks and bars
//	 * @param period the initial period of the graph
//	 * @param offerSide the initial offer side of the graph
//	 * @throws IllegalArgumentException if any of the arguments is null
//	 */
//	public TickBarGraph(ChartController controller, TimeAxis correspondingTimeAxis,
//			RateAxis correspondingRateAxis, ForexDataFeed feed) {
//		super(controller, correspondingTimeAxis, correspondingRateAxis, feed);
//
//		this.tickBarSize = controller.getTickBarSize();
//		this.offerSide = controller.getOfferSide();
//
//		this.provider = new Provider(this);
//		this.displayingTickBars = new ArrayList<ITickBar>();
//		this.buildingTickBar = new TickBarBuilder(offerSide);
//	}
//
//	@Override
//	protected long getDefaultTimeRange() {
//		return ForexConstants.DEFAULT_TICK_TIME_RANGE;
//	}
//
//	@Override
//	protected long getMaxTimeRange() {
//		return ForexConstants.MIN_TICK_TIME_RANGE;
//	}
//
//	@Override
//	protected long getMinTimeRange() {
//		return ForexConstants.MAX_TICK_TIME_RANGE;
//	}
//
//	@Override
//	public long getUpperTimeLimit() {
//		return this.getCurrentTime();
//	}
//
//	@Override
//	public Instrument getInstrument() {
//		return controller.getInstrument();
//	}
//
//	@Override
//	public TickBarSize getTickBarSize() {
//		return tickBarSize;
//	}
//
//	@Override
//	public OfferSide getOfferSide() {
//		return this.offerSide;
//	}
//
//	@Override
//	protected Double getHighestRate() {
//
//		Double high = null;
//
//		if (!displayingTickBars.isEmpty()) {
//
//			high = displayingTickBars.get(0).getHigh();
//
//			for (ITickBar tickBar : displayingTickBars)
//				if (tickBar.getHigh() > high)
//					high = tickBar.getHigh();
//		}
//
//		if (isBuildingTickBarOpenAndVisible()
//				&& (high == null || buildingTickBar.getHigh() > high))
//			high = buildingTickBar.getHigh();
//
//		return high;
//	}
//
//	@Override
//	protected Double getLowestRate() {
//
//		Double low = null;
//
//		if (!displayingTickBars.isEmpty()) {
//
//			low = displayingTickBars.get(0).getLow();
//			for (ITickBar tickBar : displayingTickBars)
//				if (tickBar.getLow() < low)
//					low = tickBar.getLow();
//		}
//
//		if (isBuildingTickBarOpenAndVisible()
//				&& (low == null || buildingTickBar.getLow() < low))
//			low = buildingTickBar.getLow();
//
//		return low;
//	}
//
//	/**
//	 * Updates the front bar upon tick arrival.
//	 */
//	@Override
//	public void onTick(Instrument instrument, ITick tick) {
//
//		if (buildingTickBar == null) {
//			buildingTickBar = provider.getBuildingTickBar(this.getUpperTimeLimit());
//		} else {
//			buildingTickBar.addTick(tick);
//		}
//
//		// updateGraph if building bar is visible
//		if (isBuildingTickBarOpenAndVisible())
//			repaint();
//
//		setTimeOfFront(tick.getTime());
//
//		lastTick = tick;
//	}
//
//	@Override
//	public void onBar(Instrument instrument, OfferSide offerSide,
//			TickBarSize tickBarSize, ITickBar tickBar) {
//		// ignore if wrong offer side or tick bar size
//		if (!this.offerSide.equals(offerSide) || !this.tickBarSize.equals(tickBarSize))
//			return;
//
//		buildingTickBar.reset(offerSide);
//		buildingTickBar.addTick(lastTick);
//
//		if (isTickBarVisible(tickBar)) {
//			displayingTickBars.add(tickBar);
//			repaint();
//		}
//	}
//
//	@Override
//	protected final void updateDisplayingElements() {
//		displayingTickBars = provider.getTickBars(getStartTime(), getEndTime());
//	}
//
//	private boolean isTickBarVisible(ITickBar tickBar) {
//		return tickBar != null
//				&& this.getTimeRange().overlaps(
//						new TimeRange(tickBar.getTime(), tickBar.getEndTime()));
//	}
//
//	private boolean isBuildingTickBarOpenAndVisible() {
//		return buildingTickBar != null && buildingTickBar.isOpen()
//				&& isTickBarVisible(buildingTickBar);
//	}
//
//	@Override
//	protected final void drawDisplayingElements(Graphics g) {
//		for (ITickBar bar : displayingTickBars)
//			drawTickBar(bar, g);
//
//		if (isBuildingTickBarOpenAndVisible())
//			drawTickBar(buildingTickBar, g);
//	}
//
//	/**
//	 * Draws a tick bar stick of the given characteristics on the given
//	 * graphics.
//	 */
//	private void drawTickBar(ITickBar tickBar, Graphics g) {
//
//		// y-positions
//		int open = rateToYPixelPos(tickBar.getOpen());
//		int close = rateToYPixelPos(tickBar.getClose());
//		int high = rateToYPixelPos(tickBar.getHigh());
//		int low = rateToYPixelPos(tickBar.getLow());
//
//		// x-positions
//		int start = timeToXPixelPos(tickBar.getTime());
//		int end = timeToXPixelPos(tickBar.getEndTime());
//
//		// set color
//		Color color;
//		if (tickBar.getClose() > tickBar.getOpen()) {
//			color = getBullColor();
//		} else if (tickBar.getClose() < tickBar.getOpen()) {
//			color = getBearColor();
//		} else {
//			color = getNeutralColor();
//		}
//
//		// adjust for horizontal edge cut
//		start += HORIZONTAL_TICK_BAR_EDGE_CUT;
//		end -= HORIZONTAL_TICK_BAR_EDGE_CUT;
//
//		int centerX = (start + end) / 2;
//
//		// draw
//		g.setColor(color);
//		g.drawLine(start, open, centerX, open);
//		g.drawLine(centerX, low, centerX, high);
//		g.drawLine(centerX, close, end, close);
//	}
//
//	@Override
//	public void modelPropertyChange(final PropertyChangeEvent evt) {
//		super.modelPropertyChange(evt);
//
//		if (evt.getPropertyName().equals(ChartController.TICK_BAR_SIZE_PROPERTY)) {
//
//			this.tickBarSize = (TickBarSize) evt.getNewValue();
//
//			// reset building tick bar
//			buildingTickBar = provider.getBuildingTickBar(this.getUpperTimeLimit());
//
//			repaint();
//
//		} else if (evt.getPropertyName().equals(ChartController.OFFER_SIDE_PROPERTY)) {
//
//			this.offerSide = (OfferSide) evt.getNewValue();
//
//			// reset building bar
//			buildingTickBar = provider.getBuildingTickBar(this.getUpperTimeLimit());
//
//			repaint();
//		}
//	}
// }
