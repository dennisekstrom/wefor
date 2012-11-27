package chart;
import java.awt.Point;

/**
 * Immutable class PixelPoint. Describes a pixel point.
 * 
 * @author Dennis Ekstrom
 */

public final class PixelPoint {
	public final int x;
	public final int y;

	/**
	 * Create a point according to given x- and y-coordinates.
	 * 
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 */
	public PixelPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Create a point according to the given Point.
	 * 
	 * @param p The point.
	 */
	public PixelPoint(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PixelPoint)) {
			return false;
		}
		return x == ((PixelPoint) o).x && y == ((PixelPoint) o).y;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}