package io;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

import chart.TimeRange;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import forex.ForexConstants;
import forex.ForexException;
import forex.Tick;
import forex.Bar;

// @formatter:off
/**
 * Class for data base io of forex data such as ticks and bars. Provides method
 * for loading and storing ticks and bars.
 * 
 * Ticks and bars of specific periods are stored in separate tables. A table
 * containing bars can contain a maximum of MAX_ROWS_PER_BAR_TABLE rows, each
 * row describing one bar. Tables containing ticks contains are organized in
 * such way that ticks on the same TIME_INTERVAL_PER_TICK_TABLE, counting
 * starting at Epoch, will be stored in the same table. Methods are provided to
 * calculate the table index of a specific tick or bar. Table indices increase 
 * as the time of elements within the tables increase. See naming conventions
 * for tick and bar tables below.
 * 
 * One table, the storage range table, keeps track of the time range for which 
 * the data base contains data. It has a single row naming the time stamps of the 
 * beginning and end of the data stored in the data base. The value in the 
 * startOfStorage column is the time of the earliest bar stored in the data base 
 * and the value in the endOfStorage column is the latest end time (bar time + 
 * period interval) of a bar in the data base.
 * 
 * Columns of the storage range table are: { instrument, startOfStorage, endOfStorage }
 * 
 * Columns of a tick count table are: { tableIndex, tableSize, previousTicksCount }
 * 
 * Columns of a tick count table organizer are: { tickCountTableIndex, startTableIndex, endTableIndex }
 * 
 * Columns of a bar table are (in order): { time, open, close, high, low, volume }
 * 
 * Columns of a tick table are (in order): { time, ask, bid, askVolume, bidVolume }
 * 
 * Naming conventions for tables contained by the data base:
 * 
 * Storage range table: STORAGE_RANGE_TABLE
 * 
 * Tick count table: [instrument]_TICK_COUNT_TABLE_[table index]
 * Example:			 EURUSD_TICK_COUNT_TABLE_1543
 * 
 * Tick count table organizer: [instrument]_TICK_COUNT_TABLE_ORGANIZER
 * Example:					   EURUSD_TICK_COUNT_TABLE_ORGANIZER
 * 
 * Tick tables: [instrument]_TICK_[table index]
 * Example: 	EURUSD_TICK_1251
 * 
 * Bar tables: [instrument.name()]_[period.name()]_[offerSide]_BAR_[table index]
 * Example: 	EURUSD_TEN_MINS_ASK_BAR_1451
 * 
 * finalize() is implemented to make sure connection is closed.
 * 
 * @author Dennis Ekstrom
 */
// @formatter:on
public class ForexDataIO {

	// methods for testing
	// private void printMemoryUsage() {
	// int mb = 1024 * 1024;
	//
	// // Getting the runtime reference from system
	// Runtime runtime = Runtime.getRuntime();
	//
	// // Print used memory
	// System.out.println("Used Memory:"
	// + (runtime.totalMemory() - runtime.freeMemory()) / mb + " MB");
	// }
	//
	//
	// private void printTable(Instrument instrument, long time, boolean
	// printTicks,
	// boolean printBars) {
	// long tickTableIndex = getTickTableIndex(time);
	//
	// // ticks
	// if (printTicks) {
	// ArrayList<ITick> ticks = loadTickTable(instrument, tickTableIndex);
	// if (ticks != null) {
	// for (ITick t : ticks) {
	// System.out.println(t);
	// }
	// } else {
	// System.out.println("NO TICKS");
	// }
	// }
	//
	// // bars
	// if (printBars) {
	// for (Period p : ForexConstants.BAR_PERIODS) {
	// System.out.println();
	//
	// long tableindex = getBarTableIndex(p, time);
	// ArrayList<IBar> bars = loadBarTable(instrument, p, OfferSide.ASK,
	// tableindex);
	// if (bars != null) {
	// System.out.println(p + ": Table " + tableindex + " - " + bars.size()
	// + " Bar(s)");
	// for (IBar b : bars) {
	// System.out.println(p + " " + b);
	// }
	// } else {
	// System.out.println(p + " NO BARS");
	// }
	// }
	// }
	// }
	//
	//
	// private void printTickCountTable(Instrument instrument, long
	// tickCountTableIndex) {
	//
	// ArrayList<Long> tableIndices = new ArrayList<Long>();
	// ArrayList<Integer> tableSize = new ArrayList<Integer>();
	// ArrayList<Long> previousTicksCount = new ArrayList<Long>();
	//
	// String table = getTickCountTable(instrument, tickCountTableIndex);
	//
	// Connection con = getConnection();
	// Statement stmt = null;
	// ResultSet rs = null;
	// try {
	// String query = "SELECT * FROM " + table;
	//
	// stmt = con.createStatement();
	//
	// rs = stmt.executeQuery(query);
	//
	// while (rs.next()) {
	// tableIndices.add(rs.getLong(TABLE_INDEX_COLUMN_LABEL));
	// tableSize.add(rs.getInt(TABLE_SIZE_COLUMN_LABEL));
	// previousTicksCount.add(rs.getLong(PREVIOUS_TICKS_COUNT_COLUMN_LABEL));
	// }
	// } catch (SQLException e) {
	// System.out.println(e);
	// } finally {
	// close(stmt, rs);
	// }
	//
	// if (!tableIndices.isEmpty())
	// System.out.println("PRINTING: " + table);
	//
	// for (int i = 0; i < tableIndices.size(); i++) {
	// System.out.print("Table: " + tableIndices.get(i));
	// System.out.print("  Size: " + tableSize.get(i));
	// System.out.println("  Previous ticks count: " +
	// previousTicksCount.get(i));
	// }
	// }
	//
	// private void printTickCountTableOrganizer(Instrument instrument) {
	// ArrayList<Long> tickCountTableIndices = new ArrayList<Long>();
	// ArrayList<Long> startTickTableIndices = new ArrayList<Long>();
	// ArrayList<Long> endTickTableIndices = new ArrayList<Long>();
	//
	// String table = getTickCountTableOrganizer(instrument);
	//
	// Connection con = getConnection();
	// Statement stmt = null;
	// ResultSet rs = null;
	// try {
	// String query = "SELECT * FROM " + table;
	//
	// stmt = con.createStatement();
	//
	// rs = stmt.executeQuery(query);
	//
	// while (rs.next()) {
	// tickCountTableIndices
	// .add(rs.getLong(TICK_COUNT_TABLE_INDEX_COLUMN_LABEL));
	// startTickTableIndices
	// .add(rs.getLong(START_TICK_TABLE_INDEX_COLUMN_LABEL));
	// endTickTableIndices.add(rs.getLong(END_TICK_TABLE_INDEX_COLUMN_LABEL));
	// }
	// } catch (SQLException e) {
	// System.out.println(e);
	// } finally {
	// close(stmt, rs);
	// }
	//
	// if (!tickCountTableIndices.isEmpty())
	// System.out.println("PRINTING: " + table);
	//
	// for (int i = 0; i < tickCountTableIndices.size(); i++) {
	// System.out.print("Tick count table: " + tickCountTableIndices.get(i));
	// System.out.print("  Start tick table: " + startTickTableIndices.get(i));
	// System.out.println("  End tick table: " + endTickTableIndices.get(i));
	// }
	// }
	//
	// private void printStorageRangeTable(Instrument instrument) {
	// ArrayList<String> periods = new ArrayList<String>();
	// ArrayList<Long> start = new ArrayList<Long>();
	// ArrayList<Long> end = new ArrayList<Long>();
	//
	// Connection con = getConnection();
	// Statement stmt = null;
	// ResultSet rs = null;
	// try {
	// String query = "SELECT * FROM " + getStorageRangeTable(instrument);
	//
	// stmt = con.createStatement();
	//
	// rs = stmt.executeQuery(query);
	//
	// while (rs.next()) {
	// periods.add(rs.getString("period"));
	// start.add(rs.getLong("startOfStorage"));
	// end.add(rs.getLong("endOfStorage"));
	// }
	// } catch (SQLException e) {
	// System.out.println(e);
	// } finally {
	// close(stmt, rs);
	// }
	//
	// if (!periods.isEmpty())
	// System.out.println("PRINTING: " + getStorageRangeTable(instrument));
	//
	// for (int i = 0; i < periods.size(); i++) {
	// System.out.printf("Period: %-12s  Start:  %s  End:  %s%n",
	// periods.get(i),
	// ForexTools.getTimeRepresentation(start.get(i)),
	// ForexTools.getTimeRepresentation(end.get(i)));
	// }
	// }
	//
	// private void printMasterTable() {
	// ArrayList<String> tables = new ArrayList<String>();
	// ArrayList<String> types = new ArrayList<String>();
	//
	// Connection con = getConnection();
	// Statement stmt = null;
	// ResultSet rs = null;
	// try {
	// String query = "SELECT * FROM sqlite_master WHERE type == 'table'";
	//
	// stmt = con.createStatement();
	//
	// rs = stmt.executeQuery(query);
	//
	// int i = 0;
	// while (rs.next()) {
	// tables.add(rs.getString("tbl_name"));
	// types.add(rs.getString("type"));
	// i++;
	// }
	// System.out.println(i);
	// } catch (SQLException e) {
	// System.out.println(e);
	// } finally {
	// close(stmt, rs);
	// }
	//
	// if (!tables.isEmpty())
	// System.out.println("PRINTING: " + "sqlite_master");
	//
	// for (int i = 0; i < tables.size(); i++) {
	// System.out.print("Table: " + tables.get(i));
	// System.out.println("  Type: " + types.get(i));
	// }
	// }

	public static void main(String[] args) throws SQLException {

		ForexDataIO io = ForexDataIO.getInstance();

		io.verifyDataBaseStorageVeracity(true);

		// Instrument instrument = Instrument.EURUSD;
		//
		// tables that contains tick and bar of this time will be printed
		// long time = ForexTools.getTimeOf(2011, Calendar.JANUARY, 4, 23, 59,
		// 0, 0);
		//
		// io.printTable(instrument, time, true, false);
		//
		// io.printStorageRangeTable(instrument);
		//
		// io.printTickCountTableOrganizer(instrument);
		//
		// io.printTickCountTable(instrument, 4315);
	}

	public void verifyDataBaseStorageVeracity(boolean printInfo) {

		long totalTableCount = 0, assumedTotalTableCount = 0;

		// calculate assumedTotalTableCount
		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String query = "SELECT COUNT(*) FROM sqlite_master WHERE type == 'table'";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			assumedTotalTableCount = rs.getInt("COUNT(*)");

		} catch (SQLException e) {
			System.err.println("Error finding table row count of sqlite master table. "
					+ e.getMessage());

			System.exit(0);
		} finally {
			close(stmt, rs);
		}

		if (storedInstruments.isEmpty()) {
			if (printInfo)
				System.out.println("Data base contains no data.");

			return;
		} else {
			totalTableCount++; // add one for STORAGE_RANGE_TABLE
		}

		long startTableIndex = 0, endTableIndex = 0;
		ArrayList<IBar> barTable;
		IBar firstBar, lastBar;

		for (Instrument instrument : storedInstruments) {

			if (printInfo)
				System.out.println("INSTRUMENT: " + instrument.name());

			// verify bars storage
			for (OfferSide offerSide : OfferSide.values()) {

				if (printInfo)
					System.out.println("BARS: " + offerSide.name());

				for (Period period : ForexConstants.BAR_PERIODS) {

					Long start = getStartOfStorage(instrument, period);
					Long end = getEndOfStorage(instrument, period);

					if (start != null && end != null) {
						startTableIndex = getBarTableIndex(period, start);
						endTableIndex = getBarTableIndex(period, end);

						// check first bar
						barTable = loadBarTable(instrument, period, offerSide,
								startTableIndex);
					} else {
						barTable = null;
					}

					if (barTable != null) {

						firstBar = barTable.get(0);

						// check last bar
						barTable = loadBarTable(instrument, period, offerSide,
								endTableIndex);

						lastBar = barTable.get(barTable.size() - 1);

						if (printInfo) {
							System.out.println(period);
							System.out.println("First: " + firstBar);
							System.out.println("Last:  " + lastBar);
							System.out.println();
						}

						// check coherency between bar tables and bar storage
						// range
						if (firstBar.getTime() != this.getStartOfStorage(instrument,
								period)
								|| lastBar.getTime() != this.getEndOfStorage(instrument,
										period)) {

							System.out
									.println(period
											+ " bars incorrectly stored (wrong start or end of storage): "
											+ instrument);

							System.exit(0);
						}

						long barTime = firstBar.getTime();
						// check for correct interval between bars
						for (long t = startTableIndex; t <= endTableIndex; t++) {

							barTable = loadBarTable(instrument, period, offerSide, t);

							if (barTable == null) {
								System.out.println(period + ": gap in bar storage(table "
										+ t + " not found): " + instrument);

								System.exit(0);
							}

							for (IBar bar : barTable) {
								if (bar.getTime() != barTime) {
									System.out
											.println(period
													+ " bars incorrectly stored (incorrect interval): "
													+ instrument);

									System.exit(0);
								}

								barTime += period.getInterval();
							}

							totalTableCount++; // add on for each bar table
						}
					} else if (printInfo) {
						System.out.println(period);
						System.out.println("NO BARS");
						System.out.println();
					}
				}
			}

			// verify tick storage
			// check coherency between tick tables and tick count table
			ArrayList<Long> tickCountTableIndices = new ArrayList<Long>();

			// retrieve tick count table indices
			con = getConnection();
			try {

				String query = "SELECT * FROM " + getTickCountTableOrganizer(instrument);

				stmt = con.createStatement();

				rs = stmt.executeQuery(query);

				totalTableCount++; // add one for each tick count table
									// organizer

				while (rs.next()) {
					tickCountTableIndices.add(rs
							.getLong(TICK_COUNT_TABLE_INDEX_COLUMN_LABEL));
				}
			} catch (SQLException e) {
				System.err
						.println("Error checking coherency between tick tables and tick count table: "
								+ e.getMessage());

				System.exit(0);
			} finally {
				close(stmt, rs);
			}

			// go trough tick count tables and check coherency
			Long firstTickTableIndex = null, lastTickTableIndex = null;
			long tickTableIndex, previousTicksCount, assumedPreviousTicksCount = 0;
			int tableSize, assumedTableSize;
			try {

				String table, query;
				for (Long tickCountTableIndex : tickCountTableIndices) {

					table = getTickCountTable(instrument, tickCountTableIndex);

					query = "SELECT * FROM " + table;

					stmt = con.createStatement();

					rs = stmt.executeQuery(query);

					while (rs.next()) {

						tickTableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);
						tableSize = rs.getInt(TABLE_SIZE_COLUMN_LABEL);
						previousTicksCount = rs
								.getLong(PREVIOUS_TICKS_COUNT_COLUMN_LABEL);

						assumedTableSize = tableRowCount(
								getTickTable(instrument, tickTableIndex), con);

						if (tableSize != assumedTableSize
								|| previousTicksCount != assumedPreviousTicksCount) {
							System.out
									.println("incoherency detected between tick storage and TICK_COUNT_TABLE: "
											+ instrument);

							System.exit(0);
						}

						assumedPreviousTicksCount += tableSize;

						// update first and last tick table indices for future
						// comparison
						if (firstTickTableIndex == null)
							firstTickTableIndex = tickTableIndex;

						lastTickTableIndex = tickTableIndex;

						totalTableCount++; // add one for each tick table

					}

					totalTableCount++; // add one for each tick count table

				}
			} catch (SQLException e) {
				System.err
						.println("Error checking coherency between tick tables and tick count table: "
								+ e.getMessage());

				System.exit(0);
			} finally {
				close(stmt, rs);
			}

			// check coherency between tick tables and tick storage range
			ArrayList<ITick> tickTable;
			ITick firstTick = null, lastTick = null;
			if (firstTickTableIndex != null) {

				tickTable = loadTickTable(instrument, firstTickTableIndex);
				firstTick = tickTable.get(0);

				tickTable = loadTickTable(instrument, lastTickTableIndex);
				lastTick = tickTable.get(tickTable.size() - 1);

				if (firstTick.getTime() != getStartOfStorage(instrument, Period.TICK)
						|| lastTick.getTime() != getEndOfStorage(instrument, Period.TICK)) {
					System.err
							.println("Incoherency detected between tick tables and tick storage range.");

					System.exit(0);
				}

			} else if (getStartOfStorage(instrument, Period.TICK) != null
					|| getEndOfStorage(instrument, Period.TICK) != null) {

				System.err
						.println("Incoherency detected between tick tables and tick storage range.");

				System.exit(0);
			}

			// print last and first tick info
			if (printInfo) {
				System.out.println("TICKS");

				if (firstTick == null && lastTick == null) {
					System.out.println("NO TICKS");
				} else {
					System.out.println("First: " + firstTick);
					System.out.println("Last:  " + lastTick);
				}

				System.out.println();
			}

			// check that total table count is accurate
			if (totalTableCount != assumedTotalTableCount) {
				System.out.println("Error: Total table count inaccurate: " + instrument);

				System.exit(0);
			}
		}

		if (printInfo)
			System.out.println("DATABASE VERIFICATION COMPLETE!");
	}

	// *** ABOVE IS TESTING ***

	// column labels and table names and structures

	// private static final String STORAGE_RANGE_TABLE = "STORAGE_RANGE_TABLE";
	private static final String STORAGE_RANGE_TABLE_STRUCTURE = "("
			+ "period TEXT PRIMARY KEY, " + "startOfStorage INTEGER, "
			+ "endOfStorage INTEGER)";

	private static final String TABLE_INDEX_COLUMN_LABEL = "tableIndex";
	private static final String TABLE_SIZE_COLUMN_LABEL = "tableSize";
	private static final String PREVIOUS_TICKS_COUNT_COLUMN_LABEL = "previousTicksCount";

	private static final String TICK_COUNT_TABLE_STRUCTURE = "("
			+ TABLE_INDEX_COLUMN_LABEL + " INTEGER PRIMARY KEY ASC, "
			+ TABLE_SIZE_COLUMN_LABEL + " INTEGER NOT NULL, "
			+ PREVIOUS_TICKS_COUNT_COLUMN_LABEL + " INTEGER NOT NULL)";

	private static final String TICK_COUNT_TABLE_INDEX_COLUMN_LABEL = "tickCountTableIndex";
	private static final String START_TICK_TABLE_INDEX_COLUMN_LABEL = "startTickTableIndex";
	private static final String END_TICK_TABLE_INDEX_COLUMN_LABEL = "endTickTableIndex";
	private static final String TICK_COUNT_TABLE_ORGANIZER_STRUCTURE = "("
			+ TICK_COUNT_TABLE_INDEX_COLUMN_LABEL + " INTEGER PRIMARY KEY ASC, "
			+ START_TICK_TABLE_INDEX_COLUMN_LABEL + " INTEGER NOT NULL, "
			+ END_TICK_TABLE_INDEX_COLUMN_LABEL + " INTEGER NOT NULL)";

	private static final String BAR_TABLE_STRUCTURE = "("
			+ "time INTEGER PRIMARY KEY ASC, " + "open REAL NOT NULL, "
			+ "close REAL NOT NULL, " + "high REAL NOT NULL, " + "low REAL NOT NULL, "
			+ "volume REAL NOT NULL)";

	private static final String TICK_TABLE_STRUCTURE = "("
			+ "time INTEGER PRIMARY KEY ASC, " + "ask REAL NOT NULL, "
			+ "bid REAL NOT NULL, " + "askVolume REAL NOT NULL, "
			+ "bidVolume REAL NOT NULL)";

	public static enum InterpolationMethod {
		OPEN_TICK, CLOSE_TICK, FOUR_TICKS
	}

	public static final int TABLES_PER_TICK_COUNT_TABLE = 500;

	/**
	 * Period, in milliseconds, for tick tables. Set to 600,000.
	 * 
	 * This value is based on a value of a little less than 70,000 ticks per 24
	 * hours and a desired table size of 500 rows.
	 */
	public static final int TIME_INTERVAL_PER_TICK_TABLE = 600000;

	/**
	 * The maximum number of rows per bar table. Set to 420, explanation
	 * follows:
	 * 
	 * The interval i1 is the interval of any period p1 defined in
	 * ForexConstants.BAR_PERIODS. The interval i2 is the interval of the period
	 * p2 defined in STORED_BAR_PERIODS which interval multiplied by 2 is
	 * smaller than or equal to i1.
	 * 
	 * Let q = i1 / i2. Then [420 / q] is an integer.
	 * 
	 * This means a table size of 420 makes it convenient to convert between
	 * bars of different periods.
	 */
	public static final int MAX_ROWS_PER_BAR_TABLE = 420;

	// driver
	private static volatile Driver driver;
	private static final String driverName = "org.sqlite.JDBC";

	// database
	private static final String url = "jdbc:sqlite:forexdata.db";

	// connection
	private static final int TIMES_TO_USE_CONNECTION = 50;
	private volatile Connection con;
	private volatile int connectionUses;

	// the one and only instance
	private static volatile ForexDataIO instance;

	// storage ranges
	private volatile HashMap<Instrument, HashMap<Period, Long>> startOfStorage;
	private volatile HashMap<Instrument, HashMap<Period, Long>> endOfStorage;
	private volatile ArrayList<Instrument> storedInstruments;

	/**
	 * Create a ForexDataIO instance.
	 */
	private ForexDataIO() {
		// make sure connection is closed on shut down
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				close(con);
			}
		}));

		startOfStorage = new HashMap<Instrument, HashMap<Period, Long>>();
		endOfStorage = new HashMap<Instrument, HashMap<Period, Long>>();
		storedInstruments = new ArrayList<Instrument>();

		for (Instrument instrument : ForexConstants.INSTRUMENTS) {
			if (tableExists(getStorageRangeTable(instrument))) {
				if (loadStorageRangeLimits(instrument)) {
					storedInstruments.add(instrument);
				}
			}
		}
	}

	/**
	 * Returns true if storage range was found and successfully loaded for the
	 * given instrument, otherwise false.
	 * 
	 * @param instrument the instrument for which to load storage range limits
	 * @return true if storage range was found and successfully loaded for the
	 *         given instrument, otherwise false
	 */
	private boolean loadStorageRangeLimits(Instrument instrument) {

		String table = getStorageRangeTable(instrument);

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {

			ArrayList<Period> periods = new ArrayList<Period>();
			periods.add(Period.TICK);
			periods.addAll(ForexConstants.BAR_PERIODS);

			for (Period period : periods) {

				String query = "SELECT * FROM " + table + " WHERE period == '"
						+ period.name() + "'";

				stmt = con.createStatement();

				rs = stmt.executeQuery(query);

				if (rs.next()) {

					long start = rs.getLong("startOfStorage");
					long end = rs.getLong("endOfStorage");

					if (rs.next()) {
						throw new ForexException("More than one row of instrument "
								+ instrument.name() + " found in " + table);
					}

					setStartOfStorage(instrument, period, start);
					setEndOfStorage(instrument, period, end);

				}
			}

			return true;

		} catch (SQLException e) {
			System.err.println("Error loading storage range limits: " + e.getMessage());
		} finally {
			close(stmt, rs);
		}

		return false;
	}

	/**
	 * Returns the connection to use within this class.
	 */
	private Connection getConnection() {
		if (con == null || ++connectionUses > TIMES_TO_USE_CONNECTION) {
			close(con);
			con = connect();
			connectionUses = 0;
		}

		return con;
	}

	/**
	 * Get the connection to the database.
	 */
	private static Connection connect() {
		try {
			if (driver != null) {
				DriverManager.deregisterDriver(driver);
			}
			// Load the JDBC driver class dynamically.
			driver = (Driver) Class.forName(driverName).newInstance();
			// DriverManager.registerDriver(driver); // TODO ska denna vara med
			// ???
			Thread.sleep(100); // wait for a while
			return DriverManager.getConnection(url);
		} catch (Exception e) {
			System.err.println("Exception connecting to database: " + e);
		}

		return null;
	}

	private static void close(Object... objects) {
		for (Object o : objects) {
			if (o == null)
				continue;

			if (o instanceof ResultSet) {
				try {
					((ResultSet) o).close();
				} catch (Exception e) {
					System.err.println("Exception closing result set: " + e.getMessage());
				}
			} else if (o instanceof Statement) {
				try {
					((Statement) o).close();
				} catch (Exception e) {
					System.err.println("Exception closing statement: " + e.getMessage());
				}
			} else if (o instanceof Connection) {
				try {
					((Connection) o).close();
				} catch (Exception e) {
					System.err.println("Exception closing connection: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Updates the storage range of given instrument and period. Time should be
	 * the time of the bar or tick for which the storage range is updated for.
	 */
	private void updateStorageRange(long time, Instrument instrument, Period period) {

		String table = getStorageRangeTable(instrument);

		if (!tableExists(table)) {
			createTableIfNotExists(table, STORAGE_RANGE_TABLE_STRUCTURE);
		}

		boolean rangeChanged = false;

		if (getStartOfStorage(instrument, period) == null
				|| time < getStartOfStorage(instrument, period)) {

			setStartOfStorage(instrument, period, time);
			rangeChanged = true;

		}

		if (getEndOfStorage(instrument, period) == null
				|| time > getEndOfStorage(instrument, period)) {

			setEndOfStorage(instrument, period, time);
			rangeChanged = true;

		}

		if (rangeChanged) {

			Connection con = getConnection();
			PreparedStatement prst = null;
			try {

				con.setAutoCommit(false);

				String query = "INSERT OR REPLACE INTO " + table + " VALUES(?, ?, ?)";

				prst = con.prepareStatement(query);

				// set values
				prst.setString(1, period.name());
				prst.setLong(2, getStartOfStorage(instrument, period));
				prst.setLong(3, getEndOfStorage(instrument, period));

				prst.executeUpdate();
				con.commit();

				con.setAutoCommit(true);

			} catch (SQLException e) {
				System.err.println("Error updating storage range: " + e.getMessage());
			} finally {
				close(prst);
			}
		}
	}

	private void updateTickCountTable(Instrument instrument, long updatedTickTableIndex) {

		HashSet<Long> updatedTickCountTables = new HashSet<Long>();

		String table = getTickTable(instrument, updatedTickTableIndex);

		if (!tableExists(table))
			return;

		long tickCountTableIndex = getTickCountTableIndex(updatedTickTableIndex);
		String tickCountTable = getTickCountTable(instrument, tickCountTableIndex);

		createTableIfNotExists(getTickCountTableOrganizer(instrument),
				TICK_COUNT_TABLE_ORGANIZER_STRUCTURE);

		createTableIfNotExists(getTickCountTable(instrument, tickCountTableIndex),
				TICK_COUNT_TABLE_STRUCTURE);

		int tableSize = 0;
		long previousTicksCount = 0;
		try {
			// find table size. Table exists, so method won't return null
			tableSize = tableRowCount(table);

			// find previous ticks counts
			previousTicksCount = getPreviousTicksCountOfTable(instrument,
					updatedTickTableIndex);
		} catch (SQLException e) {
			System.err.println("Error getting previous ticks count when updating "
					+ tickCountTable + ": " + e.getMessage());

			System.exit(0);
		}

		// insert into table
		Connection con = getConnection();
		PreparedStatement prst = null;
		try {

			con.setAutoCommit(false);

			String query = "INSERT OR REPLACE INTO "
					+ getTickCountTable(instrument, tickCountTableIndex)
					+ " VALUES(?, ?, ?)";

			prst = con.prepareStatement(query);

			// set values
			prst.setLong(1, updatedTickTableIndex);
			prst.setInt(2, tableSize);
			prst.setLong(3, previousTicksCount);

			prst.executeUpdate();

			con.setAutoCommit(true);

		} catch (SQLException e) {
			System.err.println("Error inserting values when updating " + tickCountTable
					+ ": " + e.getMessage());

			System.exit(0);
		} finally {
			close(prst);
		}

		// immediate update needed for upcoming use
		updateTickCountTableOrganizer(instrument, tickCountTableIndex);

		// update rows affected by the change

		Statement stmt = null;
		ResultSet rs = null;
		try {

			long tickCountTableIndexOfEndOfStorage = getLastStoredTickCountTableIndex(instrument);
			long tableIndex;
			String select, insert;

			con = getConnection(); // renew connection

			con.setAutoCommit(false);

			select = "SELECT * FROM " + tickCountTable + " WHERE "
					+ TABLE_INDEX_COLUMN_LABEL + " > " + updatedTickTableIndex
					+ " ORDER BY " + TABLE_INDEX_COLUMN_LABEL + " ASC";

			stmt = con.createStatement();

			rs = stmt.executeQuery(select);

			insert = "INSERT OR REPLACE INTO " + tickCountTable + " VALUES(?, ?, ?)";

			prst = con.prepareStatement(insert);
			updatedTickCountTables.add(tickCountTableIndex);
			while (rs.next()) {
				// OBS: update before tableSize.
				// tableSize here is that of previous table.
				previousTicksCount += tableSize;

				tableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);
				tableSize = rs.getInt(TABLE_SIZE_COLUMN_LABEL);

				prst.setLong(1, tableIndex);
				prst.setInt(2, tableSize);
				prst.setLong(3, previousTicksCount);

				prst.addBatch();
			}

			prst.executeBatch();
			con.commit();
			con.setAutoCommit(true);

			updatedTickCountTables.add(tickCountTableIndex);

			con = getConnection();
			con.setAutoCommit(false);
			while (++tickCountTableIndex <= tickCountTableIndexOfEndOfStorage) {

				tickCountTable = getTickCountTable(instrument, tickCountTableIndex);

				select = "SELECT * FROM " + tickCountTable;

				stmt = con.createStatement();

				rs = stmt.executeQuery(select);

				insert = "INSERT OR REPLACE INTO " + tickCountTable + " VALUES(?, ?, ?)";

				prst = con.prepareStatement(insert);
				while (rs.next()) {
					// OBS: update before tableSize.
					// tableSize here is that of previous table.
					previousTicksCount += tableSize;

					tableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);
					tableSize = rs.getInt(TABLE_SIZE_COLUMN_LABEL);

					prst.setLong(1, tableIndex);
					prst.setInt(2, tableSize);
					prst.setLong(3, previousTicksCount);

					prst.addBatch();
				}

				prst.executeBatch();
				con.commit();

				updatedTickCountTables.add(tickCountTableIndex);
			}

			con.setAutoCommit(true);

		} catch (SQLException e) {
			System.err.println("Error updating rows when updating " + tickCountTable
					+ ": " + e.getMessage());

			System.exit(0);
		} finally {
			close(stmt);
		}

		for (Long tableCountTableIndex : updatedTickCountTables)
			updateTickCountTableOrganizer(instrument, tableCountTableIndex);
	}

	private void updateTickCountTableOrganizer(Instrument instrument,
			long updatedTickCountTableIndex) {

		String updatedTable = getTickCountTable(instrument, updatedTickCountTableIndex);

		Connection con = getConnection();
		Statement stmt = null;
		PreparedStatement prst = null;
		ResultSet rs = null;
		Long startTableIndex = null, endTableIndex = null;
		try {

			con.setAutoCommit(false);

			// get first
			String query = "SELECT * FROM " + updatedTable + " ORDER BY "
					+ TABLE_INDEX_COLUMN_LABEL + " ASC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next())
				startTableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);

			// get last
			query = "SELECT * FROM " + updatedTable + " ORDER BY "
					+ TABLE_INDEX_COLUMN_LABEL + " DESC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next())
				endTableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);

			if (startTableIndex != null && endTableIndex != null) {
				query = "INSERT OR REPLACE INTO "
						+ getTickCountTableOrganizer(instrument) + " VALUES(?, ?, ?)";

				prst = con.prepareStatement(query);

				prst.setLong(1, updatedTickCountTableIndex);
				prst.setLong(2, startTableIndex);
				prst.setLong(3, endTableIndex);

				prst.executeUpdate();
			}

			con.setAutoCommit(true);

		} catch (SQLException e) {
			System.err.println("Error updating " + getTickCountTableOrganizer(instrument)
					+ ": " + e.getMessage());
		} finally {
			close(prst, stmt, rs);
		}
	}

	/**
	 * Returns the tick count table index of the first tick count table of given
	 * instrument in the database, null if no tick count tables where found.
	 * 
	 * @param instrument the instrument of the tick
	 * @return the tick count table index of the first tick count table of given
	 *         instrument in the database
	 */
	private Long getFirstStoredTickCountTableIndex(Instrument instrument)
			throws SQLException {

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// get first tick count table index
			String query = "SELECT * FROM " + getTickCountTableOrganizer(instrument)
					+ " ORDER BY " + TICK_COUNT_TABLE_INDEX_COLUMN_LABEL + " ASC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next())
				return rs.getLong(TICK_COUNT_TABLE_INDEX_COLUMN_LABEL);
			else
				return null;

		} finally {
			close(stmt, rs);
		}
	}

	/**
	 * Returns the tick count table index of the last tick count table of given
	 * instrument in the database, null if no tick count tables where found.
	 * 
	 * @param instrument the instrument of the tick
	 * @return the tick count table index of the last tick count table of given
	 *         instrument in the database
	 */
	private Long getLastStoredTickCountTableIndex(Instrument instrument)
			throws SQLException {

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// get first tick count table index
			String query = "SELECT * FROM " + getTickCountTableOrganizer(instrument)
					+ " ORDER BY " + TICK_COUNT_TABLE_INDEX_COLUMN_LABEL
					+ " DESC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next())
				return rs.getLong(TICK_COUNT_TABLE_INDEX_COLUMN_LABEL);
			else
				return null;

		} finally {
			close(stmt, rs);
		}
	}

	private long getPreviousTicksCountOfTable(Instrument instrument, long tickTableIndex)
			throws SQLException {

		Long tickCountTableIndexOfStartOfStorage = getFirstStoredTickCountTableIndex(instrument);

		if (tickCountTableIndexOfStartOfStorage == null)
			return 0L;

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {

			long tickCountTableIndex = getTickCountTableIndex(tickTableIndex);
			String tickCountTable;
			String query;
			boolean retrivedResultSet = false;
			do {
				tickCountTable = getTickCountTable(instrument, tickCountTableIndex);

				if (tableExists(tickCountTable, con)) {

					query = "SELECT * FROM " + tickCountTable + " WHERE "
							+ TABLE_INDEX_COLUMN_LABEL + " < " + tickTableIndex
							+ " ORDER BY " + TABLE_INDEX_COLUMN_LABEL + " DESC LIMIT 1";

					stmt = con.createStatement();

					rs = stmt.executeQuery(query);

					if (rs.next())
						retrivedResultSet = true;
				}
			} while (!retrivedResultSet
					&& --tickCountTableIndex >= tickCountTableIndexOfStartOfStorage);

			if (retrivedResultSet) {
				return rs.getLong(PREVIOUS_TICKS_COUNT_COLUMN_LABEL)
						+ rs.getInt(TABLE_SIZE_COLUMN_LABEL);
			} else {
				return 0L;
			}
		} finally {
			close(stmt, rs);
		}
	}

	private void createTableIfNotExists(String table, String structure) {
		createTableIfNotExists(table, structure, getConnection());
	}

	private void createTableIfNotExists(String table, String structure, Connection con) {

		Statement stmt = null;
		try {

			stmt = con.createStatement();

			String query = "CREATE TABLE IF NOT EXISTS " + table + structure;

			stmt.execute(query);

		} catch (SQLException e) {
			System.err.println("Error creating " + table + ": " + e.getMessage());
		} finally {
			close(stmt);
		}
	}

	/**
	 * Returns true if the specified table exists in the database, otherwise
	 * false.
	 * 
	 * @param table the table to check for existence
	 * @return true if the specified table exists in the database, otherwise
	 *         false
	 */
	private boolean tableExists(String table) {
		return tableExists(table, getConnection());
	}

	/**
	 * Returns true if the specified table exists in the database, otherwise
	 * false.
	 * 
	 * @param table the table to check for existence
	 * @param con the connection to use
	 * @return true if the specified table exists in the database, otherwise
	 *         false
	 */
	private boolean tableExists(String table, Connection con) {
		if (table == null)
			return false;

		ResultSet rs = null;
		try {
			DatabaseMetaData meta = con.getMetaData();

			rs = meta.getTables(null, null, table, null);

			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			return false;
		} finally {
			close(rs);
		}

		return false;
	}

	/**
	 * Returns the number of rows in the given table.
	 * 
	 * @param table the table
	 * @return the number of rows in the given table
	 * @throws SQLException if an exception was thrown during the process of
	 *             finding table row count
	 */
	private int tableRowCount(String table) throws SQLException {
		return tableRowCount(table, getConnection());
	}

	/**
	 * Returns the number of rows in a table.
	 * 
	 * @param table the table
	 * @param con the connection to use
	 * @return the number of rows in a table
	 * @throws SQLException if an exception was thrown during the process of
	 *             finding table row count, or if table does not exist
	 */
	private int tableRowCount(String table, Connection con) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT COUNT(*) FROM " + table;

			stmt = con.createStatement();

			rs = stmt.executeQuery(sql);

			return rs.getInt("COUNT(*)");

		} finally {
			close(stmt, rs);
		}
	}

	/**
	 * Returns the time stamp of the beginning of the stored data of the given
	 * instrument and period. If period is Period.TICK the start of storage for
	 * ticks is returned. Null is returned if start of storage could not be
	 * found for the given parameters.
	 * 
	 * @param instrument the instrument to find start of storage for
	 * @param period the period to find start of storage for
	 * @return the time stamp of the beginning of the stored data of the given
	 *         instrument
	 */
	public Long getStartOfStorage(Instrument instrument, Period period) {
		if (!startOfStorage.containsKey(instrument)) {
			return null;
		}

		return startOfStorage.get(instrument).get(period);
	}

	/**
	 * Sets the given time as the start of storage for given instrument and
	 * period.
	 */
	private void setStartOfStorage(Instrument instrument, Period period, long time) {
		if (!startOfStorage.containsKey(instrument)) {
			startOfStorage.put(instrument, new HashMap<Period, Long>());
		}

		startOfStorage.get(instrument).put(period, time);
	}

	/**
	 * Returns the time stamp of the end of the stored data of the given
	 * instrument and period. If period is Period.TICK the end of storage for
	 * ticks is returned. Null is returned if end of storage could not be found
	 * for the given parameters.
	 * 
	 * @param instrument the instrument to find end of storage for
	 * @param period the period to find end of storage for
	 * @return the time stamp of the end of the stored data of the given
	 *         instrument
	 */
	public Long getEndOfStorage(Instrument instrument, Period period) {
		if (!endOfStorage.containsKey(instrument)) {
			return null;
		}

		return endOfStorage.get(instrument).get(period);
	}

	/**
	 * Sets the given time as the end of storage for given instrument and
	 * period.
	 */
	private void setEndOfStorage(Instrument instrument, Period period, long time) {
		if (!endOfStorage.containsKey(instrument)) {
			endOfStorage.put(instrument, new HashMap<Period, Long>());
		}

		endOfStorage.get(instrument).put(period, time);
	}

	/**
	 * Returns the number of ticks of time lower than given time that are stored
	 * in the database. If the given time is the exact time of a tick, that tick
	 * is not counted as previous.
	 * 
	 * @param time the time to find previous ticks for
	 * @return the number of ticks of time lower than given time that are stored
	 *         in the database
	 */
	public long getPreviousTicksCount(Instrument instrument, long time) {

		Long start = getStartOfStorage(instrument, Period.TICK);
		Long end = getEndOfStorage(instrument, Period.TICK);

		if (start == null || end == null) {
			return 0L;
		} else if (time < start) {
			return 0L;
		} else if (time > end) {
			time = end;
		}

		long tickTableIndex = getTickTableIndex(time);
		ArrayList<ITick> tickTable = loadTickTable(instrument, tickTableIndex);
		long closestFoundTickTableIndex, tableSize, previousTicksCount;

		Long tickCountTableIndex = getTickCountTableThatContains(instrument,
				tickTableIndex);

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {

			if (tickCountTableIndex == null) {
				throw new ForexException(
						"Unable to find tick count table containing tick table index: "
								+ tickTableIndex
								+ " when getting previous tick count of time: " + time);
			}

			String query = "SELECT * FROM "
					+ getTickCountTable(instrument, tickCountTableIndex) + " WHERE "
					+ TABLE_INDEX_COLUMN_LABEL + " <= " + tickTableIndex + " ORDER BY "
					+ TABLE_INDEX_COLUMN_LABEL + " DESC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next()) {

				closestFoundTickTableIndex = rs.getLong(TABLE_INDEX_COLUMN_LABEL);
				tableSize = rs.getLong(TABLE_SIZE_COLUMN_LABEL);
				previousTicksCount = rs.getLong(PREVIOUS_TICKS_COUNT_COLUMN_LABEL);

				// compare table that was found to table that would contain a
				// tick of given time
				if (closestFoundTickTableIndex < tickTableIndex) {

					previousTicksCount += tableSize;

				} else { // table that would contain tick of given time exists

					int i = 0;
					while (i < tickTable.size() && tickTable.get(i).getTime() < time) {
						previousTicksCount++;
						i++;
					}
				}

				return previousTicksCount;

			} else {
				throw new ForexException(
						"Tick count table does not contain expected data.");
			}

		} catch (SQLException e) {
			throw new ForexException("Exception when selecting from "
					+ getTickCountTableOrganizer(instrument)
					+ " when getting previous tick count of time: " + time);
		} finally {
			close(stmt, rs);
		}
	}

	/**
	 * Returns the index of the tick count table of given instrument that
	 * contains the given tickTableIndex, null if no such tick count table was
	 * found
	 * 
	 * @param instrument the instrument
	 * @param tickTableIndex the tick table index
	 * @return the index of the tick count table that contains the given
	 *         tickTableIndex
	 */
	private Long getTickCountTableThatContains(Instrument instrument, long tickTableIndex) {

		String table = getTickCountTableOrganizer(instrument);

		if (!tableExists(table))
			return null;

		Connection con = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {

			String query = "SELECT * FROM " + table + " WHERE "
					+ START_TICK_TABLE_INDEX_COLUMN_LABEL + " <= " + tickTableIndex
					+ " ORDER BY " + START_TICK_TABLE_INDEX_COLUMN_LABEL
					+ " DESC LIMIT 1";

			stmt = con.createStatement();

			rs = stmt.executeQuery(query);

			if (rs.next())
				return rs.getLong(TICK_COUNT_TABLE_INDEX_COLUMN_LABEL);

		} catch (SQLException e) {
			System.err.println("Error when getting tick count table that contains "
					+ tickTableIndex + ": " + e.getMessage());
		} finally {
			close(stmt, rs);
		}

		return null;
	}

	/**
	 * Returns an instance of ForexDataIO.
	 * 
	 * @return an instance of ForexDataIO
	 */
	public static ForexDataIO getInstance() {
		if (instance == null)
			instance = new ForexDataIO();

		return instance;
	}

	/**
	 * Returns the tick count table index for the table of specified tick table
	 * index.
	 * 
	 * @param tickTableIndex the tick table index of the table
	 * @return the tick count table index
	 */
	public static synchronized long getTickCountTableIndex(long tickTableIndex) {
		if (tickTableIndex < 0)
			throw new IllegalArgumentException("tickTableIndex < 0");

		return tickTableIndex / TABLES_PER_TICK_COUNT_TABLE;
	}

	/**
	 * Returns the table index for a tick of specified time.
	 * 
	 * @param time the time of the tick
	 * @return the index of the tick table
	 */
	public static synchronized long getTickTableIndex(long time) {
		if (time < 0)
			throw new IllegalArgumentException("time < 0");

		return time / TIME_INTERVAL_PER_TICK_TABLE;
	}

	/**
	 * Returns the time range corresponding to the given table index.
	 * 
	 * @param tableIndex the index of the tick table
	 * @return the time range corresponding to the given table index
	 */
	public static synchronized TimeRange getTickTableTimeRange(long tableIndex) {

		long startTime = tableIndex * TIME_INTERVAL_PER_TICK_TABLE;

		return new TimeRange(startTime, startTime + TIME_INTERVAL_PER_TICK_TABLE - 1);
	}

	/**
	 * Returns the table index for a bar of specified time and period.
	 * 
	 * @param period the period of the bar
	 * @param time the time of the bar
	 * @return the index of the bar table
	 * @throws IllegalArgumentException if time < 0
	 * @throws IllegalArgumentException if period if not defined in
	 *             ForexConstant.BAR_PERIODS
	 */
	public static synchronized long getBarTableIndex(Period period, long time) {
		if (!ForexConstants.BAR_PERIODS.contains(period))
			throw new IllegalArgumentException("period not in ForexConstant.BAR_PERIODS");

		return time / period.getInterval() / MAX_ROWS_PER_BAR_TABLE;
	}

	/**
	 * Returns the time range corresponding to the given table index.
	 * 
	 * @param tableIndex the index of the bar table
	 * @return the time range corresponding to the given table index
	 */
	public static synchronized TimeRange getBarTableTimeRange(Period period,
			long tableIndex) {

		long startTime = tableIndex * period.getInterval() * MAX_ROWS_PER_BAR_TABLE;

		return new TimeRange(startTime, startTime + period.getInterval()
				* MAX_ROWS_PER_BAR_TABLE - 1);
	}

	/**
	 * Store a tick.
	 * 
	 * @param instrument instrument of the tick
	 * @param tick tick to store
	 */
	public synchronized void storeTick(Instrument instrument, Tick tick) {

		updateStorageRange(tick.getTime(), instrument, Period.TICK);

		Connection con = getConnection();
		PreparedStatement prst = null;
		try {
			long tableIndex = getTickTableIndex(tick.getTime());
			String table = getTickTable(instrument, tableIndex);

			createTableIfNotExists(table, TICK_TABLE_STRUCTURE, con);

			String query = "INSERT OR IGNORE INTO " + table + " VALUES(?, ?, ?, ?, ?)";

			prst = con.prepareStatement(query);

			// set values
			prst.setLong(1, tick.getTime());
			prst.setDouble(2, tick.getAsk());
			prst.setDouble(3, tick.getBid());
			prst.setDouble(4, tick.getAskVolume());
			prst.setDouble(5, tick.getBidVolume());

			prst.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Exception storing tick: " + e);
		} finally {
			close(prst);
		}
	}

	/**
	 * Store ticks in given ArrayList. The ArrayList has to be sorted in
	 * ascending order with respect to the time of the ticks.
	 * 
	 * @param instrument instrument of the tick
	 * @param ticks ticks to store, has to be sorted in ascending order with
	 *            respect to time
	 */
	public synchronized void storeTicks(Instrument instrument, ArrayList<ITick> ticks) {

		updateStorageRange(ticks.get(0).getTime(), instrument, Period.TICK);
		updateStorageRange(ticks.get(ticks.size() - 1).getTime(), instrument, Period.TICK);

		ArrayList<Long> updatedTables = new ArrayList<Long>();

		Connection con = getConnection();
		PreparedStatement prst = null;
		try {

			con.setAutoCommit(false);

			int i = 0;
			long tableIndex;
			String table, query;
			while (i < ticks.size()) {
				tableIndex = getTickTableIndex(ticks.get(i).getTime());
				table = getTickTable(instrument, tableIndex);

				createTableIfNotExists(table, TICK_TABLE_STRUCTURE, con);

				query = "INSERT OR IGNORE INTO " + table + " VALUES(?, ?, ?, ?, ?)";

				prst = con.prepareStatement(query);

				do {
					// set values
					prst.setLong(1, ticks.get(i).getTime());
					prst.setDouble(2, ticks.get(i).getAsk());
					prst.setDouble(3, ticks.get(i).getBid());
					prst.setDouble(4, ticks.get(i).getAskVolume());
					prst.setDouble(5, ticks.get(i).getBidVolume());

					prst.addBatch();

				} while (++i < ticks.size()
						&& tableIndex == getTickTableIndex(ticks.get(i).getTime()));

				prst.executeBatch();
				con.commit();

				updatedTables.add(tableIndex);
			}

			con.setAutoCommit(true);

		} catch (SQLException e) {
			System.err.println("Exception storing ticks: " + e);
		} finally {
			close(prst);
		}

		for (Long i : updatedTables) {
			updateTickCountTable(instrument, i);
		}
	}

	/**
	 * Store a bar.
	 * 
	 * @param instrument instrument of the bar
	 * @param period period of the bar
	 * @param offerSide offer side of the bar
	 * @param bar bar to store
	 */
	public synchronized void storeBar(Instrument instrument, Period period,
			OfferSide offerSide, IBar bar) {

		updateStorageRange(bar.getTime(), instrument, period);

		Connection con = getConnection();
		PreparedStatement prst = null;
		try {
			long tableIndex = getBarTableIndex(period, bar.getTime());
			String table = getBarTable(instrument, period, offerSide, tableIndex);

			createTableIfNotExists(table, BAR_TABLE_STRUCTURE, con);

			String query = "INSERT OR IGNORE INTO " + table + " VALUES(?, ?, ?, ?, ?, ?)";

			prst = con.prepareStatement(query);

			// set values
			prst.setLong(1, bar.getTime());
			prst.setDouble(2, bar.getOpen());
			prst.setDouble(3, bar.getClose());
			prst.setDouble(4, bar.getHigh());
			prst.setDouble(5, bar.getLow());
			prst.setDouble(6, bar.getVolume());

			prst.executeUpdate();

		} catch (SQLException e) {
			System.err.println("Exception storing bar: " + e);
		} finally {
			close(prst);
		}
	}

	/**
	 * Store bars in given ArrayList. The ArrayList has to be sorted in
	 * ascending order with respect to the time of the bars.
	 * 
	 * @param instrument instrument of the bar
	 * @param period period of the bar
	 * @param offerSide offer side of the bar
	 * @param bars bars to store, has to be sorted in ascending order with
	 *            respect to time
	 */
	public synchronized void storeBars(Instrument instrument, Period period,
			OfferSide offerSide, ArrayList<IBar> bars) {

		updateStorageRange(bars.get(0).getTime(), instrument, period);
		updateStorageRange(bars.get(bars.size() - 1).getTime(), instrument, period);

		Connection con = getConnection();
		PreparedStatement prst = null;
		try {

			con.setAutoCommit(false);

			int i = 0;
			while (i < bars.size()) {
				long tableIndex = getBarTableIndex(period, bars.get(i).getTime());
				String table = getBarTable(instrument, period, offerSide, tableIndex);

				createTableIfNotExists(table, BAR_TABLE_STRUCTURE, con);

				String query = "INSERT OR IGNORE INTO " + table
						+ " VALUES(?, ?, ?, ?, ?, ?)";

				prst = con.prepareStatement(query);

				do {
					// set values
					prst.setLong(1, bars.get(i).getTime());
					prst.setDouble(2, bars.get(i).getOpen());
					prst.setDouble(3, bars.get(i).getClose());
					prst.setDouble(4, bars.get(i).getHigh());
					prst.setDouble(5, bars.get(i).getLow());
					prst.setDouble(6, bars.get(i).getVolume());

					prst.addBatch();

				} while (++i < bars.size()
						&& tableIndex == getBarTableIndex(period, bars.get(i).getTime()));

				prst.executeBatch();
				con.commit();
			}

			con.setAutoCommit(true);

		} catch (SQLException e) {
			System.err.println("Exception storing bars: " + e);
		} finally {
			close(prst);
		}
	}

	/**
	 * Returns an ArrayList of all ticks in the table of specified instrument
	 * and index, null if no such ticks were found.
	 * 
	 * @param instrument the instrument of the ticks
	 * @param tableIndex the index of the table
	 * @return an ArrayList of all ticks in the table of specified instrument
	 *         and index, null if no such table was found
	 */
	public synchronized ArrayList<ITick> loadTickTable(Instrument instrument,
			long tableIndex) {
		return readTicks(getTickTable(instrument, tableIndex));
	}

	/**
	 * Returns an ArrayList of all ticks in the table of specified instrument,
	 * interval and index, null if no such ticks were found.
	 * 
	 * @param instrument the instrument of the ticks
	 * @param interval the interval of the ticks
	 * @param interpolationMethod the method for turning bars into ticks, has no
	 *            effect if interval is less than the smallest period defined by
	 *            ForexConstants.BAR_PERIODS
	 * @param tableIndex the index of the table
	 * @return an ArrayList of all ticks in the table of specified instrument
	 *         and index, null if no such table was found
	 */
	public synchronized ArrayList<ITick> loadTickTable(Instrument instrument,
			Period interval, InterpolationMethod interpolationMethod, long tableIndex) {
		if (instrument == null || interval == null)
			throw new IllegalArgumentException("arguments can't be null");

		if (interval.equals(Period.TICK))
			return loadTickTable(instrument, tableIndex);
		else if (interval.isSmallerThan(ForexConstants.BAR_PERIODS.get(0)))
			return convertTicksToTicks(instrument, tableIndex, interval);
		else
			return convertBarsToTicks(instrument, tableIndex, interval,
					interpolationMethod);
	}

	/**
	 * Returns an ArrayList of all bars in the table of specified instrument,
	 * period, offer side and index, null if no such bars were found.
	 * 
	 * @param instrument the instrument of the bar
	 * @param period the period of the bar
	 * @param offerSide the offer side of the bar
	 * @param tableIndex the index of the table
	 * @return an ArrayList of all bars in the table of specified instrument,
	 *         period, offer side and index, null if no such table was found
	 * @throws IllegalArgumentException if any of the arguments is null
	 * @throws IllegalArgumentException if given period is not defined by
	 *             ForexConstants.BAR_PERIODS
	 */
	public synchronized ArrayList<IBar> loadBarTable(Instrument instrument,
			Period period, OfferSide offerSide, long tableIndex) {
		if (instrument == null || period == null || offerSide == null)
			throw new IllegalArgumentException("arguments can't be null");
		else if (!ForexConstants.BAR_PERIODS.contains(period))
			throw new IllegalArgumentException(
					"Not a valid period (period has to be defined by ForexConstants.BAR_PERIODS)");

		return readBars(getBarTable(instrument, period, offerSide, tableIndex));
	}

	/**
	 * Returns true if given time is the start time of the bar of given period
	 * which includes specified time, otherwise false.
	 * 
	 * @param time the time stamp to check whether is bar start or not
	 * @param period the period to determine the interval
	 * @return true if given time is the start time of the bar of given period
	 *         which includes specified time, otherwise false
	 */
	public static synchronized boolean isBarStart(Period period, long time) {
		return time % period.getInterval() == 0;
	}

	/**
	 * Returns the starting time of the bar that includes time specified in time
	 * parameter.
	 * 
	 * @param time the time to find the bar start for
	 * @param period the period of the bar
	 * @return the starting time of the bar that includes time specified in time
	 *         parameter
	 */
	public static synchronized long getBarStart(Period period, long time) {
		if (period == Period.TICK)
			return time;

		if (period.isSmallerThan(Period.WEEKLY))
			return (time / period.getInterval()) * period.getInterval();

		Calendar cal = new GregorianCalendar(ForexConstants.GMT, ForexConstants.LOCALE);

		cal.setTimeInMillis(time);
		int year = cal.get(Calendar.YEAR);

		if (period.equals(Period.WEEKLY)) {
			int week = cal.get(Calendar.WEEK_OF_YEAR);
			cal.clear();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.WEEK_OF_YEAR, week);

			return cal.getTimeInMillis();
		}

		if (period.equals(Period.MONTHLY)) {
			int month = cal.get(Calendar.MONTH);
			cal.clear();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);

			return cal.getTimeInMillis();
		}

		if (period.equals(Period.ONE_YEAR)) {
			cal.clear();
			cal.set(Calendar.YEAR, year);

			return cal.getTimeInMillis();
		}

		throw new ForexException("period not defined in ForexConstants.BAR_PERIODS");
	}

	/**
	 * Converts ticks in the table of given table index to ticks of given
	 * interval. The ticks are simply a selection of all ticks, volumes not
	 * adjusted to make up for fewer ticks. Returns null if conversion didn't
	 * result in any ticks.
	 * 
	 * @param instrument the instrument of the ticks
	 * @param tableIndex the table index of the ticks to convert
	 * @param convertTo the interval of the converted ticks
	 * @return an ArrayList with the ticks that resulted from the conversion
	 */
	private ArrayList<ITick> convertTicksToTicks(Instrument instrument, long tableIndex,
			Period convertTo) {
		ArrayList<ITick> toConvert = readTicks(getTickTable(instrument, tableIndex));

		if (toConvert == null || toConvert.isEmpty())
			return null;

		ArrayList<ITick> converted = new ArrayList<ITick>(
				(int) (TIME_INTERVAL_PER_TICK_TABLE / convertTo.getInterval()) + 1);

		long prevTime = getBarStart(convertTo, toConvert.get(0).getTime());
		long time;

		for (int i = 1; i < toConvert.size(); i++) {
			time = getBarStart(convertTo, toConvert.get(i).getTime());

			if (time > prevTime) {
				converted.add(toConvert.get(i - 1));
				prevTime = time;
			}
		}
		converted.add(toConvert.get(toConvert.size() - 1));

		if (converted.isEmpty())
			return null;

		return converted;
	}

	/**
	 * Converts bars of given period in the table of given table index to ticks
	 * using the given interpolation method.
	 * 
	 * @param instrument the instrument of the ticks and the bars
	 * @param tableIndex the table index of the bars to convert
	 * @param convertFrom the period of the bars to convert and the interval of
	 *            the converted ticks
	 * @param interpolationMethod the interpolation method to be used for
	 *            conversion
	 * @return an ArrayList with the ticks that resulted from the conversion
	 * @throws ForexException if detecting incoherent storage of ask and bid
	 *             bars
	 */
	private ArrayList<ITick> convertBarsToTicks(Instrument instrument, long tableIndex,
			Period convertFrom, InterpolationMethod interpolationMethod) {
		ArrayList<IBar> askBarsToConvert = readBars(getBarTable(instrument, convertFrom,
				OfferSide.ASK, tableIndex));
		ArrayList<IBar> bidBarsToConvert = readBars(getBarTable(instrument, convertFrom,
				OfferSide.BID, tableIndex));

		if (askBarsToConvert == null || bidBarsToConvert == null)
			return null;

		ArrayList<ITick> converted = new ArrayList<ITick>();

		if (askBarsToConvert.size() != bidBarsToConvert.size())
			throw new ForexException("ask and bid bars not coherently stored");

		// @formatter:off
		switch (interpolationMethod) {
		case OPEN_TICK:
			for (int i = 0; i < askBarsToConvert.size(); i++) {
				if(askBarsToConvert.get(i).getTime() == bidBarsToConvert.get(i).getTime()) {
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime(), 
							askBarsToConvert.get(i).getOpen(), 
							bidBarsToConvert.get(i).getOpen(), 
							askBarsToConvert.get(i).getVolume(), 
							bidBarsToConvert.get(i).getVolume())); 
				} else {
					throw new ForexException("ask and bid bars not coherently stored");
				}
			}
			break;
		case CLOSE_TICK:
			for (int i = 0; i < askBarsToConvert.size(); i++) {
				if(askBarsToConvert.get(i).getTime() == bidBarsToConvert.get(i).getTime()) {
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime(), 
							askBarsToConvert.get(i).getClose(), 
							bidBarsToConvert.get(i).getClose(), 
							askBarsToConvert.get(i).getVolume(), 
							bidBarsToConvert.get(i).getVolume())); 
				} else {
					throw new ForexException("ask and bid bars not coherently stored");
				}
			}
			break;
		case FOUR_TICKS:
			for (int i = 0; i < askBarsToConvert.size(); i++) {
				if(askBarsToConvert.get(i).getTime() == bidBarsToConvert.get(i).getTime()) {
					
					long interval = convertFrom.getInterval() / 4;
					double askVolume = askBarsToConvert.get(i).getVolume() / 4; 
					double bidVolume = bidBarsToConvert.get(i).getVolume() / 4; 
					
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime(), 
							askBarsToConvert.get(i).getOpen(), 
							bidBarsToConvert.get(i).getOpen(), 
							askVolume, 
							bidVolume)); 
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime() + interval, 
							askBarsToConvert.get(i).getHigh(), 
							bidBarsToConvert.get(i).getHigh(), 
							askVolume, 
							bidVolume)); 
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime() + 2 * interval, 
							askBarsToConvert.get(i).getLow(), 
							bidBarsToConvert.get(i).getLow(), 
							askVolume, 
							bidVolume)); 
					converted.add(new Tick(
							askBarsToConvert.get(i).getTime() + 3 * interval, 
							askBarsToConvert.get(i).getClose(), 
							bidBarsToConvert.get(i).getClose(), 
							askVolume, 
							bidVolume)); 
				} else {
					throw new ForexException("ask and bid bars not coherently stored");
				}
			}
			break;
		}
		// @formatter:on

		return converted;
	}

	/**
	 * Returns an ArrayList of all ticks in specified table, null if table was
	 * not found or if something went wrong. If anything else wen't wrong than
	 * just the table not being found, an error message is printed.
	 * 
	 * @param table the table for which to return stored ticks
	 * @return an ArrayList of all ticks in table
	 */
	private ArrayList<ITick> readTicks(String table) {

		if (!tableExists(table))
			return null;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM " + table;

			stmt = getConnection().createStatement();

			rs = stmt.executeQuery(sql);

			// setting initial capacity so ArrayList won't invoke
			// ensureCapacity()
			ArrayList<ITick> ticks = new ArrayList<ITick>(MAX_ROWS_PER_BAR_TABLE + 1);
			while (rs.next()) {
				// @formatter:off
				ticks.add(new Tick(
						rs.getLong(1), 
						rs.getDouble(2), 
						rs.getDouble(3), 
						rs.getDouble(4), 
						rs.getDouble(5)));
				// @formatter:on
			}

			return ticks;
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Exception reading ticks: " + e.getMessage());
		} finally {
			close(stmt, rs);
		}

		return null;
	}

	/**
	 * Returns an ArrayList of all bars in specified table, null if table was
	 * not found or if something went wrong. If anything else wen't wrong than
	 * just the table not being found, an error message is printed.
	 * 
	 * @param table the table for which to return stored bars
	 * @return an ArrayList of all bars in table
	 */
	private ArrayList<IBar> readBars(String table) {

		if (!tableExists(table))
			return null;

		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM " + table;

			stmt = getConnection().createStatement();

			rs = stmt.executeQuery(sql);

			// setting initial capacity so ArrayList won't invoke
			// ensureCapacity()
			ArrayList<IBar> bars = new ArrayList<IBar>(MAX_ROWS_PER_BAR_TABLE + 1);
			while (rs.next()) {
				// @formatter:off
				bars.add(new Bar(
						rs.getLong(1), 
						rs.getDouble(2), 
						rs.getDouble(3), 
						rs.getDouble(4), 
						rs.getDouble(5),
						rs.getDouble(6)));
				// @formatter:on
			}

			return bars;
		} catch (SQLException e) {
			System.err.println("Exception reading bars: " + e.getMessage());
		} finally {
			close(stmt, rs);
		}

		return null;
	}

	/**
	 * Returns the name of the tick table of specified index, instrument.
	 * 
	 * @param instrument the instrument of the tick
	 * @param tableIndex the table index
	 * @return the name of the table where a tick of the specified time and
	 *         instrument would be stored
	 */
	private static String getTickTable(Instrument instrument, long tableIndex) {
		StringBuilder sb = new StringBuilder();

		sb.append(instrument.name());
		sb.append("_TICK_");
		sb.append(tableIndex);

		return sb.toString();
	}

	/**
	 * Returns the name of the bar table of specified index, time, instrument,
	 * offer side.
	 * 
	 * @param instrument the instrument of the bar
	 * @param period the period of the bar
	 * @param offerSide the offer side of the bar
	 * @param tableIndex the table index
	 * @return the name of the table of specified index, time, instrument, offer
	 *         side
	 */
	private static String getBarTable(Instrument instrument, Period period,
			OfferSide offerSide, long tableIndex) {
		StringBuilder sb = new StringBuilder();

		sb.append(instrument.name()).append("_");
		sb.append(period.name()).append("_");
		sb.append(offerSide.name());
		sb.append("_BAR_");
		sb.append(tableIndex);

		return sb.toString();
	}

	/**
	 * Returns the name of the storage range table of specified instrument.
	 * 
	 * @param instrument the instrument of the table
	 * @return the name of the storage range table of specified instrument
	 */
	private static String getStorageRangeTable(Instrument instrument) {
		return instrument.name() + "_STORAGE_RANGE_TABLE";
	}

	/**
	 * Returns the name of the tick count table of specified instrument.
	 * 
	 * @param instrument the instrument of the table
	 * @return the name of the tick count table of specified instrument
	 */
	private static String getTickCountTable(Instrument instrument,
			long tickCountTableIndex) {
		return instrument.name() + "_TICK_COUNT_TABLE_" + tickCountTableIndex;
	}

	/**
	 * Returns the name of the tick count table organizer table of specified
	 * instrument.
	 * 
	 * @param instrument the instrument of the table
	 * @return the name of the tick count table organizer of specified
	 *         instrument
	 */
	private static String getTickCountTableOrganizer(Instrument instrument) {
		return instrument.name() + "_TICK_COUNT_TABLE_ORGANIZER";
	}
}