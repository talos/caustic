package net.microscraper.client.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;


public class JDBCSQLite implements Interfaces.SQL {
	private final Connection connection;
	
	// ../../db/scraper.db"
	public JDBCSQLite(String pathToDB) throws SQLInterfaceException {
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
	public Interfaces.SQL.Cursor query(String sql) throws SQLInterfaceException {
		//return new JDBCSqliteStatement(connection, sql);
		try {
			Client.context().log.i("Querying: " + sql);
			Statement statement = connection.createStatement();
			return new JDBCSQLiteCursor(statement.executeQuery(sql));
		} catch (SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}
	
	@Override
	public boolean execute(String sql) throws SQLInterfaceException {
		try {
			Client.context().log.i("Executing: " + sql);
			Statement statement = connection.createStatement();
			return statement.execute(sql);
		} catch(SQLException e) {
			throw new SQLInterfaceException(e);
		}
	}
	/*
	private static class JDBCSqliteStatement implements Statement {
		private final PreparedStatement statement;
		public JDBCSqliteStatement(Connection connection, String sql) throws Exception {
			statement = connection.prepareStatement(sql);
		}
		
		@Override
		public void bindString(int index, String value) throws Exception {
			statement.setString(index, value);
		}

		@Override
		public CursorInterface execute() throws Exception {
			return new JDBCSqliteCursor(statement.executeQuery());
		}	
	}
	*/
	private static class JDBCSQLiteCursor implements Interfaces.SQL.Cursor {
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
	public String quoteField(String field) throws SQLInterfaceException {
		if(field == null)
			throw new SQLInterfaceException("Supplied field cannot be null.");
		return "`" + field + "`";
	}
	
	@Override
	public String nullValue() {
		return "NULL";
	}
	
	@Override
	public String quoteValue(String value) {
		if(value == null)
			return nullValue();
		else
			return "'" + value.replace("'", "\'") + "'";
	}
}
