package trading;

/**
 * Note that approving an order does not guarantee the order being filled at the
 * exact rate or time of order submission due to slippage and delays. This must be taken into account when
 * 
 * @author Dennis
 * 
 */
public interface OrderApprover {

	/**
	 * Returns true if this OrderApprover approves the given order, otherwise
	 * false.
	 * 
	 * @param order the order to check for approval
	 * @param time the time of order submission
	 * @param rate the rate at the time of order submission
	 * @return true if this OrderApprover approves the given order, otherwise
	 *         false
	 */
	public boolean orderApproved(TracableOrder order, long time, double rate);
}