package chart;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * This class implements a line border which sides can be set. Every side,
 * declared ass constants of this class, being passed to the constructor at
 * construction will have a line border, other sides will not.
 * 
 * @author Dennis Ekstrom
 */
public class SideLineBorder implements Border {

	public static final int TOP = 0;
	public static final int LEFT = 1;
	public static final int BOTTOM = 2;
	public static final int RIGHT = 3;

	int[] borderSides;

	/**
	 * Create a border with lines at specified sides. Does nothing if no sides
	 * have been specified.
	 * 
	 * @param side specifies the sides that this border will appear on.
	 */
	public SideLineBorder(int... side) {
		super();
		borderSides = side;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		Insets insets = new Insets(0, 0, 0, 0);

		if (borderSides == null)
			return insets;

		for (int side : borderSides) {
			switch (side) {
			case TOP:
				insets.top = 1;
				break;
			case LEFT:
				insets.left = 1;
				break;
			case BOTTOM:
				insets.bottom = 1;
				break;
			case RIGHT:
				insets.right = 1;
				break;
			}
		}

		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

		if (borderSides == null)
			return;

		for (int side : borderSides) {
			switch (side) {
			case TOP:
				g.drawLine(0, 0, c.getWidth() - 1, 0);
				break;
			case LEFT:
				g.drawLine(0, 0, 0, c.getHeight() - 1);
				break;
			case BOTTOM:
				g.drawLine(0, c.getHeight() - 1, c.getWidth() - 1, c.getHeight() - 1);
				break;
			case RIGHT:
				g.drawLine(c.getWidth() - 1, 0, c.getWidth() - 1, c.getHeight() - 1);
				break;
			}
		}
	}
}