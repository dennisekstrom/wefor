package forex;

import util.HasTime;

/**
 * Interface to describe a tick.
 * 
 * @author Dennis Ekstrom
 */
public interface ITick extends HasTime {

	/**
	 * Returns the ask price of the tick.
	 * 
	 * @returns the ask price of the tick
	 */
	public double getAsk();

	/**
	 * Returns the bid price of the tick.
	 * 
	 * @returns the bid price of the tick
	 */
	public double getBid();

	/**
	 * Returns the volume of the tick, should always be 1.
	 * 
	 * @returns the volume of the tick, should always be 1
	 */
	public long getVolume();
}