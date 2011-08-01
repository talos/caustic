package net.microscraper.impl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.microscraper.Utils;
import net.microscraper.interfaces.log.Logger;
import net.microscraper.interfaces.sql.SQLConnection;
import net.microscraper.interfaces.sql.SQLConnectionException;
import net.microscraper.interfaces.sql.SQLPreparedStatement;
import net.microscraper.interfaces.sql.SQLResultSet;

/**
 * An implementation of {@link SQLConnection} for org.sqlite.JDBC
 * @see SQLConnection
 * @author realest
 *
 */
public class JDBCSQLite implements SQLConnection {
	private final Connection connection;
	private final Logger logger;
	private final SQLPreparedStatement checkTableExistence;
	
	public JDBCSQLite(String pathToDB, Logger logger) throws SQLConnectionException {
		this.logger = logger;
		try {
			Class.forName("org.sqlite.JDBC"); // Make sure we have this class.
			connection = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
			checkTableExistence =
					prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?;");
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		} catch(ClassNotFoundException e) {
			throw new SQLConnectionException(e);
		}
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
			try {
				return new JDBCSQLiteCursor(statement.executeQuery());
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
		@Override
		public void execute() throws SQLConnectionException {
			try {
				statement.execute();
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
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
	public void disableAutoCommit() throws SQLConnectionException {
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public void enableAutoCommit() throws SQLConnectionException {
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public void commit() throws SQLConnectionException {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public SQLPreparedStatement prepareStatement(String sql)
			throws SQLConnectionException {
		return new JDBCSqliteStatement(connection, sql);
	}

	@Override
	public boolean tableExists(String tableName) throws SQLConnectionException {
		checkTableExistence.bindStrings(new String[] { tableName });
		SQLResultSet results = checkTableExistence.executeQuery();
		return results.next();
	}
}
