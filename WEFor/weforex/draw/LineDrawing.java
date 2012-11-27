package draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chart.ChartPoint;
import chart.DrawingPanel;
import chart.PixelPoint;

import forex.ForexException;

/**
 * This class is the super class of all drawings consisting of one or multiple
 * lines.
 * 
 * @author Dennis Ekstrom
 * 
 */
@SuppressWarnings("serial")
public abstract class LineDrawing extends Drawing {

	public enum DrawingProcess {
		ATTACH, END, LINE, PARALLEL, INVERSE;
	}

	// radius of attachment circle
	private static final int ATTACHMENT_CIRCLE_PIXEL_RADIUS = 3;

	protected List<Line> lines;
	private static final Color LINE_COLOR = Color.GRAY;

	// process iterator
	private Iterator<DrawingProcess> steps;
	private DrawingProcess previousStep, currentStep, upcomingStep;
	private PixelPoint nextPixelPosToDrawAt;
	private ArrayList<ChartPoint> clickedPoints;

	// while drawing
	private DrawingListener drawingListener = new DrawingListener() {

		@Override
		public void mousePressed(MouseEvent e) {

			clickedPoints.add(host.getChartPoint(new PixelPoint(e.getPoint())));

			// no steps are null, unless upcoming step just before the end
			previousStep = currentStep == null ? DrawingProcess.END
					: currentStep;
			currentStep = upcomingStep;
			upcomingStep = steps.hasNext() ? steps.next() : null;

			InfiniteLine il;
			FiniteLine fl;
			switch (currentStep) {
			case ATTACH:

				if (upcomingStep == DrawingProcess.ATTACH) {

					fl = new FiniteLine(getChartPoint(e.getPoint()), null);

					lines.add(fl);

				} else if (upcomingStep == DrawingProcess.LINE) {

					il = new InfiniteLine(getChartPoint(e.getPoint()), null);

					lines.add(il);

				}

				if (previousStep == DrawingProcess.ATTACH) {

					((FiniteLine) getLastLine())
							.setSecondAttachment(getChartPoint(e.getPoint()));

				}

				break;

			case END:

				((FiniteLine) getLastLine())
						.setSecondAttachment(getChartPoint(e.getPoint()));

				break;

			case LINE:

				il = (InfiniteLine) getLastLine();

				il.setDirection(Line.getDirectionBetween(il.getAttachment(),
						getChartPoint(e.getPoint())));

				break;

			case PARALLEL:

				getLastLine()
						.setAttachmentPoint(getChartPoint(e.getPoint()), 0);

				break;

			case INVERSE:

				getLastLine()
						.setAttachmentPoint(getChartPoint(e.getPoint()), 0);

				break;

			}

			if (upcomingStep == DrawingProcess.PARALLEL) {
				il = new InfiniteLine(null, getLastLine().getDirection());

				lines.add(il);
			}

			if (upcomingStep == DrawingProcess.INVERSE) {
				il = new InfiniteLine(null, getLastLine().getDirection()
						.getInverse());

				lines.add(il);
			}

			if (upcomingStep == null)
				doneDrawing();

			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			nextPixelPosToDrawAt = new PixelPoint(e.getPoint());

			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			nextPixelPosToDrawAt = null;

			repaint();
		}
	};

	/**
	 * Create a LineDrawing with the given lines.
	 * 
	 * @param host
	 *            the hosting DrawingPanel
	 * @param lines
	 *            the lines that make up this LineDrawing
	 */
	public LineDrawing(DrawingPanel host, Line[] lines) {
		super(host);

		this.lines = Arrays.asList(lines);

		doneDrawing();
	}

	/**
	 * Create a LineDrawing with the given drawing process.
	 * 
	 * @param host
	 *            the hosting DrawingPanel
	 * @param process
	 *            the drawing process of this line drawing
	 */
	public LineDrawing(DrawingPanel host, DrawingProcess[] process) {
		super(host);

		verifyDrawingProcess(process);

		this.steps = Arrays.asList(process).iterator();

		this.lines = new ArrayList<Line>();

		this.clickedPoints = new ArrayList<ChartPoint>();

		if (steps.hasNext())
			this.upcomingStep = steps.next();
	}

	private Line getLastLine() {
		return lines.get(lines.size() - 1);
	}

	private ChartPoint getChartPoint(Point p) {
		return host.getChartPoint(new PixelPoint(p));
	}

	/**
	 * Draws a circle of given radius and with center at attachment point in
	 * given DrawingPanel.
	 * 
	 * @param drawingPanel
	 *            DrawingPanel to determine position of attachment point
	 * @param pos
	 *            the position of the circle
	 * @param r
	 *            radius of the circle
	 * @param color
	 *            the color of the circle
	 * @param g
	 *            Graphics to draw on
	 */
	private static void drawCircle(DrawingPanel drawingPanel,
			ChartPoint position, int r, Graphics g) {

		PixelPoint p = drawingPanel.getPixelPoint(position);

		g.drawOval(p.x - r, p.y - r, r * 2, r * 2);
		g.fillOval(p.x - r, p.y - r, r * 2, r * 2);
	}

	@Override
	public DrawingListener getDrawingListener() {
		return drawingListener;
	}

	@Override
	public void paintFinal(Graphics g) {

		Color oldColor = g.getColor();
		g.setColor(LINE_COLOR);

		for (Line line : lines) {
			line.drawOnDrawingPanel(host, g);
		}

		g.setColor(oldColor);
	}

	@Override
	public void paintWhileDrawing(Graphics g) {
		Color oldColor = g.getColor();
		g.setColor(LINE_COLOR);

		if (nextPixelPosToDrawAt != null) {
			drawCircle(host, host.getChartPoint(nextPixelPosToDrawAt),
					ATTACHMENT_CIRCLE_PIXEL_RADIUS, g);
		}

		for (ChartPoint clickedPoint : clickedPoints) {
			drawCircle(host, clickedPoint, ATTACHMENT_CIRCLE_PIXEL_RADIUS, g);
		}

		for (Line line : lines) {
			if (line != null) {
				if (line.isDetermined()) { // is determined, draw as usual

					line.drawOnDrawingPanel(host, g);

				} else { // isn't determined, adjust temporarily

					Line tempLine = line.clone();

					if (tempLine.getAttachmentPoints()[0] == null) { // not
																		// attached
						if (nextPixelPosToDrawAt != null) {

							tempLine.setAttachmentPoint(
									host.getChartPoint(nextPixelPosToDrawAt), 0);

							if (tempLine.isDetermined())
								tempLine.drawOnDrawingPanel(host, g);
						}
					} else { // is attached

						if (nextPixelPosToDrawAt != null) {

							if (tempLine instanceof FiniteLine)
								((FiniteLine) tempLine)
										.setSecondAttachment(host
												.getChartPoint(nextPixelPosToDrawAt));

							if (tempLine instanceof InfiniteLine)
								((InfiniteLine) tempLine)
										.setDirection(Line.getDirectionBetween(
												((InfiniteLine) tempLine)
														.getAttachment(),
												host.getChartPoint(nextPixelPosToDrawAt)));

							tempLine.drawOnDrawingPanel(host, g);
						}
					}
				}
			}
		}

		g.setColor(oldColor);
	}

	/**
	 * TODO describe what's allowed
	 * 
	 * @param process
	 */
	private void verifyDrawingProcess(DrawingProcess[] process) {

		if (process.length <= 1) {

			throw new ForexException("illegal drawing process: "
					+ process.length + " step(s)");

		} else if (process[0] != DrawingProcess.ATTACH) {

			throw new ForexException(
					"illegal drawing process: step 1 must be ATTACH");

		}

		DrawingProcess current, previous = DrawingProcess.ATTACH;
		for (int i = 1; i < process.length; i++) {

			current = process[i];

			switch (process[i]) {
			case LINE:

				if (previous != DrawingProcess.ATTACH)
					throw new ForexException(
							"illegal drawing process: LINE must be preceded by ATTACH");
				break;

			case PARALLEL:

				if (previous == DrawingProcess.ATTACH)
					throw new ForexException(
							"illegal drawing process: PARALLEL can't be preceded by ATTACH");

				break;

			case INVERSE:

				if (previous == DrawingProcess.ATTACH)
					throw new ForexException(
							"illegal drawing process: INVERSE can't be preceded by ATTACH");
				break;

			case END:

				if (previous != DrawingProcess.ATTACH)
					throw new ForexException(
							"illegal drawing process: END must be preceded by ATTACH");
				break;

			case ATTACH:
				break;
			}

			previous = current;
		}
	}
}