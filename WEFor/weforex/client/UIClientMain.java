package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import chart.ChartFrame;

import feed.TimeRelativeFeed;
import forex.ForexConstants;

/**
 * Main client frame. Functions as a host frame for other components. Other than
 * components it has a feed, a user and a position controller. When the
 * constructor is invoked the feed is started.
 * 
 * @author Tobias
 * 
 */
@SuppressWarnings("serial")
public class UIClientMain extends JFrame {

	private static final String TITLE = "WEForex";
	private static final Dimension FRAME_SIZE = new Dimension(820, 400);

	private TimeRelativeFeed feed;

	private User loggedInUser;

	private PositionController positionController;

	// GUI
	private JMenuBar menuBar;
	private UIToolBar toolbar;
	private UIPositionPanel positionPanel;
	private UIEntry entryPanel;
	private JTabbedPane positionTabbedPanel;
	private UIClosedPositionPanel closedPositionPanel;
	private UIBottomPanel bottomPanel;

	// other frames
	private ChartFrame chartFrame;

	// south panel
	public JLabel balanceLabel;
	public JLabel openProfitLabel;

	private JLabel clockLabel;
	private JLabel userNameLabel;

	private static Calendar time = new GregorianCalendar(ForexConstants.GMT);

	/**
	 * Create the main frame and start feed.
	 * 
	 * @param user
	 * @param start time of feed
	 */
	public UIClientMain(User user, long startTime) {
		super(TITLE);

		this.loggedInUser = user;

		// TODO these parameters should be set in a login window
		feed = new TimeRelativeFeed(TempConstants.defaultInstrument,
				TempConstants.defaultTickInterval, TempConstants.defaultTickBarSize,
				TempConstants.defaultSpeed, startTime,
				TempConstants.defaultUpdateInterval);

		closedPositionPanel = new UIClosedPositionPanel();

		initChart(startTime);

		this.positionController = new PositionController(this, feed, loggedInUser,
				closedPositionPanel);

		feed.addCurrentTimeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				time.setTimeInMillis((Long) evt.getNewValue());
				clockLabel.setText(String.format("%1$tY/%1$tm/%1$td %1$tR", time));
			}
		});

		// start feed
		feed.startFeed();

		// create components
		initGUI();
	}

	private void initChart(long startTime) {
		chartFrame = new ChartFrame(feed, TempConstants.defaultInstrument,
				TempConstants.defaultPeriod, TempConstants.defaultOfferSide, startTime);
		chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chartFrame.setLocationRelativeTo(null);
	}

	/**
	 * Create main frame for client application
	 */
	private void initGUI() {
		// set layout
		this.setLayout(new BorderLayout(6, 6));

		// create components
		menuBar = new UIMenuBar(this);
		positionPanel = new UIPositionPanel();
		entryPanel = new UIEntry(positionController, positionPanel);
		toolbar = new UIToolBar(positionController, positionPanel);
		positionTabbedPanel = new JTabbedPane();
		bottomPanel = new UIBottomPanel();

		balanceLabel = new JLabel("" + loggedInUser.getBalance());
		openProfitLabel = new JLabel();
		clockLabel = new JLabel();
		userNameLabel = new JLabel(loggedInUser.getUsername());

		//
		positionTabbedPanel.addTab("Open Positions", null, positionPanel, "tjenare");
		positionTabbedPanel.addTab("Closed Positions", null, closedPositionPanel, "hej");

		// south panel
		bottomPanel.add(balanceLabel);
		bottomPanel.add(openProfitLabel);
		bottomPanel.add(clockLabel);
		bottomPanel.add(userNameLabel);

		this.setJMenuBar(menuBar);
		this.add(toolbar, BorderLayout.NORTH);
		this.add(entryPanel, BorderLayout.WEST);
		this.add(positionTabbedPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		this.setSize(FRAME_SIZE);
	}

	public void setStartTime(long startTime) {
		chartFrame.setVisible(false);

		initChart(startTime);

		this.positionController = new PositionController(this, feed, loggedInUser,
				closedPositionPanel);

		initGUI();

		repaint();

		// TODO these parameters should be set in a separate window
		feed = new TimeRelativeFeed(TempConstants.defaultInstrument,
				TempConstants.defaultTickInterval, TempConstants.defaultTickBarSize,
				TempConstants.defaultSpeed, startTime,
				TempConstants.defaultUpdateInterval);

	}

	public UIEntry getEntryPanel() {
		return entryPanel;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void showChartFrame() {
		chartFrame.setLocationRelativeTo(null);
		chartFrame.setVisible(true);
	}

	public void setBalanceLabelText(String text) {
		balanceLabel.setText(text);
		balanceLabel.repaint();
	}

	public void setOpenProfitLabelText(String text) {
		openProfitLabel.setText(text);
		openProfitLabel.repaint();
	}

	public void repaintPositionPanel() {
		positionPanel.repaint();
	}
}
