package chart;

import util.ForexUtils;

public final class TimeRange implements Comparable<TimeRange> {

	public final long startTime;
	public final long endTime;

	/**
	 * Create a TimeRange.
	 * 
	 * @param time1
	 *            one edge
	 * @param time2
	 *            the other edge
	 */
	public TimeRange(long time1, long time2) {
		this.startTime = Math.min(time1, time2);
		this.endTime = Math.max(time1, time2);
	}

	/**
	 * Returns true if the given time stamp is within this time range, otherwise
	 * false. Time stamp on the edge counts as in.
	 * 
	 * @return true if the given time stamp is within this time range, otherwise
	 *         false
	 */
	public boolean inRange(long time) {
		return startTime <= time && time <= endTime;
	}

	/**
	 * Returns true if the given TimeRange overlaps this TimeRange, otherwise
	 * false. Sharing an edge counts as overlapping.
	 * 
	 * @param range
	 *            the TimeRange to check if it's overlapping
	 * @return true if the given TimeRange overlaps this TimeRange, otherwise
	 *         false
	 */
	public boolean overlaps(TimeRange range) {
		return range.startTime <= this.endTime
				&& range.endTime >= this.startTime;
	}

	/**
	 * Returns the interval of this time range, that is, the time difference
	 * between startTime and EndTime.
	 * 
	 * @return the interval of this time range
	 */
	public long getInterval() {
		return endTime - startTime;
	}

	/**
	 * Returns true if o is an instance of TimeRange and this.startTime ==
	 * range.startTime && this.endTime == range.endTime, otherwise false.
	 * 
	 * @return true if o is an instance of TimeRange and this.startTime ==
	 *         range.startTime && this.endTime == range.endTime, otherwise false
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof TimeRange) {

			TimeRange range = (TimeRange) o;

			return this.startTime == range.startTime
					&& this.endTime == range.endTime;
		}

		return false;
	}

	/**
	 * Returns 0 if both startTime and endTime are equal for this and the given
	 * range.
	 * 
	 * Returns a positive integer if this.startTime > range.startTime, or, if
	 * startTimes are equal, if this.endTime > range.endTime.
	 * 
	 * Returns a negative integer if this.startTime < range.startTime, or, if
	 * startTimes are equal, if this.endTime < range.endTime.
	 * 
	 * @param range
	 *            the RateRange to compare this RateRange to
	 * @return 0 if both startTime and endTime are equal for this and the given
	 *         range.
	 * 
	 *         A positive integer if this.startTime > range.startTime, or, if
	 *         startTimes are equal, if this.endTime > range.endTime.
	 * 
	 *         A negative integer if this.startTime < range.startTime, or, if
	 *         startTimes are equal, if this.endTime < range.endTime.
	 */
	@Override
	public int compareTo(TimeRange range) {
		if (this.startTime == range.startTime && this.endTime == range.endTime)
			return 0;

		if (this.startTime > range.startTime) {

			return 1;

		} else if (this.startTime < range.startTime) {

			return -1;

		} else { // start times are equal

			if (this.endTime > range.endTime)
				return 1;
			else
				return -1;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " start: "
				+ ForexUtils.getTimeRepresentation(startTime) + " end: "
				+ ForexUtils.getTimeRepresentation(endTime);
	}
}