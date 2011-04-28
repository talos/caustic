package net.microscraper.client.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;

public class JDBCSQLite implements SQLInterface {
	private final Connection connection;
	private final Interfaces.Logger log;
	
	public JDBCSQLite(String pathToDB, Interfaces.Logger log) throws SQLInterfaceException {
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
	
	@Override
	public SQLInterface.Cursor query(String sql) throws SQLInterfaceException {
		log.i("Querying: " + sql);
		return new JDBCSqliteStatement(connection, sql).executeQuery();
	}
	
	@Override
	public boolean execute(String sql) throws SQLInterfaceException {
		log.i("Executing: " + sql);
		return new JDBCSqliteStatement(connection, sql).execute();
	}
	
	@Override
	public Cursor query(String sql, String[] substitutions) throws SQLInterfaceException {
		JDBCSqliteStatement statement = new JDBCSqliteStatement(connection, sql);
		statement.bindArrayOfStrings(substitutions);
		log.i("Querying: " + statement.toString());
		return statement.executeQuery();
	}

	@Override
	public boolean execute(String sql, String[] substitutions) throws SQLInterfaceException {
		JDBCSqliteStatement statement = new JDBCSqliteStatement(connection, sql);
		statement.bindArrayOfStrings(substitutions);
		log.i("Executing: " + statement.toString());
		return statement.execute();
	}
	
	private static class JDBCSqliteStatement implements Statement {
		private final PreparedStatement statement;
		public JDBCSqliteStatement(Connection connection, String sql) throws SQLInterfaceException {
			try {
				statement = connection.prepareStatement(sql);
			} catch(SQLException e) {
				e.printStackTrace();
				throw new SQLInterfaceException(e);
			}
		}
		
		@Override
		public void bindString(int index, String value) throws SQLInterfaceException {
			try {
				statement.setString(index, value);
			} catch(SQLException e) {
				e.printStackTrace();
				throw new SQLInterfaceException(e);
			}
		}
		
		public void bindArrayOfStrings(String[] strings) throws SQLInterfaceException {
			for(int i = 0 ; i < strings.length ; i ++) {
				Client.log.i(strings[i]);
				bindString(i + 1, strings[i]);
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
	}
	
	private static class JDBCSQLiteCursor implements SQLInterface.Cursor {
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
	public String dataColumnType() {
		return "VARCHAR";
	}
	
	@Override
	public String intColumnType() {
		return "INTEGER";
	}

	@Override
	public String nullValue() {
		return "NULL";
	}
}
