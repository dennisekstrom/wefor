package chart;

import util.Period;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;

import feed.OfferFeed;
import graph.CandleGraph;
import graph.Graph;
import graph.SingleLineGraph;

/**
 * This class implements the panel hosting all components which together forms
 * the view of a chart.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class ChartPanel extends ChartView {

	// components
	private final JPanel centralPanel;
	private final EventPropagatingPanel eventPropagatingPanel;
	private final MousePositionCross mousePositionCross;
	private final DrawingPanel drawingPanel;
	private final GraphPanel graphPanel;
	private final ChartGrid grid;
	private final RateAxis rateAxis;
	private final TimeAxis timeAxis;
	private final ChartSettingsPanel settingsPanel;

	/**
	 * Create a chart panel using the given initial parameters to form its
	 * components.
	 */
	ChartPanel(ChartController controller, OfferFeed offerFeed, boolean showSettings) {
		super(controller);

		// build components
		centralPanel = new JPanel();
		grid = new ChartGrid();
		mousePositionCross = new MousePositionCross();
		rateAxis = new RateAxis(controller, grid);
		timeAxis = new TimeAxis(controller, grid, rateAxis);

		// determine initial graph
		Graph graph;
		if (controller.getPeriod().equals(Period.TICK))
			graph = new SingleLineGraph(controller, timeAxis, rateAxis, offerFeed);
		else
			graph = new CandleGraph(controller, timeAxis, rateAxis, offerFeed);

		graphPanel = new GraphPanel(timeAxis, rateAxis, graph);
		drawingPanel = new DrawingPanel(timeAxis, rateAxis);
		eventPropagatingPanel = new EventPropagatingPanel(graphPanel, drawingPanel,
				mousePositionCross, timeAxis, rateAxis);

		if (showSettings)
			settingsPanel = new ChartSettingsPanel(controller, graphPanel, drawingPanel);
		else
			settingsPanel = null;

		// make cursor adjustments
		eventPropagatingPanel.setCursor(Cursor
				.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		timeAxis.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		rateAxis.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// set up the view
		initView(showSettings);
	}

	/**
	 * Set up and position all components in this Chart.
	 */
	private void initView(boolean showSettings) {
		this.setLayout(new BorderLayout(0, 0));

		// add to central panel
		centralPanel.setLayout(null);
		centralPanel.setBackground(Color.WHITE); // background is white

		centralPanel.add(eventPropagatingPanel);
		centralPanel.add(mousePositionCross);
		centralPanel.add(drawingPanel);
		centralPanel.add(graphPanel);
		centralPanel.add(grid);

		// update bounds of central panel components since layout is null
		updateCentralPanelComponentBounds();

		// add to this
		if (showSettings)
			this.add(settingsPanel, BorderLayout.NORTH);

		this.add(centralPanel, BorderLayout.CENTER);
		this.add(rateAxis, BorderLayout.EAST);
		this.add(timeAxis, BorderLayout.SOUTH);
	}

	/**
	 * Returns the graph panel of this chart.
	 * 
	 * @return The graph panel of this chart.
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Returns the drawing panel of this chart.
	 * 
	 * @return The drawing panel of this chart.
	 */
	public DrawingPanel getDrawingPanel() {
		return drawingPanel;
	}

	/**
	 * Updates the bounds of the components of graph panel.
	 */
	private void updateCentralPanelComponentBounds() {
		Rectangle bounds = new Rectangle(centralPanel.getSize());

		for (Component c : centralPanel.getComponents()) {
			c.setBounds(bounds);
		}
	}

	/**
	 * Returns an appropriate title of the frame hosting this ChartPanel. The
	 * title is the instrument.
	 * 
	 * Example title: EUR/USD
	 * 
	 * @return the title of this ChartPanel
	 */
	public String getTitle() {
		return rateAxis.getInstrument().toString();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		updateCentralPanelComponentBounds();
	}

	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {
		// something was changed, make sure children displays changes
		repaint();
	}
}