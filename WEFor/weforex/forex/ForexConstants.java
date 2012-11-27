package forex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.dukascopy.api.TickBarSize;

public class ForexConstants {

	public static void main(String[] args) {
		System.out.println(TickBarSize.JFOREX_TRADE_BAR_SIZES);
	}
	
	// application name
	public static final String APPLICATION_NAME = "WEForex";

	// magnifications for zooming
	public static final double ZOOM_IN_MAGNIFICATION = 1.25;
	public static final double ZOOM_OUT_MAGNIFICATION = 1 / ZOOM_IN_MAGNIFICATION;

	// constants for number of elements in a graph
	public static final long DEFAULT_TICK_TIME_RANGE = 100000; // 100 seconds
	public static final long MAX_TICK_TIME_RANGE = 1000000; // 1,000 seconds
	public static final long MIN_TICK_TIME_RANGE = 5000; // 5 seconds
	
	public static final int DEFAULT_NO_BARS_IN_RANGE = 100;
	public static final int MAX_NO_BARS_IN_RANGE = 1000;
	public static final int MIN_NO_BARS_IN_RANGE = 5;
	
	// time zones
	public static final SimpleTimeZone GMT = new SimpleTimeZone(0, "GMT");
	public static final Locale LOCALE = Locale.GERMANY;
//	public static final SimpleTimeZone CET_DST = new SimpleTimeZone(3600000, "CET + DST",
//			Calendar.MARCH, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
//			Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
//			3600000);

	// @formatter:off
	// allowed tick bar sizes for tick bars
	public static final List<TickBarSize> TICK_BAR_SIZES = 
			Collections.unmodifiableList(Arrays.asList(
					TickBarSize.TWO, 
					TickBarSize.THREE, 
					TickBarSize.FOUR, 
					TickBarSize.FIVE));
	
	// allowed periods for bars
	public static final List<Period> BAR_PERIODS = 
			Collections.unmodifiableList(Arrays.asList(
					Period.TEN_SECS,
					Period.ONE_MIN,
					Period.FIVE_MINS, 
					Period.TEN_MINS, 
					Period.FIFTEEN_MINS, 
					Period.THIRTY_MINS, 
					Period.ONE_HOUR, 
					Period.FOUR_HOURS, 
					Period.DAILY, 
					Period.WEEKLY, 
					Period.MONTHLY, 
					Period.ONE_YEAR));

	// instruments which the application supports
	public static final List<Instrument> INSTRUMENTS = 
			Collections.unmodifiableList(Arrays.asList(
					Instrument.EURUSD));
	// @formatter:on
}