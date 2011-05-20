package net.microscraper.client.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Log;
import net.microscraper.client.Utils;

public class JDBCSQLite implements SQLInterface {
	private final Connection connection;
	private final Log log;
	
	public JDBCSQLite(String pathToDB, Log log) throws SQLInterfaceException {
		this.log = log;
		try {
			Class.forName("org.sqlite.JDBC"); // Make sure we have this class.
			connection = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
		} catch(SQLException e) {
			throw new SQLInterfaceException(e);
		} catch(ClassNotFoundException e) {
			throw new SQLInterfaceException(e);
		}
	}
	/*
	@Override
	public SQLInterface.Cursor query(String sql) throws SQLInterfaceException {
		//log.i("SQL Query: " + sql);
		return new JDBCSqliteStatement(connection, sql).executeQuery();
	}
	
	@Override
	public boolean execute(String sql) throws SQLInterfaceException {
		//log.i("SQL Execution: " + sql);
		return new JDBCSqliteStatement(connection, sql).execute();
	}
	
	@Override
	public Cursor query(String sql, String[] substitutions) throws SQLInterfaceException {
		//log.i("SQL Query: " + sql + " substituting " + Utils.join(substitutions, ", "));
		JDBCSqliteStatement statement = new JDBCSqliteStatement(connection, sql);
		statement.bindArrayOfStrings(substitutions);
		return statement.executeQuery();
	}

	@Override
	public boolean execute(String sql, String[] substitutions) throws SQLInterfaceException {
		//log.i("SQL Execution: " + sql + " substituting " + Utils.join(substitutions, ", "));
		JDBCSqliteStatement statement = new JDBCSqliteStatement(connection, sql);
		statement.bindArrayOfStrings(substitutions);
		return statement.execute();
	}
	
	@Override
	public Statement getStatement(String sql, String[] substitutions) throws SQLInterfaceException {
		//log.i("SQL Statement built: " + sql + " substituting " + Utils.join(substitutions, ", "));
		JDBCSqliteStatement statement = new JDBCSqliteStatement(connection, sql);
		statement.bindArrayOfStrings(substitutions);
		
		return statement;
	}
	
	@Override
	public boolean executeBatch(Statement[] statements) throws SQLInterfaceException {
		try {
			connection.setAutoCommit(false);
			java.sql.Statement batchStatement = connection.createStatement();
			for(int i = 0 ; i < statements.length ; i ++) {
				//batchStatement.ad
			}
			batchStatement.a
			connection.setAutoCommit(true);
			return true;
		} catch(SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}
	*/
	private class JDBCSqliteStatement implements PreparedStatement {
		private final java.sql.PreparedStatement statement;
		public JDBCSqliteStatement(Connection connection, String sql) throws SQLInterfaceException {
			try {
				statement = connection.prepareStatement(sql);
			} catch(SQLException e) {
				e.printStackTrace();
				throw new SQLInterfaceException(e);
			}
		}
		
		public String toString() {
			return statement.toString();
		}
		
		@Override
		public SQLInterface.Cursor executeQuery() throws SQLInterfaceException {
			try {
				return new JDBCSQLiteCursor(statement.executeQuery());
			} catch(SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}
		@Override
		public boolean execute() throws SQLInterfaceException {
			try {
				return statement.execute();
			} catch(SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

		@Override
		public void bindStrings(String[] strings) throws SQLInterfaceException {
			try {
				for(int i = 0 ; i < strings.length ; i ++) {
					statement.setString(i + 1, strings[i]);
				}
			} catch(SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

		@Override
		public void addBatch() throws SQLInterfaceException {
			try {
				statement.addBatch();
				
			} catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

		@Override
		public int[] executeBatch() throws SQLInterfaceException {
			try {
				int[] rowCounts = statement.executeBatch();
				log.i("SQL Batch Count: " + Utils.join(rowCounts, ", "));
				return rowCounts;
			}  catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}
		
		@Override
		public Cursor query() throws SQLInterfaceException {
			try {
				return new JDBCSQLiteCursor(statement.executeQuery());
			}  catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}	
	}
	
	private class JDBCSQLiteCursor implements SQLInterface.Cursor {
		private final ResultSet resultSet;
		
		public JDBCSQLiteCursor(ResultSet rs) {
			resultSet = rs;
		}
		
		@Override
		public boolean next() throws SQLInterfaceException {
			try {
				return resultSet.next();
			} catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

		@Override
		public String getString(String columnName) throws SQLInterfaceException {
			try {
				return resultSet.getString(columnName);
			} catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

		@Override
		public int getInt(String columnName) throws SQLInterfaceException {
			try {
				return resultSet.getInt(columnName);
			} catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}
		
		@Override
		public void close() throws SQLInterfaceException {
			try {
				resultSet.close();
			} catch (SQLException e) {
				throw new SQLInterfaceException(e);
			}
		}

	}

	@Override
	public String idColumnType() {
		return "INTEGER";
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
	public void disableAutoCommit() throws SQLInterfaceException {
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}

	@Override
	public void enableAutoCommit() throws SQLInterfaceException {
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}

	@Override
	public void commit() throws SQLInterfaceException {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}

	@Override
	public PreparedStatement prepareStatement(String sql)
			throws SQLInterfaceException {
		return new JDBCSqliteStatement(connection, sql);
	}
}
