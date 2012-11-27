package feed;

/**
 * This class describes a feed which cannot be started and thus appropriate for
 * graphical strategy evaluations.
 * 
 * @author Dennis Ekstrom
 */
public class StoppedFeed extends TimeRelativeFeed {

	private final long currentTime;

	/**
	 * Create a StoppedFeed being stopped at given time.
	 * 
	 * @param currentTime the time to be treated by this feed as the current
	 *            time
	 */
	public StoppedFeed(long currentTime) {
		this.currentTime = currentTime;
	}

	@Override
	public long getCurrentTime() {
		return currentTime;
	}

	@Override
	public void startFeed() {
		// do nothing
	}

	@Override
	public void stopFeed() {
		// do nothing
	}
}