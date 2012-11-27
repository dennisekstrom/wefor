package feed;

import io.ForexDataIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.Timer;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.TickBarSize;
import com.dukascopy.api.feed.ITickBar;

import forex.ForexConstants;
import forex.ForexException;

/**
 * The feed will supply ticks and bars (ASK and BID) to interested listeners.
 * The interval at which ticks and bars are supplied can be set.
 * 
 * The speed of the feed is relative to real-time speed. The duration between
 * supplies will be twice as fast as in real-time if speed is set to 2.0.
 * 
 * The Period of a TickBar is [tickBarSize * tickInterval] and is therefore
 * directly dependent of the tickInterval.
 * 
 * If ticks aren't found, the feed keeps rolling at the speed that is set until
 * it founds one. TickBars won't be updated meanwhile. The period of those might
 * be unexpectedly big in the event of ticks not being found in the data base in
 * the middle of creation a TickBar.
 * 
 * @author Dennis Ekstrom
 */
public class RealTimeFeed extends TimeRelativeFeed {

	@SuppressWarnings("serial")
	private class UpdateTimer extends Timer {

		public UpdateTimer(int updateInterval) {
			super(updateInterval, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent evt) {

					if (latestTimeOfSupply == null || currentTime > latestTimeOfSupply)
						stopFeed();

					setCurrentTime(currentTime + getTimeIncrement());

					supplyElements();

					updateCache();
				}
			});

			// timer should not be coalesce since it keeps track of time
			this.setCoalesce(false);
		}
	}

	private class NextTickFinder implements Callable<List<ITick>> {
		private boolean outOfTickData;

		@Override
		public List<ITick> call() {
			return getUpcomingTicks();
		}

		/**
		 * Returns a list of upcoming ticks, that is ticks being stored in the
		 * upcoming tick table.
		 * 
		 * OBS: use only from within tickFuture.
		 */
		private List<ITick> getUpcomingTicks() {
			if (outOfTickData)
				return null;

			// add time increment since ticks of the current time upon supply
			// will have increased
			long tickTableIndex;
			if (upcomingTicks.isEmpty()) {
				long time = currentTime + getTimeIncrement();

				if (time >= io.getEndOfStorage(instrument, Period.TICK)) {
					outOfTickData = true;
					return null;
				} else if (time < io.getStartOfStorage(instrument, Period.TICK)) {
					time = io.getStartOfStorage(instrument, Period.TICK);
				}

				tickTableIndex = ForexDataIO.getTickTableIndex(time);
			} else {
				long time = upcomingTicks.getLast().getTime();
				tickTableIndex = ForexDataIO.getTickTableIndex(time) + 1;
			}

			List<ITick> ticks;
			do {
				ticks = io.loadTickTable(instrument, tickTableIndex++);
			} while (tickTableIndex <= ForexDataIO.getTickTableIndex(io.getEndOfStorage(
					instrument, Period.TICK)) && (ticks == null || ticks.isEmpty()));

			return ticks;
		}
	}

	private class NextBarFinder implements Callable<List<IBar>[]> {

		private Period barPeriod;

		private boolean outOfBarData;

		NextBarFinder(Period barPeriod) {
			this.barPeriod = barPeriod;
		}

		@Override
		public List<IBar>[] call() throws Exception {
			return getUpcomingBars();
		}

		/**
		 * Returns the upcoming ask and bid bars stored in an array as: { ask,
		 * bid }
		 * 
		 * OBS: use only from within barFuture.
		 * 
		 * @param barPeriod the period of the bars
		 * @return the upcoming ask and bid bars stored in an array as: { ask,
		 *         bid }
		 * @throws ForexException if detecting incoherent storage of ask and bid
		 *             bars
		 */
		private List<IBar>[] getUpcomingBars() {
			if (outOfBarData)
				return null;

			checkUpcomingBarsCoherency(barPeriod);

			// add time increment since bars of the current time upon supply
			// will have increased
			long barTableIndex;
			if (upcomingAskBars.get(barPeriod).isEmpty()) {
				long time = currentTime + getTimeIncrement();

				if (io.getStartOfStorage(instrument, barPeriod) == null
						|| io.getEndOfStorage(instrument, barPeriod) == null
						|| time >= io.getEndOfStorage(instrument, barPeriod)) {
					outOfBarData = true;
					return null;
				} else if (time < io.getStartOfStorage(instrument, barPeriod)) {
					time = io.getStartOfStorage(instrument, barPeriod);
				}

				barTableIndex = ForexDataIO.getBarTableIndex(barPeriod, time);
			} else {
				long time = upcomingAskBars.get(barPeriod).getLast().getTime();
				barTableIndex = ForexDataIO.getBarTableIndex(barPeriod, time) + 1;
			}

			List<IBar> askBars;
			List<IBar> bidBars;
			do {

				askBars = io.loadBarTable(instrument, barPeriod, OfferSide.ASK,
						barTableIndex);
				bidBars = io.loadBarTable(instrument, barPeriod, OfferSide.BID,
						barTableIndex);
				barTableIndex++;

			} while (barTableIndex <= ForexDataIO.getBarTableIndex(barPeriod,
					io.getEndOfStorage(instrument, barPeriod))
					&& (askBars == null || bidBars == null || askBars.isEmpty() || bidBars
							.isEmpty()));

			if (askBars != null && bidBars != null) {
				@SuppressWarnings("unchecked")
				List<IBar>[] ret = new List[2];
				ret[0] = askBars;
				ret[1] = bidBars;
				return ret;
			} else {
				return null;
			}
		}
	}

	private static final int NUM_ELEMENTS_TO_TRIGGER_LOADING = 200;

	private final ExecutorService executor;
	private NextTickFinder tickFinder;
	private Future<List<ITick>> tickFuture;
	private HashMap<Period, NextBarFinder> barFinders;
	private HashMap<Period, Future<List<IBar>[]>> barFutures;

	private HashSet<PropertyChangeListener> currentTimeListeners;

	// next
	private LinkedList<ITick> upcomingTicks;
	private HashMap<TickBarSize, LinkedList<ITickBar>> upcomingAskTickBars;
	private HashMap<TickBarSize, LinkedList<ITickBar>> upcomingBidTickBars;
	private HashMap<Period, LinkedList<IBar>> upcomingAskBars;
	private HashMap<Period, LinkedList<IBar>> upcomingBidBars;

	// previous
	private ITick lastSuppliedTick;

	// timer
	private final UpdateTimer timer;
	private final int updateInterval;

	// io
	private final ForexDataIO io;

	// instance specific parameters
	private final Instrument instrument;
	private final Period tickInterval; // TODO TESTA ATT DENNA FUNKAR

	private final double initialSpeed;
	private double speed;

	private final long startTime;
	private long currentTime;
	private final Long latestTimeOfSupply;

	/**
	 * Create a RealTimeFeed.
	 * 
	 * The start time will be rounded down (a maximum of one tickInterval), to
	 * match the time of the nearest tick possible which time since UNIX EPOCH
	 * can be divided into a discrete number of tickIntervals. This is needed
	 * since stored data are divided into such intervals.
	 * 
	 * The speed is relative real-time speed. That is, if speed is set to 1.0,
	 * the feeds will supply elements at real-time rate. If speed is set to 2.0,
	 * feeds will be supplied twice as fast.
	 * 
	 * @param instrument the instrument of the data supplied by this feed
	 * @param tickInterval the interval of which ticks are supplied by this feed
	 * @param tickBarSize the size of tick bars supplied by this feed
	 * @param speed the speed of the feed
	 * @param startTime the start time of the feed
	 * @param updateInterval the interval, in milliseconds, between updates of
	 *            this feed, that is, how exact the feed is relative real-time
	 * @throws IllegalArgumentException if any of the arguments are null
	 * @throws IllegalArgumentException if speed <= 0
	 * @throws IllegalArgumentException if startTime < 0
	 * @throws IllegalArgumentException if updateInterval <= 0
	 * @see Period, TickBarSize, ForexConstants
	 */
	public RealTimeFeed(Instrument instrument, Period tickInterval,
			TickBarSize tickBarSize, double speed, long startTime, int updateInterval) {
		super();

		if (instrument == null || tickInterval == null) {
			throw new IllegalArgumentException("argument not allowed to be null");
		} else if (speed <= 0) {
			throw new IllegalArgumentException("speed(" + speed + ") <= 0");
		} else if (startTime < 0) {
			throw new IllegalArgumentException("startTime(" + startTime + ") < 0");
		} else if (updateInterval <= 0) {
			throw new IllegalArgumentException("updateInterval(" + updateInterval
					+ ") <= 0");
		}

		// bar threads, finders and futures
		barFinders = new HashMap<Period, NextBarFinder>();
		barFutures = new HashMap<Period, Future<List<IBar>[]>>();

		// listener lists
		currentTimeListeners = new HashSet<PropertyChangeListener>();

		// initialize fields
		this.instrument = instrument;
		this.tickInterval = tickInterval;
		this.initialSpeed = speed;
		this.speed = speed;
		this.startTime = startTime;
		this.updateInterval = updateInterval;

		timer = new UpdateTimer(updateInterval);

		io = ForexDataIO.getInstance();

		tickFinder = new NextTickFinder();
		for (Period p : ForexConstants.BAR_PERIODS) {
			barFinders.put(p, new NextBarFinder(p));
		}

		// executor to run futures - one thread for tick and one for each
		// bar-period
		executor = Executors.newFixedThreadPool(1 + ForexConstants.BAR_PERIODS.size());

		// set current time
		setCurrentTime(startTime);

		// set latest time of supply
		Long latestTimeOfSupply = io.getEndOfStorage(instrument, Period.TICK);
		for (Period p : ForexConstants.BAR_PERIODS) {

			Long timeOfSupply = io.getStartOfStorage(instrument, p);

			if (timeOfSupply != null) {
				timeOfSupply += p.getInterval();

				if (latestTimeOfSupply == null)
					latestTimeOfSupply = timeOfSupply;
				else
					latestTimeOfSupply = Math.max(timeOfSupply, latestTimeOfSupply);
			}
		}
		this.latestTimeOfSupply = latestTimeOfSupply;

		resetCache();

		prepareUpcomingTicks(true);

		for (Period p : ForexConstants.BAR_PERIODS)
			prepareUpcomingBars(p, true);

		updateCache();
	}

	private long getTimeIncrement() {
		return (long) (updateInterval * getSpeed());
	}

	private void prepareUpcomingTicks(boolean wait) {
		if (tickFuture != null) {
			tickFuture.cancel(true);
		}

		tickFuture = executor.submit(tickFinder);

		if (wait)
			getNewlyLoadedTicks();
	}

	private void prepareUpcomingTickBars() {

		for (TickBarSize tbs : ForexConstants.TICK_BAR_SIZES) {

			checkUpcomingTickBarsCoherency(tbs);

			long time;
			TickBarBuilder askBuilder = new TickBarBuilder(OfferSide.ASK);
			TickBarBuilder bidBuilder = new TickBarBuilder(OfferSide.BID);

			if (upcomingAskTickBars.get(tbs).isEmpty()) {
				time = 0L;
			} else {
				time = upcomingAskTickBars.get(tbs).getLast().getEndTime();
			}

			for (ITick tick : upcomingTicks) {
				if (tick.getTime() > time) {

					askBuilder.addTick(tick);
					bidBuilder.addTick(tick);

					if (askBuilder.getFormedElementsCount() == tbs.getSize()) {
						upcomingAskTickBars.get(tbs).add(askBuilder.produceTickBar());
						upcomingBidTickBars.get(tbs).add(bidBuilder.produceTickBar());

						askBuilder.reset(OfferSide.ASK);
						bidBuilder.reset(OfferSide.BID);

						askBuilder.addTick(tick);
						bidBuilder.addTick(tick);
					}
				}
			}

			checkUpcomingTickBarsCoherency(tbs);
		}
	}

	private void prepareUpcomingBars(Period p, boolean wait) {
		if (barFutures.get(p) != null) {
			barFutures.get(p).cancel(true);
		}

		barFutures.put(p, executor.submit(barFinders.get(p)));

		if (wait)
			getNewlyLoadedBars(p);
	}

	private List<ITick> getNewlyLoadedTicks() {
		try {
			return tickFuture.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private List<IBar>[] getNewlyLoadedBars(Period p) {
		try {
			return barFutures.get(p).get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void updateCache() {
		// ticks and tick bars
		if (upcomingTicks.size() < NUM_ELEMENTS_TO_TRIGGER_LOADING && tickFuture.isDone()) {

			List<ITick> loadedTicks = getNewlyLoadedTicks();

			if (loadedTicks != null) {
				upcomingTicks.addAll(loadedTicks);
				prepareUpcomingTickBars();
			}

			prepareUpcomingTicks(false);
		}

		// bars
		for (Period p : ForexConstants.BAR_PERIODS) {

			checkUpcomingBarsCoherency(p);

			if (upcomingAskBars.get(p).size() < NUM_ELEMENTS_TO_TRIGGER_LOADING
					&& barFutures.get(p).isDone()) {

				List<IBar>[] loadedBars = getNewlyLoadedBars(p);

				if (loadedBars != null && loadedBars[0] != null && loadedBars[1] != null) {
					upcomingAskBars.get(p).addAll(loadedBars[0]);
					upcomingBidBars.get(p).addAll(loadedBars[1]);

					checkUpcomingBarsCoherency(p);
				}

				prepareUpcomingBars(p, false);
			}
		}
	}

	/**
	 * Adds a PropertyChangeListener to listen to changes of current time.
	 * 
	 * @param listener the PropertyChangeListener to be added
	 */
	public void addCurrentTimeListener(PropertyChangeListener listener) {
		if (listener == null)
			return;

		currentTimeListeners.add(listener);
	}

	/**
	 * Removes a PropertyChangeListener to listen to changes of current time.
	 * 
	 * @param listener the PropertyChangeListener to be removed
	 */
	public void removeCurrentTimeListener(PropertyChangeListener listener) {
		currentTimeListeners.remove(listener);
	}

	/**
	 * Returns the current time of this feed.
	 * 
	 * @return the current time of this feed
	 */
	@Override
	public long getCurrentTime() {
		return currentTime;
	}

	/**
	 * Sets the current time of the feed.
	 * 
	 * @param currentTime the current time to set
	 */
	private void setCurrentTime(long currentTime) {
		if (this.currentTime == currentTime) // nothing changed
			return;

		// notify listeners
		for (PropertyChangeListener pcl : currentTimeListeners)
			pcl.propertyChange(new PropertyChangeEvent(this, "CurrentTime",
					this.currentTime, currentTime));

		this.currentTime = currentTime;
	}

	/**
	 * Returns the instrument of the feed.
	 * 
	 * @return the instrument of the feed
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * Returns the tick interval of the feed.
	 * 
	 * @return the tick interval of the feed
	 */
	public Period getTickInterval() {
		return tickInterval;
	}

	/**
	 * Returns the start time.
	 * 
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Set the speed of the feed. The speed is relative real-time speed. That
	 * is, if speed is set to 1.0, the feed will supply bars at real-time rate.
	 * If speed is set to 2.0, bars will be supplied twice as fast.
	 * 
	 * @param speed the speed of the feed
	 * @throws IllegalArgumentException if speed <= 0
	 */
	public void setSpeed(double speed) {
		if (speed <= 0)
			throw new IllegalArgumentException("speed(" + speed + ") <= 0");

		this.speed = speed;
	}

	@Override
	public void startFeed() {
		timer.start();
	}

	@Override
	public void stopFeed() {
		timer.stop();
	}

	/**
	 * Resets the feed. Sets it the feed at the start time and with the
	 * tickBarSize, barPeriod and speed set at initialization. The argument
	 * continueAfterReset determines whether or not the feed keeps running after
	 * having been reset.
	 * 
	 * @param continueAfterReset determines whether or not the feed keeps
	 *            running after having been reset. Set to true for feed to start
	 *            automatically after reset, set to false for feed to stop
	 */
	public void reset(boolean continueAfterReset) {
		timer.stop();

		// minus one since only looking for elements of higher time than current
		// time
		setCurrentTime(startTime);

		setSpeed(initialSpeed);

		resetCache();

		if (continueAfterReset)
			timer.start();
	}

	/**
	 * Resets all caching.
	 */
	private void resetCache() {
		// ticks
		upcomingTicks = new LinkedList<ITick>();

		// tick bars
		upcomingAskTickBars = new HashMap<TickBarSize, LinkedList<ITickBar>>();
		upcomingBidTickBars = new HashMap<TickBarSize, LinkedList<ITickBar>>();
		for (TickBarSize tbs : ForexConstants.TICK_BAR_SIZES) {
			upcomingAskTickBars.put(tbs, new LinkedList<ITickBar>());
			upcomingBidTickBars.put(tbs, new LinkedList<ITickBar>());
		}

		// bars
		upcomingAskBars = new HashMap<Period, LinkedList<IBar>>();
		upcomingBidBars = new HashMap<Period, LinkedList<IBar>>();
		for (Period p : ForexConstants.BAR_PERIODS) {
			upcomingAskBars.put(p, new LinkedList<IBar>());
			upcomingBidBars.put(p, new LinkedList<IBar>());
		}
	}

	/**
	 * Elements of equal supply time gets supplied in order: { bars (in
	 * ascending order of Period), tick, tick bars }
	 */
	private void supplyElements() {

		TreeMap<Long, LinkedList<Object>> supply = new TreeMap<Long, LinkedList<Object>>();

		// check whether bars should be supplied
		for (Period p : ForexConstants.BAR_PERIODS) {

			IBar[] barSupply = clearPreviousBars(p);

			if (barSupply != null && barSupply[0] != null && barSupply[1] != null
					&& barSupply[0].getTime() + p.getInterval() >= startTime) {

				long endTime = barSupply[0].getTime() + p.getInterval();

				if (supply.containsKey(endTime)) {

					supply.get(endTime).addLast(new Object[] { barSupply, p });

				} else {

					LinkedList<Object> list = new LinkedList<Object>();
					list.addLast(new Object[] { barSupply, p });
					supply.put(endTime, list);
				}
			}
		}

		// check whether tick should be supplied
		ITick tickSupply = clearPreviousTicks();

		if (tickSupply != null
				&& tickSupply.getTime() >= startTime
				&& (tickInterval.equals(Period.TICK) || lastSuppliedTick == null || tickSupply
						.getTime() >= lastSuppliedTick.getTime()
						+ tickInterval.getInterval())) {

			LinkedList<Object> list = new LinkedList<Object>();
			list.addLast(tickSupply);
			supply.put(tickSupply.getTime(), list);
		}

		// check whether tick bar should be supplied
		for (TickBarSize tbs : ForexConstants.TICK_BAR_SIZES) {

			ITickBar[] tickBarSupply = clearPreviousTickBars(tbs);

			if (tickBarSupply != null && tickBarSupply[0] != null
					&& tickBarSupply[1] != null
					&& tickBarSupply[0].getEndTime() >= startTime) {

				long endTime = tickBarSupply[0].getEndTime();

				if (supply.containsKey(endTime)) {

					supply.get(endTime).addLast(new Object[] { tickBarSupply, tbs });

				} else {

					LinkedList<Object> list = new LinkedList<Object>();
					list.addLast(new Object[] { tickBarSupply, tbs });
					supply.put(endTime, list);
				}
			}
		}

		// supply stuff
		Iterator<LinkedList<Object>> iterator = supply.values().iterator();
		while (iterator.hasNext()) {

			for (Object o : iterator.next()) {

				if (o instanceof ITick) {

					ITick tick = (ITick) o;
					supplyTick(instrument, tick);
					lastSuppliedTick = tick;

				} else {

					if (((Object[]) o)[0] instanceof ITickBar[]) {

						TickBarSize tickBarSize = ((TickBarSize) (((Object[]) o)[1]));
						ITickBar askTickBar = ((ITickBar[]) (((Object[]) o)[0]))[0];
						ITickBar bidTickBar = ((ITickBar[]) (((Object[]) o)[0]))[1];

						supplyTickBars(instrument, tickBarSize, askTickBar, bidTickBar);

					} else {

						Period barPeriod = ((Period) (((Object[]) o)[1]));
						IBar askBar = ((IBar[]) (((Object[]) o)[0]))[0];
						IBar bidBar = ((IBar[]) (((Object[]) o)[0]))[1];

						supplyBars(instrument, barPeriod, askBar, bidBar);
					}
				}
			}
		}
	}

	/**
	 * Clears previous ticks and returns the latest tick <= currentTime, null if
	 * there was no tick <= currentTime cached.
	 * 
	 * @return the latest tick <= currentTime, null if no such tick was cached
	 */
	private ITick clearPreviousTicks() {
		ITick ret = null;

		while (!upcomingTicks.isEmpty()
				&& upcomingTicks.getFirst().getTime() <= currentTime)
			ret = upcomingTicks.removeFirst();

		return ret;
	}

	/**
	 * Clears previous tick bars and returns the latest tick bar of end time <=
	 * currentTime, null if there was no tick bar of end time <= currentTime
	 * cached.
	 * 
	 * @return the latest tick bar of end time <= currentTime, null if no tick
	 *         bar of end time <= currentTime cached
	 */
	private ITickBar[] clearPreviousTickBars(TickBarSize tbs) {

		checkUpcomingTickBarsCoherency(tbs);

		if (upcomingAskTickBars.get(tbs).isEmpty())
			return null;

		ITickBar[] ret = new ITickBar[2];

		while (upcomingAskTickBars.get(tbs).getFirst().getEndTime() <= currentTime) {
			ret[0] = upcomingAskTickBars.get(tbs).removeFirst();
		}

		while (upcomingBidTickBars.get(tbs).getFirst().getEndTime() <= currentTime) {
			ret[1] = upcomingBidTickBars.get(tbs).removeFirst();
		}

		checkUpcomingTickBarsCoherency(tbs);

		return ret;
	}

	/**
	 * Clears previous bars of given period and returns an array { askBar,
	 * bidBar } with the latest bars for which bar.getTime() +
	 * barPeriod.getInterval() <= currentTime, null if there were no such bars
	 * cached cached.
	 * 
	 * @return the latest bar for which bar.getTime() + barPeriod.getInterval()
	 *         <= currentTime, null if no such bars were cached cached
	 */
	private IBar[] clearPreviousBars(Period barPeriod) {

		checkUpcomingBarsCoherency(barPeriod);

		if (upcomingAskBars.get(barPeriod).isEmpty())
			return null;

		IBar[] ret = new IBar[2];

		while (upcomingAskBars.get(barPeriod).getFirst().getTime()
				+ barPeriod.getInterval() <= currentTime) {
			ret[0] = upcomingAskBars.get(barPeriod).removeFirst();
		}

		while (upcomingBidBars.get(barPeriod).getFirst().getTime()
				+ barPeriod.getInterval() <= currentTime) {
			ret[1] = upcomingBidBars.get(barPeriod).removeFirst();
		}

		checkUpcomingBarsCoherency(barPeriod);

		return ret;
	}

	private void checkUpcomingTickBarsCoherency(TickBarSize tickBarSize) {
		// if (upcomingAskTickBars == null && upcomingBidTickBars == null)
		// return;
		//
		// if (upcomingAskTickBars == null ^ upcomingBidTickBars == null)
		// throw new ForexException(
		// "incoherency of upcoming ask and bid tick bars detected");

		LinkedList<ITickBar> askTickBars = upcomingAskTickBars.get(tickBarSize);
		LinkedList<ITickBar> bidTickBars = upcomingBidTickBars.get(tickBarSize);

		if (askTickBars.isEmpty() && bidTickBars.isEmpty())
			return;

		if (askTickBars.size() != bidTickBars.size()
				|| askTickBars.getFirst().getTime() != bidTickBars.getFirst().getTime()
				|| askTickBars.getLast().getEndTime() != bidTickBars.getLast()
						.getEndTime()) {
			throw new ForexException(
					"incoherency of upcoming ask and bid tick bars detected");
		}
	}

	private void checkUpcomingBarsCoherency(Period barPeriod) {
		// if (upcomingAskBars == null && upcomingBidBars == null)
		// return;
		//
		// if (upcomingAskBars == null ^ upcomingBidBars == null)
		// throw new
		// ForexException("incoherency of upcoming ask and bid bars detected");

		LinkedList<IBar> askBars = upcomingAskBars.get(barPeriod);
		LinkedList<IBar> bidBars = upcomingBidBars.get(barPeriod);

		if (askBars.isEmpty() && bidBars.isEmpty())
			return;

		if (askBars.size() != bidBars.size()
				|| askBars.getFirst().getTime() != bidBars.getFirst().getTime()
				|| askBars.getLast().getTime() != bidBars.getLast().getTime()) {
			throw new ForexException("incoherency of upcoming ask and bid bars detected");
		}
	}
}