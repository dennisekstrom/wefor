package graph;

import java.beans.PropertyChangeListener;

import chart.ChartBounds;

/**
 * Describes a graph which provides useful tools for user interaction.
 * 
 * @author Dennis Ekstrom
 */
public interface InteractiveGraph {
	/**
	 * Zoom this graph so given ChartBounds define the time and rate ranges.
	 * 
	 * @param bounds the ChartBounds to use for setting time and rate ranges.
	 */
	public void setBounds(ChartBounds bounds);

	/**
	 * Zoom this graph with given magnification. Magnification > 1 means zooming
	 * in, magnification < 1 means zooming out.
	 * 
	 * @param magnification the magnification of the zooming
	 * @throws IllegalArgumentException if magnification <= 0
	 */
	public void zoom(double magnification);

	/**
	 * Sets whether rates should be adjusted according to feed.
	 * 
	 * @param adjustRates true if rates should be adjusted to feed
	 */
	public void setAdjustRatesToFeed(boolean adjustRates);

	/**
	 * Returns a boolean representing whether rates should adjust to feed or
	 * not.
	 * 
	 * @return true if rates should adjust to feed, otherwise false
	 */
	public boolean isRateAdjustingToFeed();

	/**
	 * Returns a boolean representing whether the graph should follow newly
	 * supplied elements, adjusting the time range accordingly.
	 * 
	 * @return true if time range should adjust to feed, otherwise false
	 */
	public boolean isFollowingFeed();

	/**
	 * Set whether this graph is following the feed or not. A graph following
	 * the feed adjusts the time range to display newly supplied elements.
	 * 
	 * @param following A boolean deciding whether graph should be following the
	 *            feed or not
	 */
	public void setFollowingFeed(boolean following);

	/**
	 * Sets this graph to display data that is currently distributed by the
	 * feed.
	 */
	public void displayCurrent();

	/**
	 * Adds a listener that listens to changes of the adjust rate property.
	 * 
	 * @param listener the listener to be added
	 */
	public void addAdjustRateListener(PropertyChangeListener listener);

	/**
	 * Removes a listener that listens to changes of the adjust rate property.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removeAdjustRateListener(PropertyChangeListener listener);

	/**
	 * Removes all listeners listening to changes of the adjust rate property.
	 */

	public void removeAllAdjustRateListeners();
}