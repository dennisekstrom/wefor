package draw;

import chart.DrawingPanel;

/**
 * This class describes a chanel line drawing. That is two parallel lines.
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class ChanelLines extends LineDrawing {

	/**
	 * Create a ChanelLines with given attachment lines.
	 * 
	 * @param host the hosting DrawingPanel
	 * @param l1 the first line
	 * @param l2 the second line
	 */
	public ChanelLines(DrawingPanel host, InfiniteLine l1, InfiniteLine l2) {
		super(host, new Line[] { l1, l2 });
	}

	public ChanelLines(DrawingPanel host) {
		super(host, new DrawingProcess[] { DrawingProcess.ATTACH, DrawingProcess.LINE,
				DrawingProcess.PARALLEL });
	}

	// // color of lines
	// private static final Color lineColor = Color.GRAY;
	//
	// // the lines
	// private InfiniteLine l1;
	// private InfiniteLine l2;
	//
	// // while drawing
	// private DrawingListener drawingListener = new DrawingListener() {
	//
	// @Override
	// public void mousePressed(MouseEvent e) {
	//
	// if (l1.getAttachment() == null) {
	// l1.setAttachment(host.getChartPoint(new PixelPoint(e.getPoint())));
	//
	// nextPixelPosToDrawAt = new PixelPoint(e.getPoint());
	// } else if (l1.getDirection() == null) {
	// l1.setDirection(InfiniteLine.getDirectionBetween(l1.getAttachment(),
	// host.getChartPoint(new PixelPoint(e.getPoint()))));
	//
	// l2.setDirection(l1.getDirection());
	//
	// nextPixelPosToDrawAt = new PixelPoint(e.getPoint());
	// } else {
	// l2.setAttachment(host.getChartPoint(new PixelPoint(e.getPoint())));
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
	// * Create a ChanelLines with given attachment lines.
	// *
	// * @param host the hosting DrawingPanel
	// * @param l1 the first line
	// * @param l2 the second line
	// */
	// public ChanelLines(DrawingPanel host, InfiniteLine l1, InfiniteLine l2) {
	// super(host);
	//
	// this.l1 = l1;
	// this.l2 = l2;
	//
	// this.l1.setColor(lineColor);
	// this.l2.setColor(lineColor);
	//
	// lines = new InfiniteLine[] { l1, l2 };
	//
	// doneDrawing();
	// }
	//
	// public ChanelLines(DrawingPanel host) {
	// super(host);
	//
	// l1 = new InfiniteLine(null, null, lineColor);
	// l2 = new InfiniteLine(null, null, lineColor);
	//
	// lines = new InfiniteLine[] { l1, l2 };
	// }
	//
	// @Override
	// public DrawingListener getDrawingListener() {
	// return drawingListener;
	// }
}