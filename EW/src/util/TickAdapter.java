package util;

import forex.ITick;

public class TickAdapter implements ITick {

	@Override
	public long getTime() {
		return 0;
	}

	@Override
	public double getAsk() {
		return 0;
	}

	@Override
	public double getBid() {
		return 0;
	}
	
	@Override
	public long getVolume() {
		return 0;
	}
}