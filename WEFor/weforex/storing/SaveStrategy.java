package storing;

import forex.ForexConstants;
import forex.ForexException;
import forex.ForexTools;
import io.ForexDataIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class SaveStrategy implements IStrategy {

	private static final int NUM_TICKS_TO_TRIGGER_STORING = 10000;
	private static final int NUM_BARS_TO_TRIGGER_STORING = 10000;
	private static final Period BASIC_BAR_PERIOD = ForexConstants.BAR_PERIODS.get(0);

	private Instrument instrument;

	private ArrayList<ITick> ticks;
	private HashMap<Period, ArrayList<IBar>> askBars;
	private HashMap<Period, ArrayList<IBar>> bidBars;

	private ForexDataIO io;
	private ExecutorService executor;

	// private Long timeOfFirstBar;
	// private long endTimeOfLastBar;

	private Runnable storeTicks = new Runnable() {
		@Override
		public void run() {
			System.out.printf("Storing ticks:           %s --> %s\n", ForexTools
					.getTimeRepresentation(ticks.get(0).getTime()), ForexTools
					.getTimeRepresentation(ticks.get(ticks.size() - 1).getTime()));

			io.storeTicks(instrument, ticks);
		}
	};
	private Runnable storeBars = new Runnable() {
		@Override
		public void run() {
			for (Period p : ForexConstants.BAR_PERIODS) {
				if (!askBars.get(p).isEmpty()) {
					System.out.printf(
							"Storing bars:  %8s  %s --> %s\n",
							p.toString(),
							ForexTools.getTimeRepresentation(askBars.get(p).get(0)
									.getTime()),
							ForexTools.getTimeRepresentation(askBars.get(p)
									.get(askBars.get(p).size() - 1).getTime()));

					io.storeBars(instrument, p, OfferSide.ASK, askBars.get(p));
					io.storeBars(instrument, p, OfferSide.BID, bidBars.get(p));
				}
			}
		}
	};

	/**
	 * Create a SaveStrategy. The strategy will store ticks and bars of
	 * specified instrument in the database defined in ForexDataIO.
	 * 
	 * @param instrument the instrument of the elements to store
	 * @param mode the mode
	 * @throws IllegalArgumentExcepton if instrument is null
	 */
	public SaveStrategy(Instrument instrument) {
		if (instrument == null) {
			throw new IllegalArgumentException("instrument=null");
		}

		this.instrument = instrument;

		ticks = new ArrayList<ITick>();
		askBars = new HashMap<Period, ArrayList<IBar>>();
		bidBars = new HashMap<Period, ArrayList<IBar>>();
		for (Period p : ForexConstants.BAR_PERIODS) {
			askBars.put(p, new ArrayList<IBar>(getInitialCapacity(p)));
			bidBars.put(p, new ArrayList<IBar>(getInitialCapacity(p)));
		}

		io = ForexDataIO.getInstance();
		executor = Executors.newFixedThreadPool(2);
	}

	private int getInitialCapacity(Period period) {
		long minInterval = BASIC_BAR_PERIOD.getInterval();
		int multiple = (int) (period.getInterval() / minInterval);
		return NUM_BARS_TO_TRIGGER_STORING / multiple + 2;
	}

	@Override
	public void onStart(IContext arg0) throws JFException {
		System.out.println("STARTED SaveStrategy");
	}

	@Override
	public void onStop() throws JFException {
		// store data that still hasn't been stored
		storeTicks();
		storeBars();

		System.out.println("STOPPED SaveStrategy");
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		if (!instrument.equals(this.instrument))
			return;

		ticks.add(tick);

		if (ticks.size() >= NUM_TICKS_TO_TRIGGER_STORING)
			storeTicks();
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar)
			throws JFException {
		if (!instrument.equals(this.instrument))
			return;

		if (askBar.getTime() != bidBar.getTime())
			throw new ForexException("incoherency between ask and bid bars detected");

		// if (timeOfFirstBar == null)
		// timeOfFirstBar = askBar.getTime();

		askBars.get(period).add(askBar);
		bidBars.get(period).add(bidBar);

		if (period.equals(BASIC_BAR_PERIOD)
				&& askBars.get(period).size() >= NUM_BARS_TO_TRIGGER_STORING)
			storeBars();

		// endTimeOfLastBar = askBar.getTime() + period.getInterval();
	}

	private void storeTicks() {
		try {
			executor.submit(storeTicks).get();
		} catch (Exception e) {
			System.err.println("Failed to store ticks: " + e);
			e.printStackTrace();
			System.exit(0);
		}

		clearTickData();
	}

	private void storeBars() {
		if (askBars.get(BASIC_BAR_PERIOD).size() != askBars.get(BASIC_BAR_PERIOD).size())
			throw new ForexException("Incoherency detected between ask- and bid bars.");

		try {
			executor.submit(storeBars).get();
		} catch (Exception e) {
			System.err.println("Failed to store bars: " + e);
			e.printStackTrace();
			System.exit(0);
		}

		clearBarData();
	}

	private void clearTickData() {
		ticks.clear();
	}

	private void clearBarData() {
		for (Period p : ForexConstants.BAR_PERIODS) {
			askBars.get(p).clear();
			bidBars.get(p).clear();
		}
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
	}
}