package chart;
import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class ChartView extends JPanel {

	protected ChartController controller;

	public ChartView(ChartController controller) {
		this.controller = controller;
		controller.addView(this);
	}

	/**
	 * Adjust properties according to the given PropertyChangeEvent.
	 * 
	 * @param evt The PropertyChangeEvent to adjust to.
	 */
	public abstract void modelPropertyChange(final PropertyChangeEvent evt);
}