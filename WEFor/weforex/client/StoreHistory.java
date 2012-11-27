package client;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;

import forex.ForexConstants;

/**
 * Stores order history in text file.
 * 
 * @author Tobias
 *
 */

public class StoreHistory {

	private static Calendar cal = new GregorianCalendar(ForexConstants.GMT);

	private final String FILENAME_HISTORY = "orderhistory.txt";
	private String output;
	private File file;

	public StoreHistory() {
		file = new File(FILENAME_HISTORY);
		file.setReadable(true);
		file.setWritable(false);
	}

	/**
	 * Write this order to text file.
	 * @param order
	 * @param current feed time
	 */
	public void writeToFile(Order order, long time) {
		// @formatter:off
		output = order.getInstrument().toString() + " "
				+ order.getOrderCommand().toString() + " " 
				+ order.getAmount() + " - " 
				+ String.format("%1$tY/%1$tm/%1$td %1$tT", cal);
		// @formatter:on

		storeOrder();
		
	}
	
	public File getFile() {
		return file;
	}
	private void storeOrder() {

		// write
		// File file = null;
		FileOutputStream fos = null;
		Writer out = null;
		file.setWritable(true);
		try {
			fos = new FileOutputStream(file, true);
			try {
				out = new OutputStreamWriter(fos, "UTF-8");
				out.write(output + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		file.setWritable(false);
	}

}
