package forex;

import com.dukascopy.api.*;

/**
 * Immutable class Tick, describes a tick.
 * 
 * @author Dennis Ekstrom
 */
public final class Tick implements ITick {

	private final double[] asks;
	private final double[] bids;
	private final double[] askVolumes;
	private final double[] bidVolumes;
	private final long time;

	/**
	 * Create a tick.
	 * 
	 * @param time Time of the tick.
	 * @param asks array of ask prices on market.
	 * @param bids array of bid prices on market.
	 * @param askVolumes volume available at each ask price.
	 * @param bidVolumes volume available at each bid price.
	 * @precondition asks and bids arrays sorted in ascending order. Elements in
	 *               ask- and bidVolumes are corresponding to the price at the
	 *               same index in bids and asks arrays.
	 */
	public Tick(long time, double[] asks, double[] bids, double[] askVolumes,
			double[] bidVolumes) {
		this.time = time;
		this.asks = asks;
		this.bids = bids;
		this.askVolumes = askVolumes;
		this.bidVolumes = bidVolumes;
	}

	/**
	 * Create a tick given the best ask and bid price and their volume. Any
	 * other price is ignored.
	 * 
	 * @param time Time of the tick.
	 * @param ask the best ask price on market.
	 * @param bid the best ask price on market.
	 * @param askVolume volume available at the best ask price.
	 * @param bidVolume volume available at the best bid price.
	 */
	public Tick(long time, double ask, double bid, double askVolume, double bidVolume) {
		this.time = time;
		this.asks = new double[] { ask };
		this.bids = new double[] { bid };
		this.askVolumes = new double[] { askVolume };
		this.bidVolumes = new double[] { bidVolume };
	}

	/**
	 * Create a tick using the given ITick.
	 * 
	 * @param tick the tick to use for creation
	 */
	public Tick(ITick tick) {
		this.time = tick.getTime();
		this.asks = tick.getAsks();
		this.bids = tick.getBids();
		this.askVolumes = tick.getAskVolumes();
		this.bidVolumes = tick.getBidVolumes();
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getAsk() {
		return asks[0];
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getAskVolume() {
		return askVolumes[0];
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double[] getAskVolumes() {
		return askVolumes;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double[] getAsks() {
		return asks;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getBid() {
		return bids[0];
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getBidVolume() {
		return bidVolumes[0];
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double[] getBidVolumes() {
		return bidVolumes;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double[] getBids() {
		return bids;
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
	public double getTotalAskVolume() {
		double total = 0.0;
		for (double vol : askVolumes) {
			total += vol;
		}
		return total;
	}

	/**
	 * {@inheritDoc ITick}
	 */
	@Override
	public double getTotalBidVolume() {
		double total = 0.0;
		for (double vol : bidVolumes) {
			total += vol;
		}
		return total;
	}

	/**
	 * Returns the spread of the best price of this tick, i.e. asks[0] -
	 * bids[0].
	 * 
	 * @return the spread of the best price of this tick
	 */
	public double getSpread() {
		return asks[0] - bids[0];
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
				ForexTools.getTimeRepresentation(time),
				getAsk(),
				getBid());		
		// @formatter:on
	}
}