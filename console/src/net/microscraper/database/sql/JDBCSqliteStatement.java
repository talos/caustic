package net.microscraper.database.sql;
/*
import java.sql.Connection;
import java.sql.SQLException;

class JDBCSqliteStatement implements SQLPreparedStatement {

	private final JDBCSqliteConnection jdbcSqliteConnection;
	private final java.sql.PreparedStatement statement;
	private final String rawSQL;
	
	public JDBCSqliteStatement(JDBCSqliteConnection jdbcSqliteConnection, Connection connection, String sql) throws SQLConnectionException {
		this.rawSQL = sql;
		this.jdbcSqliteConnection = jdbcSqliteConnection;
		try {
			statement = connection.prepareStatement(sql);
		} catch(SQLException e) {
			//e.printStackTrace();
			throw new SQLConnectionException(e);
		}
	}
	
	public String toString() {
		return rawSQL;
	}
	
	@Override
	public SQLResultSet executeQuery() throws SQLConnectionException {
		this.jdbcSqliteConnection.commit(); // queries should be executed against updated data.
		try {
			return new JDBCSQLiteCursor(statement.executeQuery());
			
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	@Override
	public void execute() throws SQLConnectionException {
		this.jdbcSqliteConnection.statementQueue.add(statement);
		if(this.jdbcSqliteConnection.statementQueue.size() >= this.jdbcSqliteConnection.maxStatementsBeforeCommit) {
			//statement.execute();
			this.jdbcSqliteConnection.commit();
		}
	}
	
	@Override
	public void bindStrings(String[] strings) throws SQLConnectionException {
		try {
			for(int i = 0 ; i < strings.length ; i ++) {
				statement.setString(i + 1, strings[i]);
			}
			statement.addBatch();
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public void close() throws SQLConnectionException {
		try {
			statement.close();
		} catch (SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
}*/