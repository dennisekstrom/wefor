package draw;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This is listener provided to chart drawings that listens to various mouse
 * events that implements both MouseListener and MouseMotionListener.
 * 
 * Methods not implemented has to be implemented by children of this class.
 * 
 * Methods that has to be implemented by children are mouseMoved(MouseEvent),
 * mousePressed(MouseEvent) and mouseExited(MouseEvent).
 * 
 * @author Dennis Ekstrom
 */
public abstract class DrawingListener implements MouseListener, MouseMotionListener {

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}