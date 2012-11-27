package client;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.TickBarSize;

/**
 * Temporary constants while there's no user interface to set them.
 * 
 * @author Tobias
 * 
 */
public final class TempConstants {

	// should be chosen by the user at login //TODO
	public static final double LEVERAGE = 1000.0;
	public static final Instrument defaultInstrument = Instrument.EURUSD;
	public static final Period defaultPeriod = Period.TEN_MINS;
	public static final OfferSide defaultOfferSide = OfferSide.ASK;
	public static final Period defaultTickInterval = Period.TICK;
	public static final TickBarSize defaultTickBarSize = TickBarSize.TWO;
	public static final Double defaultSpeed = 1000D;
	public static final int defaultUpdateInterval = 10;

}
