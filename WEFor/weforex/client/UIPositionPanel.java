package client;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

/**
 * @author Tobias
 * 
 */

@SuppressWarnings("serial")
public class UIPositionPanel extends JScrollPane {

	// private static String[] positions;
	private JList list;
	private DefaultListModel listModel;

	/**
	 * Create position panel
	 */
	public UIPositionPanel() {
		list = new JList();
		listModel = new DefaultListModel();
		list.setModel(listModel);
		this.getViewport().setView(list);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	}

	/**
	 * @return position list
	 */
	public JList getList() {
		return list;
	}

	/**
	 * Update and add position to position list
	 */
	public void setPositions(ArrayList<Position> positions) {
		listModel.clear();
		
		for (Position pos : positions) {
			listModel.addElement(pos);
		}
		
	}

}
