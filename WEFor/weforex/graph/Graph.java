package graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.dukascopy.api.IHistory;

import chart.ChartBounds;
import chart.ChartController;
import chart.ChartView;
import chart.RateAxis;
import chart.RateRange;
import chart.TimeAxis;
import chart.TimeRange;

import feed.TimeRelativeFeed;
import forex.ForexException;

/**
 * This class implements a graph in a chart.
 * 
 * Children of this class must invoke
 * super.modelPropertyChange(PropertyChangeEvent evt) if overriding the method.
 * 
 * For the getInstance() method to work for a child of the Graph class, it must
 * have a constructor taking the following parameters in the declared order:
 * ChartController, TimeAxis, RateAxis, ForexDataFeed
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public abstract class Graph extends ChartView implements InteractiveGraph {

	/**
	 * An array with all types of graphs.
	 */
	@SuppressWarnings("unchecked")
	// @formatter:off
	public static final Class<? extends Graph>[] types = new Class[] { 
			SingleLineGraph.class,
//			TickBarGraph.class,
			CandleGraph.class,
			TwinLineGraph.class};
	// @formatter:on

	// space between top and highest high and bottom and lowest low, relative to
	// the rate difference of highest high and lowest low.
	// protected static final double RELATIVE_VERTICAL_SPACE = 0.05;
	protected static final int VERTICAL_EDGE_PIXEL_SPACE = 30;

	// private static final String TIME_OF_FRONT_PROPERTY = "TimeOfFront";

	private static final Color BULL_GREEN = new Color(0, 200, 0);
	private static final Color BEAR_RED = new Color(200, 0, 0);
	private static final Color NEUTRAL_COLOR = Color.black;

	private TimeAxis correspondingTimeAxis;
	private RateAxis correspondingRateAxis;

	private Color bullColor;
	private Color bearColor;
	private Color neutralColor;

	private boolean isRateAdjustingToFeed;
	private volatile boolean isFollowing;
	private boolean propertyChanged;

	private volatile long timeOfFront;

	// parameters at last repaint
	private long oldStartTime;
	private long oldEndTime;
	private double oldHighRate;
	private double oldLowRate;
	private Dimension oldSize;

	// listeners to be informed about changes of rateAdjustingToFeed
	private ArrayList<PropertyChangeListener> registeredListeners;

	private final TimeRelativeFeed feed;

	/**
	 * Create a graph.
	 * 
	 * @param controller the controller of this graph
	 * @param correspondingTimeAxis the time axis to adjust or adjust to
	 * @param correspondingRateAxis the rate axis to adjust
	 * @param feed the feed providing the graph with ticks and bars
	 * @param initOfferSide the initial offer side of the graph
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	protected Graph(ChartController controller, TimeAxis correspondingTimeAxis,
			RateAxis correspondingRateAxis, TimeRelativeFeed feed) {
		super(controller);
		if (controller == null || correspondingTimeAxis == null
				|| correspondingRateAxis == null || feed == null)
			throw new IllegalArgumentException("Argument can't be null.");

		this.correspondingTimeAxis = correspondingTimeAxis;
		this.correspondingRateAxis = correspondingRateAxis;

		bullColor = BULL_GREEN;
		bearColor = BEAR_RED;
		neutralColor = NEUTRAL_COLOR;

		this.feed = feed;
		this.timeOfFront = feed.getCurrentTime();

		this.registeredListeners = new ArrayList<PropertyChangeListener>();

		setAdjustRatesToFeed(true);

		// make transparent
		this.setOpaque(false);
	}

	/**
	 * Returns a graph of given type working on the same set of parameters as
	 * this graph, null if no such graph could be constructed.
	 * 
	 * @param type the type of the graph to be returned
	 * @return a graph of given type working on the same set of parameters as
	 *         this graph
	 */
	public Graph getInstance(Class<? extends Graph> type) {
		Graph graph = null;

		try {

			Constructor<? extends Graph> constructor = type.getConstructor(
					ChartController.class, TimeAxis.class, RateAxis.class,
					TimeRelativeFeed.class);

			graph = constructor.newInstance(controller, correspondingTimeAxis,
					correspondingRateAxis, feed);

		} catch (Exception e) {

			e.printStackTrace();

			return null;
		}

		// adjust following feed
		graph.setFollowingFeed(this.isFollowingFeed());

		return graph;
	}

	/**
	 * Sets whether this graph is listening to the feed or not. Listening to the
	 * feed means listening to supplies of all possible types of elements
	 * depending on which types of feed listeners this graph is an instance of.
	 * 
	 * @param listening set to true if graph should be listening to the feed,
	 *            otherwise false
	 */
	public void setListeningToFeed(boolean listening) {
		if (listening)
			feed.addListener(this);
		else
			feed.removeListener(this);
	}

	/**
	 * Supplies the provider(s) of this graph with the given history object.
	 * 
	 * @param history the history to supply the provider(s) of this graph with
	 */
	public abstract void setProviderHistory(IHistory history);

	/**
	 * Returns the highest rate currently displayed by any element of this
	 * graph, null if no rate (no element) is currently in the displayed area.
	 * 
	 * @return the highest rate currently displayed by any element of this graph
	 */
	protected abstract Double getHighestRate();

	/**
	 * Returns the lowest rate currently displayed by any element of this graph,
	 * null if no rate (no element) is currently in the displayed area.
	 * 
	 * @return the lowest rate currently displayed by any element of this graph
	 */
	protected abstract Double getLowestRate();

	/**
	 * Makes sure all that should be visible within the current time range is
	 * updated and prepared to be drawn.
	 */
	protected abstract void updateDisplayingElements();

	/**
	 * Draw the displaying elements on the given graphics.
	 * 
	 * @param g the graphics to draw on
	 */
	protected abstract void drawDisplayingElements(Graphics g);

	/**
	 * Returns the size of the default time range of this graph, that is in
	 * which range an appropriate number of elements is displayed.
	 * 
	 * @return the size of the default time range of this graph
	 */
	protected abstract long getDefaultTimeRange();

	/**
	 * Returns the size of the minimum time range of this graph, that is in
	 * which range a minimum number of elements is displayed.
	 * 
	 * @return the size of the minimum time range of this graph
	 */
	protected abstract long getMaxTimeRange();

	/**
	 * Returns the size of the maximum time range of this graph, that is in
	 * which range a maximum number of elements is displayed.
	 * 
	 * @return the size of the maximum time range of this graph
	 */
	protected abstract long getMinTimeRange();

	protected void setTimeOfFront(long timeOfFront) {
		if (timeOfFront < 0)
			throw new ForexException("timeOfFront<0(" + timeOfFront + ")");
		// else if (this.timeOfFront != null && timeOfFront < this.timeOfFront)
		// throw new ForexException("timeOfFront can't decrease");

		this.timeOfFront = timeOfFront;

		if (inTimeRange(timeOfFront)) {

			setFollowingFeed(true);

			repaint();

		} else if (isFollowingFeed()) {

			changeTimeRange(timeOfFront - getEndTime());
		}
	}

	protected long getStartTime() {
		return correspondingTimeAxis.getStartTime();
	}

	protected long getEndTime() {
		return correspondingTimeAxis.getEndTime();
	}

	/**
	 * Returns the time range of this graph.
	 * 
	 * @return the time range of this graph
	 */
	protected TimeRange getTimeRange() {
		return new TimeRange(getStartTime(), getEndTime());
	}

	protected double getHighRate() {
		return correspondingRateAxis.getHighRate();
	}

	protected double getLowRate() {
		return correspondingRateAxis.getLowRate();
	}

	/**
	 * Returns the rate range of this graph.
	 * 
	 * @return the rate range of this graph
	 */
	public RateRange getRateRange() {
		return new RateRange(getLowRate(), getHighRate());
	}

	protected boolean inTimeRange(long time) {
		return correspondingTimeAxis.inRange(time);
	}

	protected boolean inRateRange(double rate) {
		return correspondingRateAxis.inRange(rate);
	}

	protected int timeToXPixelPos(long time) {
		return correspondingTimeAxis.timeToXPixelPos(time);
	}

	protected int rateToYPixelPos(double rate) {
		return correspondingRateAxis.rateToYPixelPos(rate);
	}

	/**
	 * Returns the current time of this graph's feed.
	 * 
	 * @return the current time of this graph's feed
	 */
	protected long getCurrentTime() {
		return feed.getCurrentTime();
	}

	/**
	 * Returns the color of bullish development.
	 * 
	 * @return the color of bullish development
	 */
	protected Color getBullColor() {
		return bullColor;
	}

	/**
	 * Sets the color of bullish development
	 * 
	 * @param bullColor the color to set
	 */
	protected void setBullColor(Color bullColor) {
		this.bullColor = bullColor;
	}

	/**
	 * Returns the color of bearish development.
	 * 
	 * @return the color of bearish development
	 */
	protected Color getBearColor() {
		return bearColor;
	}

	/**
	 * Sets the color of bearish development
	 * 
	 * @param bullColor the color to set
	 */
	protected void setBearColor(Color bearColor) {
		this.bearColor = bearColor;
	}

	/**
	 * Returns the color of neutral development.
	 * 
	 * @return the color of neutral development
	 */
	protected Color getNeutralColor() {
		return neutralColor;
	}

	/**
	 * Sets the color of neutral development
	 * 
	 * @param neutralColor the color to set
	 */
	protected void setNeutralColor(Color neutralColor) {
		this.neutralColor = neutralColor;
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void addAdjustRateListener(PropertyChangeListener listener) {
		if (listener == null)
			return;

		registeredListeners.add(listener);
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void removeAdjustRateListener(PropertyChangeListener listener) {
		registeredListeners.remove(listener);
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void removeAllAdjustRateListeners() {
		registeredListeners.clear();
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public boolean isRateAdjustingToFeed() {
		return isRateAdjustingToFeed;
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void setAdjustRatesToFeed(boolean adjustRates) {
		if (isRateAdjustingToFeed == adjustRates)
			return;

		// inform listeners
		for (PropertyChangeListener pcl : registeredListeners) {
			pcl.propertyChange(new PropertyChangeEvent(this, null, isRateAdjustingToFeed,
					adjustRates));
		}

		// perform the change
		this.isRateAdjustingToFeed = adjustRates;

		// repaint to adjust graphics
		repaint();
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public boolean isFollowingFeed() {
		return isFollowing;
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void setFollowingFeed(boolean following) {
		this.isFollowing = following;
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void displayCurrent() {
		long newStartTime = timeOfFront - correspondingTimeAxis.getRangeInterval() / 2;
		long newEndTime = newStartTime + correspondingTimeAxis.getRangeInterval();

		setAdjustRatesToFeed(true);
		setFollowingFeed(true);

		changeTimeRange(newStartTime, newEndTime);
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void setBounds(ChartBounds bounds) {
		changeTimeRange(bounds.startTime, bounds.endTime);
		changeRateRange(bounds.lowRate, bounds.highRate);
	}

	/**
	 * {@inheritDoc InteractiveGraph}
	 */
	@Override
	public void zoom(final double magnification) {
		if (magnification <= 0)
			throw new IllegalArgumentException("magnification(" + magnification
					+ ") <= 0");
		else if (magnification == 1.0)
			return;

		long oldTimeRange = correspondingTimeAxis.getRangeInterval();
		long newTimeRange = (long) (oldTimeRange / magnification);

		focusAboutTime(newTimeRange, timeOfFront);

		double rateRangeDiffEachSide = 0.0;
		if (!isRateAdjustingToFeed) {
			double oldRateRange = correspondingRateAxis.getRangeInterval();
			double newRateRange = oldRateRange / magnification;

			rateRangeDiffEachSide = (newRateRange - oldRateRange) / 2;
		}

		changeRateRange(getLowRate() - rateRangeDiffEachSide, getHighRate()
				+ rateRangeDiffEachSide);
	}

	/**
	 * Focuses time range so the given time remains at the same position on the
	 * time axis. If time is not within the current time range, the new time
	 * range is set by equally adjusting both ends of the time range.
	 * 
	 * @param newTimeRange the time range to set
	 * @param time the time which to focus about
	 * @param stayInPlace the time stamp which to focus about
	 */
	protected void focusAboutTime(long newTimeRange, long time) {
		long oldTimeRange = correspondingTimeAxis.getRangeInterval();
		long startTime, endTime;

		if (getTimeRange().inRange(time)) {

			double frontRelativeRange = (double) (time - getStartTime())
					/ getTimeRange().getInterval();

			startTime = time - (long) (newTimeRange * frontRelativeRange);
			endTime = startTime + newTimeRange;

		} else {

			long timeRangeDiffEachSide = (newTimeRange - oldTimeRange) / 2;

			startTime = getStartTime() - timeRangeDiffEachSide;
			endTime = getEndTime() + timeRangeDiffEachSide;
		}

		changeTimeRange(startTime, endTime);
	}

	/**
	 * Keeps range unchanged but moves start and end time forward by
	 * timeToMoveForward.
	 * 
	 * @param timeToMoveForward the time to move forward, set to negative to
	 *            move backwards
	 */
	public void changeTimeRange(long timeToMoveForward) {
		changeTimeRange(correspondingTimeAxis.getStartTime() + timeToMoveForward,
				correspondingTimeAxis.getEndTime() + timeToMoveForward);
	}

	/**
	 * Adjusts start and end time according to given values.
	 * 
	 * @param newStartTime the start time to set
	 * @param newEndTime the end time to set
	 */
	public void changeTimeRange(long newStartTime, long newEndTime) {
		if (newStartTime >= newEndTime)
			throw new IllegalArgumentException("newStartTime(" + newStartTime
					+ ") >= newEndTime(" + newEndTime + ")");

		if (newEndTime > correspondingTimeAxis.getEndTime()) {
			// change end time first since end time is increasing
			((ChartController) controller).changeEndTime(newEndTime);
			((ChartController) controller).changeStartTime(newStartTime);
		} else {
			// change start time first since end time is decreasing
			((ChartController) controller).changeStartTime(newStartTime);
			((ChartController) controller).changeEndTime(newEndTime);
		}

		if (inTimeRange(timeOfFront)) {
			setFollowingFeed(true);
		} else {
			setFollowingFeed(false);
		}
	}

	/**
	 * Adjusts high and low rate according to given values.
	 * 
	 * @param newLowRate the low rate to set
	 * @param newHighRate the high rate to set
	 */
	public void changeRateRange(double newLowRate, double newHighRate) {
		if (newLowRate >= newHighRate)
			throw new IllegalArgumentException("newLowRate(" + newLowRate
					+ ") >= newHighRate(" + newHighRate + ")");

		if (newHighRate > correspondingRateAxis.getHighRate()) {
			// change high rate first since high rate is increasing
			((ChartController) controller).changeHighRate(newHighRate);
			((ChartController) controller).changeLowRate(newLowRate);
		} else {
			// change low rate first since high rate is decreasing
			((ChartController) controller).changeLowRate(newLowRate);
			((ChartController) controller).changeHighRate(newHighRate);
		}
	}

	/**
	 * Adjusts the highRate and lowRate of this graph appropriately according to
	 * displaying elements.
	 */
	private void adjustHighAndLowRates() {
		if (!isRateAdjustingToFeed())
			return;

		Double highRate = getHighestRate();
		Double lowRate = getLowestRate();

		if (highRate == null || lowRate == null)
			return;

		double marginRelativeToGraph = (double) VERTICAL_EDGE_PIXEL_SPACE
				/ (this.getHeight() - 2 * VERTICAL_EDGE_PIXEL_SPACE);

		double verticalRateMargin;
		if (marginRelativeToGraph <= 0)
			return;

		if (lowRate >= highRate) {
			verticalRateMargin = correspondingRateAxis.getRangeInterval() / 2;
		} else {
			verticalRateMargin = (highRate - lowRate) * marginRelativeToGraph;
		}

		changeRateRange(lowRate - verticalRateMargin, highRate + verticalRateMargin);
	}

	/**
	 * This works since paintComponent for a parent is invoked before
	 * paintComponent of children.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (propertyChanged || graphicsDependentParametersChanged()) {

			if (getTimeRange().getInterval() < getMinTimeRange())
				focusAboutTime(getMinTimeRange(), timeOfFront);
			else if (getTimeRange().getInterval() > getMaxTimeRange())
				focusAboutTime(getMaxTimeRange(), timeOfFront);

			updateDisplayingElements();

			propertyChanged = false;
		}

		adjustHighAndLowRates();
		drawDisplayingElements(g);

		updateOldParameters();
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {

		propertyChanged = true;
	}

	protected boolean graphicsDependentParametersChanged() {
		// @formatter:off
		return getStartTime() != oldStartTime 
				|| getEndTime() != oldEndTime 
				|| getHighRate() != oldHighRate
				|| getLowRate() != oldLowRate
				|| !getSize().equals(oldSize);
		// @formatter:on
	}

	private void updateOldParameters() {
		oldStartTime = getStartTime();
		oldEndTime = getEndTime();
		oldHighRate = getHighRate();
		oldLowRate = getLowRate();
		oldSize = getSize();
	}

	/**
	 * Disables this graph from communicating with other parts of the program.
	 */
	public void destroy() {
		this.setListeningToFeed(false);
		controller.removeView(this);
	}
}