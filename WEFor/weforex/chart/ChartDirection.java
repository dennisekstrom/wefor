package chart;

/**
 * This class describes a direction in a chart, having time and rate
 * differences.
 */
public final class ChartDirection {
	public final long timeDiff;
	public final double rateDiff;

	public ChartDirection(long timeDiff, double rateDiff) {
		this.timeDiff = timeDiff;
		this.rateDiff = rateDiff;
	}

	/**
	 * Returns true if this direction is determined, otherwise false. A
	 * direction is determined if either timeDiff or rateDiff is nonzero.
	 * 
	 * @return true if this direction is determined, otherwise false
	 */
	public boolean isDetermined() {
		return timeDiff != 0 || rateDiff != 0;
	}

	/**
	 * Returns a ChartDirection with an inverted direction relative to this.
	 * 
	 * @return a ChartDirection with an inverted direction relative to this
	 */
	public ChartDirection getInverse() {
		return new ChartDirection(timeDiff, -rateDiff);
	}

	@Override
	public String toString() {
		return "(time, rate) = (" + timeDiff + ", " + rateDiff + ")";
	}
}