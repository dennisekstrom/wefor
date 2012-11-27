package feed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import forex.Bar;
import forex.ForexConstants;
import forex.ForexException;
import forex.ForexTools;
import forex.Tick;
import io.ForexDataIO;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IBarFeedListener;

/**
 * This feed supplies ticks and bars of all periods defined in
 * ForexConstants.BAR_PERIODS. Elements are supplied without any delay so the
 * feed does not simulate a real-time feed in the matter of delay between
 * supplies.
 * 
 * Feed will stop automatically when running out of historical data. To manually
 * stop the feed at a specific time, add an appropriate listener for the
 * purpose.
 * 
 * @author Dennis Ekstršm
 */

// TODO for testing
class FeedList implements ITickFeedListener, IBarFeedListener {

	Feed feed;

	FeedList(Feed feed) {
		this.feed = feed;
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) {
		if (stopFeed(tick.getTime()))
			return;

		System.out.println(new Tick(tick));

	}

	@Override
	public void onBar(Instrument instrument, Period period, OfferSide offerSide, IBar bar) {
		if (stopFeed(bar.getTime()))
			return;

		System.out.println(period + " " + new Bar(bar));
	}

	private boolean stopFeed(long time) {
		if (time >= ForexTools.getTimeOf(2011, Calendar.JANUARY, 5, 0, 0, 0, 0)) {
			feed.stopFeed();
			return true;
		}

		return false;
	}
}

public class MultiPeriodFeed extends Feed {

	// TODO for testing
	public static void main(String[] args) {
		long startTime = ForexTools.getTimeOf(2011, Calendar.JANUARY, 4, 0, 0, 0, 0);

		MultiPeriodFeed feed = new MultiPeriodFeed(Instrument.EURUSD, startTime);

		FeedList list = new FeedList(feed);

		feed.addTickFeedListener(list);
		feed.addBarFeedListener(list);

		feed.startFeed();
	}

	private static final int NUM_ELEMENTS_TO_TRIGGER_UPDATE = 500;

	private final Instrument instrument;
	private final long startTime;

	private LinkedList<ITick> upcomingTicks;
	private HashMap<Period, LinkedList<IBar>> upcomingAskBars;
	private HashMap<Period, LinkedList<IBar>> upcomingBidBars;
	private HashMap<Period, Boolean> outOfData;

	private boolean isRunning;

	private ForexDataIO io;

	/**
	 * TODO
	 * 
	 * @param instrument
	 * @param period
	 * @param startTime
	 */
	public MultiPeriodFeed(Instrument instrument, long startTime) {
		if (instrument == null)
			throw new IllegalArgumentException("instrument can't be null");

		upcomingTicks = new LinkedList<ITick>();
		upcomingAskBars = new HashMap<Period, LinkedList<IBar>>();
		upcomingBidBars = new HashMap<Period, LinkedList<IBar>>();
		outOfData = new HashMap<Period, Boolean>();

		// initialize HashMap contents
		outOfData.put(Period.TICK, false);
		for (Period p : ForexConstants.BAR_PERIODS) {
			upcomingAskBars.put(p, new LinkedList<IBar>());
			upcomingBidBars.put(p, new LinkedList<IBar>());
			outOfData.put(p, false);
		}

		io = ForexDataIO.getInstance();

		Long earliestTimeOfSupply = io.getStartOfStorage(instrument, Period.TICK);

		for (Period p : ForexConstants.BAR_PERIODS) {

			outOfData.put(p, false);

			Long timeOfSupply = io.getStartOfStorage(instrument, p);

			if (timeOfSupply != null) {
				timeOfSupply += p.getInterval();
			} else {
				outOfData.put(p, true);
				continue;
			}

			if (earliestTimeOfSupply == null)
				earliestTimeOfSupply = timeOfSupply;
			else
				earliestTimeOfSupply = Math.min(timeOfSupply, earliestTimeOfSupply);

			// end of storage won't be null since found start of storage
			timeOfSupply = io.getEndOfStorage(instrument, p) + p.getInterval();

			if (startTime > timeOfSupply)
				outOfData.put(p, true);
		}

		if (startTime < earliestTimeOfSupply)
			startTime = earliestTimeOfSupply;

		this.instrument = instrument;
		this.startTime = startTime;

		updateUpcoming();
		clearPreviousElements();
	}

	private void clearPreviousElements() {

		// ticks
		while (!upcomingTicks.isEmpty() && upcomingTicks.getFirst().getTime() < startTime) {

			upcomingTicks.removeFirst();
		}

		// bars
		for (Period p : ForexConstants.BAR_PERIODS) {
			//
			// if (p.equals(Period.ONE_HOUR)) {
			// System.out.println(upcomingAskBars.get(p).isEmpty());
			//
			// //
			// System.out.println(ForexTools.getTimeRepresentation(upcomingAskBars
			// // .get(p).getFirst().getTime()));
			// }

			while (!upcomingAskBars.get(p).isEmpty()
					&& upcomingAskBars.get(p).getFirst().getTime() + p.getInterval() < startTime) {
				//
				// if (p.equals(Period.ONE_HOUR))
				// System.out.println(new
				// Bar(upcomingAskBars.get(p).getFirst()));
				// else
				// System.out.println(p);

				upcomingAskBars.get(p).removeFirst();
				upcomingBidBars.get(p).removeFirst();

				checkUpcomingBarsCoherency(p);
			}
		}
	}

	@Override
	public void startFeed() {

		isRunning = true;

		Long timeOfNextSupply = getTimeOfNextSupply();

		while (isRunning && timeOfNextSupply != null) {

			for (Period p : ForexConstants.BAR_PERIODS) {

				if (!upcomingAskBars.get(p).isEmpty()
						&& upcomingAskBars.get(p).getFirst().getTime() + p.getInterval() == timeOfNextSupply) {

					supplyBars(instrument, p, upcomingAskBars.get(p).removeFirst(),
							upcomingBidBars.get(p).removeFirst());
				}
			}

			if (!upcomingTicks.isEmpty()
					&& upcomingTicks.getFirst().getTime() == timeOfNextSupply) {

				supplyTick(instrument, upcomingTicks.removeFirst());
			}

			updateUpcoming();

			timeOfNextSupply = getTimeOfNextSupply();
		}
	}

	/**
	 * Returns the time of the next element to be supplied, null if no elements
	 * of any kind were found.
	 */
	private Long getTimeOfNextSupply() {
		Long timeOfNextSupply = null;

		if (!upcomingTicks.isEmpty())
			timeOfNextSupply = upcomingTicks.getFirst().getTime();

		for (Period p : ForexConstants.BAR_PERIODS) {

			if (!upcomingAskBars.get(p).isEmpty())
				timeOfNextSupply = Math.min(timeOfNextSupply, upcomingAskBars.get(p)
						.getFirst().getTime()
						+ p.getInterval());
		}

		return timeOfNextSupply;
	}

	@Override
	public void stopFeed() {
		isRunning = false;
	}

	private void updateUpcoming() {

		// update ticks
		if (!outOfData.get(Period.TICK) && updateNeeded(Period.TICK)) {
			long tickTableIndex;

			if (upcomingTicks.isEmpty())
				tickTableIndex = ForexDataIO.getTickTableIndex(startTime);
			else
				tickTableIndex = ForexDataIO.getTickTableIndex(upcomingTicks.getLast()
						.getTime()) + 1;

			while (!outOfData.get(Period.TICK) && updateNeeded(Period.TICK)) {

				if (tickTableIndex > ForexDataIO.getTickTableIndex(io.getEndOfStorage(
						instrument, Period.TICK))) {

					outOfData.put(Period.TICK, true);
					break;
				}

				ArrayList<ITick> ticks = io.loadTickTable(instrument, tickTableIndex);

				if (ticks != null)
					upcomingTicks.addAll(ticks);

				tickTableIndex++;
			}
		}

		// update bars
		for (Period p : ForexConstants.BAR_PERIODS) {

			if (outOfData.get(p) || !updateNeeded(p))
				continue;

			long barTableIndex;

			if (upcomingAskBars.get(p).isEmpty())
				barTableIndex = ForexDataIO.getBarTableIndex(p, startTime);
			else
				barTableIndex = ForexDataIO.getBarTableIndex(p, upcomingAskBars.get(p)
						.getLast().getTime()) + 1;

			while (!outOfData.get(p) && updateNeeded(p)) {

				if (barTableIndex > ForexDataIO.getBarTableIndex(p,
						io.getEndOfStorage(instrument, p))) {
					outOfData.put(p, true);
					break;
				}

				ArrayList<IBar> askBars = io.loadBarTable(instrument, p, OfferSide.ASK,
						barTableIndex);
				ArrayList<IBar> bidBars = io.loadBarTable(instrument, p, OfferSide.BID,
						barTableIndex);

				if (askBars != null && bidBars != null) {
					upcomingAskBars.get(p).addAll(askBars);
					upcomingBidBars.get(p).addAll(bidBars);
				}

				checkUpcomingBarsCoherency(p);

				barTableIndex++;
			}
		}
	}

	private boolean updateNeeded(Period p) {
		if (p.equals(Period.TICK)) {
			return upcomingTicks.size() <= NUM_ELEMENTS_TO_TRIGGER_UPDATE;
		} else {
			return upcomingAskBars.get(p).size() <= NUM_ELEMENTS_TO_TRIGGER_UPDATE;
		}
	}

	private void checkUpcomingBarsCoherency(Period p) {
		if (upcomingAskBars.get(p).isEmpty() && upcomingBidBars.get(p).isEmpty())
			return;

		if (upcomingAskBars.get(p).size() != upcomingBidBars.get(p).size()
				|| upcomingAskBars.get(p).getFirst().getTime() != upcomingBidBars.get(p)
						.getFirst().getTime()
				|| upcomingAskBars.get(p).getLast().getTime() != upcomingBidBars.get(p)
						.getLast().getTime()) {
			throw new ForexException(
					"incoherency of upcoming ask and bid bars of period " + p
							+ " detected");
		}
	}
}