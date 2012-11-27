package chart;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;

/**
 * This class describes the underlying grid of the chart. It's a transparent
 * panel with a light gray grid displayed. The lines of the grid are specified
 * by integer arrays, in which each integer represents the position (x or y) of
 * a line in the grid.
 * 
 * @author Dennis Ekstrom
 * 
 */
@SuppressWarnings("serial")
public class ChartGrid extends JPanel {
	private int[] horLines;
	private int[] verLines;

	ChartGrid() {
		setGrid(new int[0], new int[0]); // initially no lines

		// make transparent
		setOpaque(false);
	}

	public void setGrid(int[] horLines, int[] verLines) {
		this.horLines = horLines;
		this.verLines = verLines;
	}

	/**
	 * @param horLines The horizontal lines to set
	 */
	public void setHorLines(int[] horLines) {
		if (horLines != null)
			this.horLines = horLines;
	}

	/**
	 * @param verLines The vertical lines to set
	 */
	public void setVerLines(int[] verLines) {
		if (verLines != null)
			this.verLines = verLines;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(new Color(230, 230, 230)); // Very light gray

		for (int y : horLines) {
			g.drawLine(0, y, this.getWidth(), y);
		}

		for (int x : verLines) {
			g.drawLine(x, 0, x, this.getHeight());
		}
	}
}