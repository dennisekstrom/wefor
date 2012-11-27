package client;

import com.dukascopy.api.*;

/**
 * Order class. An order is created when either buy, sell or close commands are called.
 * 
 * TODO fixa slippage och massa andra grejer från IOrder
 * 
 * @author Tobias
 *
 */

public class Order {
	
	private final OrderCommand orderCommand;
	private final Integer amount;
	private final Instrument instrument;
	
	public Order(Instrument instrument, OrderCommand orderCommand, Integer amount) {
		this.amount = amount;
		this.orderCommand = orderCommand;
		this.instrument = instrument;
	
	}
	
	public Integer getAmount() {
		return amount;
	}
	
	public OrderCommand getOrderCommand() {
		return orderCommand;
	}
	
	public Instrument getInstrument() {
		return instrument;
	}
	

}
