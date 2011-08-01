package net.microscraper.impl.publisher;

import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.SingleTableDatabase;
import net.microscraper.interfaces.database.Table;
import net.microscraper.interfaces.sql.SQLConnection;
import net.microscraper.interfaces.sql.SQLConnectionException;
import net.microscraper.interfaces.sql.SQLTable;

public class SQLSingleTableDatabase extends SingleTableDatabase {
	
	/**
	 * The {@link SQLConnection} used by this {@link SQLSingleTableDatabase}.
	 */
	private final SQLConnection connection;

	public SQLSingleTableDatabase(SQLConnection connection, int batchSize) throws SQLConnectionException {
		this.connection = connection;
	}
	
	@Override
	public void close() throws DatabaseException {
		try {
			connection.disableAutoCommit();
			connection.commit();
		} catch(SQLConnectionException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void open() throws DatabaseException { }

	@Override
	public Table newTable(String tableName, String[] columnNames) throws DatabaseException {
		try {
			return new SQLTable(connection, tableName, columnNames );
		} catch(SQLConnectionException e) {
			throw new DatabaseException(e);
		}
	}
	
}
