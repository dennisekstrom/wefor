//package feed;
//
//import java.util.HashSet;
//
//import forex.IBar;
//import forex.ITick;
//
//import util.Instrument;
//import util.OfferSide;
//import util.Period;
//
//public abstract class Feed {
//
//	// debugging
//	private boolean printTickSupply = false;
//	private boolean printBarSupply = false;
//
//	private HashSet<ITickFeedListener> tickFeedListeners;
//	private HashSet<IBarFeedListener> barFeedListeners;
//
//	protected Feed() {
//		tickFeedListeners = new HashSet<ITickFeedListener>();
//		barFeedListeners = new HashSet<IBarFeedListener>();
//	}
//
//	/**
//	 * Starts the feed.
//	 */
//	public abstract void startFeed();
//
//	/**
//	 * Stops the feed.
//	 */
//	public abstract void stopFeed();
//	
//	/**
//	 * Returns the current time of the feed.
//	 */
//	public abstract long getCurrentTime();
//	
//	/**
//	 * Adds an ITickFeedListener to the feed.
//	 * 
//	 * @param listener the ITickFeedListener to be added
//	 */
//	public final void addTickFeedListener(ITickFeedListener listener) {
//		if (listener == null)
//			return;
//
//		tickFeedListeners.add(listener);
//	}
//
//	/**
//	 * Removes an ITickFeedListener to the feed.
//	 * 
//	 * @param listener the ITickFeedListener to be removed
//	 */
//	public final void removeTickFeedListener(ITickFeedListener listener) {
//		tickFeedListeners.remove(listener);
//	}
//
//	/**
//	 * Adds an IBarFeedListener to the feed.
//	 * 
//	 * @param listener the IBarFeedListener to be added
//	 */
//	public final void addBarFeedListener(IBarFeedListener listener) {
//		if (listener == null)
//			return;
//
//		barFeedListeners.add(listener);
//	}
//
//	/**
//	 * Removes an IBarFeedListener to the feed.
//	 * 
//	 * @param listener the IBarFeedListener to be removed
//	 */
//	public final void removeBarFeedListener(IBarFeedListener listener) {
//		barFeedListeners.remove(listener);
//	}
//
//	/**
//	 * Add a listener to all possible feeds.
//	 * 
//	 * @param listener the listener to be added to all possible feeds
//	 */
//	public final void addListener(Object listener) {
//		if (listener == null)
//			return;
//
//		if (listener instanceof ITickFeedListener)
//			tickFeedListeners.add((ITickFeedListener) listener);
//
//		if (listener instanceof IBarFeedListener)
//			barFeedListeners.add((IBarFeedListener) listener);
//	}
//
//	/**
//	 * Removes a listener from all feeds.
//	 * 
//	 * @param listener the listener to be removed form all feeds
//	 */
//	public final void removeListener(Object listener) {
//		if (listener == null)
//			return;
//
//		if (listener instanceof ITickFeedListener)
//			tickFeedListeners.remove(listener);
//
//		if (listener instanceof IBarFeedListener)
//			barFeedListeners.remove(listener);
//	}
//
//	/**
//	 * Supplies a Tick to registered ITickFeedListeners if an appropriate tick
//	 * is available.
//	 */
//	protected final void supplyTick(Instrument instrument, ITick tick) {
//
//		if (printTickSupply)
//			System.out.printf("%-11s%s%n", "", tick);
//
//		for (ITickFeedListener listener : tickFeedListeners) {
//			listener.onTick(instrument, tick);
//		}
//	}
//
//	/**
//	 * Supplies an askBar with corresponding bidBar to registered
//	 * ITickFeedListeners if appropriate bars are available.
//	 */
//	protected final void supplyBars(Instrument instrument, Period barPeriod, IBar askBar,
//			IBar bidBar) {
//
//		if (printBarSupply)
//			System.out.printf("%-12s%s%n", barPeriod, askBar);
//
//		for (IBarFeedListener listener : barFeedListeners) {
//			listener.onBar(instrument, barPeriod, OfferSide.ASK, askBar);
//			listener.onBar(instrument, barPeriod, OfferSide.BID, bidBar);
//		}
//	}
//
//}