package io;

import java.util.Random;
import java.util.TreeSet;

import util.ForexUtils;
import util.HasTime;
import util.Instrument;
import util.OfferSide;
import util.Period;

import feed.BarBuilder;
import forex.Bar;
import forex.ITick;
import forex.Tick;

/**
 * Temp interface with methods for io.
 */
public class IO {

	private static final TreeSet<ITick> ticks = new TreeSet<ITick>(
			ForexUtils.hasTimeComparator);
	private static final double defaultRate = 1.3;

	static {
		// generate fake ticks
		Random rand = new Random();
		long time = 0;
		double ask, bid;
		for (int i = 0; i < 10000; i++) {
			bid = 1.3 + rand.nextDouble() / 20;
			ask = bid + rand.nextDouble() / 100;
			ticks.add(new Tick(time, ask, bid, 1));
			time += 100 + rand.nextInt(2000);
		}
	}

	/**
	 * Download [from, to]. (If a bar's interval contains any timestamp that
	 * lies within the interval [from, to], include that bar.)
	 */
	public synchronized static TreeSet<HasTime> get(Instrument instrument,
			OfferSide offerSide, Period period, long from, long to) {

		if (period == Period.TICK) {

			TreeSet<HasTime> ret = new TreeSet<HasTime>(
					ForexUtils.hasTimeComparator);

			ret.addAll(ticks.subSet(ForexUtils.longToTimeTick(from), true,
					ForexUtils.longToTimeTick(to), true));

			System.out.println(Thread.currentThread().toString()
					+ ": IO Returning: " + ret);

			return ret;

		} else {

			TreeSet<HasTime> bars = new TreeSet<HasTime>(
					ForexUtils.hasTimeComparator);

			from = ForexUtils.getStartTime(period, from);
			to = ForexUtils.getEndTime(period, to);

			while (from <= to) {
				BarBuilder builder = new BarBuilder(offerSide,
						ForexUtils.getStartTime(period, from));

				ITick start = ForexUtils.longToTimeTick(from);
				ITick end = ForexUtils.longToTimeTick(from
						+ period.getInterval());

				builder.addTicks(ticks.subSet(start, true, end, false));

				if (!builder.isOpen())
					builder.addTick(new Tick(0, defaultRate, defaultRate, 1));

				bars.add(new Bar(builder));

				from += period.getInterval();
			}

			System.out.println("IO Returning: "
					+ Thread.currentThread().toString() + " : " + bars);

			return bars;
		}
	}
}