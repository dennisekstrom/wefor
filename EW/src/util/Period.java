package util;

/**
 * Represents a period of time. Special case is TICK, which interval is set to
 * -1 to indicate a TICK has no period.
 * 
 * @author Dennis
 */
public enum Period {
	// @formatter:off
	TICK(0, "Tick", TimeUnit.MS), 
	M1(1, "1 Min", TimeUnit.MINUTE), 
	M5(5, "5 Min", TimeUnit.MINUTE),
	M15(15, "15 Min", TimeUnit.MINUTE),
	M30(30, "30 Min", TimeUnit.MINUTE),
	H1(1, "1 Hour", TimeUnit.HOUR),
	H2(2, "2 Hour", TimeUnit.HOUR), 
	H3(3, "3 Hour", TimeUnit.HOUR), 
	H4(4, "4 Hour", TimeUnit.HOUR), 
	H6(6, "6 Hour", TimeUnit.HOUR), 
	H8(8, "8 Hour", TimeUnit.HOUR), 
	D1(1, "1 Day", TimeUnit.DAY), 
	W1(7, "1 Week", TimeUnit.DAY);
	// @formatter:on

	private enum TimeUnit {
		MS(1), SECOND(1000), MINUTE(60000), HOUR(3600000), DAY(24 * 3600000);

		final int ms;

		TimeUnit(int ms) {
			this.ms = ms;
		}
	}

	private final long millis;
	private final String label;

	private Period(int val, String label, TimeUnit unit) {
		this.millis = val * unit.ms;
		this.label = label;
	}

	public boolean isBarPeriod() {
		return this != Period.TICK;
	}

	public long getInterval() {
		return millis;
	}
	
	public String toString() {
		return label;
	}
}