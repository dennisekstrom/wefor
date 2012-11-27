package client;

import java.util.Arrays;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class User {

	// TODO those are temporary
	public static final String defaultName = "Mr. Default";
	public static final char[] defaultPassword = { 't', 'o', 'b', 'i', 'a', 's' };
	public static final double defaultBalance = 20000D;

	private String username;
	private char[] password;
	private double balance;
	private final String id;
	private String loginTime;

	private User(String name, char[] password, double initialBalance, String id) {
		this.username = name;
		this.password = Arrays.copyOf(password, password.length);
		this.balance = initialBalance;
		this.id = id;
		this.loginTime = getDateTime();
	}

	public static User getUser(String id) {
		if (idRecognized(id)) {
			return loadUser(id);
		} else {
			return new User(defaultName, defaultPassword, defaultBalance, "0");
		}
	}

	private static boolean idRecognized(String id) {
		return false; // TODO användardatabas
	}

	private static User loadUser(String id) {
		return null; // TODO användardatabas
	}
	
	public String getLoginTime() {
		return loginTime;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the password
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	public static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
