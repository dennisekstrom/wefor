package feed;


import java.util.HashSet;

import forex.Offer;

public abstract class OfferFeed {

	// debugging
	private boolean printSupply = false;
	private final HashSet<OfferListener> offerListeners;

	protected OfferFeed() {
		offerListeners = new HashSet<OfferListener>();
	}

	/**
	 * Returns the current time of this feed.
	 * 
	 * @return the current time of this feed
	 */
	public abstract long getCurrentTime();
	
	/**
	 * Adds an OfferListener to the feed.
	 * 
	 * @param listener the OfferListener to be added
	 */
	public final void addListener(OfferListener listener) {
		if (listener == null)
			return;

		offerListeners.add(listener);
	}

	/**
	 * Removes an ITickFeedListener to the feed.
	 * 
	 * @param listener the ITickFeedListener to be removed
	 */
	public final void removeListener(OfferListener listener) {
		offerListeners.remove(listener);
	}

	/**
	 * Supplies an Offer to registered OfferListners.
	 * 
	 * @param offer the Offer to supply
	 * @param period the Period of the offer
	 */
	protected final void supplyOffer(Offer offer) {

		if (printSupply)
			System.out.println("Supplying: " + offer.toString());

		for (OfferListener listener : offerListeners) {
			listener.onOffer(offer);
		}
	}
}