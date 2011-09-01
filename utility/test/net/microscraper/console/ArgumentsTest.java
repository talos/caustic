package net.microscraper.console;

import static net.microscraper.console.Arguments.*;
import static net.microscraper.util.TestUtils.*;
import net.microscraper.console.Arguments;
import net.microscraper.console.InvalidOptionException;

import org.junit.Test;

public class ArgumentsTest {

	@Test(expected=InvalidOptionException.class)
	public void testArgumentsNeedsOneArgument() throws Exception {
		new Arguments(new String[] {});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testArgumentsFailsOnUnknownArgument()  throws Exception {
		new Arguments(new String[] {"not", "options=bleh"});
	}
	

	@Test(expected=InvalidOptionException.class)
	public void testBatchSizeMustBeInt() throws Exception {
		String notAnInt = randomString();
		Arguments arguments =new Arguments(new String[] { randomString(),
				BATCH_SIZE + "=" + notAnInt,
				SAVE_TO_FILE.toString(),
				OUTPUT_FORMAT_OPTION.toString() + "=" + SQLITE_OUTPUT_FORMAT_VALUE});
		arguments.getDatabase();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testBatchSizeOnlyWithSQLOutput() throws Exception {
		Arguments arguments = new Arguments(new String[] { randomString(),
				BATCH_SIZE + "=" + randomInt(),
				OUTPUT_FORMAT_OPTION.toString() + "=" + CSV_OUTPUT_FORMAT_VALUE});
		
		arguments.getDatabase();
	}
	/*
	@Test
	public void testDefaultsAreTheSameWithQuotesOrWithout() {
		String defaults = randomString();
		
		Arguments withQuotes = new Arguments(new String[] { randomString(),
				INPUT + "=" + '"' + defaults + '"' });
		Arguments withoutQuotes = new Arguments(new String[] { randomString(),
				INPUT + "=" + defaults });
		
		assertEquals(withoutQuotes.get(INPUT), withQuotes.get(INPUT));
	}
	*/
	@Test(expected=InvalidOptionException .class)
	public void testMustHaveInputToDefineInputDelimiter() throws Exception {
		Arguments arguments = new Arguments(new String[] { randomString(),
				INPUT_COLUMN_DELIMITER + "=" + randomString(1) });
		arguments.getInput();
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInputColumnDelimiterMustBeOneCharacter() throws Exception {
		Arguments arguments = new Arguments(new String[] { randomString(),
				INPUT_COLUMN_DELIMITER + "=" + randomString(10) });
		arguments.getInput();
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
