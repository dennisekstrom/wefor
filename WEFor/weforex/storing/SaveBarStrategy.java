//package storing;
//
//import io.ForexDataIO;
//
//import java.util.ArrayList;
//
//
//import com.dukascopy.api.*;
//
//public class SaveBarStrategy implements IStrategy {
//
//	Instrument instrument;
//	Period period;
//	long from;
//	long to;
//
//	ArrayList<IBar> askBars;
//	ArrayList<IBar> bidBars;
//	ForexDataIO io;
//
//	/**
//	 * Create a SaveBarStrategy. The strategy will store bars of specified
//	 * instrument and period in the database defined in ForexDataIO.
//	 * 
//	 * The specified values from and to should be equivalent to those set using
//	 * IClient.setDataInterval(DataLoadingMethod, from, to). Functionality does
//	 * not depend on the value of the interval [from, to), but a correct
//	 * interval will improve efficiency.
//	 * 
//	 * @param instrument
//	 * @param period
//	 * @param from
//	 * @param to
//	 * @param interval the interval of the bars to save
//	 * @throws TODO
//	 */
//	public SaveBarStrategy(Instrument instrument, Period period, long from, long to) {
//		if (instrument == null || period == null) {
//			throw new IllegalArgumentException("Parameters can't be null.");
//		} else if (from > to) {
//			throw new IllegalArgumentException("from(" + from + ") > to(" + to + ")");
//		}
//
//		this.instrument = instrument;
//		this.period = period;
//		this.from = from;
//		this.to = to;
//
//		int initialCapacity = (int) ((to - from) / period.getInterval()) + 1;
//
//		askBars = new ArrayList<IBar>(initialCapacity);
//		bidBars = new ArrayList<IBar>(initialCapacity);
//
//		io = new ForexDataIO();
//	}
//
//	public void onStart(IContext context) throws JFException {
//		System.out.println("STRATEGY STARTED");
//
//	}
//
//	public void onStop() throws JFException {
//		System.out.println("STRATEGY STOPPED");
//
//		io.storeBars(instrument, period, OfferSide.ASK, askBars);
//		io.storeBars(instrument, period, OfferSide.BID, bidBars);
//	}
//
//	public void onTick(Instrument instrument, ITick tick) throws JFException {
//	}
//
//	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
//		if (instrument.equals(this.instrument) && period.equals(this.period)) {
//			askBars.add(askBar);
//			bidBars.add(bidBar);
//		}
//	}
//
//	public void onMessage(IMessage message) throws JFException {
//	}
//
//	public void onAccount(IAccount account) throws JFException {
//	}
//}