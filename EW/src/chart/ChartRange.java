//package chart;
//
//public class ChartRange {
//	public final long startTime;
//	public final long endTime;
//	public final double highRate;
//	public final double lowRate;
//
//	public ChartRange(long time1, double rate1, long time2, double rate2) {
//		this.startTime = Math.min(time1, time2);
//		this.endTime = Math.max(time1, time2);
//		this.highRate = Math.max(rate1, rate2);
//		this.lowRate = Math.min(rate1, rate2);
//	}
//
//	public ChartRange(ChartPoint cp1, ChartPoint cp2) {
//		this(cp1.time, cp1.rate, cp2.time, cp2.rate);
//	}
//	
//	public ChartBounds toChartBounds() {
//		return new ChartBounds(startTime, endTime, highRate, lowRate);
//	}
//}