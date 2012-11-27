package feed;

import java.util.HashSet;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.TickBarSize;
import com.dukascopy.api.feed.IBarFeedListener;
import com.dukascopy.api.feed.ITickBar;
import com.dukascopy.api.feed.ITickBarFeedListener;

public abstract class Feed {

	// debugging
	private boolean printTickSupply = false;
	private boolean printTickBarSupply = false;
	private boolean printBarSupply = false;

	private HashSet<ITickFeedListener> tickFeedListeners;
	private HashSet<ITickBarFeedListener> tickBarFeedListeners;
	private HashSet<IBarFeedListener> barFeedListeners;

	protected Feed() {
		tickFeedListeners = new HashSet<ITickFeedListener>();
		tickBarFeedListeners = new HashSet<ITickBarFeedListener>();
		barFeedListeners = new HashSet<IBarFeedListener>();
	}

	/**
	 * Starts the feed.
	 */
	public abstract void startFeed();

	/**
	 * Stops the feed.
	 */
	public abstract void stopFeed();
	
	/**
	 * Adds an ITickFeedListener to the feed.
	 * 
	 * @param listener the ITickFeedListener to be added
	 */
	public final void addTickFeedListener(ITickFeedListener listener) {
		if (listener == null)
			return;

		tickFeedListeners.add(listener);
	}

	/**
	 * Removes an ITickFeedListener to the feed.
	 * 
	 * @param listener the ITickFeedListener to be removed
	 */
	public final void removeTickFeedListener(ITickFeedListener listener) {
		tickFeedListeners.remove(listener);
	}

	/**
	 * Adds an ITickBarFeedListener to the feed.
	 * 
	 * @param listener the ITickBarFeedListener to be added
	 */
	public final void addTickBarFeedListener(ITickBarFeedListener listener) {
		if (listener == null)
			return;

		tickBarFeedListeners.add(listener);
	}

	/**
	 * Removes an ITickBarFeedListener to the feed.
	 * 
	 * @param listener the ITickBarFeedListener to be removed
	 */
	public final void removeTickBarFeedListener(ITickBarFeedListener listener) {
		tickBarFeedListeners.remove(listener);
	}

	/**
	 * Adds an IBarFeedListener to the feed.
	 * 
	 * @param listener the IBarFeedListener to be added
	 */
	public final void addBarFeedListener(IBarFeedListener listener) {
		if (listener == null)
			return;

		barFeedListeners.add(listener);
	}

	/**
	 * Removes an IBarFeedListener to the feed.
	 * 
	 * @param listener the IBarFeedListener to be removed
	 */
	public final void removeBarFeedListener(IBarFeedListener listener) {
		barFeedListeners.remove(listener);
	}

	/**
	 * Add a listener to all possible feeds.
	 * 
	 * @param listener the listener to be added to all possible feeds
	 */
	public final void addListener(Object listener) {
		if (listener == null)
			return;

		if (listener instanceof ITickFeedListener)
			tickFeedListeners.add((ITickFeedListener) listener);

		if (listener instanceof ITickBarFeedListener)
			tickBarFeedListeners.add((ITickBarFeedListener) listener);

		if (listener instanceof IBarFeedListener)
			barFeedListeners.add((IBarFeedListener) listener);
	}

	/**
	 * Removes a listener from all feeds.
	 * 
	 * @param listener the listener to be removed form all feeds
	 */
	public final void removeListener(Object listener) {
		if (listener == null)
			return;

		if (listener instanceof ITickFeedListener)
			tickFeedListeners.remove(listener);

		if (listener instanceof ITickBarFeedListener)
			tickBarFeedListeners.remove(listener);

		if (listener instanceof IBarFeedListener)
			barFeedListeners.remove(listener);
	}

	/**
	 * Supplies a Tick to registered ITickFeedListeners if an appropriate tick
	 * is available.
	 */
	protected final void supplyTick(Instrument instrument, ITick tick) {

		if (printTickSupply)
			System.out.printf("%-11s%s%n", "", tick);

		for (ITickFeedListener listener : tickFeedListeners) {
			listener.onTick(instrument, tick);
		}
	}

	/**
	 * Supplies an askTickBar with corresponding bidTickBar to registered
	 * ITickBarFeedListeners.
	 */
	protected final void supplyTickBars(Instrument instrument, TickBarSize tickBarSize,
			ITickBar askTickBar, ITickBar bidTickBar) {

		if (printTickBarSupply)
			System.out.printf("%-2s%s%n", tickBarSize, askTickBar);

		for (ITickBarFeedListener listener : tickBarFeedListeners) {
			listener.onBar(instrument, OfferSide.ASK, tickBarSize, askTickBar);
			listener.onBar(instrument, OfferSide.BID, tickBarSize, bidTickBar);
		}
	}

	/**
	 * Supplies an askBar with corresponding bidBar to registered
	 * ITickFeedListeners if appropriate bars are available.
	 */
	protected final void supplyBars(Instrument instrument, Period barPeriod, IBar askBar,
			IBar bidBar) {

		if (printBarSupply)
			System.out.printf("%-12s%s%n", barPeriod, askBar);

		for (IBarFeedListener listener : barFeedListeners) {
			listener.onBar(instrument, barPeriod, OfferSide.ASK, askBar);
			listener.onBar(instrument, barPeriod, OfferSide.BID, bidBar);
		}
	}

}