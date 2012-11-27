package feed;

import java.util.ArrayList;
import java.util.LinkedList;

import forex.ForexException;
import io.ForexDataIO;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

/**
 * This feed supplies ticks or bars of the period specified upon construction.
 * Elements are supplied without any delay so the feed does not simulate a
 * real-time feed in the matter of delay between supplies.
 * 
 * Feed will stop automatically when running out of historical data. To manually
 * stop the feed at a specific time, add an appropriate listener for the
 * purpose.
 * 
 * @author Dennis Ekstršm
 */
public class SinglePeriodFeed extends Feed {

	private static final int NUM_ELEMENTS_TO_TRIGGER_UPDATE = 500;

	private final Instrument instrument;
	private final Period period;
	private final long startTime;

	private LinkedList<ITick> upcomingTicks;
	private LinkedList<IBar> upcomingAskBars;
	private LinkedList<IBar> upcomingBidBars;

	private boolean outOfData, isRunning;

	private ForexDataIO io;

	/**
	 * TODO
	 * 
	 * @param instrument
	 * @param period
	 * @param startTime
	 */
	public SinglePeriodFeed(Instrument instrument, Period period, long startTime) {
		if (instrument == null || period == null)
			throw new IllegalArgumentException("argument can't be null");

		io = ForexDataIO.getInstance();

		Long startOfStorage = io.getStartOfStorage(instrument, period);
		if (startOfStorage == null || startTime > io.getEndOfStorage(instrument, period))
			outOfData = true;
		else if (startTime < startOfStorage)
			startTime = startOfStorage;

		this.instrument = instrument;
		this.period = period;
		this.startTime = startTime;

		if (period.equals(Period.TICK)) {

			upcomingTicks = new LinkedList<ITick>();

			updateUpcoming();

			while (!upcomingTicks.isEmpty()
					&& upcomingTicks.getFirst().getTime() < startTime) {

				upcomingTicks.removeFirst();
			}

		} else {

			upcomingAskBars = new LinkedList<IBar>();
			upcomingBidBars = new LinkedList<IBar>();

			updateUpcoming();

			while (!upcomingAskBars.isEmpty()
					&& upcomingAskBars.getFirst().getTime() + period.getInterval() < startTime) {

				upcomingAskBars.removeFirst();
				upcomingBidBars.removeFirst();

				checkUpcomingBarsCoherency();
			}
		}
	}

	@Override
	public void startFeed() {

		isRunning = true;

		while (gotSupply() && isRunning) {

			if (period.equals(Period.TICK)) {

				supplyTick(instrument, upcomingTicks.removeFirst());

			} else {

				supplyBars(instrument, period, upcomingAskBars.removeFirst(),
						upcomingBidBars.removeFirst());
			}

			updateUpcoming();
		}
	}

	private boolean gotSupply() {
		if (period.equals(Period.TICK)) {
			return !upcomingTicks.isEmpty();
		} else {
			checkUpcomingBarsCoherency();
			return !upcomingAskBars.isEmpty();
		}
	}

	@Override
	public void stopFeed() {
		isRunning = false;
	}

	private void updateUpcoming() {
		if (outOfData || !updateNeeded())
			return;

		if (period.equals(Period.TICK)) {

			long tickTableIndex;

			if (upcomingTicks.isEmpty())
				tickTableIndex = ForexDataIO.getTickTableIndex(startTime);
			else
				tickTableIndex = ForexDataIO.getTickTableIndex(upcomingTicks.getLast()
						.getTime()) + 1;

			while (!outOfData && updateNeeded()) {
				if (tickTableIndex > ForexDataIO.getTickTableIndex(io.getEndOfStorage(
						instrument, period))) {
					outOfData = true;
					return;
				}

				ArrayList<ITick> ticks = io.loadTickTable(instrument, tickTableIndex);

				if (ticks != null)
					upcomingTicks.addAll(ticks);

				tickTableIndex++;
			}

		} else {

			long barTableIndex;

			if (upcomingAskBars.isEmpty())
				barTableIndex = ForexDataIO.getBarTableIndex(period, startTime);
			else
				barTableIndex = ForexDataIO.getBarTableIndex(period, upcomingAskBars
						.getLast().getTime()) + 1;

			while (!outOfData && updateNeeded()) {

				if (barTableIndex > ForexDataIO.getBarTableIndex(period,
						io.getEndOfStorage(instrument, period))) {
					outOfData = true;
					return;
				}

				ArrayList<IBar> askBars = io.loadBarTable(instrument, period,
						OfferSide.ASK, barTableIndex);
				ArrayList<IBar> bidBars = io.loadBarTable(instrument, period,
						OfferSide.BID, barTableIndex);

				if (askBars != null && bidBars != null) {
					upcomingAskBars.addAll(askBars);
					upcomingBidBars.addAll(bidBars);
				}

				checkUpcomingBarsCoherency();

				barTableIndex++;
			}
		}
	}

	private boolean updateNeeded() {
		if (period.equals(Period.TICK)) {
			return upcomingTicks.size() <= NUM_ELEMENTS_TO_TRIGGER_UPDATE;
		} else {
			checkUpcomingBarsCoherency();
			return upcomingAskBars.size() <= NUM_ELEMENTS_TO_TRIGGER_UPDATE;
		}
	}

	private void checkUpcomingBarsCoherency() {
		if (upcomingAskBars.isEmpty() && upcomingBidBars.isEmpty())
			return;

		if (upcomingAskBars.size() != upcomingBidBars.size()
				|| upcomingAskBars.getFirst().getTime() != upcomingBidBars.getFirst()
						.getTime()
				|| upcomingAskBars.getLast().getTime() != upcomingBidBars.getLast()
						.getTime()) {
			throw new ForexException("incoherency of upcoming ask and bid bars detected");
		}
	}

}