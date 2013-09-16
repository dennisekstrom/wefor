package chart;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * This class describes a display in a TimeAxis to show exact rates for specific
 * x-coordinates.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class TimeDisplay extends JPanel {

	private static final Color BG_COLOR = new Color(255, 200, 145); // light
																	// orange

	private TimeAxis host;
	private String text; // text to display
	private Integer width; // width of the display
	private Integer xPos; // position within host

	/**
	 * Create a TimeDisplay with given hosting time axis.
	 * 
	 * @param host the hosting time axis
	 */
	TimeDisplay(TimeAxis host) {
		this.host = host;

		// set font
		this.setFont(ChartAxis.FONT);

		// set border
		this.setBorder(new SideLineBorder(SideLineBorder.LEFT, SideLineBorder.TOP,
				SideLineBorder.RIGHT));

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
		if (text != null) {
			this.width = this.getFontMetrics(this.getFont()).stringWidth(text)
					+ ChartAxis.INDENT * 2;
			this.setBounds(xPos - width / 2, 0, width, host.getHeight());
		}
	}

	/**
	 * Returns the x-coordinate of this display.
	 * 
	 * @return the x-coordinate of this display
	 */
	public Integer getXPos() {
		return xPos;
	}

	/**
	 * Sets the x-coordinate of this display.
	 * 
	 * @param xPos the x-coordinate to set
	 */
	public void setXPos(Integer xPos) {
		this.xPos = xPos;
		if (xPos != null && width != null) {
			this.setBounds(xPos - width / 2, 0, width, host.getHeight());
			this.setVisible(true);
		} else {
			this.setVisible(false);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (xPos == null || width == null)
			return;

		if (text != null) {
			int stringWidth = g.getFontMetrics().stringWidth(text);

			g.drawString(text, (this.getWidth() - stringWidth) / 2,
					(this.getHeight() + this.getFont().getSize()) / 2);
		}
	}
}