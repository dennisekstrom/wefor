package forex;

import util.HasTime;

/**
 * Interface to describe a bar.
 * 
 * @author Dennis Ekstrom
 */
public interface IBar extends HasTime {

	/**
	 * @return the closing price
	 */
	public double getClose();

	/**
	 * @return the highest price
	 */
	public double getHigh();

	/**
	 * @return the lowest price
	 */
	public double getLow();

	/**
	 * @return the opening price
	 */
	public double getOpen();

	/**
	 * Returns the volume of the bar. This is the sum of volumes of all ticks in
	 * this bar.
	 * 
	 * @return the volume of the bar.
	 */
	public long getVolume();
}