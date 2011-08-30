package net.microscraper.impl.commandline;

import static net.microscraper.impl.commandline.Arguments.*;
import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;

import org.junit.Before;
import org.junit.Test;

public class ArgumentsMicroscraperTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testBatchSizeMustBeInt() {
		String notAnInt = "nope";
		new Arguments(new String[] { randomString(),
				BATCH_SIZE + "=" + notAnInt,
				SAVE_TO_FILE.toString()});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBatchSizeOnlyWithSQLOutput() {
		new Arguments(new String[] { randomString(),
				BATCH_SIZE + "=" + randomInt() });
	}
	
	@Test
	public void testDefaultsAreTheSameWithQuotesOrWithout() {
		String defaults = randomString();
		
		Arguments withQuotes = new Arguments(new String[] { randomString(),
				DEFAULTS + "=" + '"' + defaults + '"' });
		Arguments withoutQuotes = new Arguments(new String[] { randomString(),
				DEFAULTS + "=" + defaults });
		
		assertEquals(withoutQuotes.get(DEFAULTS), withQuotes.get(DEFAULTS));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMustHaveInputToDefineInputDelimiter() {
		new Arguments(new String[] { randomString(), INPUT_COLUMN_DELIMITER + "=" + randomString(1) });
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInputColumnDelimiterMustBeOneCharacter() {
		new Arguments(new String[] { randomString(), INPUT_COLUMN_DELIMITER + "=" + randomString(10) });
	}
	
	@Test
	public void testScrape() {
		fail("Not yet implemented");
	}

}
