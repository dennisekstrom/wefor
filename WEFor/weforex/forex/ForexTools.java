package forex;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ForexTools {

	public static void main(String[] args) {
		System.out.println(getTimeRepresentation(43242345235L));
	}

	/**
	 * Returns a string presenting the given time on the format: yyyy-mm-dd
	 * HH:MM:SS.mmm
	 * 
	 * @param time the time the get representation for
	 * @return a string presenting the given time on the format: yyyy-mm-dd
	 *         HH:MM:SS.mmm
	 */
	public static String getTimeRepresentation(long time) {
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
	public static long getTimeOf(int year, int month, int day, int hour, int min, int sec,
			int millis) {

		Calendar c = new GregorianCalendar(ForexConstants.GMT);
		c.setTimeInMillis(millis);
		c.set(year, month, day, hour, min, sec);

		return c.getTimeInMillis();
	}

}