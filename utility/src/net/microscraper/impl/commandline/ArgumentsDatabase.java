package net.microscraper.impl.commandline;

import java.io.IOException;

import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DelimitedConnection;
import net.microscraper.database.IOConnection;
import net.microscraper.database.JDBCSqliteConnection;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.database.SingleTableDatabase;
import net.microscraper.util.StringUtils;

import static net.microscraper.impl.commandline.Arguments.*;

public class ArgumentsDatabase implements Database {
	private final Database database;
	
	public ArgumentsDatabase(Arguments args)
			throws SQLConnectionException, DatabaseException, IOException {
		
		// Determine format.
		String format;
		format = args.get(OUTPUT_FORMAT_OPTION);
		if(!validOutputFormats.contains(format)) {
			throw new IllegalArgumentException(StringUtils.quote(format)
					+ " is not a valid output format.");
		}
			
		// Determine delimiter.
		char delimiter;
		if(format.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			delimiter = CSV_OUTPUT_COLUMN_DELIMITER;
		} else { // (format.equals(TAB_OUTPUT_COLUMN_DELIMITER)) {
			delimiter = TAB_OUTPUT_COLUMN_DELIMITER;
		}
		
		// Set up output and databases.
		if(args.has(OUTPUT_TO_FILE)) {
			String outputLocation = args.get(OUTPUT_TO_FILE);
			if(outputLocation == null) {
				outputLocation = TIMESTAMP + "." + format;
			}
			
			if(format.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				
				int batchSize = Integer.parseInt(args.get(BATCH_SIZE));
				
				IOConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);

				if(args.has(SINGLE_TABLE)) {
					database = new SingleTableDatabase(connection);
				} else {
					database = new MultiTableDatabase(connection);
				}
				
			} else {
				database = new SingleTableDatabase(DelimitedConnection.toFile(outputLocation, delimiter));
			}
			
		} else { // output to STDOUT
			database = new SingleTableDatabase(DelimitedConnection.toStdOut(delimiter));
		}
	}
	
	@Override
	public int store(String name, String value, int resultNum)
			throws DatabaseException {
		return database.store(name, value, resultNum);
	}

	@Override
	public int store(String sourceName, int sourceId, String name,
			String value, int resultNum) throws DatabaseException {
		return database.store(sourceName, sourceId, name, value, resultNum);
	}

	@Override
	public void close() throws DatabaseException {
		database.close();
	}
}
