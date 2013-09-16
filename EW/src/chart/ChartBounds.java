package chart;

/**
 * This class represents the bounds of a chart. Start and end time combined with
 * high and low rate determines a chart bounds field.
 * 
 * @author Dennis Ekstrom
 */
public final class ChartBounds {

	public final long startTime;
	public final long endTime;
	public final double highRate;
	public final double lowRate;

	public ChartBounds(long time1, long time2, double rate1, double rate2) {
		this.startTime = Math.min(time1, time2);
		this.endTime = Math.max(time1, time2);
		this.highRate = Math.max(rate1, rate2);
		this.lowRate = Math.min(rate1, rate2);
	}

	public ChartBounds(ChartPoint cp1, ChartPoint cp2) {
		this(cp1.time, cp2.time, cp1.rate, cp2.rate);
	}

	/**
	 * Returns true if the given ChartBounds overlaps this ChartBounds,
	 * otherwise false. Sharing an edge counts as overlapping.
	 * 
	 * @param bounds the ChartBounds to check if they're overlapping
	 * @return true if the given ChartBounds overlaps this ChartBounds,
	 *         otherwise false
	 */
	public boolean overlaps(ChartBounds bounds) {
		return bounds.startTime < this.endTime && bounds.endTime > this.startTime
				&& bounds.lowRate < this.highRate && bounds.highRate > this.lowRate;
	}

	/**
	 * Returns true if the given ChartPoint is within the bounds, otherwise
	 * false. A ChartPoint is within the bounds if it lies within its limits or
	 * on the line.
	 * 
	 * @param cp the ChartPoint to check whether it lies within the ChartBounds.
	 * @return true if the given ChartPoint is within the bounds, otherwise
	 *         false
	 */
	public boolean withingBounds(ChartPoint cp) {
		return withinTimeRange(cp) && withinRateRange(cp);
	}

	/**
	 * Returns true if the given ChartPoint is within the time range of the
	 * bounds, otherwise false. A ChartPoint is within the time range if it lies
	 * within its limits or on the line.
	 * 
	 * @param cp the ChartPoint to check whether it lies within the time range.
	 * @return true if the given ChartPoint is within the time range of the
	 *         bounds, otherwise false
	 */
	public boolean withinTimeRange(ChartPoint cp) {
		return startTime <= cp.time && cp.time <= endTime;
	}

	/**
	 * Returns true if the given ChartPoint is within the rate range of the
	 * bounds, otherwise false. A ChartPoint is within the rate range if it lies
	 * within its limits or on the line.
	 * 
	 * @param cp the ChartPoint to check whether it lies within the rate range.
	 * @return true if the given ChartPoint is within the rate range of the
	 *         bounds, otherwise false
	 */
	public boolean withinRateRange(ChartPoint cp) {
		return lowRate <= cp.rate && cp.rate <= highRate;
	}

}