package trading;

import java.util.concurrent.TimeUnit;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

public class Order implements IOrder {

	public static final double DEFAULT_SLIPPAGE = 5.0;

	State state;
	double stopLossPrice;
	double takeProfitPrice;
	double amount;
	double closePrice;
	long closeTime;
	
	@Override
	public void close() throws JFException {
		// TODO Auto-generated method stub
		// adjust close price and time after each closing

	}

	@Override
	public void close(double amount) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close(double amount, double price) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close(double amount, double price, double slippage) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	public double getClosePrice() {
		return closePrice;
	}

	@Override
	public long getCloseTime() {
		return closeTime;
	}

	@Override
	public String getComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCommission() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCommissionInUSD() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getFillTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getGoodTillTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instrument getInstrument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getOpenPrice() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OrderCommand getOrderCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProfitLossInAccountCurrency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getProfitLossInPips() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getProfitLossInUSD() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRequestedAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public double getStopLossPrice() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OfferSide getStopLossSide() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getTakeProfitPrice() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTrailingStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLong() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setGoodTillTime(long arg0) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOpenPrice(double arg0) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRequestedAmount(double arg0) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStopLossPrice(double arg0) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStopLossPrice(double arg0, OfferSide arg1) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStopLossPrice(double arg0, OfferSide arg1, double arg2)
			throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTakeProfitPrice(double arg0) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void waitForUpdate(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public IMessage waitForUpdate(State... arg0) throws JFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage waitForUpdate(long arg0, TimeUnit arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage waitForUpdate(long arg0, State... arg1) throws JFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMessage waitForUpdate(long arg0, TimeUnit arg1, State... arg2)
			throws JFException {
		// TODO Auto-generated method stub
		return null;
	}

}