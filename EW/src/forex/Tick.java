package forex;

import util.ForexUtils;
import forex.Tick;

/**
 * Immutable class Tick, describes a tick.
 * 
 * @author Dennis Ekstrom
 */
public final class Tick implements ITick {

	private final double ask;
	private final double bid;
	private final long time;
	private final long volume;

	/**
	 * Create a tick.
	 * 
	 * @param time Time of the tick.
	 * @param ask The ask price.
	 * @param bid The bid price.
	 */
	public Tick(long time, double ask, double bid, long volume) {
		this.time = time;
		this.ask = ask;
		this.bid = bid;
		this.volume = volume;
	}

	/**
	 * Create a tick using the given ITick.
	 * 
	 * @param tick the tick to use for creation
	 */
	public Tick(ITick tick) {
		this.time = tick.getTime();
		this.ask = tick.getAsk();
		this.bid = tick.getBid();
		this.volume = tick.getVolume();
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getAsk() {
		return ask;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getBid() {
		return bid;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public long getTime() {
		return time;
	}
	
	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public long getVolume() {
		return volume;
	}

	/**
	 * Returns the spread, i.e. ask - bid.
	 * 
	 * @return the spread of the best price of this tick
	 */
	public double getSpread() {
		return ask - bid;
	}

	/**
	 * Compare this bar to another object, returns true if they're equal,
	 * otherwise false.
	 * 
	 * @return true if given object equals this bar, otherwise false
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Tick))
			return false;

		Tick tick = (Tick) o;

		if (this.ask == tick.getAsk() && this.bid == tick.getBid()
				&& this.time == tick.getTime())
			return true;

		return false;
	}

	/**
	 * Returns a string representation of this tick.
	 * 
	 * @return a string representation of this tick
	 */
	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s:  %s  ask=%3$f  bid=%4$f",
				getClass().getSimpleName(),
				ForexUtils.getTimeRepresentation(time),
				getAsk(),
				getBid());		
		// @formatter:on
	}
}