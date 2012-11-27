package client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Password class. The login frame for the client. Must be executed and approved
 * before client starts.
 * 
 * @author Tobias
 * 
 */

@SuppressWarnings("serial")
public class UIPassword extends JFrame implements ActionListener {
	private ArrayList<PropertyChangeListener> passwordListeners;

	private static String OK = "ok";
	private static String HELP = "help";

	// private JFrame controllingFrame;
	private JLabel passwordLabel;
	private JLabel idLabel;
	private JPasswordField passwordField;
	private JTextField idField;

	private User user;

	public UIPassword() {

		// initialize fields
		passwordListeners = new ArrayList<PropertyChangeListener>();

		// GUI
		this.setLayout(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 0));

		idField = new JTextField(10);

		passwordField = new JPasswordField(10);
		passwordField.setActionCommand(OK);
		passwordField.addActionListener(this);

		idLabel = new JLabel("ID: ");
		passwordLabel = new JLabel("Password: (tobias)");

		idLabel.setLabelFor(idField);
		passwordLabel.setLabelFor(passwordField);

		JComponent buttonPane = createButtonPanel();

		// Lay out everything.
		p.add(idLabel);
		p.add(idField);
		p.add(passwordLabel);
		p.add(passwordField);

		this.add(p);
		this.add(buttonPane);
		this.pack();
		this.setTitle("Login");

		resetFocus();
	}

	/**
	 * Add a listener to be notified when correct password is entered.
	 * 
	 * @param listener listener to be added
	 */
	public void addPasswordListener(PropertyChangeListener listener) {
		if (listener == null)
			return;

		this.passwordListeners.add(listener);
	}

	private void notifyListeners() {
		for (PropertyChangeListener pcl : passwordListeners)
			pcl.propertyChange(new PropertyChangeEvent(this, "PasswordCorrect", false,
					true));
	}

	protected JComponent createButtonPanel() {
		JPanel p = new JPanel(new GridLayout(0, 1));
		JButton okButton = new JButton("OK");
		JButton helpButton = new JButton("Help");

		okButton.setActionCommand(OK);
		helpButton.setActionCommand(HELP);
		okButton.addActionListener(this);
		helpButton.addActionListener(this);

		p.add(okButton);
		p.add(helpButton);

		return p;
	}
	
	/**
	 * @return user
	 */
	public User getUser() {
		return user;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (OK.equals(cmd)) { // Process the password.
			char[] input = passwordField.getPassword();
			if (isPasswordCorrect(input)) {
				user = User.getUser(idField.getText());
				JOptionPane.showMessageDialog(this, " Welcome " + user.getUsername());
				notifyListeners();
			} else {
				JOptionPane.showMessageDialog(this, "Invalid password. Try again.",
						"Error Message", JOptionPane.ERROR_MESSAGE);
			}

			// Zero out the possible password, for security.
			Arrays.fill(input, '0');

			passwordField.selectAll();
		} else { // The user has asked for help.
			JOptionPane.showMessageDialog(this,
					"Enter the correct password to login to the client.\n"
							+ "Password is only available for strategy testers.");
		}
		resetFocus();
	}

	/**
	 * Checks the passed-in array against the correct password. After this
	 * method returns, you should invoke eraseArray on the passed-in array.
	 */
	private static boolean isPasswordCorrect(char[] input) {
		boolean isCorrect = true;
		char[] correctPassword = { 't', 'o', 'b', 'i', 'a', 's' };

		if (input.length != correctPassword.length) {
			isCorrect = false;
		} else {
			isCorrect = Arrays.equals(input, correctPassword);
		}

		// Zero out the password.
		Arrays.fill(correctPassword, '0');

		return isCorrect;
	}

	protected void resetFocus() {
		passwordField.requestFocusInWindow();
	}

}