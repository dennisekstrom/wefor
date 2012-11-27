package forex;

import com.dukascopy.api.feed.ITickBar;

/**
 * Describes a tick bar.
 * 
 * @author Dennis Ekstrom
 */
public class TickBar extends Bar implements ITickBar {

	private long endTime;
	private long formedElementsCount;

	/**
	 * Construct a TickBar using an ITickBar
	 */
	public TickBar(ITickBar tickBar) {
		super(tickBar);
		this.endTime = tickBar.getEndTime();
		this.formedElementsCount = tickBar.getFormedElementsCount();
	}

	/**
	 * Construct a tick bar using the given parameters.
	 * 
	 * @param startTime the start time of the tick bar
	 * @param open the open rate of the tick bar
	 * @param close the close rate of the tick bar
	 * @param high the high rate of the tick bar
	 * @param low the low rate of the tick bar
	 * @param volume the volume of the tick bar
	 * @param endTime the end time of the tick bar
	 * @param formedElementsCount the number of elements (ticks) that were used
	 *            to form the tick bar
	 */
	public TickBar(long startTime, double open, double close, double high, double low,
			double volume, long endTime, long formedElementsCount) {
		super(startTime, open, close, high, low, volume);
		this.endTime = endTime;
		this.formedElementsCount = formedElementsCount;
	}

	/**
	 * Returns the endTime of the tick bar.
	 * 
	 * @return the endTime of the tick bar.
	 */
	@Override
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Returns the number of elements (ticks) that were used to form the tick
	 * bar.
	 * 
	 * @return the number of elements (ticks) that were used to form the tick
	 *         bar
	 */
	@Override
	public long getFormedElementsCount() {
		return formedElementsCount;
	}

	/**
	 * Returns a string representation of this tick bar.
	 * 
	 * @return A string representation of this tick bar
	 */
	@Override
	public String toString() {
		// @formatter:off
		return String.format(
				"%s: Start: %s  End: %s  O: %4$.4f  " + 
						"C: %5$.4f  H: %6$.4f  L: %7$.4f  V: %8$.4f  FEC: %9$d",
				getClass().getSimpleName(),
				ForexTools.getTimeRepresentation(getTime()),
				ForexTools.getTimeRepresentation(getEndTime()),
				getOpen(),
				getClose(),
				getHigh(), 
				getLow(),
				getVolume(),
				getFormedElementsCount());
		// @formatter:on
	}
}