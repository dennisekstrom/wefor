package client;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

/**
 * Closed positions frame. Displays all closed positions.
 * 
 * @author Tobias
 * 
 */
@SuppressWarnings("serial")
public class UIClosedPositionPanel extends JScrollPane {

	private JList closedPositionList;
	private DefaultListModel listModel;

	public UIClosedPositionPanel() {

		listModel = new DefaultListModel();
		closedPositionList = new JList();
		closedPositionList.setModel(listModel);

		this.add(closedPositionList);
		this.getViewport().setView(closedPositionList);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	}

	/**
	 * Add this closed position to list
	 * 
	 * @param position
	 */
	public void addClosedPosition(Position position) {
		listModel.addElement(String.format("%s  Closing time: %2$tY/%2$tm/%2$td %2$tT",
				position.toString(), position.getCloseTime()));
	}
}
