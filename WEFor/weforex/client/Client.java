package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

/**
 * Main class for client
 * 
 * @author Tobias
 * 
 */

public class Client {
	private static UIPassword passwordFrame;
	private static UIClientMain client;

	public static void main(String[] args) {
		Client.reboot();
	}

	/**
	 * Reboot client
	 */
	public static void reboot() {
		if (client != null)
			client.setVisible(false);

		client = null;
		requestPassword();
	}

	private static void requestPassword() {
		passwordFrame = new UIPassword();
		passwordFrame.addPasswordListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getSource() == passwordFrame) {
					User user = passwordFrame.getUser();
					passwordFrame.setVisible(false);
					passwordFrame = null;
					startClient(user);
				}
			}
		});
		passwordFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		passwordFrame.setLocationRelativeTo(null);
		passwordFrame.setVisible(true);
	}

	private static void startClient(User user) {
		client = new UIClientMain(user, getStartTime());
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setLocationRelativeTo(null);
		client.setVisible(true);
	}

	private static long getStartTime() {
		return 1294009200000L; // TODO skapa gui för att välja start time
	}
}
