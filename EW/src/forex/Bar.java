package forex;

import util.ForexUtils;
import forex.IBar;

/**
 * Immutable class Bar, describes a bar.
 * 
 * @author Dennis Ekstrom
 */
public class Bar implements IBar {

	private double open;
	private double close;
	private double high;
	private double low;
	private long volume;
	private long time;

	/**
	 * Create a bar with given parameters.
	 */
	public Bar(long time, double open, double close, double high, double low,
			long volume) {
		this.time = time;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = volume;
	}

	/**
	 * Create a bar according to given IBar.
	 */
	public Bar(IBar bar) {
		this.high = bar.getHigh();
		this.low = bar.getLow();
		this.open = bar.getOpen();
		this.close = bar.getClose();
		this.volume = bar.getVolume();
		this.time = bar.getTime();
	}

	// /**
	// * Create a bar using given ticks.
	// *
	// * @precondition time of ticks in array are in ascending chronological
	// order
	// * @param ticks Ticks that this bar will use for construction.
	// * @param offerSide OfferSide of given ticks which to be used upon
	// creation.
	// */
	// public Bar(ITick[] ticks, OfferSide offerSide) {
	// switch (offerSide) {
	// case ASK:
	// time = ticks[0].getTime();
	// open = ticks[0].getAsk();
	// close = ticks[ticks.length - 1].getAsk();
	// high = ticks[0].getAsk();
	// low = ticks[0].getAsk();
	// volume = 0;
	// for (ITick tick : ticks) {
	// if (high < tick.getAsk())
	// high = tick.getAsk();
	// if (low > tick.getAsk())
	// low = tick.getAsk();
	//
	// volume += tick.getAskVolume();
	// }
	// break;
	// case BID:
	// time = ticks[0].getTime();
	// open = ticks[0].getBid();
	// close = ticks[ticks.length - 1].getBid();
	// high = ticks[0].getBid();
	// low = ticks[0].getBid();
	// volume = 0;
	// for (ITick tick : ticks) {
	// if (high < tick.getBid())
	// high = tick.getBid();
	// if (low > tick.getBid())
	// low = tick.getBid();
	//
	// volume += tick.getBidVolume();
	// }
	// break;
	// default:
	// throw new IllegalArgumentException("offerSide=" + offerSide);
	// }
	// }

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public double getClose() {
		return close;
	}

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public double getHigh() {
		return high;
	}

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public double getLow() {
		return low;
	}

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public double getOpen() {
		return open;
	}

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public long getVolume() {
		return volume;
	}

	/**
	 * {@inheritDoc IBar}
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * Compare this bar to another object, returns true if they're equal,
	 * otherwise false.
	 * 
	 * @return true if given object equals this bar, otherwise false
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Bar))
			return false;

		Bar bar = (Bar) o;

		// @formatter:off
		if(		this.open == bar.getOpen() &&
				this.close == bar.getClose() &&
				this.high == bar.getHigh() &&
				this.low == bar.getLow() && 
				this.volume == bar.getVolume() && 
				this.time == bar.getTime()) 
			return true;
		// @formatter:on

		return false;
	}

	/**
	 * Returns a string representation of this bar.
	 * 
	 * @return A string representation of this bar.
	 */
	@Override
	public String toString() {
		// @formatter:off
		return String.format(
				"%s:  %s  O: %3$.4f  C: %4$.4f  H: %5$.4f  L: %6$.4f  V: %7$d",
				getClass().getSimpleName(),
				ForexUtils.getTimeRepresentation(time),
				getOpen(),
				getClose(),
				getHigh(), 
				getLow(),
				getVolume());
		// @formatter:on
	}
}