package chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;

import util.Instrument;

/**
 * This class implements the rate axis (vertical axis) of the chart view.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class RateAxis extends ChartAxis {

	// width of a rate axis
	public static final int RATE_AXIS_WIDTH = 53;

	// length of the line marking the shown rates
	public static final int RATE_MARKING_LINE_LENGTH = 5;

	// minimum distance, in pixels, between rate lines
	private static final int MIN_PIXEL_RATE_INTERVAL = 20;

	private RateDisplay display;

	private Instrument instrument;

	private double lowRate;

	private double highRate;

	// all positions on which rate lines are located
	private int[] rateLinePositions;
	// rates corresponding to the lines
	private String[] ratesAtRateLines;

	/**
	 * Create a rate axis.
	 * 
	 * @param controller the controller of the rate axis
	 * @param correspondingGrid the grid to adjust
	 * @param instrument the initial instrument of this rate axis
	 */
	RateAxis(ChartController controller, ChartGrid correspondingGrid) {
		super(controller, correspondingGrid, RATE_AXIS_WIDTH,
				Alignment.VERTICAL);

		this.instrument = controller.getInstrument();

		// set up display
		display = new RateDisplay(this);
		this.setLayout(null);
		this.add(display);

		// set font
		this.setFont(FONT);

		// set initial high and low rates
		lowRate = controller.getLowRate();
		highRate = controller.getHighRate();
		
		// set border
		this.setBorder(new SideLineBorder(SideLineBorder.LEFT));

		// set white background
		this.setBackground(Color.WHITE);
		
		// set minimum size
		this.setMinimumSize(new Dimension(0, RATE_AXIS_WIDTH));
		
	}

	/**
	 * Returns the low rate.
	 * 
	 * @return the low rate
	 */
	public double getLowRate() {
		return lowRate;
	}

	/**
	 * Returns the high rate.
	 * 
	 * @return the high rate
	 */
	public double getHighRate() {
		return highRate;
	}

	/**
	 * Returns the rate range of this rate axis.
	 * 
	 * @return the rate range of this rate axis
	 */
	public double getRangeInterval() {
		return highRate - lowRate;
	}

	/**
	 * Returns the instrument.
	 * 
	 * @return the instrument
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	@Override
	protected void setDisplayPosition(Integer pixelPosAlongAxis) {
		display.setYPos(pixelPosAlongAxis);
		updateDisplay();
	}

	@Override
	protected void updateDisplay() {
		if (display.getYPos() != null)
			display.setText(String.format("%.4f", yPixelPosToRate(display.getYPos())));
	}

	@Override
	public boolean isOnAxis(int pixelPosAlongAxis) {
		return pixelPosAlongAxis < this.getHeight();
	}

	/**
	 * @param rateLinePositions
	 * @param ratesAtRateLines
	 * @throws IllegalArgumentException if given arrays are not of equal length
	 *             i.e. rateLinePositions.length != ratesAtRateLines.length
	 */
	public void setRateLines(int[] rateLinePositions, String[] ratesAtRateLines) {
		if (rateLinePositions.length != ratesAtRateLines.length)
			throw new IllegalArgumentException("Arrays not of equal length.");

		this.rateLinePositions = rateLinePositions;
		this.ratesAtRateLines = ratesAtRateLines;
	}

	/**
	 * Returns the y-coordinate corresponding to given rate on this rate axis.
	 * The returned y-position is not necessarily in the range of this rate
	 * axis. To check if a y-coordinate is in the range use inRange(int
	 * yPixelPos).
	 * 
	 * @param rate rate to find corresponding y-coordinate for
	 * @return the y-coordinate corresponding to the given rate
	 */
	public int rateToYPixelPos(double rate) {
		return (int) Math.round((highRate - rate) / (highRate - lowRate)
				* this.getHeight());
	}

	/**
	 * Calculate the rate corresponding to given y-coordinate on this rate axis.
	 * The returned rate is not necessarily in the range of this rate axis. To
	 * check if a rate is in the range use inRange(double rate).
	 * 
	 * @param y the y-coordinate to calculate corresponding rate for
	 * @return the rate corresponding to the given y-coordinate
	 */
	public double yPixelPosToRate(int y) {
		return highRate - (highRate - lowRate) * (double) y / this.getHeight();
	}

	/**
	 * Returns true if the given y-coordinate is in the range of this axis,
	 * otherwise false.
	 * 
	 * @param yPixelPos the y-coordinate
	 * @return true if the given y-coordinate if in the range of this axis,
	 *         otherwise false
	 */
	public boolean inRange(int yPixelPos) {
		if (0 <= yPixelPos && yPixelPos <= this.getHeight())
			return true;

		return false;
	}

	/**
	 * Returns true if the given rate is in the range of this axis, otherwise
	 * false.
	 * 
	 * @param rate the rate
	 * @return true if the given rate is in the range of this axis, otherwise
	 *         false
	 */
	public boolean inRange(double rate) {
		if (lowRate <= rate && rate <= highRate)
			return true;

		return false;
	}

	/**
	 * Update grid and rate axis to display appropriate data.
	 */
	private void updateRateAxisAndHorizontalGridLines() {
		// lowest and highest integer pip-rates that fits in chart
		int lowPipRate = (int) Math.ceil(lowRate / instrument.getPipValue());
		int highPipRate = (int) Math.floor(highRate / instrument.getPipValue());

		// highRatePos needs to be updated first since lowRate can never be
		// higher than highRate
		int lowRatePos = rateToYPixelPos((double) lowPipRate * instrument.getPipValue());
		int highRatePos = rateToYPixelPos((double) highPipRate * instrument.getPipValue());

		// distance between rates, in pixels
		double pixelDiff = (double) (lowRatePos - highRatePos)
				/ (highPipRate - lowPipRate);

		// difference between rates to be displayed, in pips
		int pipDiff = (int) (MIN_PIXEL_RATE_INTERVAL / pixelDiff) + 1;

		// highest rate to display
		int highestRateToDisplay = highPipRate
				- ((highPipRate - lowPipRate + 1) % pipDiff) / 2;

		// number of rates to be displayed
		int ratesToDisplay = (highestRateToDisplay - lowPipRate + 1) / pipDiff + 1;

		// array with all integer pip-rates that fits in the interval
		String[] rates = new String[ratesToDisplay];

		// array with corresponding Y-coordinates for all values of pipRates
		int[] rateYCoordinates = new int[ratesToDisplay];

		double previousRate = (double) highestRateToDisplay * instrument.getPipValue();
		double rateDiff = (double) pipDiff * instrument.getPipValue();

		for (int i = 0; i < ratesToDisplay; i++) {
			// rates are represented as a 4 decimal string
			rates[i] = String.format("%.4f", previousRate);
			rateYCoordinates[i] = rateToYPixelPos(previousRate);
			previousRate -= rateDiff;
		}

		// adjust grid
		correspondingGrid.setHorLines(rateYCoordinates);

		// set rate lines
		setRateLines(rateYCoordinates, rates);
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(ChartController.INSTRUMENT_PROPERTY)) {

			instrument = (Instrument) evt.getNewValue();

			updateRateAxisAndHorizontalGridLines();

		} else if (evt.getPropertyName().equals(ChartController.LOW_RATE_PROPERTY)) {

			lowRate = (Double) evt.getNewValue();

			updateDisplay();

			updateRateAxisAndHorizontalGridLines();
		} else if (evt.getPropertyName().equals(ChartController.HIGH_RATE_PROPERTY)) {

			highRate = (Double) evt.getNewValue();

			updateDisplay();

			updateRateAxisAndHorizontalGridLines();
		}
	}

	/**
	 * Paint this component. Rates are displayed with 4 decimals, i.e. with pip
	 * accuracy. X-positions are hardcoded
	 * 
	 * @param g
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		updateRateAxisAndHorizontalGridLines();

		// Nothing should be painted if any of these are null
		if (rateLinePositions == null || ratesAtRateLines == null)
			return;

		for (int i = 0; i < rateLinePositions.length; i++) {
			g.drawLine(0, rateLinePositions[i], RATE_MARKING_LINE_LENGTH,
					rateLinePositions[i]);

			g.drawString(ratesAtRateLines[i], RATE_MARKING_LINE_LENGTH + INDENT,
					rateLinePositions[i] + FONT.getSize() / 2 - 1);
		}
	}
}