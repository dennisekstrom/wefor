package forex;

import java.util.Calendar;

import util.Instrument;
import util.Period;

/**
 * Datatype for a offer - a row.
 * 
 * 
 * @author Tobias W
 * 
 */

public class Offer implements Comparable<Offer> {

	private final Instrument instrument;
	private final Period period;
	private final double bidOpen;
	private final double bidHigh;
	private final double bidLow;
	private final double bidClose;
	private final double askOpen;
	private final double askHigh;
	private final double askLow;
	private final double askClose;

	private final int volume;

	private final long time;

	/**
	 * 
	 * @param bidOpen
	 * @param bidHigh
	 * @param bidLow
	 * @param bidClose
	 * @param askOpen
	 * @param askHigh
	 * @param askLow
	 * @param askClose
	 * @param time
	 * @param volume
	 */
	public Offer(Instrument instrument, Period period, double bidOpen,
			double bidHigh, double bidLow, double bidClose, double askOpen,
			double askHigh, double askLow, double askClose, long time,
			int volume) {
		this.instrument = instrument;
		this.period = period;
		this.bidOpen = bidOpen;
		this.bidHigh = bidHigh;
		this.bidLow = bidLow;
		this.bidClose = bidClose;
		this.askOpen = askOpen;
		this.askHigh = askHigh;
		this.askLow = askLow;
		this.askClose = askClose;
		this.time = time;
		this.volume = volume;
	}

	/**
	 * Converts a O2GOfferTableRow to a TradeData
	 * 
	 * @param row
	 * @return
	 */
	public static Offer convertRow(O2GOfferTableRow row) {
		Offer data = new Offer(row.getInstrument(), row.getBid(),
				row.getHigh(), row.getLow(), row.getBid(), row.getAsk(),
				row.getAsk(), row.getAsk(), row.getAsk(), row.getTime()
						.getTimeInMillis(), row.getVolume());

		return data;
	}

	public int getVolume() {
		return volume;
	}

	public double getAskClose() {
		return askClose;
	}

	public double getAskLow() {
		return askLow;
	}

	public double getAskHigh() {
		return askHigh;
	}

	public double getAskOpen() {
		return askOpen;
	}

	public double getBidClose() {
		return bidClose;
	}

	public double getBidHigh() {
		return bidHigh;
	}

	public double getBidLow() {
		return bidLow;
	}

	public double getBidOpen() {
		return bidOpen;
	}

	public long getTime() {
		return time;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public Period getPeriod() {
		return period;
	}

	@Override
	public int compareTo(Offer o) {
		if (askHigh < o.getAskHigh())
			return -1;
		else if (askHigh > o.getAskHigh())
			return 1;
		else
			return 0;
	}
}
