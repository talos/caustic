package net.microscraper.impl.commandline;

import java.io.IOException;

import net.microscraper.database.Database;
import net.microscraper.database.DelimitedConnection;
import net.microscraper.database.UpdateableConnection;
import net.microscraper.database.JDBCSqliteConnection;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.database.SingleTableDatabase;
import net.microscraper.util.StringUtils;

import static net.microscraper.impl.commandline.Arguments.*;

public class ArgumentsDatabase {
	
	public static Database get(Arguments args)
			throws SQLConnectionException, IOException {
		final Database result;
		
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
				
				UpdateableConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);

				if(args.has(SINGLE_TABLE)) {
					result = new SingleTableDatabase(connection);
				} else {
					result = new MultiTableDatabase(connection);
				}
				
			} else {
				result = new SingleTableDatabase(DelimitedConnection.toFile(outputLocation, delimiter));
			}
			
		} else { // output to STDOUT
			result = new SingleTableDatabase(DelimitedConnection.toStdOut(delimiter));
		}
		return result;
	}
}
