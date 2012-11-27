package storing;

import com.dukascopy.api.*;

import forex.Bar;
import forex.Tick;

public class PrintStrategy implements IStrategy {

	Instrument instrument;

	boolean printTicks;
	boolean printBars;

	int tickCount = 0;
	int barCount = 0;

	public PrintStrategy(Instrument instrument, boolean printTicks, boolean printBars) {
		this.instrument = instrument;
		this.printTicks = printTicks;
		this.printBars = printBars;
	}

	public void onStart(IContext context) throws JFException {
		System.out.println("STARTED PrintStrategy");
	}

	public void onStop() throws JFException {
		System.out.println("Ticks printed: " + tickCount);
		System.out.println("Bars printed: " + barCount);
		System.out.println("STOPPED PrintStrategy");
	}

	public void onTick(Instrument instrument, ITick tick) throws JFException {
		if (printTicks && instrument.equals(this.instrument)) {
			System.out.println(new Tick(tick));
			tickCount++;
		}
	}

	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		if (printBars && instrument.equals(this.instrument)) {
			System.out.println("Ask: " + period + "  " + new Bar(askBar));
			System.out.println("Bid: " + period + "  " + new Bar(bidBar));
			barCount++;
		}
	}

	public void onMessage(IMessage message) throws JFException {
	}

	public void onAccount(IAccount account) throws JFException {
	}
}