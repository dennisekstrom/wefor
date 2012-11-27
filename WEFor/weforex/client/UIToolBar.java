package client;

import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * User interface toolbar. Consists of user interactive buttons and a combo box
 * for adjusting the feed speed.
 * 
 * @author Tobias
 * 
 */
@SuppressWarnings("serial")
public class UIToolBar extends JPanel {

	private final PositionController positionController;
	private final UIPositionPanel positionPanel;

	// GUI
	private JButton hedgeButton;
	private JButton closeButton;
	private JButton closeAllButton;
	private JButton pauseButton;
	private JComboBox speedComboBox;
	
	private JPanel speedPanel;

	private boolean isPaused;

	private final Double[] speeds = { 1D, 10D, 100D, 1000D, 3000D, 5000D, 7000D, 10000D,
			20000D, 50000D, 100000D, 1000000D };

	public UIToolBar(PositionController positionController, UIPositionPanel positionPanel) {
		super(new FlowLayout(FlowLayout.LEFT, 4, -3)); 

		// initialize
		this.positionController = positionController;
		this.positionPanel = positionPanel;

		// set border
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), "Toolbar"));

		speedPanel = new JPanel();
		speedComboBox = new JComboBox(speeds);
		hedgeButton = new JButton("HEDGE");
		closeButton = new JButton("CLOSE");
		closeAllButton = new JButton("CLOSE ALL");
		pauseButton = new JButton("PAUSE");
		
		Dimension buttonSize = new Dimension(100, 30);
		
		hedgeButton.setPreferredSize(new Dimension(buttonSize));
		closeButton.setPreferredSize(new Dimension(buttonSize));
		closeAllButton.setPreferredSize(new Dimension(buttonSize));
		pauseButton.setPreferredSize(new Dimension(buttonSize));


		speedComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getPositionController().getFeed().setSpeed(
						(Double) speedComboBox.getSelectedItem());
			}
		});

		// make sure to display default speed at initalization
		speedComboBox.setSelectedItem(TempConstants.defaultSpeed);

		speedPanel.add(speedComboBox);

		speedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Speeds"));
		
		this.add(hedgeButton);
		this.add(closeButton);
		this.add(closeAllButton);
		this.add(pauseButton);
		this.add(speedPanel);

		closeAction(closeButton);
		closeAllAction(closeAllButton);
		pauseAction(pauseButton);
		hedgeAction(hedgeButton);

		positionPanel.getList().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				adjustCloseButton();
			}
		});

		adjustCloseButton();
	}

	private void adjustCloseButton() {
		if (getPositionPanel().getList().isSelectionEmpty())
			closeButton.setEnabled(false);
		else
			closeButton.setEnabled(true);
	}

	private PositionController getPositionController() {
		return positionController;
	}

	private UIPositionPanel getPositionPanel() {
		return positionPanel;
	}


	private void closeAction(JButton close) {
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] positions = positionPanel.getList().getSelectedValues();

				for (Object pos : positions)
					positionController.closePosition((Position) pos);

				positionPanel.setPositions(positionController.getOpenPositions());
			}
		});
	}

	private void closeAllAction(JButton closeAll) {
		closeAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				positionController.closeAllPositions();

				positionPanel.setPositions(positionController.getOpenPositions());
			}
		});
	}
	
	private void hedgeAction(JButton hedgeButton) {
		hedgeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				positionController.handleOrder(new Order(
						positionController.feed.getInstrument(), 
						OrderCommand.BUY, 
						positionController.getHost().getEntryPanel().getAmount()));
				
				positionController.handleOrder(new Order(
						positionController.feed.getInstrument(), 
						OrderCommand.SELL, 
						positionController.getHost().getEntryPanel().getAmount()));

				positionPanel.setPositions(positionController.getOpenPositions());
				
			}
		});
	}

	private void pauseAction(JButton pause) {
		pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isPaused) {
					positionController.getFeed().startFeed();
					pauseButton.setText("PAUSE");
					isPaused = false;
				} else {
					positionController.getFeed().stopFeed();
					pauseButton.setText("START");
					isPaused = true;
				}

				pauseButton.repaint();
			}
		});
	}

}
