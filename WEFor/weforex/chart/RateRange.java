package chart;

public final class RateRange implements Comparable<RateRange> {

	public final double lowRate;
	public final double highRate;

	/**
	 * Create a RateRange.
	 * 
	 * @param rate1 one edge
	 * @param rate2 the other edge
	 */
	public RateRange(double rate1, double rate2) {
		this.lowRate = Math.min(rate1, rate2);
		this.highRate = Math.max(rate1, rate2);
	}

	/**
	 * Returns true if the given rate is within this rate range, otherwise
	 * false. Rate on the edge counts as in.
	 * 
	 * @return true if the given rate is within this rate range, otherwise false
	 */
	public boolean inRange(double rate) {
		return lowRate <= rate && rate <= highRate;
	}

	/**
	 * Returns true if the given RateRange overlaps this RateRange, otherwise
	 * false. Sharing an edge counts as overlapping.
	 * 
	 * @param range the RateRange to check if it's overlapping
	 * @return true if the given RateRange overlaps this RateRange, otherwise
	 *         false
	 */
	public boolean overlaps(RateRange range) {
		return range.lowRate < this.highRate && range.highRate > this.lowRate;
	}

	/**
	 * Returns the interval of this rate range, that is, the rate difference
	 * between highRate and lowRate.
	 * 
	 * @return the interval of this rate range
	 */
	public double getInterval() {
		return highRate - lowRate;
	}

	/**
	 * Returns true if o is an instance of RateRange and this.lowRate ==
	 * range.lowRate && this.highRate == range.highRate, otherwise false.
	 * 
	 * @return true if o is an instance of RateRange and this.lowRate ==
	 *         range.lowRate && this.highRate == range.highRate, otherwise false
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof RateRange) {

			RateRange range = (RateRange) o;

			return this.lowRate == range.lowRate && this.highRate == range.highRate;
		}

		return false;
	}

	/**
	 * Returns 0 if both lowRate and highRate are equal for this and the given
	 * range.
	 * 
	 * Returns a positive integer if this.lowRate > range.lowRate, or, if
	 * lowRates are equal, if this.highRate > range.highRate.
	 * 
	 * Returns a negative integer if this.lowRate < range.lowRate, or, if
	 * lowRates are equal, if this.highRate < range.highRate.
	 * 
	 * @param range the RateRange to compare this RateRange to
	 * @return 0 if both lowRate and highRate are equal for this and the given
	 *         range.
	 * 
	 *         A positive integer if this.lowRate > range.lowRate, or, if
	 *         lowRates are equal, if this.highRate > range.highRate.
	 * 
	 *         A negative integer if this.lowRate < range.lowRate, or, if
	 *         lowRates are equal, if this.highRate < range.highRate.
	 */
	@Override
	public int compareTo(RateRange range) {
		if (this.lowRate == range.lowRate && this.highRate == range.highRate)
			return 0;

		if (this.lowRate > range.lowRate) {

			return 1;

		} else if (this.lowRate < range.lowRate) {

			return -1;

		} else { // low rates are equal

			if (this.highRate > range.highRate)
				return 1;
			else
				return -1;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " high: " + highRate + " low: " + lowRate;
	}
}