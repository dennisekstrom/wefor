package util;

public final class Interval {
	public final long start, end;

	public Interval(long start, long end) {
		if (start > end)
			throw new IllegalArgumentException(String.format(
					"start > end (%d > %d)", start, end));

		this.start = start;
		this.end = end;
	}

	/**
	 * @return the difference between end and start times. (end - start)
	 */
	public long timeDiff() {
		return end - start;
	}

	/**
	 * @return true if this interval fully contains the given interval. Common
	 *         edges counts as containing.
	 */
	public boolean contains(Interval interval) {
		return this.start <= interval.start && interval.end <= this.end;
	}

	/**
	 * @return true if this interval fully or partly contains the given
	 *         interval. Common edges counts as overlapping.
	 */
	public boolean overlaps(Interval interval) {
		return interval.start <= end && interval.end >= start;
	}

	/**
	 * @return true if the given time stamp is within the interval. Time stamps
	 *         on edges counts as in.
	 */
	public boolean inInterval(long time) {
		return start <= time && time <= end;
	}

	/**
	 * @return the intervals of time stamps that are in this interval but not in
	 *         the given.
	 */
	public Interval[] difference(Interval interval) {
		if (!overlaps(interval) || interval.contains(this)) {
			return new Interval[] {};
		} else if (interval.start <= this.start) {
			Interval endPart = new Interval(interval.end + 1, this.end);
			return new Interval[] { endPart };
		} else if (interval.end >= this.end) {
			Interval startPart = new Interval(this.start, interval.start - 1);
			return new Interval[] { startPart };
		} else {
			Interval startPart = new Interval(this.start, interval.start - 1);
			Interval endPart = new Interval(interval.end + 1, this.end);
			return new Interval[] { startPart, endPart };
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval))
			return false;

		Interval interval = (Interval) o;

		return this.start == interval.start && this.end == interval.end;
	}

	@Override
	public Interval clone() {
		return new Interval(this.start, this.end);
	}

	@Override
	public String toString() {
		return String.format("%s <start=%d end=%d>",
				this.getClass().getName(), start, end);
	}
}