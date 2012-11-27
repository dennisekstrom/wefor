package storing;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import forex.ForexTools;

public class TestStrategy implements IStrategy {

	IEngine engine;
	IAccount account;
	IOrder order;
	int count;
	boolean hasClosed, recentlyClosed;
	double amount = 1, reduceBy = 0.01;
	double price;
	double slippage;

	@Override
	public void onAccount(IAccount account) throws JFException {

	}

	@Override
	public void onBar(Instrument arg0, Period period, IBar arg2, IBar arg3)
			throws JFException {
		if (period.equals(Period.ONE_HOUR)) {
			// if (order.getState().equals(IOrder.State.FILLED)) {
			// order.close(reduceBy);
			// System.out.println("closed");
			// }

			// System.out.println("onBar(): " + order.getState() + " amount: "
			// + order.getAmount());
			//
			// if (order.getState().equals(IOrder.State.FILLED)) {
			// order.close();
			// System.out.println("closed");
			// }

			// count++;
			// if (count > 100 && !hasClosed) {
			// order.close();
			// System.out.println("close(): " + order.getState());
			// hasClosed = true;
			// }
		}

	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		System.out.println(message);
		System.out.println("profit: " + order.getProfitLossInUSD());
		System.out.println("mBALANCE: " + account.getBalance());
		// TODO Auto-generated method stub
	}

	@Override
	public void onStart(IContext context) throws JFException {
		engine = context.getEngine();
		account = context.getAccount();

		order = engine.submitOrder("order", Instrument.EURUSD, OrderCommand.BUY, amount);

		System.out.println("BALANCE: " + account.getBalance());
		System.out.println("account currency: " + account.getCurrency());
		System.out.println("onStart(): " + order.getState());

	}

	@Override
	public void onStop() throws JFException {
		System.out.println("commission: " + order.getCommissionInUSD());
		System.out.println("BALANCE: " + account.getBalance());
	}

	@Override
	public void onTick(Instrument arg0, ITick arg1) throws JFException {

		System.out.println("onTick(): " + order.getState() + " amount: "
				+ order.getAmount());

		// if (recentlyClosed) {
//		 System.out.println("price: " + order.getClosePrice());
		// System.out.println("time: "
		// + ForexTools.getTimeRepresentation(order.getCloseTime()));
		// System.out.println("profit: " + order.getProfitLossInUSD());
		// }

		if (order.getState().equals(IOrder.State.FILLED))
			order.close(1, 1,33587);

		// if (order.getState().equals(IOrder.State.FILLED)) {
		// order.close(reduceBy, price, slippage);
		// System.out.println("Closing...");
		// recentlyClosed = true;
		// } else {
		// recentlyClosed = false;
		// }
	}

}