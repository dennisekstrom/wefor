package chart;

import graph.Graph;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements MouseWheelListener {

	private TimeAxis correspondingTimeAxis;
	private RateAxis correspondingRateAxis;

	private Graph graph;

	/**
	 * Create a GraphPanel with the given initial graph.
	 * 
	 * @param initGraph the initial graph
	 */
	GraphPanel(TimeAxis correspondingTimeAxis, RateAxis correspondingRateAxis,
			Graph initGraph) {

		this.correspondingTimeAxis = correspondingTimeAxis;
		this.correspondingRateAxis = correspondingRateAxis;

		// make transparent
		setOpaque(false);

		// set layout
		setLayout(null);

		// listen to mouse wheel events
		addMouseWheelListener(this);

		setGraph(initGraph);
	}

	/**
	 * Returns the graph of this GraphPanel.
	 * 
	 * @return the graph of this GraphPanel
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Sets the graph of this GraphPanel.
	 * 
	 * @param graph the graph to set
	 */
	public void setGraph(Graph graph) {
		if (graph == null)
			throw new IllegalArgumentException("initGraph=null");

		if (this.graph != null)
			this.graph.destroy(); // destroy old graph

		// start listening to feed with new graph
		graph.setListeningToFeed(true);

		graph.setBounds(new Rectangle(Toolkit.getDefaultToolkit()
				.getScreenSize()));

		this.graph = graph;

		this.removeAll();
		this.add(graph);

		repaint();
	}

	/**
	 * Invoked when mouse wheel was moved in the graph. Scroll ten pixels per
	 * mouse wheel notch. Scroll horizontally if shift is down, otherwise
	 * vertically.
	 */
	@Override
	public void mouseWheelMoved(final MouseWheelEvent evt) {

		int pixelsPerNotch = 10;

		int notches = evt.getWheelRotation();

		if (!evt.isShiftDown()) { // scroll rates (REMOVE "!" IF NOT ON MAC)

			// don't scroll if rates are adjusting to feed
			if (graph.isRateAdjustingToFeed())
				return;

			double newHighRate = correspondingRateAxis.yPixelPosToRate(notches
					* pixelsPerNotch); // (ADD A "-" IF NOT ON MAC)
			double newLowRate = newHighRate
					- correspondingRateAxis.getRangeInterval();

			graph.changeRateRange(newLowRate, newHighRate);

		} else { // scroll time

			long newStartTime = correspondingTimeAxis.xPixelPosToTime(notches
					* pixelsPerNotch); // (ADD A "-" IF NOT ON MAC)
			long newEndTime = newStartTime
					+ correspondingTimeAxis.getRangeInterval();

			graph.changeTimeRange(newStartTime, newEndTime);
		}
	}
}