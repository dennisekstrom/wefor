package client;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.dukascopy.api.ITick;

import forex.ForexConstants;
import forex.ForexException;

/**
 * Position class.
 * 
 * @author Tobias
 * 
 */

public class Position {

	private static Calendar cal = new GregorianCalendar(ForexConstants.GMT);

	private double profit;
	private double openRate;
	private long openTime;
	private long closeTime;
	private Order order;

	boolean isClosed;

	public Position(Order order, double openRate, long openTime) {
		if (!order.getOrderCommand().equals(OrderCommand.CLOSE))
			this.order = order;
		else
			throw new ForexException("A close order can't be added as a position");
		
		this.openRate = openRate;
		this.openTime = openTime;
		profit = 0;

		isClosed = false;
	}

	/**
	 * Closes this position
	 * 
	 * @param position close rate
	 * @param position close time
	 */
	public void close(double closeRate, long closeTime) {
		this.closeTime = closeTime;
		if (order.getOrderCommand().equals(OrderCommand.BUY))
			this.profit = closeRate - openRate;
		else if (order.getOrderCommand().equals(OrderCommand.SELL))
			this.profit = openRate - closeRate;

		profit *= order.getAmount();

		isClosed = true;
	}

	public double getOpenRate() {
		return openRate;
	}

	/**
	 * @return position close rate
	 */
	public Double getCloseRate() {
		if (isClosed)
			return openRate + profit;
		else
			return null;
	}

	public long getOpenTime() {
		return openTime;
	}

	public long getCloseTime() {
		return closeTime;
	}

	public String getStartTimeStringRepresentation() {
		return String.format("%1$tY/%1$tm/%1$td %1$tT", cal);
	}

	/**
	 * @return the order amount of this position
	 */
	public int getAmount() {
		return order.getAmount();
	}

	/**
	 * @return the order for this position
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @return the profit of this position
	 */
	public double getProfit() {
		return profit;
	}

	/**
	 * Updates this position's profit according to current tick
	 * 
	 * @param current tick
	 */
	public void adjustProfit(ITick tick) {
		if (order.getOrderCommand().equals(OrderCommand.BUY))
			this.profit = tick.getAsk() - openRate;
		else if (order.getOrderCommand().equals(OrderCommand.SELL))
			this.profit = openRate - tick.getBid();

		profit *= order.getAmount() * TempConstants.LEVERAGE; //TODO temp lot size = 1000
	}

	public String toString() {
		// @formatter:off
		return String.format("%s   %s   %-4s   %s  P/L: %+.5f %s", 
				String.format("%1$tY/%1$tm/%1$td %1$tT", getOpenTime()),
				order.getInstrument().toString(),
				order.getOrderCommand().toString(),
				order.getAmount(),
				profit,
				order.getInstrument().getPrimaryCurrency().toString());
		// @formatter:on
	}
}
