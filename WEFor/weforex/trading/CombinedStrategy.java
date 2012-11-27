package trading;

import java.util.Collections;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import chart.RateRange;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

import forex.ForexException;

/**
 * TODO fundera šver om RateRanges ska kunna šverlappa varandra.
 * 
 * Changed can't be made after strategy has started.
 * 
 * @author Dennis Ekstrom
 */
public class CombinedStrategy implements Strategy, OrderApprover {

	private HashMap<Strategy, RateRange> strategies;

	private boolean hasStarted;

	public CombinedStrategy() {
		this.strategies = new HashMap<Strategy, RateRange>();
	}

	/**
	 * Create a CombinedStrategy with given strategies and corresponding rate
	 * ranges.
	 * 
	 * @param strategies the strategies to add to this CombinedStrategy
	 * @param rateRanges the rate ranges corresponding to the given strategies
	 */
	public CombinedStrategy(Strategy[] strategies, RateRange[] rateRanges) {
		if (strategies.length != rateRanges.length)
			throw new ForexException("strategies and rateRanges not of equal length");

		this.strategies = new HashMap<Strategy, RateRange>();
		for (int i = 0; i < strategies.length; i++)
			addStrategy(strategies[i], rateRanges[i]);
	}

	/**
	 * Returns an unmodifiable SortedMap with RateRange keys mapping Strings
	 * generating from each RateRange's corresponding Strategy's
	 * toString()-method.
	 * 
	 * @return an unmodifiable SortedMap with RateRange keys mapping Strings
	 *         generating from each RateRange's corresponding Strategy's
	 *         toString()-method
	 */
	public SortedMap<RateRange, String> getRanges() {

		TreeMap<RateRange, String> map = new TreeMap<RateRange, String>();

		for (Strategy strategy : strategies.keySet())
			map.put(strategies.get(strategy), strategy.toString());

		return Collections.unmodifiableSortedMap(map);
	}

	/**
	 * Add a strategy. If this CombinedStrategy has started, nothing is done and
	 * method simply returns.
	 * 
	 * @param strategy the Strategy to add to this CombinedStrategy
	 * @param rateRanges the RateRange corresponding to the given Strategy
	 * @throws ForexException if any parameter is null or if given RateRange
	 *             overlaps a RateRange already registered with this
	 *             CombinedStrategy.
	 */
	public void addStrategy(Strategy strategy, RateRange rateRange) {
		if (strategy == null || rateRange == null)
			throw new ForexException("parameters can't be null");

		if (hasStarted)
			return;

		if (rangeOverlaps(rateRange))
			throw new ForexException(
					"rate range overlaps other rate range of this CombinedStrategy");

		this.strategies.put(strategy, rateRange);
	}

	/**
	 * Changes the RateRange associated to the given Strategy to the given
	 * RateRange. If this CombinedStrategy has started, nothing is done and
	 * method simply returns.
	 * 
	 * @param strategy the Strategy for which to change the corresponding
	 *            RateRange
	 * @param rateRange the new RateRange to set for the given Strategy
	 * @throws ForexException if any parameter is null, if given Strategy is not
	 *             contained by this CombinedStrategy or if given RateRange
	 *             overlaps a RateRange already registered with this
	 *             CombinedStrategy
	 */

	public void changeRateRange(Strategy strategy, RateRange rateRange) {
		if (strategy == null || rateRange == null)
			throw new ForexException("parameters can't be null");

		if (hasStarted)
			return;

		if (!strategies.containsKey(strategy))
			throw new ForexException("strategy not contained by this CombinedStrategy");

		if (rangeOverlaps(rateRange))
			throw new ForexException(
					"rate range overlaps other rate range of this CombinedStrategy");

		this.strategies.put(strategy, rateRange);
	}

	private boolean rangeOverlaps(RateRange range) {
		for (RateRange rr : strategies.values())
			if (range.overlaps(rr))
				return true;

		return false;
	}

	/**
	 * Returns true if this CombinedStrategy approves the given order with
	 * corresponding rate and time of submission, otherwise false.
	 * 
	 * For order to be approved it must have been submitted by a strategy of
	 * this combined strategy and rate must be in the correct range for that
	 * strategy.
	 * 
	 * @param order the order to check for approval
	 * @param time the time of order submission
	 * @param rate the rate at the time of order submission
	 * @return true if this CombinedStrategy approves the given order with
	 *         corresponding rate and time of submission, otherwise false
	 */
	@Override
	public boolean orderApproved(TracableOrder order, long time, double rate) {

		RateRange range = strategies.get(order.getSubmitter());

		if (range != null && range.inRange(rate))
			return true;

		return false;
	}

	@Override
	public void onStart() {
		hasStarted = true;

		for (Strategy strategy : strategies.keySet())
			strategy.onStart();
	}

	@Override
	public void onStop() {
		for (Strategy strategy : strategies.keySet())
			strategy.onStop();
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) {
		for (Strategy strategy : strategies.keySet())
			strategy.onTick(instrument, tick);
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		for (Strategy strategy : strategies.keySet())
			strategy.onBar(instrument, period, askBar, bidBar);

	}

	@Override
	public void onMessage(IMessage message) {
		for (Strategy strategy : strategies.keySet())
			strategy.onMessage(message);
	}

	@Override
	public void onAccount(IAccount account) {
		for (Strategy strategy : strategies.keySet())
			strategy.onAccount(account);
	}
}