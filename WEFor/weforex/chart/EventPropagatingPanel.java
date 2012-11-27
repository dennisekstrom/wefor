package chart;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

/**
 * This is the top panel in the portion of the chart panel on which panels are
 * put in layers. For mouse events to reach all components interested in mouse
 * events occurring in this area, EventPropagatingPanel is put at the top to
 * notify all interested components about any mouse event occurring in it.
 * 
 * Even components positioned directly underneath the EventPropagatingPanel,
 * such as the graph, drawing panel and mouse position cross, needs to be
 * notified as mouse events are only propagated to the topmost panel when panels
 * are stacked on top of each other.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class EventPropagatingPanel extends JPanel implements MouseListener,
		MouseMotionListener, MouseWheelListener {

	// listening components
	GraphPanel graphPanel;
	DrawingPanel drawingPanel;
	MousePositionCross mousePositionCross;
	TimeAxis timeAxis;
	RateAxis rateAxis;

	/**
	 * Create and EventPropagatingPanel.
	 * 
	 * @param graphPanel the underlying graph
	 * @param drawingPanel the underlying drawing panel
	 * @param mousePositionCross the underlying mouse position cross
	 * @param timeAxis the underlying graph
	 * @param rateAxis
	 */
	EventPropagatingPanel(GraphPanel graphPanel, DrawingPanel drawingPanel,
			MousePositionCross mousePositionCross, TimeAxis timeAxis, RateAxis rateAxis) {
		this.graphPanel = graphPanel;
		this.drawingPanel = drawingPanel;
		this.mousePositionCross = mousePositionCross;
		this.timeAxis = timeAxis;
		this.rateAxis = rateAxis;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		// make transparent
		this.setOpaque(false);
	}

	private ArrayList<MouseListener> getUnderlyingMouseListeners(Component... components) {
		ArrayList<MouseListener> listeners = new ArrayList<MouseListener>();
		for (Component comp : components) {
			listeners.addAll(Arrays.asList(comp.getMouseListeners()));
		}
		return listeners;
	}

	private ArrayList<MouseMotionListener> getUnderlyingMouseMotionListeners(
			Component... components) {
		ArrayList<MouseMotionListener> listeners = new ArrayList<MouseMotionListener>();
		for (Component comp : components) {
			listeners.addAll(Arrays.asList(comp.getMouseMotionListeners()));
		}
		return listeners;
	}

	private ArrayList<MouseWheelListener> getUnderlyingMouseWheelListeners(
			Component... components) {
		ArrayList<MouseWheelListener> listeners = new ArrayList<MouseWheelListener>();
		for (Component comp : components) {
			listeners.addAll(Arrays.asList(comp.getMouseWheelListeners()));
		}
		return listeners;
	}

	/**
	 * When mouse wheel was moved:
	 * 
	 * notify graph.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent evt) {
		for (MouseWheelListener listener : getUnderlyingMouseWheelListeners(graphPanel)) {
			listener.mouseWheelMoved(evt);
		}
	}

	/**
	 * When mouse was moved:
	 * 
	 * If user driven drawing is currently ongoing, notify drawing panel, time
	 * axis and rate axis
	 * 
	 * Otherwise, notify mouse motion cross, time axis and rate axis
	 */
	@Override
	public void mouseMoved(MouseEvent evt) {
		Component[] propagateTo;

		if (drawingPanel.isDrawing()) {
			propagateTo = new Component[] { drawingPanel, timeAxis, rateAxis };
		} else {
			propagateTo = new Component[] { mousePositionCross, timeAxis, rateAxis };
		}

		for (MouseMotionListener listener : getUnderlyingMouseMotionListeners(propagateTo)) {
			listener.mouseMoved(evt);
		}
	}

	/**
	 * When mouse was pressed:
	 * 
	 * notify drawing panel
	 */
	@Override
	public void mousePressed(MouseEvent evt) {
		for (MouseListener listener : getUnderlyingMouseListeners(drawingPanel)) {
			listener.mousePressed(evt);
		}
	}

	/**
	 * When mouse exited:
	 * 
	 * notify drawing panel, mouse position cross, time axis and rate axis
	 */
	@Override
	public void mouseExited(MouseEvent evt) {
		for (MouseListener listener : getUnderlyingMouseListeners(drawingPanel,
				mousePositionCross, timeAxis, rateAxis)) {
			listener.mouseExited(evt);
		}
	}

	// leave further methods unimplemented
	/**
	 * Empty body.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * Empty body.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Empty body.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Empty body.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}