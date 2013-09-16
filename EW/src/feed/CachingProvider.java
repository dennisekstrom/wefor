package feed;

import io.IO;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import graph.Graph;
import util.ForexUtils;
import util.HasTime;
import util.Instrument;
import util.Interval;
import util.Lock;
import util.OfferSide;
import util.Period;

/**
 * TODO comment TODO fixa remove() i IntervalTreeSet TODO fixa cache() och allt
 * efteråt
 * 
 * @author Dennis Ekstrom
 */
public class CachingProvider {

	public class Data {
		public final Instrument instrument;
		public final OfferSide offerSide;
		public final Period period;
		public final NavigableSet<? extends HasTime> data;

		public Data(Instrument instrument, OfferSide offerSide, Period period,
				NavigableSet<? extends HasTime> data) {
			this.instrument = instrument;
			this.offerSide = offerSide;
			this.period = period;
			this.data = data;
		}
	}

	/**
	 * A <code>LoadWorker</code> is assigned for each interval of ticks or bars
	 * that needs to be loaded from IO, that is, the ticks or bars of the
	 * interval that weren't cached.
	 */
	public class LoadWorker extends SwingWorker<Data, Object> {

		private final Interval interval;
		private final boolean cacheLoaded;
		private final PropertyChangeListener listener;

		public LoadWorker(Interval interval, boolean cacheLoaded,
				PropertyChangeListener listener) {
			this.interval = interval;
			this.cacheLoaded = cacheLoaded;
			this.listener = listener;
		}

		@Override
		public Data doInBackground() {
			addPropertyChangeListener(listener);
			TreeSet<HasTime> ticksOrBars;
			// FIXME IO stuff is not yet implemented
			System.out.println("WORKER Calling IO: "
					+ Thread.currentThread().toString() + " : " + interval);
			// ticksOrBars = new TreeSet<HasTime>();
			ticksOrBars = IO.get(instrument, offerSide, period, interval.start,
					interval.end);
			System.out.println("WORKER Finished loading: "
					+ Thread.currentThread().toString() + " : " + interval);
			return new Data(instrument, offerSide, period, ticksOrBars);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void done() {
			try {
				Data data = get();
				
				String property = (period == Period.TICK ? Graph.TICKS_LOADED_PROPERTY
						: Graph.BARS_LOADED_PROPERTY);
				firePropertyChange(property, null, data);
				if (cacheLoaded)
					cache((NavigableSet<HasTime>) data.data);
				runningLoadingTasks.remove(interval);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public synchronized String toString() {
			return String.format("%s <interval: %s, caching: %b>", getClass()
					.getName(), interval.toString(), cacheLoaded);
		}
	}

	private static final Logger logger = Logger
			.getLogger(Logger.GLOBAL_LOGGER_NAME);

	// number of elements to cache
	public static final int NUM_ELEMENTS_TO_CACHE = 10000;

	// currently running loading tasks
	private volatile ConcurrentHashMap<Interval, LoadWorker> runningLoadingTasks;

	// cache - concurrency handled by locks
	private volatile NavigableSet<HasTime> cache;
	private volatile IntervalTreeSet cacheIntervals;
	private volatile Lock cacheLock;
	private volatile Lock cacheIntervalsLock;
	private volatile long recentlyRequestedTime;

	// to keep track of whats in cache
	private final Instrument instrument;
	private final OfferSide offerSide;
	private final Period period;

	/**
	 * Create a provider.
	 * 
	 * @param instrument the instrument of this provider
	 * @param offerSide the offer side of this provider
	 * @param period the periodof this provider
	 * @throws IllegalArgumentException if any argument is null
	 */
	public CachingProvider(Instrument instrument, OfferSide offerSide,
			Period period) {
		if (instrument == null || offerSide == null || period == null)
			throw new IllegalArgumentException("Argument must not be null.");

		this.instrument = instrument;
		this.offerSide = offerSide;
		this.period = period;

		runningLoadingTasks = new ConcurrentHashMap<Interval, LoadWorker>();

		cache = new TreeSet<HasTime>(ForexUtils.hasTimeComparator);
		cacheIntervals = new IntervalTreeSet();

		cacheLock = new Lock();
		cacheIntervalsLock = new Lock();
	}

	/**
	 * TODO
	 * 
	 * If the Graph's period is Tick, the tick previous to the given time is
	 * returned. If a tick exists of time equals to the given time, that tick is
	 * returned.
	 * 
	 * If the Graph's period is any other period than Tick, the previously fully
	 * completed bar relative to the given time is returned. If the given time
	 * is the end time of a bar, that bar is returned.
	 * 
	 * @param time the time to find previous tick or bar for
	 * @return the tick or bar previous to the given time
	 */
	public void loadPrevious(long time) {
		// TODO
	}

	/**
	 * TODO
	 * 
	 * If the Graph's period is Tick, the upcoming tick relative to the given
	 * time is returned. If a tick exists of time equals to the given time, that
	 * tick is returned.
	 * 
	 * If the Graph's period is any other period than Tick, the upcoming bar
	 * relative to the given time is returned. If the given time is the start
	 * time of a bar, that bar is returned.
	 * 
	 * @param time the time to find upcoming tick or bar for
	 * @return the upcoming tick or bar relative to the given time
	 */
	public void loadUpcoming(long time) {
		// TODO
	}

	/**
	 * If the Graph's period is Tick, all ticks on the interval [from, to] will
	 * be loaded.
	 * 
	 * If the Graph's period is any other period than tick, the first bar loaded
	 * will be the closest bar which getTime()-method returns a time lower than
	 * or equal to from. The last bar loaded will be the closest bar which
	 * getTime()-method returns a time lower than to.
	 * 
	 * Loaded elements are provided as property changes to the Graph.
	 * 
	 * @param from the start of the interval to load, as described above.
	 * @param to the end of the interval to load, as described above.
	 * @param cacheLoaded if true, loaded stuff is cached.
	 * @param cancelOngoingLoading if true, attempts to cancel ongoing loading
	 *            tasks to give room for the requested interval. Only loading
	 *            tasks with intervals completely outside the requested interval
	 *            will be canceled.
	 */
	public void load(long from, long to, final boolean cacheLoaded,
			final boolean cancelOngoingLoading,
			final PropertyChangeListener listener) {
		System.out.println("Loading: " + new Interval(from, to).toString()); // TODO
																				// remove

		if (from > to) {
			logger.warning("Trying to load interval but from > to.");
			return;
		}

		// If loading bars, adjust interval to bar periods.
		if (this.period != Period.TICK) {
			from = ForexUtils.getStartTime(period, from);
			to = ForexUtils.getEndTime(period, to);
		}

		// Quickly fire away what's in cache.
		Data data = new Data(instrument, offerSide, period, cache.subSet(
				ForexUtils.longToHasTime(from), true,
				ForexUtils.longToHasTime(to), true));
		String property = (period == Period.TICK ? Graph.TICKS_LOADED_PROPERTY
				: Graph.BARS_LOADED_PROPERTY);
		listener.propertyChange(new PropertyChangeEvent(this, property, null,
				data));

		// Now find out which intervals will need to be loaded.
		Interval loadInterval = new Interval(from, to);
		IntervalTreeSet intervalsToLoad = new IntervalTreeSet();
		intervalsToLoad.add(loadInterval);

		// Remove intervals that are already cached (and thus already fired)
		// from the intervals to load.
		ArrayList<Interval> intervalsFromCache = null;
		while (cacheIntervalsLock.tryLock()) {
		}
		try {
			intervalsFromCache = cacheIntervals.getIntervals(loadInterval);
			for (Interval alreadyCached : intervalsFromCache) {
				intervalsToLoad.remove(alreadyCached);
			}
		} finally {
			cacheIntervalsLock.unlock();
		}

		// If overlapping and nothing differs from cache, change requested
		// interval to avoid duplicate loading. Otherwise, cancel currently
		// loading if cancelOngoingLoading == true.
		for (Interval alreadyLoading : runningLoadingTasks.keySet()) {
			if (alreadyLoading.overlaps(loadInterval)) {
				intervalsToLoad.remove(alreadyLoading);
			} else if (cancelOngoingLoading) {
				System.out.println("Canceling: "
						+ runningLoadingTasks.get(alreadyLoading).toString()); // TODO
																				// remove
				runningLoadingTasks.get(alreadyLoading).cancel(true);
				runningLoadingTasks.remove(alreadyLoading);
			}
		}

		// Update recently requested time if we're caching the loaded stuff.
		if (cacheLoaded) {
			recentlyRequestedTime = (to - from) / 2;
		}

		// Finally, load the intervals that weren't already being loaded.
		for (Interval interval : intervalsToLoad.getIntervals()) {
			LoadWorker worker = new LoadWorker(interval, cacheLoaded, listener);
			runningLoadingTasks.put(interval, worker);
			worker.execute();
		}
	}

	private synchronized void cache(NavigableSet<HasTime> toCache) {

		// ignore if list is empty
		if (toCache.isEmpty())
			return;

		while (!cacheLock.tryLock()) {
		}
		while (!cacheIntervalsLock.tryLock()) {
		}
		try {

			long from = ForexUtils.getStartTime(period, toCache.first()
					.getTime());
			long to = ForexUtils.getEndTime(period, toCache.last().getTime());

			
			// TODO Kolla varför from blir > to vid tick
			
			cache.addAll((NavigableSet<HasTime>) toCache); // TODO youre here
			cacheIntervals.add(new Interval(from, to));

			if (cache.size() > NUM_ELEMENTS_TO_CACHE) {
				// Cutting to 1/8 of period.getInteval() *
				// NUM_ELEMENTS_TO_CACHE. Assuming 2 ticks every second.
				NavigableSet<HasTime> newCache = cache;
				long timeOnSides = (period == Period.TICK ? NUM_ELEMENTS_TO_CACHE / 32
						: NUM_ELEMENTS_TO_CACHE * period.getInterval() / 16);
				do {
					HasTime startTime = ForexUtils.longToHasTime(ForexUtils
							.getStartTime(period, recentlyRequestedTime
									- timeOnSides));
					HasTime endTime = ForexUtils.longToHasTime(ForexUtils
							.getEndTime(period, recentlyRequestedTime
									+ timeOnSides));
					newCache = newCache.subSet(startTime, true, endTime, true);
					timeOnSides /= 2;
				} while (cache.size() > NUM_ELEMENTS_TO_CACHE);
				cache.clear();
				cache.addAll(newCache);
			}
		} finally {
			cacheLock.unlock();
			cacheIntervalsLock.unlock();
		}
	}

	/**
	 * Attempts to cancel all loading tasks. Returns true if all loading tasks
	 * where successfully canceled, otherwise false.
	 * 
	 * @return true if all loading tasks where successfully canceled, otherwise
	 *         false
	 */
	public boolean cancelAllLoadingTasks() {
		boolean failed = false;
		System.out.println("Cancelling all tasks."); // TODO remove
		for (Interval interval : runningLoadingTasks.keySet()) {
			if (runningLoadingTasks.get(interval).cancel(true)) {
				runningLoadingTasks.remove(interval);
			} else {
				failed = true;
			}
		}
		return !failed; // TODO log warning of not all closed
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
		// if (!(requester instanceof BarRequester)) {
		// throw new ForexException(
		// "The requester registered with provider is not a BarRequester");
		// }
		//
		// BarRequester requester = (BarRequester) this.requester;
		//
		// long openTime = ForexDataIO.getBarStart(requester.getPeriod(), time);
		// long startTime = openTime;
		// long endTime;
		// BarBuilder builder = new BarBuilder(requester.getOfferSide(),
		// openTime);
		// Period p;
		// for (int i = ForexConstants.BAR_PERIODS.size() - 1; i >= 0; i--) {
		// p = ForexConstants.BAR_PERIODS.get(i);
		// if (p.getInterval() >= requester.getPeriod().getInterval())
		// continue;
		//
		// endTime = ForexDataIO.getBarStart(p, time);
		// if (startTime + p.getInterval() > endTime)
		// continue;
		//
		// ArrayList<IBar> bars = loadBars(requester.getInstrument(), p,
		// requester.getOfferSide(), startTime, endTime, false);
		//
		// builder.addBars(bars);
		//
		// startTime = endTime;
		// }
		//
		// builder.addTicks(loadTicks(requester.getInstrument(), startTime,
		// time,
		// false));
		//
		// if (builder.isOpen())
		// return builder;
		// else
		return null;
	}

	public void clearCache() {
		while (!cacheLock.tryLock()) {
		}
		while (!cacheIntervalsLock.tryLock()) {
		}
		try {
			cache.clear();
			cacheIntervals.clear();
		} finally {
			cacheLock.unlock();
			cacheIntervalsLock.unlock();
		}
	}

	public void destroy() {

		System.out.println("Destroying provider."); // TODO remove

		// Cancel all workers
		for (LoadWorker worker : runningLoadingTasks.values()) {
			worker.cancel(true);
		}

		// Clear collections.
		runningLoadingTasks.clear();
		clearCache();

	}
}