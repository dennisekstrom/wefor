package feed;

import java.util.List;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.OfferSide;

import forex.ForexTools;

/**
 * Builds an ask and bid bar simultaneously, using ticks for continuous
 * construction.
 * 
 * For a bar builder to be able to produce a bar, it cannot be open. That is,
 * the time and an open rate must have been set.
 * 
 * Setting the time can be accomplished by either invoking setTime, adding a bar
 * to the bar builder, reseting the bar builder with a bar or, before any reset
 * of the bar builder, using the constructor taking a bar as parameter.
 * 
 * Setting the open rate can be accomplished by either adding a tick of a bar to
 * the bar builder, reseting the bar builder with a bar or, before any reset of
 * the bar builder, using the constructor taking a bar as parameter.
 * 
 * @author Dennis Ekstrom
 */
public class BarBuilder implements IBar {

	private Double open;
	private Double close;
	private Double high;
	private Double low;
	private Double volume;
	private long time;

	private OfferSide offerSide;

	/**
	 * Construct a bar builder with given offer side and no parameters assigned.
	 * 
	 * @param offerSide the offer side of the bar builder
	 */
	public BarBuilder(OfferSide offerSide, long time) {
		this.time = time;
		this.offerSide = offerSide;
	}

	/**
	 * Construct a bar builder with given offer side using parameters of given
	 * bar as parameters for the bar builder.
	 * 
	 * @param bar the bar of which to use parameters
	 * @param offerSide the offer side of the bar builder
	 */
	public BarBuilder(OfferSide offerSide, IBar bar) {
		this.high = bar.getHigh();
		this.low = bar.getLow();
		this.open = bar.getOpen();
		this.close = bar.getClose();
		this.volume = bar.getVolume();
		this.time = bar.getTime();

		this.offerSide = offerSide;
	}

	public boolean isOpen() {
		return open != null;
	}

	@Override
	public double getClose() {
		checkGetter(close, "close");
		return close;
	}

	@Override
	public double getHigh() {
		checkGetter(high, "high");
		return high;
	}

	@Override
	public double getLow() {
		checkGetter(low, "low");
		return low;
	}

	@Override
	public double getOpen() {
		checkGetter(open, "open");
		return open;
	}

	@Override
	public double getVolume() {
		checkGetter(volume, "volume");
		return volume;
	}

	@Override
	public long getTime() {
		return time;
	}

	private void checkGetter(Double value, String description) {
		if (value == null) {
			throw new NullPointerException(description
					+ "=null as BarBuilder is not open");
		}
	}

	/**
	 * Adds a tick to this bar builder. Bar builder parameters are adjusted
	 * accordingly.
	 * 
	 * @param tick the tick to add
	 */
	public void addTick(ITick tick) {
		double rate = getRate(offerSide, tick);
		double volume = getVolume(offerSide, tick);

		if (!isOpen()) {

			this.high = rate;
			this.low = rate;
			this.open = rate;
			this.close = rate;
			this.volume = volume;

		} else {

			if (this.high < rate)
				this.high = rate;
			if (this.low > rate)
				this.low = rate;

			this.close = rate;

			this.volume += volume;
		}
	}

	/**
	 * Adds ticks to this bar builder. Bar builder parameters are adjusted
	 * accordingly.
	 * 
	 * @param ticks the ticks to add
	 */
	public void addTicks(List<ITick> ticks) {
		for (ITick tick : ticks)
			addTick(tick);
	}

	/**
	 * Adds a bar to this bar builder. Bar builder parameters are adjusted
	 * accordingly.
	 * 
	 * @param bar the bar to add
	 */
	public void addBar(IBar bar) {
		if (!isOpen()) {

			this.high = bar.getHigh();
			this.low = bar.getLow();
			this.open = bar.getOpen();
			this.close = bar.getClose();
			this.volume = bar.getVolume();

		} else {

			if (high < bar.getHigh())
				high = bar.getHigh();
			if (low > bar.getLow())
				low = bar.getLow();

			close = bar.getClose();

			volume += bar.getVolume();
		}
	}

	/**
	 * Adds bars to this bar builder. Bar builder parameters are adjusted
	 * accordingly.
	 * 
	 * @param bars the bars to add
	 */
	public void addBars(List<IBar> bars) {
		for (IBar bar : bars) {
			addBar(bar);
		}
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

	/**
	 * Returns a string representation of this bar.
	 * 
	 * @return A string representation of this bar.
	 */
	@Override
	public String toString() {

		String ret;

		if (isOpen()) {
			// @formatter:off
			ret = String.format(
					"%s:  %s  O: %3$.4f  C: %4$.4f  H: %5$.4f  L: %6$.4f  " + 
							"V: %7$.4f",
					getClass().getSimpleName(),
					ForexTools.getTimeRepresentation(time),
					getOpen(),
					getClose(),
					getHigh(), 
					getLow(),
					getVolume());
			// @formatter:on
		} else {
			// @formatter:off
			ret = String.format(
					"%s:  %s  NOT OPEN",
					getClass().getSimpleName(),
					ForexTools.getTimeRepresentation(time));
			// @formatter:on
		}

		return ret;
	}
}