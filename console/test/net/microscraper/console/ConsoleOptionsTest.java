package net.microscraper.console;

import static net.microscraper.console.ConsoleOptions.BATCH_SIZE;
import static net.microscraper.console.ConsoleOptions.CSV_FORMAT;
import static net.microscraper.console.ConsoleOptions.FORMAT;
import static net.microscraper.console.ConsoleOptions.INPUT;
import static net.microscraper.console.ConsoleOptions.INPUT_DELIMITER;
import static net.microscraper.console.ConsoleOptions.SAVE_TO_FILE;
import static net.microscraper.console.ConsoleOptions.SQLITE_FORMAT;
import static net.microscraper.console.ConsoleOptions.THREADS;
import static net.microscraper.util.TestUtils.randomInt;
import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.NonPersistedDatabase;
import net.microscraper.database.csv.CSVConnection;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.uuid.IntUUIDFactory;

import org.junit.Test;

public class ConsoleOptionsTest {
	
	@Test(expected=InvalidOptionException.class)
	public void testArgumentsNeedsOneArgument() throws Exception {
		new ConsoleOptions(new String[] {});
		
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testArgumentsFailsOnUnknownArgument()  throws Exception {
		new ConsoleOptions(new String[] {"not", "options=bleh"});
	}
	

	@Test(expected=InvalidOptionException.class)
	public void testBatchSizeMustBeInt() throws Exception {
		String notAnInt = randomString();
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				BATCH_SIZE + "=" + notAnInt,
				SAVE_TO_FILE.toString(),
				FORMAT.toString() + "=" + SQLITE_FORMAT});
		options.getDatabase();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testBatchSizeOnlyWithSQLOutput() throws Exception {
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				BATCH_SIZE + "=" + randomInt(),
				FORMAT.toString() + "=" + CSV_FORMAT});
		
		options.getDatabase();
	}
	
	@Test
	public void testInputIsTheSameWithQuotesOrWithout() throws Exception {
		Database db = new NonPersistedDatabase(CSVConnection.toSystemOut(','), new IntUUIDFactory());
		db.open();
		
		Hashtable<String, String> origHash = new Hashtable<String, String>();
		for(int i = 0 ; i < 10 ; i ++) {
			origHash.put(randomString(), randomString());
		}
		
		String defaults = HashtableUtils.toFormEncoded(new JavaNetEncoder(Encoder.UTF_8), origHash);
		
		ConsoleOptions withQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + '=' + '"' + defaults + '"' });
		ConsoleOptions withoutQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + "=" + defaults });
		
		DatabaseView viewWithQuotes = withQuotes.getInput().next(db);
		DatabaseView viewWithoutQuotes = withoutQuotes.getInput().next(db);
		
		for(String key : origHash.keySet()) {
			assertEquals(origHash.get(key), viewWithQuotes.get(key));
			assertEquals(origHash.get(key), viewWithoutQuotes.get(key));
		}
	}
	
	@Test(expected=InvalidOptionException .class)
	public void testMustHaveInputToDefineInputDelimiter() throws Exception {
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				INPUT_DELIMITER + "=" + randomString(1) });
		options.getInput();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInputColumnDelimiterMustBeOneCharacter() throws Exception {
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				INPUT_DELIMITER + "=" + randomString(10) });
		options.getInput();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testThreadsCannotBeZero() throws Exception {
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				THREADS + "=0" });
		options.getScraperRunner();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testThreadsCannotBeNegative() throws Exception {
		ConsoleOptions options = new ConsoleOptions(new String[] { randomString(),
				THREADS + "=" + (-1 - randomInt()) });
		options.getScraperRunner();
	}
	
	
}
