package util;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import forex.ForexConstants;
import forex.IBar;
import forex.ITick;

public class ForexUtils {

	/**
	 * A Comparator to be used for SortedSets of Ticks and Bars. Does not permit
	 * null elements.
	 */
	public static final Comparator<HasTime> hasTimeComparator = new Comparator<HasTime>() {

		@Override
		public int compare(HasTime ht1, HasTime ht2) {
			long t1 = ht1.getTime();
			long t2 = ht2.getTime();
			if (t1 > t2)
				return 1;
			else if (t1 < t2)
				return -1;
			else
				return 0;
		}
	};

	/**
	 * Returns a string presenting the given time on the format: yyyy-mm-dd
	 * HH:MM:SS.mmm
	 * 
	 * @param time the time the get representation for
	 * @return a string presenting the given time on the format: yyyy-mm-dd
	 *         HH:MM:SS.mmm
	 */
	public static final String getTimeRepresentation(long time) {
		Calendar c = new GregorianCalendar(ForexConstants.GMT);
		c.setTimeInMillis(time);

		return String.format("%1$tF %1$tT.%1$tL", c);
	}

	/**
	 * TODO comment
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param min
	 * @param sec
	 * @param millis
	 * @return
	 */
	public static final long getTimeOf(int year, int month, int day, int hour,
			int min, int sec, int millis) {

		Calendar c = new GregorianCalendar(ForexConstants.GMT);
		c.setTimeInMillis(millis);
		c.set(year, month, day, hour, min, sec);

		return c.getTimeInMillis();
	}

	/**
	 * If the given period is Tick, the time is returned.
	 * 
	 * Otherwise, the first time stamp contained by the period containing the
	 * given time is returned.
	 */
	public static final long getStartTime(Period period, long time) {
		if (period == Period.TICK) {
			return time;
		} else if (period != Period.W1) {
			return (time / period.getInterval()) * period.getInterval();
		} else {
			// 1970-01-01 was a Thursday
			return ((time - 3 * Period.D1.getInterval()) / period
					.getInterval())
					* period.getInterval()
					+ 3
					* Period.D1.getInterval();

		}
	}

	/**
	 * If the given period is Tick, the time of the tick is returned.
	 * 
	 * Otherwise, the last time stamp contained by the period containing the
	 * given time is returned. This is equivalent to
	 * <code>ForexUtils.getAfterTime(period, time) - 1</code>.
	 */
	public static final long getEndTime(Period period, long time) {
		if (period == Period.TICK)
			return time;
		else
			return getStartTime(period, time) + period.getInterval() - 1;
	}

	/**
	 * If the given period is Tick, the time of the tick is returned.
	 * 
	 * Otherwise, the first time stamp after the period containing the given
	 * time is returned. This is equivalent to
	 * <code>ForexUtils.getStartTime(period, time) + period.getInterval()</code>
	 * .
	 */
	public static final long getAfterTime(Period period, long time) {
		if (period == Period.TICK)
			return time;
		else
			return getStartTime(period, time) + period.getInterval();
	}

	public static HasTime longToHasTime(final long time) {
		return new HasTime() {
			@Override
			public long getTime() {
				return time;
			}
		};
	}

	public static ITick longToTimeTick(final long time) {
		return new TickAdapter() {
			@Override
			public long getTime() {
				return time;
			}
		};
	}

	public static IBar longToTimeBar(final long time) {
		return new BarAdapter() {
			@Override
			public long getTime() {
				return time;
			}
		};
	}
}