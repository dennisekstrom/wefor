package storing;

/*
 * Copyright (c) 2009 Dukascopy (Suisse) SA. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Dukascopy (Suisse) SA or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. DUKASCOPY (SUISSE) SA ("DUKASCOPY")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL DUKASCOPY OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF DUKASCOPY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingProgressListener;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.TesterFactory;

import forex.ForexConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * This small program demonstrates how to initialize Dukascopy tester and start
 * a strategy
 */
public class ConnectAndSave {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectAndSave.class);

	// url of the DEMO jnlp
	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	// user name
	private static String userName = "DEMO2wFNCx";
	// password
	private static String password = "wFNCx";

	public static void main(String[] args) throws Exception {
		// get the instance of the IClient interface
		final ITesterClient client = TesterFactory.getDefaultInstance();
		// set the listener that will receive system events
		client.setSystemListener(new ISystemListener() {
			@Override
			public void onStart(long processId) {
				LOGGER.info("Strategy started: " + processId);
			}

			@Override
			public void onStop(long processId) {
				LOGGER.info("Strategy stopped: " + processId);
				File reportFile = new File("C:\\report.html");
				try {
					client.createReport(processId, reportFile);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
				if (client.getStartedStrategies().size() == 0) {
					System.exit(0);
				}
			}

			@Override
			public void onConnect() {
				LOGGER.info("Connected");
			}

			@Override
			public void onDisconnect() {
				// tester doesn't disconnect
			}
		});

		LOGGER.info("Connecting...");
		// connect to the server using jnlp, user name and password
		// connection is needed for data downloading
		client.connect(jnlpUrl, userName, password);

		// wait for it to connect
		int i = 10; // wait max ten seconds
		while (i > 0 && !client.isConnected()) {
			Thread.sleep(1000);
			i--;
		}
		if (!client.isConnected()) {
			LOGGER.error("Failed to connect Dukascopy servers");
			System.exit(1);
		}

		// set instruments that will be used in testing
		Set<Instrument> instruments = new HashSet<Instrument>();
		instruments.add(Instrument.EURUSD);
		LOGGER.info("Subscribing instruments...");
		client.setSubscribedInstruments(instruments);
		// setting initial deposit
		client.setInitialDeposit(Instrument.EURUSD.getSecondaryCurrency(), 50000);

		// ******************** COSTUM DATA ********************
		// data of bars to load
		Instrument instrument = Instrument.EURUSD;

		// set time range
		Calendar from = new GregorianCalendar(ForexConstants.GMT);
		from.setTimeInMillis(0);
		from.set(2011, Calendar.JANUARY, 3, 0, 0, 0); // 2011-01-03 00:00:00.000
														// (inclusive)

		Calendar to = new GregorianCalendar(ForexConstants.GMT);
		to.setTimeInMillis(0);
		to.set(2011, Calendar.JANUARY, 3, 0, 0, 30); // 2011-01-10 00:00:00.000
														// (exclusive)

		// set data loading method
		// SPARAR ALLA TICKS
		ITesterClient.DataLoadingMethod dlm = ITesterClient.DataLoadingMethod.ALL_TICKS;

		// SPARAR UTVALDA TICKS
		// ITesterClient.DataLoadingMethod dlm =
		// ITesterClient.DataLoadingMethod.TICKS_WITH_TIME_INTERVAL;
		// dlm.setTimeIntervalBetweenTicks(Period.ONE_MIN.getInterval());

		// set data interval
		client.setDataInterval(dlm, from.getTimeInMillis(), to.getTimeInMillis());

		// strategy to use
		IStrategy strategy = new TestStrategy();
		// IStrategy strategy = new SaveStrategy(instrument);
		// IStrategy strategy = new PrintStrategy(instrument, true, false);
		// *****************************************************

		// load data
		LOGGER.info("Downloading data");
		Future<?> future = client.downloadData(null);
		// wait for downloading to complete
		future.get();
		// start the strategy
		LOGGER.info("Starting strategy");

		client.startStrategy(strategy, new LoadingProgressListener() {
			@Override
			public void dataLoaded(long startTime, long endTime, long currentTime,
					String information) {
				// LOGGER.info(information);
			}

			@Override
			public void loadingFinished(boolean allDataLoaded, long startTime,
					long endTime, long currentTime) {
				System.out.println("LOADING FINISHED");
			}

			@Override
			public boolean stopJob() {
				return false;
			}
		});
		// now it's running
	}
}
