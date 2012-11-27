package trading;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public interface Strategy {

	public void onStart();

	public void onStop();

	public void onTick(Instrument instrument, ITick tick);

	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar);

	public void onMessage(IMessage message);

	public void onAccount(IAccount account);
}