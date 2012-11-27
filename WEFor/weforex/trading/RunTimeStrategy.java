package trading;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import feed.TimeRelativeFeed;

public class RunTimeStrategy extends TimeRelativeFeed implements IStrategy {

	private long currentTime;

	@Override
	public void onStart(IContext context) throws JFException {
		TradingIntermediator.onStart(context);
	}

	@Override
	public void onStop() throws JFException {
		TradingIntermediator.onStop();
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		TradingIntermediator.onTick(instrument, tick);
		supplyTick(instrument, tick);

		currentTime = tick.getTime();
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar)
			throws JFException {
		TradingIntermediator.onBar(instrument, period, askBar, bidBar);
		supplyBars(instrument, period, askBar, bidBar);

		currentTime = askBar.getTime() + period.getInterval();
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		TradingIntermediator.onMessage(message);
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		TradingIntermediator.onAccount(account);
	}

	@Override
	public long getCurrentTime() {
		return currentTime;
	}

	@Override
	public void startFeed() {
		// do nothing
	}

	@Override
	public void stopFeed() {
		// do nothing
	}
}