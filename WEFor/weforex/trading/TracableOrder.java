package trading;

import com.dukascopy.api.Instrument;

import client.Order;
import client.OrderCommand;

public class TracableOrder extends Order {

	private final OrderSubmitter submitter;

	/**
	 * Create an order.
	 * 
	 * @param instrument the instrument of the order
	 * @param command the OrderCommand to determine type of the order
	 * @param amount the amount of the order
	 * @param submitter the OrderSubmitter submitting this order
	 */
	public TracableOrder(Instrument instrument, OrderCommand command, Integer amount,
			OrderSubmitter submitter) {
		super(instrument, command, amount);

		this.submitter = submitter;
	}

	public OrderSubmitter getSubmitter() {
		return submitter;
	}
}