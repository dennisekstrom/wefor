package feed;

import com.dukascopy.api.ITick;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.feed.ITickBar;

import forex.ForexTools;
import forex.TickBar;

/**
 * Builds an ask and bid tick bar simultaneously, using ticks for continuous
 * construction.
 * 
 * @author Dennis Ekstrom
 */
public class TickBarBuilder implements ITickBar {

	private Double open;
	private Double close;
	private Double high;
	private Double low;
	private Double volume;
	private Long time;
	private Long endTime;
	private long formedElementsCount;

	private OfferSide offerSide;

	public TickBarBuilder(OfferSide offerSide) {
		reset(offerSide);
	}

	/**
	 * Adds a tick to this tick bar builder. Tick bar parameters are adjusted
	 * accordingly.
	 * 
	 * @param tick the tick to add
	 */
	public void addTick(ITick tick) {
		long time = tick.getTime();
		double rate = getRate(offerSide, tick);
		double volume = getVolume(offerSide, tick);

		if (!isOpen()) {

			this.time = time;
			this.endTime = time;
			this.high = rate;
			this.low = rate;
			this.open = rate;
			this.close = rate;
			this.volume = volume;

		} else {

			this.endTime = time;

			if (this.high < rate)
				this.high = rate;
			if (this.low > rate)
				this.low = rate;

			this.close = rate;

			this.volume += volume;
		}

		formedElementsCount++;
	}

	public boolean isOpen() {
		return open != null;
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getVolume() {
		return volume;
	}

	public long getTime() {
		checkGetter(time, "time");
		return time;
	}

	public long getEndTime() {
		checkGetter(endTime, "endTime");
		return endTime;
	}

	private void checkGetter(Long value, String description) {
		if (value == null) {
			throw new NullPointerException(description
					+ "=null as TickBarBuilder is not open");
		}
	}

	/**
	 * Returns the number of ticks that have been used to create this
	 * TickBarBuilder at its current state.
	 * 
	 * @return the number of ticks that have been used to create this
	 *         TickBarBuilder at its current state
	 */
	@Override
	public long getFormedElementsCount() {
		return formedElementsCount;
	}

	/**
	 * Produces a bar using the ticks that have been added since creation or
	 * reset. Returns the produced bar, or null if no ticks has been added since
	 * creation or reset.
	 * 
	 * @return the produced bar, or null if no ticks has been added since
	 *         creation or reset
	 */
	public ITickBar produceTickBar() {
		if (!isOpen())
			return null;

		// @formatter:off
			return new TickBar(
					time, 
					open,
					close, 
					high, 
					low, 
					volume, 
					endTime, 
					formedElementsCount);
			// @formatter:on
	}

	public void reset(OfferSide offerSide) {
		this.offerSide = offerSide;

		open = null;
		close = null;
		high = null;
		low = null;
		volume = null;
		time = null;
		formedElementsCount = 0;
	}

	/**
	 * Returns a string representation of this tick bar builder.
	 * 
	 * @return A string representation of this tick bar builder
	 */
	@Override
	public String toString() {

		String ret;

		if (isOpen()) {
			// @formatter:off
			ret =  String.format(
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
		} else {
			// @formatter:off
			ret = String.format(
					"%s: NOT OPEN",
					getClass().getSimpleName());
			// @formatter:on
		}

		return ret;
	}

	private static double getRate(OfferSide offerSide, ITick tick) {
		switch (offerSide) {
		case ASK:
			return tick.getAsk();
		case BID:
			return tick.getBid();
		default:
			throw new IllegalArgumentException("offerSide can't be null");
		}
	}

	private static double getVolume(OfferSide offerSide, ITick tick) {
		switch (offerSide) {
		case ASK:
			return tick.getAskVolume();
		case BID:
			return tick.getBidVolume();
		default:
			throw new IllegalArgumentException("offerSide can't be null");
		}
	}
}