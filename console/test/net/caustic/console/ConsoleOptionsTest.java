package net.caustic.console;

import static net.caustic.console.ConsoleOptions.*;
import static net.caustic.util.TestUtils.randomInt;
import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.Map;

import net.caustic.console.ConsoleOptions;
import net.caustic.console.InvalidOptionException;
import net.caustic.util.HashtableUtils;

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
	
	@Test
	public void testInputIsTheSameWithQuotesOrWithout() throws Exception {
		
		Hashtable<String, String> origHash = new Hashtable<String, String>();
		for(int i = 0 ; i < 10 ; i ++) {
			origHash.put(randomString(), randomString());
		}
		
		String defaults = HashtableUtils.toFormEncoded(origHash);
		
		ConsoleOptions withQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + '=' + '"' + defaults + '"' });
		ConsoleOptions withoutQuotes = new ConsoleOptions(new String[] { randomString(),
				INPUT + "=" + defaults });
		
		Map<String, String> mapWithQuotes = withQuotes.getInput().next();
		Map<String, String> mapWithoutQuotes = withoutQuotes.getInput().next();
		
		for(String key : origHash.keySet()) {
			assertEquals(origHash.get(key), mapWithQuotes.get(key));
			assertEquals(origHash.get(key), mapWithoutQuotes.get(key));
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
	
}
