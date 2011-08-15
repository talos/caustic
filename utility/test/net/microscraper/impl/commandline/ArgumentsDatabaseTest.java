package net.microscraper.impl.commandline;

import static net.microscraper.impl.commandline.Arguments.*;
import static net.microscraper.test.TestUtils.randomString;
import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.database.SingleTableDatabase;
import net.microscraper.database.WritableConnection;

import org.junit.Before;
import org.junit.Test;

public class ArgumentsDatabaseTest {
	
	@Mocked Arguments args;
	@Tested ArgumentsDatabase database;

	@Before
	public void setup() {
		new NonStrictExpectations() {{
			args.has(URI_INSTRUCTION); result = true;
			args.get(URI_INSTRUCTION); result = randomString();
		}};
	}
	/*
	@Test(expected = IllegalArgumentException.class)
	public void testThrowsExceptionOnInvalidFormat() throws Exception {
		new NonStrictExpectations() {{
			args.get(OUTPUT_FORMAT_OPTION); result = "notadatabaseformat";
		}};
		new ArgumentsDatabase(args);
	}
	*/
	@Test
	public void testDefaultsToSingleTableForNonSqlite() throws Exception {
		new NonStrictExpectations() {{
			args.has(OUTPUT_TO_FILE); result = true;
			args.get(OUTPUT_FORMAT_OPTION); result = CSV_OUTPUT_FORMAT_VALUE;
		}};
		new ArgumentsDatabase(args);
	}
	

	/*@Test
	public void testDefaultsToMultiTableForSqlite() throws Exception {
		new Expectations() {
			@Mocked SingleTableDatabase unused;
			{
				args.has(OUTPUT_TO_FILE); result = true;
				args.get(OUTPUT_FORMAT_OPTION); result = SQLITE_OUTPUT_FORMAT_VALUE;
				
				new SingleTableDatabase((WritableConnection) any); times = 1;
			}
		};
	}*/
}
