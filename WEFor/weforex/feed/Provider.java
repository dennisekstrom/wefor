package feed;

import forex.ForexConstants;
import forex.ForexException;
import io.ForexDataIO;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import chart.TimeRange;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

/**
 * This class uses a BarRequester and ForexDataIO to provide bars of time less
 * than or equal to BarRequester.getUpperTimeLimit(). Bars of time higher than
 * the upper time limit of the bar requester shouldn't be supplied as they
 * virtually lie in the future.
 * 
 * @author Dennis Ekstrom
 */
public class Provider {
	// number of tables to keep in cache
	public static final int NUM_TABLES_TO_CACHE = 200;

	private ForexDataIO io;

	private final Requester requester;
	private IHistory history;

	// tick cache
	private NavigableMap<Long, List<ITick>> tickCache;
	private long recentlyRequestedTickTable;

	// bar cache
	private NavigableMap<Long, List<IBar>> barCache;
	private long recentlyRequestedBarTable;
	private OfferSide cachingOfferSide;
	private Period cachingPeriod;

	/**
	 * Create a provider.
	 * 
	 * @param requester the Requester to register with this provider
	 * @throws IllegalArgumentException if requester is null
	 */
	public Provider(Requester requester) {
		if (requester == null)
			throw new IllegalArgumentException("requester can't be null");

		this.requester = requester;

		io = ForexDataIO.getInstance();

		resetTickCache();

		if (requester instanceof BarRequester)
			resetBarCache();
	}

	/**
	 * Sets the history to use when outside database storage ranges.
	 * 
	 * @param history the history to use when outside database storage ranges
	 */
	public void setHistory(IHistory history) {
		this.history = history;
	}

	private Long getStartOfData(Period period) {
		return io.getStartOfStorage(requester.getInstrument(), period);
	}

	private Long getEndOfData(Period period) {
		Long endOfStorage = io.getEndOfStorage(requester.getInstrument(), period);

		if (endOfStorage != null && history != null) {

			try {

				return Math.max(endOfStorage, history.getStartTimeOfCurrentBar(
						requester.getInstrument(), period));

			} catch (JFException e) {
				throw new ForexException(e.getMessage());
			}
		} else {
			return endOfStorage;
		}
	}

	private List<ITick> loadTickTable(long tableIndex) {
		Instrument instrument = requester.getInstrument();

		if (history == null
				|| tableIndex <= ForexDataIO.getTickTableIndex(io.getEndOfStorage(
						instrument, Period.TICK))) {

			return io.loadTickTable(instrument, tableIndex);

		} else {

			TimeRange range = ForexDataIO.getTickTableTimeRange(tableIndex);

			try {

				return history.getTicks(instrument, range.startTime, range.endTime);

			} catch (JFException e) {
				throw new ForexException(e.getMessage());
			}
		}
	}

	private List<IBar> loadBarTable(Period period, OfferSide offerSide, long tableIndex) {
		Instrument instrument = requester.getInstrument();

		if (history == null
				|| tableIndex <= ForexDataIO.getBarTableIndex(period,
						io.getEndOfStorage(instrument, period))) {

			return io.loadBarTable(instrument, period, offerSide, tableIndex);

		} else {

			TimeRange range = ForexDataIO.getTickTableTimeRange(tableIndex);

			try {

				return history.getBars(instrument, period, offerSide, range.startTime,
						history.getBarStart(period, range.endTime));

			} catch (JFException e) {
				throw new ForexException(e.getMessage());
			}
		}
	}

	/**
	 * Returns a list of all ticks of time on the interval [from, to]. An empty
	 * list is returned if any bar on the interval couldn't be loaded.
	 * 
	 * @param offerSide the offer side of the bar
	 * @param from the beginning of the interval (inclusive)
	 * @param to the end of the interval (inclusive)
	 * @return a list of all bars of specified offer side and of time on the
	 *         interval [from, to]
	 * @throws IllegalArgumentException if from > to
	 */
	public ArrayList<ITick> getTicks(long from, long to) {
		if (from > to) {
			throw new IllegalArgumentException("Illegal interval from(" + from
					+ ") > to(" + to + ")");
		}

		long futureTime = requester.getUpperTimeLimit();

		// set recently requested table
		long centerOfRequestedTicks = (to - from) / 2;
		if (centerOfRequestedTicks > futureTime)
			centerOfRequestedTicks = futureTime;
		recentlyRequestedTickTable = ForexDataIO
				.getTickTableIndex(centerOfRequestedTicks);

		// don't provide ticks of the future
		if (from >= futureTime)
			return new ArrayList<ITick>();
		if (to >= futureTime)
			to = futureTime;

		return loadTicks(requester.getInstrument(), from, to, true);
	}

	/**
	 * Returns the most recent tick using the given time as current time. If a
	 * tick exists of time equals to the given time, that tick is returned.
	 * Returns null if no such tick was found.
	 * 
	 * @param time the time to find previous tick for
	 * @return the most recent tick using the given time as current time
	 */
	public ITick getPreviousTick(long time) {
		if (time < getStartOfData(Period.TICK))
			return null;
		else if (time > requester.getUpperTimeLimit() || time > getEndOfData(Period.TICK))
			time = Math.min(requester.getUpperTimeLimit(), getEndOfData(Period.TICK));

		List<ITick> ticks;
		long tableIndex = ForexDataIO.getTickTableIndex(time);
		do {
			if (tickCache.containsKey(tableIndex)) {
				ticks = tickCache.get(tableIndex);
			} else {
				ticks = loadTickTable(tableIndex);
				cacheTicks(tableIndex, ticks);
			}

			if (ticks != null && !ticks.isEmpty() && ticks.get(0).getTime() <= time) {

				// check last
				if (ticks.get(ticks.size() - 1).getTime() <= time)
					return ticks.get(ticks.size() - 1);

				for (int i = 0; i < ticks.size(); i++) {
					if (ticks.get(i).getTime() > time) {
						return ticks.get(i - 1);
					}
				}
			}
		} while (--tableIndex >= ForexDataIO
				.getTickTableIndex(getStartOfData(Period.TICK)));

		return null;
	}

	/**
	 * Returns the upcoming tick using the given time as current time. If a tick
	 * exists of time equals to the given time, that tick is returned. Returns
	 * null if no such tick was found.
	 * 
	 * @param time the time to find upcoming tick for
	 * @return the most recent tick using the given time as current time
	 */
	public ITick getUpcomingTick(long time) {
		if (time > requester.getUpperTimeLimit() || time > getEndOfData(Period.TICK))
			return null;
		else if (time < getStartOfData(Period.TICK))
			time = getStartOfData(Period.TICK);

		List<ITick> ticks;
		long tableIndex = ForexDataIO.getTickTableIndex(time);
		do {
			if (tickCache.containsKey(tableIndex)) {
				ticks = tickCache.get(tableIndex);
			} else {
				ticks = loadTickTable(tableIndex);
				cacheTicks(tableIndex, ticks);
			}

			if (ticks != null && !ticks.isEmpty()
					&& ticks.get(ticks.size() - 1).getTime() >= time) {

				// check first
				if (ticks.get(0).getTime() >= time)
					return ticks.get(0);

				for (int i = ticks.size() - 2; i <= 0; i--) {
					if (ticks.get(i).getTime() < time) {
						return ticks.get(i + 1);
					}
				}
			}
		} while (++tableIndex <= ForexDataIO.getTickTableIndex(getEndOfData(Period.TICK)));

		return null;
	}

	private ArrayList<ITick> loadTicks(Instrument instrument, long from, long to,
			boolean cacheLoadedTicks) {
		long startTableIndex = ForexDataIO.getTickTableIndex(from);
		long endTableIndex = ForexDataIO.getTickTableIndex(to);

		ArrayList<ITick> ticks = new ArrayList<ITick>();
		List<ITick> table = null;
		boolean cacheContains = false;
		for (long i = startTableIndex; i <= endTableIndex; i++) {
			// look for requested table in cache, otherwise load from database
			if (tickCache.containsKey(i)) {
				cacheContains = true;
				table = tickCache.get(i);
			} else {
				table = loadTickTable(i);
			}

			if (table != null) // loading successful
				ticks.addAll(table);

			if (cacheLoadedTicks && !cacheContains)
				cacheTicks(i, table); // cache loaded table
		}

		int fromIndex = -1, toIndex = -1;
		for (int i = 0; i < ticks.size(); i++) {
			if (ticks.get(i).getTime() >= from) {
				fromIndex = i;
				break;
			}
		}

		for (int i = ticks.size() - 1; i >= 0; i--) {
			if (ticks.get(i).getTime() <= to) {
				toIndex = i;
				break;
			}
		}

		if (fromIndex < 0 || fromIndex < 0 || toIndex < fromIndex)
			return new ArrayList<ITick>();

		ArrayList<ITick> ret = new ArrayList<ITick>(toIndex - fromIndex + 1);

		for (int i = fromIndex; i <= toIndex; i++)
			ret.add(ticks.get(i));

		return ret;
	}

	/**
	 * Cache the given tick table. If cache is full, clear all tables but those
	 * within (NUM_TABLES_TO_CACHE / 4) on both sides of
	 * recentlyRequestedTickTable.
	 */
	private void cacheTicks(Long tableIndex, List<ITick> tableData) {
		if (tickCache.size() < NUM_TABLES_TO_CACHE) {
			tickCache.put(tableIndex, tableData);
		} else {
			Long fromKey = recentlyRequestedTickTable, toKey = recentlyRequestedTickTable;
			for (int i = 0; i < NUM_TABLES_TO_CACHE / 4; i++) {
				if (fromKey != null)
					fromKey = tickCache.lowerKey(fromKey);
				if (toKey != null)
					toKey = tickCache.lowerKey(toKey);
			}
			tickCache = tickCache.subMap(fromKey, true, toKey, true);
		}
	}

	private void resetTickCache() {
		tickCache = new TreeMap<Long, List<ITick>>();
		recentlyRequestedTickTable = 0;
	}

	/**
	 * Returns a bar builder representing the bar as it would look if it was
	 * currently being built, treating the time given as the current time.
	 * 
	 * @param time the time to be treated as upper time limit of the returned
	 *            building bar
	 * @return a bar builder representing the bar as it would look if it was
	 *         currently being built, given the time as the current time
	 */
	public BarBuilder getBuildingBar(long time) {
		if (!(requester instanceof BarRequester)) {
			throw new ForexException(
					"The requester registered with provider is not a BarRequester");
		}

		BarRequester requester = (BarRequester) this.requester;

		long openTime = ForexDataIO.getBarStart(requester.getPeriod(), time);
		long startTime = openTime;
		long endTime;
		BarBuilder builder = new BarBuilder(requester.getOfferSide(), openTime);
		Period p;
		for (int i = ForexConstants.BAR_PERIODS.size() - 1; i >= 0; i--) {
			p = ForexConstants.BAR_PERIODS.get(i);
			if (p.getInterval() >= requester.getPeriod().getInterval())
				continue;

			endTime = ForexDataIO.getBarStart(p, time);
			if (startTime + p.getInterval() > endTime)
				continue;

			ArrayList<IBar> bars = loadBars(requester.getInstrument(), p,
					requester.getOfferSide(), startTime, endTime, false);

			builder.addBars(bars);

			startTime = endTime;
		}

		builder.addTicks(loadTicks(requester.getInstrument(), startTime, time, false));

		if (builder.isOpen())
			return builder;
		else
			return null;
	}

	/**
	 * Returns a list of all bars of specified offer side and of time on the
	 * interval [from, to). That is, the first bar in the list will be the
	 * closest bar which getTime()-method returns a time lower than or equal to
	 * from. The last bar in the list will be the closest bar which
	 * getTime()-method returns a time lower than to.
	 * 
	 * An empty list is returned if no bars on the interval could be loaded.
	 * 
	 * @param offerSide the offer side of the bars
	 * @param from the beginning of the interval (inclusive)
	 * @param to the end of the interval (exclusive)
	 * @return a list of all bars of specified offer side and of time on the
	 *         interval [from, to)
	 * @throws IllegalArgumentException if from >= to
	 * @throws ForexException if the requester registered with this provider is
	 *             not a BarRequester
	 */
	public ArrayList<IBar> getBars(long from, long to) {
		if (!(requester instanceof BarRequester)) {
			throw new ForexException(
					"The requester registered with provider is not a BarRequester");
		} else if (from >= to) {
			throw new IllegalArgumentException("Illegal interval from(" + from
					+ ") >= to(" + to + ")");
		}

		BarRequester requester = (BarRequester) this.requester;

		long barTimeLimit = ForexDataIO.getBarStart(requester.getPeriod(),
				requester.getUpperTimeLimit());

		// reset cache if offer side or period has changed
		if (!cachingOfferSide.equals(requester.getOfferSide())
				|| !cachingPeriod.equals(requester.getPeriod()))
			resetBarCache();

		// set recently requested table
		long centerOfRequestedBars = (from + to) / 2;
		if (centerOfRequestedBars > barTimeLimit)
			centerOfRequestedBars = barTimeLimit;

		recentlyRequestedBarTable = ForexDataIO.getBarTableIndex(requester.getPeriod(),
				centerOfRequestedBars);

		// don't provide ticks of the future
		if (from >= barTimeLimit)
			return new ArrayList<IBar>();
		else if (to > barTimeLimit)
			to = barTimeLimit;

		return loadBars(requester.getInstrument(), requester.getPeriod(),
				requester.getOfferSide(), from, to, true);
	}

	/**
	 * Returns the most recent bar using the given time as current time. If a
	 * bar exists that would have been supplied at the given time, that bar is
	 * returned. Returns null if no such bar was found.
	 * 
	 * @param time the time to find previous bar for
	 * @return the most recent bar using the given time as current time
	 */
	public IBar getPreviousBar(long time) {
		if (!(requester instanceof BarRequester)) {
			throw new ForexException(
					"The requester registered with provider is not a BarRequester");
		}

		BarRequester requester = (BarRequester) this.requester;

		Long startOfStorage = getStartOfData(requester.getPeriod());
		Long endOfStorage = getEndOfData(requester.getPeriod());

		if (startOfStorage == null) {

			return null;

		} else if (time <= startOfStorage) {

			return null;

		} else if (time > requester.getUpperTimeLimit() || time > endOfStorage) {

			time = Math.min(requester.getUpperTimeLimit(), endOfStorage);
		}

		List<IBar> bars;
		long tableIndex = ForexDataIO.getBarTableIndex(requester.getPeriod(), time);
		do {
			if (barCache.containsKey(tableIndex)) {
				bars = barCache.get(tableIndex);
			} else {
				bars = loadBarTable(requester.getPeriod(), requester.getOfferSide(),
						tableIndex);
				cacheBars(tableIndex, bars);
			}

			if (bars != null
					&& !bars.isEmpty()
					&& bars.get(0).getTime() + requester.getPeriod().getInterval() <= time) {

				// check last
				if (bars.get(bars.size() - 1).getTime()
						+ requester.getPeriod().getInterval() <= time)
					return bars.get(bars.size() - 1);

				for (int i = 0; i < bars.size(); i++) {
					if (bars.get(i).getTime() + requester.getPeriod().getInterval() > time) {
						return bars.get(i - 1);
					}
				}
			}
		} while (--tableIndex >= ForexDataIO.getBarTableIndex(requester.getPeriod(),
				getStartOfData(requester.getPeriod())));

		return null;
	}

	/**
	 * Returns a list of all bars of time on the interval [from, to). That is,
	 * the first bar in the list will be the closest bar which getTime()-method
	 * returns a time lower than or equal to from. The last bar in the list will
	 * be the closest bar which getTime()-method returns a time lower than to.
	 * 
	 * An empty list is returned if any bar on the interval couldn't be loaded.
	 */
	private ArrayList<IBar> loadBars(Instrument instrument, Period period,
			OfferSide offerSide, long from, long to, boolean cacheLoadedBars) {

		long startTableIndex = ForexDataIO.getBarTableIndex(period, from);
		long endTableIndex = ForexDataIO.getBarTableIndex(period, to);

		int initialCapacity = (int) (endTableIndex - startTableIndex + 1)
				* ForexDataIO.MAX_ROWS_PER_BAR_TABLE + 1;

		ArrayList<IBar> bars = new ArrayList<IBar>(initialCapacity);
		List<IBar> table = null;
		boolean cacheContains = false;
		for (long i = startTableIndex; i <= endTableIndex; i++) {
			// look for requested table in cache, otherwise load from database
			if (barCache.containsKey(i)) {
				cacheContains = true;
				table = barCache.get(i);
			} else {
				table = loadBarTable(period, offerSide, i);
			}

			if (table != null) // loading successful
				bars.addAll(table);

			if (cacheLoadedBars && !cacheContains)
				cacheBars(i, table); // cache loaded table
		}

		int fromIndex = -1, toIndex = -1;
		for (int i = 1; i < bars.size(); i++) {
			if (bars.get(i).getTime() > from) {
				fromIndex = i - 1;
				break;
			}
		}

		for (int i = bars.size() - 1; i >= 0; i--) {
			if (bars.get(i).getTime() < to) {
				toIndex = i;
				break;
			}
		}

		if (fromIndex < 0 || fromIndex < 0 || toIndex < fromIndex)
			return new ArrayList<IBar>();

		ArrayList<IBar> ret = new ArrayList<IBar>(toIndex - fromIndex + 1);

		for (int i = fromIndex; i <= toIndex; i++)
			ret.add(bars.get(i));

		return ret;
	}

	/**
	 * Cache the given bar table. If cache is full, clear all tables but those
	 * within (NUM_TABLES_TO_CACHE / 4) on both sides of
	 * recentlyRequestedBarTable.
	 */
	private void cacheBars(Long tableIndex, List<IBar> tableData) {
		if (barCache.size() < NUM_TABLES_TO_CACHE) {
			barCache.put(tableIndex, tableData);
		} else {
			Long fromKey = recentlyRequestedBarTable, toKey = recentlyRequestedBarTable;
			for (int i = 0; i < NUM_TABLES_TO_CACHE / 4; i++) {
				if (fromKey != null)
					fromKey = barCache.lowerKey(fromKey);
				if (toKey != null)
					toKey = barCache.lowerKey(toKey);
			}
			barCache = barCache.subMap(fromKey, true, toKey, true);
		}
	}

	private void resetBarCache() {
		barCache = new TreeMap<Long, List<IBar>>();
		cachingOfferSide = ((BarRequester) requester).getOfferSide();
		cachingPeriod = ((BarRequester) requester).getPeriod();
		recentlyRequestedBarTable = 0;
	}
}