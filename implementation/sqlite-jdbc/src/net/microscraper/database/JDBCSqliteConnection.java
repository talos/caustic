package net.microscraper.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.microscraper.database.Updateable;
import net.microscraper.database.Insertable;

/**
 * An implementation of {@link SQLConnection} for org.sqlite.JDBC
 * @see SQLConnection
 * @author realest
 *
 */
public class JDBCSqliteConnection implements SQLConnection {
	private Connection connection;
	private final int batchSize;
	private final String connectionPath;
		
	/**
	 * Statements yet to be executed.
	 */
	private final List<PreparedStatement> batch = new ArrayList<PreparedStatement>();
	
	private JDBCSqliteConnection(String connectionPath, int batchSize) {
		this.batchSize = batchSize;
		this.connectionPath = connectionPath;
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} using a path to a database.
	 * @param pathToDB {@link String} path to database.
	 * @param batchSize How many statements to make before committing.
	 */
	public static JDBCSqliteConnection toFile(String pathToDB, int batchSize) {
		return new JDBCSqliteConnection("jdbc:sqlite:" + pathToDB, batchSize);
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} in-memory.
	 * @param batchSize How many statements to make before committing.
	 */
	public static JDBCSqliteConnection inMemory(int batchSize) {
		return new JDBCSqliteConnection("jdbc:sqlite::memory:", batchSize);
	}
	
	private class JDBCSqliteStatement implements SQLPreparedStatement {
		private final java.sql.PreparedStatement statement;
		public JDBCSqliteStatement(Connection connection, String sql) throws SQLConnectionException {
			try {
				statement = connection.prepareStatement(sql);
			} catch(SQLException e) {
				//e.printStackTrace();
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

	/**
	 * Open {@link JDBCSqliteConnection} using org.sqlite.JDBC.
	 */
	@Override
	public void open() throws IOException {
		try {
			Class.forName("org.sqlite.JDBC"); // Make sure we have this class.
			connection = DriverManager.getConnection(connectionPath);
			connection.setAutoCommit(false);
		} catch(SQLException e) {
			throw new IOException(e);
		} catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public Updateable newUpdateable(String name, String[] textColumns)
			throws IOException {
		try {
			Updateable table = new SQLTable(this, name, textColumns);
			runBatch();
			return table;
		} catch(SQLConnectionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			runBatch();
			connection.commit();
		} catch(SQLConnectionException e) {
			throw new IOException(e);
		} catch(SQLException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public Insertable newInsertable(String name, String[] textColumns)
			throws IOException {
		return newUpdateable(name, textColumns);
	}
}
