package client;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

@SuppressWarnings("serial")
public class UIBottomPanel extends JPanel{
	
	public UIBottomPanel() {
		
		this.setLayout(new GridLayout(2, 2));
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	}

}
