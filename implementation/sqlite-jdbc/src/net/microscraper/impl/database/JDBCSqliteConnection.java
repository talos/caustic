package net.microscraper.impl.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.IOTable;
import net.microscraper.interfaces.database.WritableTable;
import net.microscraper.interfaces.log.Logger;

/**
 * An implementation of {@link SQLConnection} for org.sqlite.JDBC
 * @see SQLConnection
 * @author realest
 *
 */
public class JDBCSqliteConnection implements SQLConnection {
	private final Connection connection;
	private final Logger log;
	//private final SQLPreparedStatement checkTableExistence;
	private final int batchSize;
	
	/**
	 * The name of a single table for {@link #getWritableTable(String[])}.
	 */
	private static final String SINGLE_TABLE_NAME = "results";
	
	/**
	 * Statements yet to be executed.
	 */
	private final List<PreparedStatement> batch = new ArrayList<PreparedStatement>();
	
	private JDBCSqliteConnection(String connectionPath, Logger log, int batchSize)
			throws SQLConnectionException {
		this.log = log;
		this.batchSize = batchSize;
		try {
			Class.forName("org.sqlite.JDBC"); // Make sure we have this class.
			connection = DriverManager.getConnection(connectionPath);
			connection.setAutoCommit(false);
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		} catch(ClassNotFoundException e) {
			throw new SQLConnectionException(e);
		}
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} using a path to a database.
	 * @param pathToDB {@link String} path to database.
	 * @param logger {@link Logger} to use for logs.
	 * @param batchSize How many statements to make before committing.
	 * @throws SQLConnectionException if the {@link JDBCSqliteConnection} could not be created.
	 */
	public static JDBCSqliteConnection toFile(String pathToDB, Logger logger, int batchSize) throws SQLConnectionException {
		return new JDBCSqliteConnection("jdbc:sqlite:" + pathToDB, logger, batchSize);
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} in-memory.
	 * @param logger {@link Logger} to use for logs.
	 * @param batchSize How many statements to make before committing.
	 * @throws SQLConnectionException if the {@link JDBCSqliteConnection} could not be created.
	 */
	public static JDBCSqliteConnection inMemory(Logger logger, int batchSize) throws SQLConnectionException {
		return new JDBCSqliteConnection("jdbc:sqlite::memory:", logger, batchSize);
	}
	
	private class JDBCSqliteStatement implements SQLPreparedStatement {
		private final java.sql.PreparedStatement statement;
		public JDBCSqliteStatement(Connection connection, String sql) throws SQLConnectionException {
			try {
				statement = connection.prepareStatement(sql);
			} catch(SQLException e) {
				e.printStackTrace();
				throw new SQLConnectionException(e);
			}
		}
		
		public String toString() {
			return statement.toString();
		}
		
		@Override
		public SQLResultSet executeQuery() throws SQLConnectionException {
			runBatch(); // queries should be executed against updated data.
			try {
				return new JDBCSQLiteCursor(statement.executeQuery());
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
		@Override
		public void execute() throws SQLConnectionException {
			batch.add(statement);
			if(batch.size() >= batchSize) {
				//statement.execute();
				runBatch();
			}
		}

		@Override
		public void bindStrings(String[] strings) throws SQLConnectionException {
			try {
				for(int i = 0 ; i < strings.length ; i ++) {
					statement.setString(i + 1, strings[i]);
				}
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
		/*
		@Override
		public void addBatch() throws SQLConnectionException {
			try {
				statement.addBatch();
				
			} catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}

		@Override
		public int[] executeBatch() throws SQLConnectionException {
			try {
				int[] rowCounts = statement.executeBatch();
				logger.i("SQL Batch Count: " + Utils.join(rowCounts, ", "));
				return rowCounts;
			}  catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
		*/
	}
	
	private class JDBCSQLiteCursor implements SQLResultSet {
		private final ResultSet resultSet;
		
		public JDBCSQLiteCursor(ResultSet rs) {
			resultSet = rs;
		}
		
		@Override
		public boolean next() throws SQLConnectionException {
			try {
				return resultSet.next();
			} catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}

		@Override
		public String getString(String columnName) throws SQLConnectionException {
			try {
				return resultSet.getString(columnName);
			} catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}

		@Override
		public int getInt(String columnName) throws SQLConnectionException {
			try {
				return resultSet.getInt(columnName);
			} catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
		
		@Override
		public void close() throws SQLConnectionException {
			try {
				resultSet.close();
			} catch (SQLException e) {
				throw new SQLConnectionException(e);
			}
		}

	}
	
	public void runBatch() throws SQLConnectionException {
		try {
			while(batch.size() > 0) {
				batch.remove(0).execute();
			}
			connection.commit();
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public String keyColumnDefinition() {
		return "PRIMARY KEY";
	}
	
	@Override
	public String varcharColumnType() {
		return "VARCHAR";
	}

	@Override
	public String textColumnType() {
		return "TEXT";
	}
	
	@Override
	public String intColumnType() {
		return "INTEGER";
	}

	@Override
	public String nullValue() {
		return "NULL";
	}

	@Override
	public int defaultVarcharLength() {
		return (int) Math.pow(10, 9);
	}
	
	@Override
	public SQLPreparedStatement prepareStatement(String sql)
			throws SQLConnectionException {
		return new JDBCSqliteStatement(connection, sql);
	}

	@Override
	public boolean tableExists(String tableName) throws SQLConnectionException {
		SQLPreparedStatement checkTableExistence =
				prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?;");

		checkTableExistence.bindStrings(new String[] { tableName });
		SQLResultSet results = checkTableExistence.executeQuery();
		return results.next();
	}

	@Override
	public void open() throws DatabaseException { }
	
	@Override
	public IOTable getIOTable(String name, String[] textColumns)
			throws DatabaseException {
		try {
			IOTable table = new SQLTable(this, name, textColumns);
			runBatch();
			return table;
		} catch(SQLConnectionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void close() throws DatabaseException {
		try {
			runBatch();
			connection.commit();
		} catch(SQLConnectionException e) {
			throw new DatabaseException(e);
		} catch(SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public WritableTable getWritableTable(String[] textColumns)
			throws DatabaseException {
		return getIOTable(SINGLE_TABLE_NAME, textColumns);
	}
}
