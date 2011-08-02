package net.microscraper.impl.publisher;

import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.MultiTableDatabase;
import net.microscraper.interfaces.database.SingleTableDatabase;
import net.microscraper.interfaces.database.AllResultsTable;
import net.microscraper.interfaces.sql.SQLConnection;
import net.microscraper.interfaces.sql.SQLConnectionException;
import net.microscraper.interfaces.sql.SQLTable;

/**
 * A SQL implementation of {@link SingleTableDatabase}, using {@link SQLConnection}.
 * @author talos
 * @see Database
 * @see SingleTableDatabase
 * @see SQLConnection
 *
 */
public final class SQLMultiTableDatabase extends MultiTableDatabase {
	
	/**
	 * The {@link SQLConnection} used by this {@link SQLMultiTableDatabase}.
	 */
	private final SQLConnection connection;
	
	public SQLMultiTableDatabase(SQLConnection connection) {
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
	public AllResultsTable newTable(String tableName, String[] columnNames) throws DatabaseException {
		try {
			return new SQLTable(connection, tableName, columnNames );
		} catch(SQLConnectionException e) {
			throw new DatabaseException(e);
		}
	}
}
