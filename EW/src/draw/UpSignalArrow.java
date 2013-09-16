package draw;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

@SuppressWarnings("serial")
public class UpSignalArrow extends SignalArrow {

	private static final PixelPoint arrowTipPoint = new PixelPoint(8, 0);

	private static final String fileName = "Image/UpSignalArrow.png";

	private static Image image;

	/**
	 * Create an indicator up arrow that is not yet drawn.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param arrowType Constants for arrowType defined in this class
	 */
	public UpSignalArrow(DrawingPanel host) throws IOException {
		super(host, arrowTipPoint);

		if (image == null)
			image = ImageIO.read(new File(fileName));
	}

	/**
	 * Create an indicator up arrow at a specific chart position.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param chartPosition Position where the arrow will be pointing
	 * @param arrowType Constants for arrowType defined in this class
	 */
	public UpSignalArrow(DrawingPanel host, ChartPoint chartPosition) throws IOException {
		super(host, chartPosition, arrowTipPoint);

		if (image == null)
			image = ImageIO.read(new File(fileName));
	}

	@Override
	protected Image getImage() {
		return image;
	}
}