package chart;

import util.Period;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import util.Instrument;
import util.OfferSide;

/**
 * This model contains all the necessary parameters to describe a chart.
 * 
 * The class provides getters and setters for all of those and notifies any
 * listeners about changes.
 * 
 * @author Dennis Ekstrom
 * 
 */
public class ChartModel {

	private PropertyChangeSupport propertyChangeSupport;

	// data
	private Instrument instrument;
	private Period period;
	private OfferSide offerSide;
	private long startTime;
	private long endTime;
	private double lowRate;
	private double highRate;

	ChartModel() {
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	private void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument) {
		Instrument oldValue = this.instrument;
		this.instrument = instrument;

		firePropertyChange(ChartController.INSTRUMENT_PROPERTY, oldValue,
				instrument);
	}

	/**
	 * @return the period
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(Period period) {
		Period oldValue = this.period;
		this.period = period;

		firePropertyChange(ChartController.PERIOD_PROPERTY, oldValue, period);
	}

	/**
	 * @return the offer side
	 */
	public OfferSide getOfferSide() {
		return offerSide;
	}

	/**
	 * @param offer
	 *            side the offer side to set
	 */
	public void setOfferSide(OfferSide offerSide) {
		OfferSide oldValue = this.offerSide;
		this.offerSide = offerSide;

		firePropertyChange(ChartController.OFFER_SIDE_PROPERTY, oldValue,
				offerSide);
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Long startTime) {
		long oldValue = this.startTime;
		this.startTime = startTime;

		firePropertyChange(ChartController.START_TIME_PROPERTY, oldValue,
				startTime);
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(Long endTime) {
		long oldValue = this.endTime;
		this.endTime = endTime;

		firePropertyChange(ChartController.END_TIME_PROPERTY, oldValue, endTime);
	}

	/**
	 * @return the lowRate
	 */
	public double getLowRate() {
		return lowRate;
	}

	/**
	 * @param lowRate
	 *            the lowRate to set
	 */
	public void setLowRate(Double lowRate) {
		double oldValue = this.lowRate;
		this.lowRate = lowRate;

		firePropertyChange(ChartController.LOW_RATE_PROPERTY, oldValue, lowRate);
	}

	/**
	 * @return the highRate
	 */
	public double getHighRate() {
		return highRate;
	}

	/**
	 * @param highRate
	 *            the highRate to set
	 */
	public void setHighRate(Double highRate) {
		double oldValue = this.highRate;
		this.highRate = highRate;

		firePropertyChange(ChartController.HIGH_RATE_PROPERTY, oldValue,
				highRate);
	}
}