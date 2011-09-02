package net.microscraper.console;

import static net.microscraper.console.ConsoleOptions.*;
import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.Hashtable;

import net.microscraper.console.ConsoleOptions;
import net.microscraper.console.InvalidOptionException;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public class ConsoleOptionsTest {

	private ConsoleOptions options;
	
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
		new ConsoleOptions(new String[] { randomString(),
				BATCH_SIZE + "=" + notAnInt,
				SAVE_TO_FILE.toString(),
				FORMAT.toString() + "=" + SQLITE_FORMAT});
		options.getDatabase();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testBatchSizeOnlyWithSQLOutput() throws Exception {
		new ConsoleOptions(new String[] { randomString(),
				BATCH_SIZE + "=" + randomInt(),
				FORMAT.toString() + "=" + CSV_FORMAT});
		
		options.getDatabase();
	}
	
	@Test
	public void testDefaultsAreTheSameWithQuotesOrWithout() throws Exception {
		Hashtable<String, String> origHash = new Hashtable<String, String>();
		for(int i = 0 ; i < 10 ; i ++) {
			origHash.put(randomString(), randomString());
		}
		
		String defaults = HashtableUtils.toFormEncoded(new JavaNetEncoder(Encoder.UTF_8), origHash);
		
		ConsoleOptions withQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + "=\"" + defaults + '"' });
		ConsoleOptions withoutQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + "=" + defaults });
		
		Hashtable<String, String> withQuotesInput = withQuotes.getInput().next();
		Hashtable<String, String> withoutQuotesInput = withoutQuotes.getInput().next();
		
		assertTrue(withQuotesInput.keySet().containsAll(withoutQuotesInput.keySet()));
		assertTrue(withQuotesInput.values().containsAll(withoutQuotesInput.values()));
	}
	
	@Test(expected=InvalidOptionException .class)
	public void testMustHaveInputToDefineInputDelimiter() throws Exception {
		new ConsoleOptions(new String[] { randomString(),
				INPUT_DELIMITER + "=" + randomString(1) });
		options.getInput();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInputColumnDelimiterMustBeOneCharacter() throws Exception {
		new ConsoleOptions(new String[] { randomString(),
				INPUT_DELIMITER + "=" + randomString(10) });
		options.getInput();
	}
	
	/*
	@Test
	public void testHasArgumentWithValue() throws Exception {
		int batchSize = randomInt();
		
		Arguments args = new Arguments(new String[] { randomString(), INPUT_FILE + "=" + batchSize });
		
		assertTrue(args.has(INPUT_FILE));
	}
	
	@Test
	public void testHasArgumentWithoutValue() throws Exception {
		Arguments args = new Arguments(new String[] { randomString(), SAVE_TO_FILE.toString() });
		
		assertTrue(args.has(SAVE_TO_FILE));
	}

	@Test
	public void testGetArgumentWithValue() {
		String responseSize = Integer.toString(randomInt());
		
		Arguments args = new Arguments(new String[] { randomString(), MAX_RESPONSE_SIZE + "=" + responseSize });
		
		assertEquals(responseSize, args.get(MAX_RESPONSE_SIZE));
	}

	@Test
	public void testInstructionIsDefaultFirstArgument() {
		String uri = randomString();
		
		Arguments args = new Arguments(new String[] { uri } );
		
		assertTrue(args.has(INSTRUCTION));
		assertEquals(uri, args.get(INSTRUCTION));
	}
	
	@Test
	public void testArgumentHasDefaultValue() {
		Arguments args = new Arguments(new String[] { randomString() } );
		
		assertEquals(BATCH_SIZE.getDefault(), args.get(BATCH_SIZE));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testArgumentWithDefaultValueCantBeCheckedForExistence() {
		Arguments args = new Arguments(new String[] { randomString() } );
		
		args.has(BATCH_SIZE);
	}
	*/
}
