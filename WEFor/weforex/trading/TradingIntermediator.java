package trading;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

/**
 * Class to keep track of the current strategy of this trading session, as well
 * as to provide a link between JForex- and WEForex strategies.
 * 
 * @author Dennis Ekstrom
 */
public final class TradingIntermediator {

	private static IEngine engine;
	private static IHistory history;
	private static Strategy currentStrategy;

	/**
	 * Sets the current strategy of this trading session.
	 * 
	 * @param strategy the strategy to set
	 */
	public static void setCurrentStrategy(Strategy strategy) {
		currentStrategy = strategy;
	}

	/**
	 * Returns the engine of this trading session.
	 * 
	 * @return the engine of this trading session
	 */
	public static IEngine getEngine() {
		return engine;
	}

	/**
	 * Returns the history of this trading session.
	 * 
	 * @return the history of this trading session
	 */
	public static IHistory getHistory() {
		return history;
	}

	public static void onStart(IContext context) {
		engine = context.getEngine();
		history = context.getHistory();

		currentStrategy.onStart();
	}

	public static void onStop() {
		currentStrategy.onStop();
	}

	public static void onTick(Instrument instrument, ITick tick) throws JFException {
		currentStrategy.onTick(instrument, tick);
	}

	public static void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {
		currentStrategy.onBar(instrument, period, askBar, bidBar);
	}

	public static void onMessage(IMessage message) {
		currentStrategy.onMessage(message);
	}

	public static void onAccount(IAccount account) {
		currentStrategy.onAccount(account);
	}
}