package trading;

/**
 * This class implements a strategy that approves all orders.
 * 
 * @author Dennis Ekstrom
 */
public abstract class SimpleStrategy extends ReliableStrategy implements OrderApprover {

	@Override
	public boolean orderApproved(TracableOrder order) {
		// approve all orders
		return true;
	}

}