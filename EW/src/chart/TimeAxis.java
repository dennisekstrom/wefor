package chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;

import util.Period;

import forex.ForexConstants;

/**
 * This class implements the time axis (horizontal axis) of the chart view.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class TimeAxis extends ChartAxis {

	// displays
	public static final int EDGE_DISPLAY = 0;
	public static final int INTERVAL_DISPLAY = 1;
	public static final int SLIDING_DISPLAY = 2;

	// width of a time axis
	public static final int TIME_AXIS_WIDTH = 14;

	// distance, in pixels, between displayed times
	private static final int PIXEL_TIME_INTERVAL = 90;
	// width, in pixels, of the year-field in south-west corner
	private static final int YEAR_FIELD_WIDTH = 33;
	// number of vertical grid lines to be displayed for each line in time axis
	private static final int GRID_LINES_PER_TIME_LINE = 3;

	// initially -1 to avoid adjustment at startup
	private int oldWidthWithoutRateAxis = -1;

	private TimeDisplay display;

	// private Period period;

	private long startTime;
	private long endTime;

	// needs to be aware of corresponding rate axis to draw line to separate
	// the actual time axis from what's under rate axis
	private RateAxis correspondingRateAxis;
	// all positions on which time lines are located
	private int[] timeLinePositions;
	// rates corresponding to the lines
	private String[] timesAtTimeLines;
	// year to be displayed on left end
	private String startTimeRepresentation = ""; // default is empty
	// year to be displayed on left end
	private String endTimeRepresentation = ""; // default is empty

	/**
	 * Create a time axis with given corresponding rate axis.
	 * 
	 * @param controller the controller of the time axis
	 * @param correspondingGrid the grid to adjust
	 * @param correspondingRateAxis the rate axis to adjust to
	 * @param initStartTime the initial start time to set
	 * @param initEndTime the initial end time to set
	 * @param initPeriod the initial period to set
	 */
	TimeAxis(ChartController controller, ChartGrid correspondingGrid,
			RateAxis correspondingRateAxis) {
		super(controller, correspondingGrid, TIME_AXIS_WIDTH, Alignment.HORIZONTAL);

		this.correspondingRateAxis = correspondingRateAxis;

		// set up display
		display = new TimeDisplay(this);
		this.setLayout(null);
		this.add(display);

		// set font
		this.setFont(FONT);

		// set inital start and end times
		setStartTime(controller.getStartTime());
		setEndTime(controller.getEndTime());

		// set border
		this.setBorder(new SideLineBorder(SideLineBorder.TOP));

		// set background
		this.setBackground(Color.WHITE);

		// set minimum size
		this.setMinimumSize(new Dimension(TIME_AXIS_WIDTH, 0));
	}

	/**
	 * Returns the start time.
	 * 
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 * 
	 * @param startTime the start time to set
	 */
	private void setStartTime(long startTime) {
		this.startTime = startTime;
		startTimeRepresentation = appropriateTimeRepresentation(startTime, EDGE_DISPLAY);
		updateDisplay();
	}

	/**
	 * Returns the end time.
	 * 
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 * 
	 * @param endTime the end time to set
	 */
	private void setEndTime(long endTime) {
		this.endTime = endTime;
		endTimeRepresentation = appropriateTimeRepresentation(endTime, EDGE_DISPLAY);
		updateDisplay();
	}

	/**
	 * Returns the time range of this time axis.
	 * 
	 * @return the time range of this time axis
	 */
	public long getRangeInterval() {
		return endTime - startTime;
	}

	@Override
	protected void setDisplayPosition(Integer pixelPosAlongAxis) {
		display.setXPos(pixelPosAlongAxis);
		updateDisplay();
	}

	@Override
	protected void updateDisplay() {
		if (display.getXPos() != null)
			display.setText(appropriateTimeRepresentation(
					xPixelPosToTime(display.getXPos()), SLIDING_DISPLAY));
	}

	@Override
	public boolean isOnAxis(int pixelPosAlongAxis) {
		return pixelPosAlongAxis < this.getWidthWithoutRateAxis();
	}

	/**
	 * @param timeLinePositions
	 * @param timesAtTimeLines
	 * @throws IllegalArgumentException if given arrays are not of equal length
	 *             i.e. timeLinePositions.length != timesAtTimeLines.length
	 */
	public void setTimeLines(int[] timeLinePositions, String[] timesAtTimeLines) {
		if (timeLinePositions.length != timesAtTimeLines.length)
			throw new IllegalArgumentException("Arrays not of equal length.");

		this.timeLinePositions = timeLinePositions;
		this.timesAtTimeLines = timesAtTimeLines;
	}

	/**
	 * Returns the x-coordinate corresponding to given time on this time axis.
	 * The returned x-position is not necessarily in the range of this time
	 * axis. To check if an x-coordinate is in the range use inRange(int
	 * xPixelPos).
	 * 
	 * @param time time to find corresponding x-coordinate for
	 * @return the x-coordinate corresponding to the given time
	 */
	public int timeToXPixelPos(long time) {
		return (int) Math.round((double) (time - startTime) / (endTime - startTime)
				* getWidthWithoutRateAxis());
	}

	/**
	 * Calculate the time corresponding to given x-coordinate on this time axis.
	 * The returned time is not necessarily in the range of this time axis. To
	 * check if a time is in the range use inRange(long time).
	 * 
	 * @param x the x-coordinate to calculate corresponding time for
	 * @return the time corresponding to the given x-coordinate
	 */
	public long xPixelPosToTime(int x) {
		return (long) (startTime + (endTime - startTime) * (double) x
				/ getWidthWithoutRateAxis());
	}

	/**
	 * Calculate the time corresponding to given x-coordinate on this time axis
	 * before last time it was resized. The returned time neither is or was not
	 * necessarily in the range of this time axis.
	 * 
	 * @param x the x-coordinate to calculate corresponding time for
	 * @return the time corresponding to the given x-coordinate
	 */
	private long oldXPixelPosToTime(int x) {
		return (long) (startTime + (endTime - startTime) * (double) x
				/ oldWidthWithoutRateAxis);
	}

	/**
	 * Returns true if the given x-coordinate is in the range of this axis,
	 * otherwise false.
	 * 
	 * @param xPixelPos the x-coordinate
	 * @return true if the given x-coordinate if in the range of this axis,
	 *         otherwise false
	 */
	public boolean inRange(int xPixelPos) {
		if (0 <= xPixelPos && xPixelPos <= this.getWidth())
			return true;

		return false;
	}

	/**
	 * Returns true if the given time is in the range of this axis, otherwise
	 * false.
	 * 
	 * @param time the time
	 * @return true if the given time is in the range of this axis, otherwise
	 *         false
	 */
	public boolean inRange(long time) {
		return startTime <= time && time <= endTime;
	}

	/**
	 * Update grid and time axis to display appropriate data.
	 */
	private void updateTimeAxisAndVerticalGridLines() {
		int timesToDisplay = (getWidthWithoutRateAxis() - YEAR_FIELD_WIDTH)
				/ PIXEL_TIME_INTERVAL;

		int gridLinesToDisplay = getWidthWithoutRateAxis() * GRID_LINES_PER_TIME_LINE
				/ PIXEL_TIME_INTERVAL;

		String[] times = new String[timesToDisplay];
		int[] timeXCoordinates = new int[timesToDisplay];
		int[] verticalGridLinesXCoordinates = new int[gridLinesToDisplay];

		int timeX = getWidthWithoutRateAxis();
		for (int i = 0; i < timesToDisplay; i++) {
			timeX -= PIXEL_TIME_INTERVAL;

			times[i] = appropriateTimeRepresentation(xPixelPosToTime(timeX),
					INTERVAL_DISPLAY);
			timeXCoordinates[i] = timeX;
		}

		double gridX = getWidthWithoutRateAxis();
		double gridXPixelDiff = (double) PIXEL_TIME_INTERVAL / GRID_LINES_PER_TIME_LINE;
		for (int i = 0; i < gridLinesToDisplay; i++) {
			gridX -= gridXPixelDiff;

			verticalGridLinesXCoordinates[i] = (int) Math.round(gridX);
		}

		// adjust grid
		correspondingGrid.setVerLines(verticalGridLinesXCoordinates);

		// set time lines
		setTimeLines(timeXCoordinates, times);
	}

	private int getWidthWithoutRateAxis() {
		return this.getWidth() - correspondingRateAxis.getWidth();
	}

	/**
	 * Returns an appropriate string representation of the given time on the
	 * time axis.
	 * 
	 * @param millisTime the time to determine appropriate representation of
	 * @param displayType the type of display to determine appropriate
	 *            representation of
	 * @return an appropriate string representation of the given time on the
	 *         time axis
	 */
	public String appropriateTimeRepresentation(long millisTime, int displayType) {

		Period period = controller.getPeriod();

		// make sure time is displayed relative CET_DST
		GregorianCalendar gc = new GregorianCalendar(ForexConstants.GMT);
		gc.setTimeInMillis(millisTime);

		if (displayType == EDGE_DISPLAY) { // just display year
			// Format: yyyy
			return String.format("%tY", gc);
		} else if (displayType == INTERVAL_DISPLAY) {
			/*if (period.compareTo(Period.S1) < 0) {
				// Format: HH:MM:SS.mmm
				return String.format("%1$tT.%1$tL", gc);
			} else */if (period.compareTo(Period.M1) < 0) {
				// Format: HH:MM:SS
				return String.format("%1$tT", gc);
			} else if (period.compareTo(Period.D1) < 0) {
				// Format: dd Mon HH:MM
				return String.format("%1$td %1$tb %1$tR", gc);
			} else {
				// Format: yyyy-mm-dd
				return String.format("%tF", gc);
			}
		} else if (displayType == SLIDING_DISPLAY) {
			/*if (period.compareTo(Period.S1) < 0) {
				// Format: dd Mon HH:MM:SS.mmm
				return String.format("%1$td %1$tb %1$tT.%1$tL", gc);
			} else */if (period.compareTo(Period.M1) < 0) {
				// Format: dd Mon HH:MM:SS
				return String.format("%1$td %1$tb %1$tT", gc);
			} else if (period.compareTo(Period.D1) < 0) {
				// Format: dd Mon HH:MM
				return String.format("%1$td %1$tb %1$tR", gc);
			} else {
				// Format: yyyy-mm-dd
				return String.format("%tF", gc);
			}
		} else {
			return null;
		}
	}

	/**
	 * Adjusts the time range of the time axis appropriately if it was resized.
	 */
	private void adjustTimeRangeOnResize() {
		if (oldWidthWithoutRateAxis != getWidthWithoutRateAxis()
				&& oldWidthWithoutRateAxis >= 0)
			((ChartController) controller)
					.changeStartTime(oldXPixelPosToTime(oldWidthWithoutRateAxis
							- getWidthWithoutRateAxis()));

		oldWidthWithoutRateAxis = getWidthWithoutRateAxis();
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ChartController.PERIOD_PROPERTY)) {

			updateTimeAxisAndVerticalGridLines();

		} else if (evt.getPropertyName().equals(ChartController.START_TIME_PROPERTY)) {

			setStartTime((Long) evt.getNewValue());

			updateTimeAxisAndVerticalGridLines();

		} else if (evt.getPropertyName().equals(ChartController.END_TIME_PROPERTY)) {

			setEndTime((Long) evt.getNewValue());

			updateTimeAxisAndVerticalGridLines();
		}
	}

	/**
	 * Paint this component. Times are displayed regarding to the time
	 * resolution that is set.
	 * 
	 * @param g
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		adjustTimeRangeOnResize();

		updateTimeAxisAndVerticalGridLines();

		// Nothing should be painted if any of these are null
		if (timeLinePositions == null || timesAtTimeLines == null)
			return;

		// draw startYear at: x = INDENT, y = this.height / 2 + font_size / 2
		g.drawString(startTimeRepresentation, INDENT,
				(this.getHeight() + FONT.getSize()) / 2);

		// draw line to separate space that's under the rate axis
		int lineXPos = this.getWidthWithoutRateAxis();
		g.drawLine(lineXPos, 0, lineXPos, this.getHeight() - 1);

		// draw startYear at: x = {right edge of string INDENT pixels from right
		// edge}, y = this.height / 2 + font_size / 2
		g.drawString(endTimeRepresentation, this.getWidth()
				- g.getFontMetrics().stringWidth(endTimeRepresentation) - INDENT,
				(this.getHeight() + FONT.getSize()) / 2);

		for (int i = 0; i < timeLinePositions.length; i++) {
			// draw a line for each element in timeLinePositions
			g.drawLine(timeLinePositions[i], 0, timeLinePositions[i],
					this.getHeight() - 1);

			// draw string at: x = INDENT, y = this.height / 2 + font_size / 2
			g.drawString(timesAtTimeLines[i], timeLinePositions[i] + INDENT,
					(this.getHeight() + FONT.getSize()) / 2);
		}
	}
}