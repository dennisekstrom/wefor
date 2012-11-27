package feed;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

/**
 * Describes a bar requester which can request bars from a Provider.
 * 
 * @author Dennis Ekstrom
 */
public interface BarRequester extends Requester {

	/**
	 * Returns the period of bars being requested.
	 * 
	 * @return the period of bars being requested
	 */
	public Period getPeriod();

	/**
	 * Returns the offer side of bars being requested.
	 * 
	 * @return the offer side of bars being requested
	 */
	public OfferSide getOfferSide();
}