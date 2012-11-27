package chart;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * This class describes a display in a RateAxis to show exact rates for specific
 * y-coordinates.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class RateDisplay extends JPanel {

	private static final int PIXEL_HEIGHT = ChartAxis.FONT.getSize() + 2;
	private static final Color BG_COLOR = new Color(255, 200, 200); // light red

	private RateAxis host;
	private String text; // text to display
	private Integer yPos; // position within host

	/**
	 * Create a RateDisplay with given hosting rate axis.
	 * 
	 * @param host the hosting rate axis
	 */
	RateDisplay(RateAxis host) {
		this.host = host;

		// set font
		this.setFont(ChartAxis.FONT);

		// set border
		this.setBorder(new SideLineBorder(SideLineBorder.LEFT, SideLineBorder.TOP,
				SideLineBorder.BOTTOM));

		// set background
		this.setBackground(BG_COLOR);

		// make initially invisible
		this.setVisible(false);
	}

	/**
	 * Sets the text of this display.
	 * 
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns the y-coordinate of this display.
	 * 
	 * @return the y-coordinate of this display
	 */
	public Integer getYPos() {
		return yPos;
	}

	/**
	 * Sets the y-coordinate of this display.
	 * 
	 * @param yPos the y-coordinate to set
	 */
	public void setYPos(Integer yPos) {
		this.yPos = yPos;
		if (yPos != null) {
			// this also repaints
			this.setBounds(0, yPos - PIXEL_HEIGHT / 2, host.getWidth(), PIXEL_HEIGHT);
			this.setVisible(true);
		} else {
			this.setVisible(false);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (yPos == null)
			return;

		g.setFont(ChartAxis.FONT);
		if (text != null) {
			g.drawString(text, RateAxis.RATE_MARKING_LINE_LENGTH + ChartAxis.INDENT,
					(this.getHeight() + ChartAxis.FONT.getSize()) / 2 - 1);

		}
	}
}