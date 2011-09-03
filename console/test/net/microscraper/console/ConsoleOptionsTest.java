package net.microscraper.console;

import static net.microscraper.console.ConsoleOptions.*;
import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.Map;

import net.microscraper.console.ConsoleOptions;
import net.microscraper.console.InvalidOptionException;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.JavaNetEncoder;

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
		Hashtable<String, String> origHash = new Hashtable<String, String>();
		for(int i = 0 ; i < 10 ; i ++) {
			origHash.put(randomString(), randomString());
		}
		
		String defaults = HashtableUtils.toFormEncoded(new JavaNetEncoder(Encoder.UTF_8), origHash);
		
		ConsoleOptions withQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + '=' + '"' + defaults + '"' });
		ConsoleOptions withoutQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + "=" + defaults });
		
		Map<String, String> withQuotesInput = withQuotes.getInput().next();
		Map<String, String> withoutQuotesInput = withoutQuotes.getInput().next();
				
		assertTrue(withQuotesInput.keySet().containsAll(withoutQuotesInput.keySet()));
		assertTrue(withQuotesInput.values().containsAll(withoutQuotesInput.values()));
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
}
