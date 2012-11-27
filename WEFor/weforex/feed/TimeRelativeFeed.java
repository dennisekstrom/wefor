package feed;

/**
 * This class describes a feed with a current time which increases relative to
 * real-time as the feed is running.
 * 
 * @author Dennis Ekstrom
 */
public abstract class TimeRelativeFeed extends Feed {

	public abstract long getCurrentTime();
}