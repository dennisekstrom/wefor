package feed;

import com.dukascopy.api.Instrument;

/**
 * Describes a requester which can request data from a Provider.
 * 
 * A bar provider having been given this requester won't provide elements of
 * later time than the time returned by Requestor.getUpperTimeLimit().
 * 
 * @author Dennis Ekstrom
 * 
 */
public interface Requester {

	/**
	 * The upper time limit of this requester. A provider having been given this
	 * requester won't provide ticks or bars that would not have been supplied
	 * by a feed at current time equal to the returned value of this method.
	 * 
	 * @return the upper time limit of this requester
	 */
	public long getUpperTimeLimit();

	/**
	 * Returns the instrument of bars being requested.
	 * 
	 * @return the instrument of bars being requested
	 */
	public Instrument getInstrument();
}