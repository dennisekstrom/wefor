package client;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

/**
 * This class describes a button displaying a title, a rate and the difference
 * to previous rate. Its color is determined depending on the value of the rate
 * difference.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class EntryButton extends JButton {

	private String title;

	private Border defaultBorder = BorderFactory.createRaisedBevelBorder();
	private Border pressedBorder = BorderFactory.createLoweredBevelBorder();

	private Color positiveColor = new Color(102, 255, 102);
	private Color negativeColor = new Color(255, 102, 102);
	private Color neutralColor = Color.LIGHT_GRAY;

	private MouseListener mouseListener = new MouseAdapter() {

		@Override
		public void mousePressed(final MouseEvent evt) {
			setBorder(pressedBorder);
		}

		@Override
		public void mouseReleased(final MouseEvent evt) {
			setBorder(defaultBorder);
		}

		@Override
		public void mouseExited(final MouseEvent evt) {
			setBorder(defaultBorder);
		}
	};

	EntryButton(String title) {
		this.title = title;

		this.addMouseListener(mouseListener);

		this.setBorder(defaultBorder);
		this.setOpaque(true);
	}

	/**
	 * Sets parameters determining the view of the button.
	 * 
	 * @param rate the current rate
	 * @param rateDiff the difference between current rate and previous rate
	 */
	public void setRate(double rate, double rateDiff) {
		String sign;
		if (rateDiff > 0) {
			this.setBackground(positiveColor);
			sign = "+";
		} else if (rateDiff < 0) {
			this.setBackground(negativeColor);
			sign = "";
		} else {
			this.setBackground(neutralColor);
			sign = "\u00B1";
		}

		String rateDiffString = new String("<html><font color = GREY size = 3>(" + sign
				+ String.format("%.5f", rateDiff) + ")</font></html>");

		// show rate
		this.setText("<html><center>" + title + "<br>" + String.format("%.5f", rate)
				+ "<br>" + rateDiffString + "</center></html>");

	}
}