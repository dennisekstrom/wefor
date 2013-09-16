package graph;

import java.awt.Graphics;
import java.beans.PropertyChangeEvent;

import util.Instrument;
import util.OfferSide;
import util.Period;

import chart.ChartController;
import chart.RateAxis;
import chart.TimeAxis;
import feed.OfferFeed;
import forex.IBar;
import forex.ITick;

/**
 * Currently not used, uncomment in Graph to use. Then test! Last time not
 * working.
 * 
 * @author Dennis
 */
@SuppressWarnings("serial")
public class TwinLineGraph extends LineGraph {

	private SingleLineGraph askGraph, bidGraph;

	public TwinLineGraph(ChartController controller,
			TimeAxis correspondingTimeAxis, RateAxis correspondingRateAxis,
			OfferFeed offerFeed) {
		super(controller, correspondingTimeAxis, correspondingRateAxis,
				offerFeed);

		this.add(askGraph);
		this.add(bidGraph);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Handled by separate line graphs.
	}

	@Override
	protected Double getHighestRate() {
		return askGraph.getHighestRate();
	}

	@Override
	protected Double getLowestRate() {
		return bidGraph.getLowestRate();
	}

	@Override
	protected void updateDisplayingElements() {
		askGraph.updateDisplayingElements();
		bidGraph.updateDisplayingElements();

	}

	@Override
	protected void drawDisplayingElements(Graphics g) {
		askGraph.drawDisplayingElements(g);
		bidGraph.drawDisplayingElements(g);
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) {
		askGraph.onTick(instrument, tick);
		bidGraph.onTick(instrument, tick);

	}

	@Override
	public void onBar(Instrument instrument, Period period,
			OfferSide offerSide, IBar bar) {
		askGraph.onBar(instrument, period, offerSide, bar);
		bidGraph.onBar(instrument, period, offerSide, bar);
	}
}