package draw;

import chart.ChartDirection;
import chart.ChartPoint;
import chart.DrawingPanel;

/**
 * This class describes a single line drawing. That is just a single line which
 * can be drawn on a chart.
 * 
 * @author Dennis Ekstrom
 * 
 */
@SuppressWarnings("serial")
public class SingleLineDrawing extends LineDrawing {

	/**
	 * Create a SingleLineDrawing.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param line the line
	 */
	public SingleLineDrawing(DrawingPanel host, InfiniteLine line) {
		super(host, new Line[] { line });
	}

	/**
	 * Create a SingleLineDrawing with given attachment point and direction.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param line the line
	 */
	public SingleLineDrawing(DrawingPanel host, ChartPoint attachment,
			ChartDirection direction) {
		this(host, new InfiniteLine(attachment, direction));
	}

	/**
	 * Create a SingleLineDrawing which is yet to be drawn.
	 * 
	 * @param host the hosting DrawingPanel
	 */
	public SingleLineDrawing(DrawingPanel host) {
		super(host, new DrawingProcess[] { DrawingProcess.ATTACH, DrawingProcess.LINE });
	}

	// // color of line
	// private static final Color lineColor = Color.GRAY;
	//
	// // the line
	// private InfiniteLine line;
	//
	// // while drawing
	// private boolean attachmentDrawn = false;
	//
	// private DrawingListener drawingListener = new DrawingListener() {
	//
	// @Override
	// public void mousePressed(MouseEvent e) {
	//
	// if (!attachmentDrawn) {
	// line.setAttachment(host.getChartPoint(new PixelPoint(e.getPoint())));
	//
	// nextPixelPosToDrawAt = new PixelPoint(e.getPoint());
	//
	// attachmentDrawn = true;
	// } else {
	// line.setDirection(InfiniteLine.getDirectionBetween(line.getAttachment(),
	// host.getChartPoint(new PixelPoint(e.getPoint()))));
	//
	// doneDrawing();
	// }
	//
	// repaint();
	// }
	//
	// @Override
	// public void mouseMoved(MouseEvent e) {
	// nextPixelPosToDrawAt = new PixelPoint(e.getPoint());
	//
	// repaint();
	// }
	//
	// @Override
	// public void mouseExited(MouseEvent e) {
	// nextPixelPosToDrawAt = null;
	//
	// repaint();
	// }
	// };
	//
	// /**
	// * Create a SingleLineDrawing.
	// *
	// * @param host the hosting DrawingPanel
	// * @param line the line
	// */
	// public SingleLineDrawing(DrawingPanel host, InfiniteLine line) {
	// super(host);
	//
	// this.line = line;
	// this.line.setColor(lineColor);
	//
	// lines = new InfiniteLine[] { line };
	//
	// doneDrawing();
	// }
	//
	// /**
	// * Create a SingleLineDrawing with given attachment point and direction.
	// *
	// * @param host the hosting DrawingPanel
	// * @param line the line
	// */
	// public SingleLineDrawing(DrawingPanel host, ChartPoint attachment,
	// ChartDirection direction) {
	// this(host, new InfiniteLine(attachment, direction, lineColor));
	// }
	//
	// /**
	// * Create a SingleLineDrawing which is yet to be drawn.
	// *
	// * @param host the hosting DrawingPanel
	// */
	// public SingleLineDrawing(DrawingPanel host) {
	// super(host);
	//
	// this.line = new InfiniteLine(null, null, lineColor);
	//
	// lines = new InfiniteLine[] { line };
	// }
	//
	// @Override
	// public DrawingListener getDrawingListener() {
	// return drawingListener;
	// }
}