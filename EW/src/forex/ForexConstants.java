package forex;

import util.Period;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import util.Instrument;

public class ForexConstants {

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
	// public static final SimpleTimeZone CET_DST = new SimpleTimeZone(3600000,
	// "CET + DST",
	// Calendar.MARCH, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
	// Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000, SimpleTimeZone.UTC_TIME,
	// 3600000);

	// @formatter:off
	// allowed periods for bars
	public static final List<Period> BAR_PERIODS = 
			Collections.unmodifiableList(getBarPeriods());

	// instruments which the application supports
	public static final List<Instrument> INSTRUMENTS = 
			Collections.unmodifiableList(Arrays.asList(
					Instrument.EURUSD));
	// @formatter:on

	private static List<Period> getBarPeriods() {
		List<Period> ret = Arrays.asList(Period.values());
//		ret.remove(Period.TICK);
		return ret;
	}
}