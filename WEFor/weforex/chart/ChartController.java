package chart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.TickBarSize;

import feed.RealTimeFeed;
import forex.ForexConstants;

/**
 * This is the controller for a chart. Properties that are changed by this
 * controller registers with the model and are propagated to views from there.
 * 
 * @author Dennis Ekstrom
 */
public class ChartController implements PropertyChangeListener {

	public static final String INSTRUMENT_PROPERTY = "Instrument";
	public static final String TICK_BAR_SIZE_PROPERTY = "TickBarSize";
	public static final String PERIOD_PROPERTY = "Period";
	public static final String OFFER_SIDE_PROPERTY = "OfferSide";
	public static final String START_TIME_PROPERTY = "StartTime";
	public static final String END_TIME_PROPERTY = "EndTime";
	public static final String LOW_RATE_PROPERTY = "LowRate";
	public static final String HIGH_RATE_PROPERTY = "HighRate";

	private ChartModel model;
	private ArrayList<ChartView> registeredViews;

	public ChartController(ChartModel model) {
		this.model = model;
		model.addPropertyChangeListener(this);
		registeredViews = new ArrayList<ChartView>();
	}

	public void addView(ChartView view) {
		if (view != null)
			registeredViews.add(view);
	}

	public void removeView(ChartView view) {
		registeredViews.remove(view);
	}

	public Instrument getInstrument() {
		return model.getInstrument();
	}

	public TickBarSize getTickBarSize() {
		return model.getTickBarSize();
	}

	public Period getPeriod() {
		return model.getPeriod();
	}

	public OfferSide getOfferSide() {
		return model.getOfferSide();
	}

	public long getStartTime() {
		return model.getStartTime();
	}

	public long getEndTime() {
		return model.getEndTime();
	}

	public double getLowRate() {
		return model.getLowRate();
	}

	public double getHighRate() {
		return model.getHighRate();
	}

	public void changeInstrument(Instrument newInstrument) {
		model.setInstrument(newInstrument);
	}

	public void changeTickBarSize(TickBarSize newTickBarSize) {
		model.setTickBarSize(newTickBarSize);
	}

	public void changePeriod(Period newPeriod) {
		model.setPeriod(newPeriod);
	}

	public void changeOfferSide(OfferSide newOfferSide) {
		model.setOfferSide(newOfferSide);
	}

	public void changeStartTime(long newStartTime) {
		model.setStartTime(newStartTime);
	}

	public void changeEndTime(long newEndTime) {
		model.setEndTime(newEndTime);
	}

	public void changeLowRate(double newLowRate) {
		model.setLowRate(newLowRate);
	}

	public void changeHighRate(double newHighRate) {
		model.setHighRate(newHighRate);
	}

	// TODO This is just for testing without starting the client
	public static void main(String[] args) {

		// default values
		Instrument instrument = Instrument.EURUSD;
		Period tickInterval = Period.TICK;
		Period barPeriod = Period.ONE_MIN;
		TickBarSize tickBarSize = TickBarSize.FIVE;
		OfferSide offerSide = OfferSide.ASK;
		double speed = 100;
		int updateInterval = 10;

		// time range
		Calendar c = new GregorianCalendar(ForexConstants.GMT);
		c.clear();
		c.set(2011, Calendar.JANUARY, 10, 0, 0, 0);
		long startTime = c.getTimeInMillis();

		// create feed
		RealTimeFeed feed = new RealTimeFeed(instrument, tickInterval, tickBarSize,
				speed, startTime, updateInterval);

		// StoppedFeed feed = new StoppedFeed(startTime);

		// crate chart frame
		ChartFrame chartFrame = new ChartFrame(feed, instrument, tickBarSize, barPeriod,
				offerSide, startTime, true);

		// frame
		chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chartFrame.setLocationRelativeTo(null);
		chartFrame.setVisible(true);

		// start feed
		feed.startFeed();
	}

	// Use this to observe property changes from registered models
	// and propagate them on to all the views.
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		for (ChartView view : registeredViews) {
			view.modelPropertyChange(evt);
		}
	}
}