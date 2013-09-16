package util;

public enum Instrument {
	EURUSD(false, 0.0001);

	private boolean inverted;
	private double pipValue;

	private Instrument(boolean inverted, double pipValue) {
		this.inverted = inverted;
		this.pipValue = pipValue;
	}

	/**
	 * Returns the value of one pip for this currency pair.
	 * 
	 * @return The value of one pip for this currency pair.
	 */
	public double getPipValue() {
		return pipValue;
	}

	/**
	 * Returns <code>true</code> if this currency pair is inverted. For example,
	 * since EUR/USD is not inverted, USD/EUR is.
	 * 
	 * @return <code>true</code> if this currency pair is inverted.
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Returns a string on the format "CU1/CU2".
	 * 
	 * @return A string on the format "CU1/CU2".
	 */
	@Override
	public String toString() {
		return name().substring(0, 3) + "/" + name().substring(3, 6);
	}
}