package net.microscraper.impl.commandline;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import static net.microscraper.impl.commandline.Arguments.*;

import org.junit.Test;

public class ArgumentsTest {

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentsNeedsOneArgument() {
		new Arguments(new String[] {});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentsFailsOnUnknownArgument() {
		new Arguments(new String[] {"not", "options=bleh"});
	}
	
	@Test
	public void testHasArgumentWithValue() throws Exception {
		int batchSize = randomInt();
		
		Arguments args = new Arguments(new String[] { randomString(), INPUT + "=" + batchSize });
		
		assertTrue(args.has(INPUT));
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
}
