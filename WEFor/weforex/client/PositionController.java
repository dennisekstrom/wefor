package client;

import java.util.ArrayList;
import java.util.Collections;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

import feed.TimeRelativeFeed;
import feed.ITickFeedListener;
import forex.ForexException;

/**
 * Handles orders and positions. Implements feed to update each positions
 * accordingly. Also updates the host (Client UI) to display the current values.
 * 
 * @author Tobias
 * 
 */
public class PositionController implements ITickFeedListener {

	private UIClientMain host;

	// stores closed positions
	public ArrayList<Position> closedPositions;
	// stores open positions
	public ArrayList<Position> openPositions;

	private StoreHistory printRead;

	private double currentAskRate;
	private double currentBidRate;

	private Double previousAskRate;
	private Double previousBidRate;

	public double dailyProfit;
	private double openProfit;

	protected TimeRelativeFeed feed;
	private User user;

	// components to be notified about changes
	private UIClosedPositionPanel closedPosPanel;

	public PositionController(UIClientMain host, TimeRelativeFeed feed, User user,
			UIClosedPositionPanel closedPosPanel) {
		openPositions = new ArrayList<Position>();
		closedPositions = new ArrayList<Position>();

		printRead = new StoreHistory();

		this.host = host;
		this.feed = feed;
		this.user = user;
		this.closedPosPanel = closedPosPanel;

		this.feed.addTickFeedListener(this);
	}

	public ArrayList<Position> getOpenPositions() {
		return openPositions;
	}

	/**
	 * Handles this order and stores it in text file.
	 * 
	 * @param order
	 */
	public void handleOrder(Order order) {
		if (order.getOrderCommand().equals(OrderCommand.BUY))
			openPositions.add(new Position(order, currentAskRate, feed.getCurrentTime()));
		else if (order.getOrderCommand().equals(OrderCommand.SELL))
			openPositions.add(new Position(order, currentBidRate, feed.getCurrentTime()));
		else
			throw new ForexException("handleOrder() can't handle close orders");

		// write to file
		printRead.writeToFile(order, feed.getCurrentTime());
	}

	/**
	 * Closes this position.
	 * 
	 * @param position
	 */
	public void closePosition(Position position) {

		if (position.getOrder().getOrderCommand().equals(OrderCommand.BUY))
			position.close(currentAskRate, feed.getCurrentTime());
		else if (position.getOrder().getOrderCommand().equals(OrderCommand.SELL))
			position.close(currentBidRate, feed.getCurrentTime());
		else
			throw new ForexException("position shouldn't have close order as order");

		// update stuff
		openPositions.remove(position);
		closedPosPanel.addClosedPosition(position);

		// Update balance and daily profit
		dailyProfit += position.getProfit();
		user.setBalance(user.getBalance() + position.getProfit() * TempConstants.LEVERAGE);
	}

	/**
	 * Close all open positions
	 */
	public void closeAllPositions() {
		ArrayList<Position> positionsTemp = new ArrayList<Position>();

		// create copyable arraylist //TODO fulhack
		for (int i = 0; i < openPositions.size(); i++) {
			positionsTemp.add(null);

		}
		// make a copy of arraylist to avoid iteration when thread is modifying
		// the list
		Collections.copy(positionsTemp, openPositions);

		for (Position pos : positionsTemp) {
			closePosition(pos);
		}

	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Position position : openPositions) {
			sb.append("\n" + position.getOrder().getInstrument().toString() + " "
					+ position.getOrder().getOrderCommand().toString() + " "
					+ position.getOrder().getAmount() + " - " + position.getOpenTime()
					+ "\n " + position.getProfit());
		}

		return sb.toString();
	}

	private void updateOpenProfitLabel() {
		if (!openPositions.isEmpty())
			host.setOpenProfitLabelText("Open profit: " + openProfit + " EUR");
		else
			host.setOpenProfitLabelText("Open profit: (No open positions)");
	}

	private void updateRateLabel() {
		if (previousAskRate == null || previousBidRate == null) {
			host.getEntryPanel().setAskRate(currentAskRate, 0D);
			host.getEntryPanel().setBidRate(currentBidRate, 0D);
		}
		host.getEntryPanel().setAskRate(currentAskRate, currentAskRate - previousAskRate);
		host.getEntryPanel().setBidRate(currentBidRate, currentBidRate - previousBidRate);
	}

	private void updateBalanceLabel() {
		host.setBalanceLabelText(String.format("Balance: %.2f EUR", user.getBalance()));
	}

	private void updateDynamicDailyProfit() {
		openProfit = 0;

		for (Position position : openPositions) {
			openProfit += position.getProfit();
		}
		openProfit = Double.parseDouble(String.format("%.4g%n", openProfit));
	}

	/**
	 * @return the main feed
	 */
	public TimeRelativeFeed getFeed() {
		return feed;
	}
	
	public UIClientMain getHost() {
		return host;
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) {

		previousAskRate = currentAskRate;
		previousBidRate = currentBidRate;

		// use closed bar rate as current rate
		currentAskRate = tick.getAsk();
		currentBidRate = tick.getBid();

		for (Position position : openPositions)
			position.adjustProfit(tick);

		host.repaintPositionPanel();

		// update rate label in client
		updateRateLabel();
		// update balance label
		updateBalanceLabel();
		// update daily profit label
		updateDynamicDailyProfit();
		updateOpenProfitLabel();

	}

}
