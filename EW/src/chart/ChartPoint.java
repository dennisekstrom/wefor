package chart;

import util.HasTime;

/**
 * This class describes a point in a chart and has a time and a rate coordinate.
 * 
 * @author Dennis Ekstrom
 */
public final class ChartPoint implements HasTime {
	public final long time;
	public final double rate;
	
	public ChartPoint(long time, double rate) {
		this.time = time;
		this.rate = rate;
	}
	
	@Override
	public long getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		return "time=" + time + " rate=" + rate;
	}
}