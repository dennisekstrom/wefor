package feed;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

/**
 * Describes a listener which listens to ticks supplied by a ForexDataFeed.
 * 
 * @author Dennis Ekstrom
 */
public interface ITickFeedListener {

	/**
	 * The method is being called when a Tick arrives
	 * 
	 * @param instrument the instrument of the tick
	 * @param tick the tick
	 */
	public void onTick(
			Instrument instrument, 
			ITick tick);
}