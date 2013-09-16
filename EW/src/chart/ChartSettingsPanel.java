package chart;

import util.Period;
import util.OfferSide;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;

import util.Instrument;

import draw.Drawing;

import forex.ForexConstants;
import graph.CandleGraph;
import graph.Graph;
import graph.SingleLineGraph;
import graph.TwinLineGraph;

/**
 * This class implements the panel at the top of the chart where settings are
 * decided.
 * 
 * @author Dennis Ekstrom
 * 
 */
@SuppressWarnings("serial")
public class ChartSettingsPanel extends ChartView implements ActionListener,
		PropertyChangeListener {

	public static final class ComboBoxRenderer extends JLabel implements
			ListCellRenderer {

		public ComboBoxRenderer(Dimension elementDimension) {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			setPreferredSize(elementDimension);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof String) { // is the title
				JLabel ret = new JLabel(value.toString());
				ret.setPreferredSize(new Dimension(0, 0));
				return ret;
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// set icon and text here
			this.setText(((Class<?>) value).getSimpleName());

			return this;
		}
	}

	private static final int HOR_GAP = 5; // horizontal gap of FlowLayout
	private static final int VER_GAP = 5; // vertical gap of FlowLayout

	private static final String DRAWING_LIST_TITLE = "Add a drawing";

	private static final Dimension BUTTON_DIMENSIONS = new Dimension(28, 28);
	private static final Dimension GRAPH_LIST_ELEMENT_DIMENSION = new Dimension(
			100, 20);
	private static final Dimension DRAWING_LIST_ELEMENT_DIMENSION = new Dimension(
			100, 20);

	private static final ComboBoxRenderer graphListRenderer = new ComboBoxRenderer(
			GRAPH_LIST_ELEMENT_DIMENSION);
	private static final ComboBoxRenderer drawingListRenderer = new ComboBoxRenderer(
			DRAWING_LIST_ELEMENT_DIMENSION);

	private OfferSide currentOfferSide;

	// components
	private final JComboBox instrumentList;
	private final JComboBox periodList;
	private final JComboBox graphList;
	private final JComboBox drawingList;
	private final JRadioButton askBtn;
	private final JRadioButton bidBtn;
	private final ButtonGroup offerSideButtons;
	private final JCheckBox adjustRatesCheckBox;
	private final JButton zoomInBtn;
	private final JButton zoomOutBtn;
	private final JButton goToCurrentTimeBtn;

	// needed components
	private GraphPanel correspondingGraphPanel;
	private DrawingPanel correspondingDrawingPanel;

	/**
	 * Create a ChartSettingsPanel.
	 * 
	 * @param controller
	 *            the controller of this panel
	 * @param correspondingChartPanel
	 *            the graph on which settings in this panel will reflect
	 * @param correspondingDrawingPanel
	 *            the drawing panel on which settings in this panel will reflect
	 * @param initPeriod
	 *            the initial period the settings panel should adjust to
	 * @param initOfferSide
	 *            the initial offer side the settings panel should adjust to
	 */
	ChartSettingsPanel(ChartController controller,
			GraphPanel correspondingGraphPanel,
			DrawingPanel correspondingDrawingPanel) {

		super(controller);

		this.correspondingGraphPanel = correspondingGraphPanel;
		this.correspondingDrawingPanel = correspondingDrawingPanel;

		// periods which to choose from
		Period[] periods = new Period[1 + ForexConstants.BAR_PERIODS.size()];
		int i = 0;
		periods[i++] = Period.TICK;
		for (Period p : ForexConstants.BAR_PERIODS) {
			periods[i++] = p;
		}

		// initialize components
		// this will change when adding more types of graphs
		instrumentList = new JComboBox(ForexConstants.INSTRUMENTS.toArray());
		periodList = new JComboBox(periods);
		offerSideButtons = new ButtonGroup();
		askBtn = new JRadioButton("Ask");
		bidBtn = new JRadioButton("Bid");
		graphList = new JComboBox(Graph.types);
		drawingList = new JComboBox(Drawing.types);
		adjustRatesCheckBox = new JCheckBox("Adjust Rates", true);
		zoomInBtn = new JButton("[+]");
		zoomOutBtn = new JButton("[-]");
		goToCurrentTimeBtn = new JButton("\u2192|");

		// add buttons to group
		offerSideButtons.add(askBtn);
		offerSideButtons.add(bidBtn);

		// add action listeners
		instrumentList.addActionListener(this);
		periodList.addActionListener(this);
		askBtn.addActionListener(this);
		bidBtn.addActionListener(this);
		graphList.addActionListener(this);
		drawingList.addActionListener(this);
		adjustRatesCheckBox.addActionListener(this);
		zoomInBtn.addActionListener(this);
		zoomOutBtn.addActionListener(this);
		goToCurrentTimeBtn.addActionListener(this);

		// add adjust rate listener
		correspondingGraphPanel.getGraph().addAdjustRateListener(this);

		// set combo box renderers
		graphList.setRenderer(graphListRenderer);
		drawingList.setRenderer(drawingListRenderer);

		// adjust drawing list
		drawingList.insertItemAt(DRAWING_LIST_TITLE, 0);
		drawingList.setSelectedIndex(0);

		// set initial values
		instrumentList.setSelectedItem(controller.getInstrument());
		periodList.setSelectedItem(controller.getPeriod());
		graphList
				.setSelectedItem(correspondingGraphPanel.getGraph().getClass());
		setSelectedOfferSide(controller.getOfferSide());

		initView();
	}

	private void initView() {

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		// set preferred sizes
		zoomInBtn.setPreferredSize(BUTTON_DIMENSIONS);
		zoomOutBtn.setPreferredSize(BUTTON_DIMENSIONS);
		goToCurrentTimeBtn.setPreferredSize(BUTTON_DIMENSIONS);

		// set layout
		this.setLayout(new FlowLayout(FlowLayout.LEFT, HOR_GAP, VER_GAP));

		this.add(instrumentList);
		this.add(periodList);
		this.add(askBtn);
		this.add(bidBtn);
		this.add(graphList);
		this.add(drawingList);
		this.add(adjustRatesCheckBox);
		this.add(zoomInBtn);
		this.add(zoomOutBtn);
		this.add(goToCurrentTimeBtn);
	}

	private void setSelectedOfferSide(OfferSide offerSide) {

		if (offerSide != null && offerSide.equals(currentOfferSide))
			return;

		// update current offer side
		currentOfferSide = offerSide;

		if (OfferSide.ASK.equals(offerSide)) {
			askBtn.setSelected(true);
			bidBtn.setSelected(false);

		} else if (OfferSide.BID.equals(offerSide)) {
			askBtn.setSelected(false);
			bidBtn.setSelected(true);
		}
	}

	/**
	 * {@inheritDoc AbstractView}
	 */
	@Override
	public void modelPropertyChange(final PropertyChangeEvent evt) {

		// only properties of interest are instrument and period
		if (evt.getPropertyName().equals(ChartController.INSTRUMENT_PROPERTY)) {

			instrumentList.setSelectedItem((Instrument) evt.getNewValue());

		} else if (evt.getPropertyName()
				.equals(ChartController.PERIOD_PROPERTY)) {

			periodList.setSelectedItem((Period) evt.getNewValue());

		} else if (evt.getPropertyName().equals(
				ChartController.OFFER_SIDE_PROPERTY)) {

			setSelectedOfferSide((OfferSide) evt.getNewValue());

		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {

		if (evt.getSource() == correspondingGraphPanel.getGraph()) {

			adjustRatesCheckBox.setSelected((Boolean) evt.getNewValue());
		}
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {

		if (evt.getSource() == instrumentList) {

			controller.changeInstrument((Instrument) instrumentList
					.getSelectedItem());

		} else if (evt.getSource() == periodList) {

			Period p = (Period) periodList.getSelectedItem();

			if (p.equals(Period.TICK)
					&& correspondingGraphPanel.getGraph() instanceof CandleGraph)
				graphList.setSelectedItem(SingleLineGraph.class);

			controller.changePeriod(p);

		} else if (evt.getSource() == askBtn) {

			controller.changeOfferSide(OfferSide.ASK);

		} else if (evt.getSource() == bidBtn) {

			controller.changeOfferSide(OfferSide.BID);

		} else if (evt.getSource() == graphList) {

			@SuppressWarnings("unchecked")
			Class<? extends Graph> type = (Class<? extends Graph>) graphList
					.getSelectedItem();

			if (type.equals(TwinLineGraph.class)) {
				askBtn.setEnabled(false);
				bidBtn.setEnabled(false);
			} else {
				askBtn.setEnabled(true);
				bidBtn.setEnabled(true);
			}

			if (controller.getPeriod().equals(Period.TICK)
					&& type.equals(CandleGraph.class))
				controller.changePeriod(ForexConstants.BAR_PERIODS.get(1));

			Graph graph = correspondingGraphPanel.getGraph();

			if (graph.getClass().equals(type)) // graph didn't change
				return;

			// remove this from listeners
			graph.removeAllAdjustRateListeners();

			// change graph
			graph = graph.getInstance(type);
			graph.addAdjustRateListener(this);
			correspondingGraphPanel.setGraph(graph);

		} else if (evt.getSource() == drawingList) {

			// do nothing if selected item is the title
			if (drawingList.getSelectedItem() instanceof Class) {
				@SuppressWarnings("unchecked")
				Class<? extends Drawing> c = (Class<? extends Drawing>) drawingList
						.getSelectedItem();

				correspondingDrawingPanel.draw(c);
			}

			// make title selected for it to display
			drawingList.setSelectedIndex(0);

		} else if (evt.getSource() == adjustRatesCheckBox) {

			if (adjustRatesCheckBox.isSelected()) {
				correspondingGraphPanel.getGraph().setAdjustRatesToFeed(true);
			} else {
				correspondingGraphPanel.getGraph().setAdjustRatesToFeed(false);
			}

		} else if (evt.getSource() == zoomInBtn) {

			correspondingGraphPanel.getGraph().zoom(
					ForexConstants.ZOOM_IN_MAGNIFICATION);

		} else if (evt.getSource() == zoomOutBtn) {

			correspondingGraphPanel.getGraph().zoom(
					ForexConstants.ZOOM_OUT_MAGNIFICATION);

		} else if (evt.getSource() == goToCurrentTimeBtn) {

			correspondingGraphPanel.getGraph().displayCurrent();

		}
	}
}