package chart;

import util.Period;

import java.awt.Dimension;

import javax.swing.JFrame;

import util.Instrument;
import util.OfferSide;

import feed.OfferFeed;
import forex.ForexConstants;

/**
 * This class implements a frame hosting a ChartPanel.
 * 
 * It adjusts it title according to current settings of the ChartPanel.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class ChartFrame extends JFrame {

	/**
	 * Create a ChartFrame.
	 * 
	 * @param offerFeed the feed of the containing ChartPanel
	 * @param initialInstrument the initial Instrument of the containing
	 *            ChartPanel
	 * @param initialPeriod the initial Period of the containing ChartPanel
	 * @param initialTickBarSize the initial TickBarSize of the containing
	 *            ChartPanel
	 * @param initialOfferSide the initial OfferSide of the containing
	 *            ChartPanel
	 * @param initialStartTime the initial start time of the containing
	 *            ChartPanel
	 * @param showSettings set to true if settings panel should show, otherwise
	 *            false
	 */
	public ChartFrame(OfferFeed offerFeed, Instrument initialInstrument,
			Period initialPeriod, OfferSide initialOfferSide,
			long initialStartTime, boolean showSettings) {

		// this.instrument = initialInstrument;
		// this.period = initialPeriod;

		// set title
		// adjustTitle();
		setTitle(ForexConstants.APPLICATION_NAME);

		// MVC
		ChartModel model = new ChartModel();
		ChartController controller = new ChartController(model);

		controller.changeInstrument(initialInstrument);
		controller.changePeriod(initialPeriod);
		controller.changeOfferSide(initialOfferSide);
		controller.changeStartTime(initialStartTime);
		controller.changeEndTime(initialStartTime + initialPeriod.getInterval()
				* ForexConstants.DEFAULT_NO_BARS_IN_RANGE);
		controller.changeLowRate(0.0);
		controller.changeHighRate(1.0);
		
		ChartPanel chartPanel = new ChartPanel(controller, offerFeed, showSettings);

		// add components
		this.add(chartPanel);

		// frame size
		this.setSize(new Dimension(874, 400));

		// set minimum width if displaying settings panel is displayed
		if (showSettings)
			this.setMinimumSize(new Dimension(874, 0));
	}

	// /**
	// * Adjusts the title of the frame. The title is the instrument followed by
	// * the barPeriod of the feed.
	// *
	// * Example title: EUR/USD 10 Mins
	// */
	// private void adjustTitle() {
	// this.setTitle(instrument.toString() + " " + period.toString());
	// }
	//
	// @Override
	// public void propertyChange(final PropertyChangeEvent evt) {
	//
	// if (evt.getPropertyName().equals(ChartController.INSTRUMENT_PROPERTY)) {
	//
	// instrument = (Instrument) evt.getNewValue();
	// adjustTitle();
	//
	// } else if (evt.getPropertyName().equals(ChartController.PERIOD_PROPERTY))
	// {
	//
	// period = (Period) evt.getNewValue();
	// adjustTitle();
	//
	// }
	// }
}