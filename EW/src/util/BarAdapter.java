package util;

import forex.IBar;

public class BarAdapter implements IBar {

	@Override
	public long getTime() {
		return 0;
	}

	@Override
	public double getClose() {
		return 0;
	}

	@Override
	public double getHigh() {
		return 0;
	}

	@Override
	public double getLow() {
		return 0;
	}

	@Override
	public double getOpen() {
		return 0;
	}

	@Override
	public long getVolume() {
		return 0;
	}

}