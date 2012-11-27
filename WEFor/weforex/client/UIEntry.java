package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.dukascopy.api.*;

/**
 * Frame to display the static internal entry window.
 * 
 * 
 */

@SuppressWarnings("serial")
public class UIEntry extends JPanel {

	private static Font ratePanelFont = new Font("Rate panel font", 20, 20);

	private String buyTitle = "BUY";
	private String sellTitle = "SELL";

	private EntryButton buyBtn;
	private EntryButton sellBtn;

	private JComboBox currencyBox;
	private JComboBox amountBox;

	private final Instrument[] currencies = { Instrument.EURUSD, Instrument.EURSEK,
			Instrument.USDJPY, Instrument.EURGBP, Instrument.USDSEK };
	private final Integer[] amount = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };

	private PositionController positionController;
	private UIPositionPanel positionPanel;

	/**
	 * Create UIEntry
	 * 
	 * @param positionController
	 * @param positionPanel
	 */
	public UIEntry(PositionController positionController, UIPositionPanel positionPanel) {
		super();
		this.positionController = positionController;
		this.positionPanel = positionPanel;
		init();

	}

	public void init() {
		// create components
		currencyBox = new JComboBox(currencies);
		amountBox = new JComboBox(amount);
		buyBtn = new EntryButton(buyTitle);
		sellBtn = new EntryButton(sellTitle);

		currencyBox.setSelectedIndex(0);
		currencyBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Currency"));
		currencyBox.setPreferredSize(new Dimension(50, 50));
		amountBox.setSelectedIndex(0);
		amountBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Amount"));
		amountBox.setPreferredSize(new Dimension(50, 50));

		// combo boxes
		JPanel comboPanel = new JPanel(new GridLayout(1, 3));
		comboPanel.add(currencyBox);
		comboPanel.add(amountBox);

		// rate panel
		JPanel ratePanel = new JPanel(new GridLayout(1, 2));
		buyBtn.setFont(ratePanelFont);
		sellBtn.setFont(ratePanelFont);


		ratePanel.add(buyBtn);
		ratePanel.add(sellBtn);
		

		this.setLayout(new BorderLayout());
		this.add(comboPanel, BorderLayout.NORTH);
		this.add(ratePanel, BorderLayout.CENTER);

		// set preferred size
		this.setPreferredSize(new Dimension(300, 0));

		buyBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				positionController.handleOrder(new Order((Instrument) currencyBox
						.getSelectedItem(), OrderCommand.BUY, (Integer) amountBox
						.getSelectedItem()));

				positionPanel.setPositions(positionController.getOpenPositions());
			}
		});

		sellBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				positionController.handleOrder(new Order((Instrument) currencyBox
						.getSelectedItem(), OrderCommand.SELL, (Integer) amountBox
						.getSelectedItem()));

				positionPanel.setPositions(positionController.getOpenPositions());
			}
		});
	}

	/**
	 * Update ask rate interface in client
	 * 
	 * @param rate
	 * @param previousRate
	 */
	public void setAskRate(double rate, double rateDiff) {
		buyBtn.setRate(rate, rateDiff);
	}

	/**
	 * Update bid rate interface in client
	 * 
	 * @param rate
	 * @param previousRate
	 */
	public void setBidRate(double rate, double rateDiff) {
		sellBtn.setRate(rate, rateDiff);
	}
	
	public int getAmount() {
		return (Integer) amountBox.getSelectedItem();
	}
}
