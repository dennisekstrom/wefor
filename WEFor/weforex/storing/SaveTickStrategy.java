//package storing;
//
//import io.ForexDataIO;
//
//import java.util.ArrayList;
//
//
//import com.dukascopy.api.*;
//
//public class SaveTickStrategy implements IStrategy {
//
//	Instrument instrument;
//	long from;
//	long to;
//
//	ArrayList<ITick> ticks;
//	ForexDataIO io;
//
//	/**
//	 * Create a SaveTickStrategy. The strategy will store ticks of an interval
//	 * of one second and given instrument and period in the database defined in
//	 * ForexDataIO.
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
//	public SaveTickStrategy(Instrument instrument, long from, long to) {
//		if (instrument == null) {
//			throw new IllegalArgumentException("instrument can't be null");
//		} else if (from > to) {
//			throw new IllegalArgumentException("from(" + from + ") > to(" + to + ")");
//		}
//
//		this.instrument = instrument;
//		this.from = from;
//		this.to = to;
//
//		// usually not more than one tick per second
//		int initialCapacity = (int) ((to - from) / Period.ONE_SEC.getInterval()) + 1;
//
//		ticks = new ArrayList<ITick>(initialCapacity);
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
//		io.storeTicks(instrument, ticks);
//	}
//
//	public void onTick(Instrument instrument, ITick tick) throws JFException {
//		if (instrument.equals(this.instrument)) {
//			ticks.add(tick);
//		}
//	}
//
//	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
//	}
//
//	public void onMessage(IMessage message) throws JFException {
//	}
//
//	public void onAccount(IAccount account) throws JFException {
//	}
//}