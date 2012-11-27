package chart;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import draw.Drawing;
import draw.OnePointDrawing;

/**
 * The component in the chart panel hosting all drawings.
 * 
 * To add a drawing yet to be drawn by the user, use draw(Class<? extends
 * ChartDrawing> type).
 * 
 * TODO ersätt OnePointDrawing med FiniteDrawing
 * 
 * @author Dennis Ekstrom
 */
@SuppressWarnings("serial")
public class DrawingPanel extends JPanel implements PropertyChangeListener {

	private TimeAxis correspondingTimeAxis;
	private RateAxis correspondingRateAxis;

	private Drawing currentlyDrawing;

	private TreeMap<BigInteger, OnePointDrawing> drawingsToUpdateInRange;
	private ArrayList<Drawing> drawingsToAlwaysUpdate;

	/**
	 * Create a DrawingPanel.
	 * 
	 * @param correspondingTimeAxis the time axis to adjust to
	 * @param correspondingRateAxis the rate axis to adjust to
	 */
	DrawingPanel(TimeAxis correspondingTimeAxis, RateAxis correspondingRateAxis) {
		if (correspondingTimeAxis == null)
			throw new IllegalArgumentException("correspondingTimeAxis=null");

		this.correspondingTimeAxis = correspondingTimeAxis;
		this.correspondingRateAxis = correspondingRateAxis;

		drawingsToUpdateInRange = new TreeMap<BigInteger, OnePointDrawing>();
		drawingsToAlwaysUpdate = new ArrayList<Drawing>();

		// make transparent
		setOpaque(false);

		// set layout
		setLayout(null);
	}

	// todo fixa så den inte returnerar drawings som målas currently
	public Drawing[] getDrawings() {
		ArrayList<Drawing> drawings = new ArrayList<Drawing>();

		for (Component c : this.getComponents())
			if (c instanceof Drawing)
				drawings.add((Drawing) c);

		return drawings.toArray(new Drawing[] {});

	}

	/**
	 * Adds the drawings in given array that are drawn (isDrawn() returns true)
	 * to this drawing panel. If two drawings are equal, only one of those are
	 * added. The drawing of highest index will have the topmost position.
	 */
	public void addDrawings(Drawing[] drawings) {
		for (Drawing d : drawings)
			if (d.isDrawn())
				addDrawing(d);
	}

	/**
	 * Draws a drawing on this DrawingPanel. If given drawing is already in
	 * panel, nothing is done.
	 * 
	 * If drawing.isDrawn() returns true, drawing is simply added to the top
	 * layer of this DrawingPanel.
	 * 
	 * If drawing.isDrawn() returns false, a user-driven drawing of the given
	 * drawing is started.
	 * 
	 * @param drawing Drawing to be drawn.
	 */
	public void draw(Drawing drawing) {
		if (drawing == null || this.contains(drawing))
			return;

		if (drawing.isDrawn())
			addDrawing(drawing);
		else
			startDrawing(drawing);
	}

	/**
	 * Tries to draw a ChartDrawing of given type by invoking the given types
	 * constructor that takes a ChartPanel as its only parameter. The ChartPanel
	 * that is sent as an argument is the closest related ChartPanel in the
	 * hierarchy of parents to this DrawingPanel. If there is no such parent,
	 * the method returns without any further actions.
	 * 
	 * Also, if no such constructor is found, access is denied or any other
	 * exception is thrown upon execution of this method, an error message is
	 * printed to the console and the method returns without further actions.
	 * 
	 * @param type the type ChartDrawing which to draw
	 */
	public void draw(Class<? extends Drawing> type) {
		try {
			Drawing drawing = type
					.getConstructor(new Class[] { DrawingPanel.class }).newInstance(this);

			draw(drawing);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Add a drawing to the top layer of this DrawingPanel. Clears the drag
	 * layer immediately after drawing. If given drawing is already in panel,
	 * nothing is done and false is returned.
	 * 
	 * @param drawing Drawing to be added.
	 * @return true if drawing successfully added, otherwise false.
	 */
	private boolean addDrawing(Drawing drawing) {
		if (drawing == null || this.contains(drawing))
			return false;

		drawing.setBounds(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

		this.add(drawing, 0); // 0 since adding to the top layer

		// add to appropriate collection
		if (drawing instanceof OnePointDrawing)
			drawingsToUpdateInRange.put(getDrawingKey((OnePointDrawing) drawing),
					(OnePointDrawing) drawing);
		else
			drawingsToAlwaysUpdate.add(drawing);

		return true;
	}

	/**
	 * Remove a drawing. If given drawing is not in pane, nothing is done and
	 * false is returned.
	 * 
	 * @param drawing Drawing to be removed.
	 * @return true if drawing successfully removed, otherwise false.
	 */
	public boolean removeDrawing(Drawing drawing) {
		if (drawing == null || !this.contains(drawing))
			return false;

		this.remove(drawing);

		if (drawing instanceof OnePointDrawing)
			drawingsToUpdateInRange.remove(getDrawingKey((OnePointDrawing) drawing));
		else
			drawingsToAlwaysUpdate.remove(drawing);

		return true;
	}

	/**
	 * Returns true if drawing is currently going on, otherwise false.
	 * 
	 * @return true if drawing is currently going on, otherwise false
	 */
	public boolean isDrawing() {
		return currentlyDrawing != null;
	}

	/**
	 * Returns the start time of this DrawingPanel.
	 * 
	 * @return the start time of this DrawingPanel
	 */
	public long getStartTime() {
		return correspondingTimeAxis.getStartTime();
	}

	/**
	 * Returns the end time of this DrawingPanel.
	 * 
	 * @return the end time of this DrawingPanel
	 */
	public long getEndTime() {
		return correspondingTimeAxis.getEndTime();
	}

	/**
	 * Returns the low rate of this DrawingPanel.
	 * 
	 * @return the low rate of this DrawingPanel
	 */
	public double getLowRate() {
		return correspondingRateAxis.getLowRate();
	}

	/**
	 * Returns the high rate of this DrawingPanel.
	 * 
	 * @return the high rate of this DrawingPanel
	 */
	public double getHighRate() {
		return correspondingRateAxis.getHighRate();
	}

	/**
	 * Returns the ChartBounds of this DrawingPanel.
	 * 
	 * @return the ChartBounds of this DrawingPanel
	 */
	public ChartBounds getChartBounds() {
		return new ChartBounds(getStartTime(), getEndTime(), getHighRate(), getLowRate());
	}

	/**
	 * Returns the pixel point— corresponding to given chart point. The returned
	 * point might not be in the currently visible area of this ChartPanel.
	 * 
	 * @param cp The ChartPoint which to find corresponding pixel position for.
	 * @return The pixel corresponding to given chart point.
	 */
	public PixelPoint getPixelPoint(ChartPoint cp) {
		// return null if chart point is not in the visible area
		int xPos = correspondingTimeAxis.timeToXPixelPos(cp.time);
		int yPos = correspondingRateAxis.rateToYPixelPos(cp.rate);

		return new PixelPoint(xPos, yPos);
	}

	/**
	 * Returns the chart point corresponding to given pixel point. Returns null
	 * is p is not in the currently visible area of the chart panel.
	 * 
	 * @param p The PixelPoint which to find corresponding ChartPoint for.
	 * @return The ChartPoint corresponding to given PixelPoint.
	 */
	public ChartPoint getChartPoint(PixelPoint p) {
		long time = correspondingTimeAxis.xPixelPosToTime(p.x);
		double rate = correspondingRateAxis.yPixelPosToRate(p.y);

		// check that p is currently visible
		if (!correspondingTimeAxis.inRange(time) || !correspondingRateAxis.inRange(rate))
			return null;

		return new ChartPoint(time, rate);
	}

	/**
	 * Returns true if this DrawingPanel contains the given drawing, otherwise
	 * false.
	 * 
	 * @param drawing the drawing to look for
	 * @return true if this DrawingPanel contains the given drawing, otherwise
	 *         false
	 */
	private boolean contains(Drawing drawing) {
		if (drawing == null || !drawing.isDrawn())
			return false;

		for (Component c : this.getComponents())
			if (c.equals(drawing))
				return true;

		return false;
	}

	/**
	 * Start user-driven drawing of given ChartDrawing on this DrawingPanel.
	 */
	private void startDrawing(Drawing drawing) {
		// make sure only one drawing is drawn at one time
		stopDrawing();

		currentlyDrawing = drawing;

		currentlyDrawing.addPropertyChangeListener(this);
		this.addMouseListener(currentlyDrawing.getDrawingListener());
		this.addMouseMotionListener(currentlyDrawing.getDrawingListener());

		drawing.setBounds(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

		this.add(currentlyDrawing, 0);
	}

	/**
	 * Stop user-driven drawing.
	 */
	private void stopDrawing() {
		if (currentlyDrawing == null)
			return;

		currentlyDrawing.removePropertyChangeListener(this);
		this.removeMouseListener(currentlyDrawing.getDrawingListener());
		this.removeMouseMotionListener(currentlyDrawing.getDrawingListener());

		removeDrawing(currentlyDrawing);

		if (currentlyDrawing.isDrawn())
			addDrawing(currentlyDrawing);

		currentlyDrawing = null;
	}

	private BigInteger getDrawingKey(OnePointDrawing drawing) {
		return getDrawingKey(drawing.getChartPosition().time, drawing.hashCode());
	}

	private BigInteger getDrawingKey(long time, int hashCode) {
		BigInteger tim = BigInteger.valueOf(time);
		BigInteger has = BigInteger.valueOf(hashCode);

		tim = tim.multiply(BigInteger.valueOf(Long.MAX_VALUE));
		return tim.add(has);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Drawing.IS_DRAWN_PROPERTY)) {
			stopDrawing();
		}
	}

	@Override
	protected void paintChildren(Graphics g) {
		SortedMap<BigInteger, OnePointDrawing> toUpdate = drawingsToUpdateInRange.subMap(
				getDrawingKey(correspondingTimeAxis.getStartTime(), 0),
				getDrawingKey(correspondingTimeAxis.getEndTime(), 0).add(
						BigInteger.valueOf(1L)));

		for (OnePointDrawing drawing : toUpdate.values()) {
			drawing.paint(g);
		}

		for (Drawing drawing : drawingsToAlwaysUpdate)
			drawing.paint(g);

		if (currentlyDrawing != null)
			currentlyDrawing.paint(g);
	}
}