package net.microscraper.database.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

class JDBCSQLiteResultSet implements SQLResultSet {
	private final ResultSet resultSet;
	
	public JDBCSQLiteResultSet(ResultSet rs) {
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

	@Override
	public boolean hasColumnName(String columnName) throws SQLConnectionException {
		try {
			ResultSetMetaData meta = resultSet.getMetaData();
			
			int numCol = meta.getColumnCount();
			
			for (int i = 1; i < numCol+1; i++) {
			    if(meta.getColumnName(i).equals(columnName)) {
			    	return true;
			    }
			}
			return false;
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

}