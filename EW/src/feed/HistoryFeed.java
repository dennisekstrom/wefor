package feed;

import java.util.List;

import forex.ITick;

public class HistoryFeed extends OfferFeed {

	// The size of the chunks that are downloaded one at a time.
	private static final int CHUNK_SIZE = 300;
	
	// The data
	private List<ITick> ticks;
//	private Hash

	public void startFeed() {
		// TODO
	}
	
	@Override
	public long getCurrentTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}