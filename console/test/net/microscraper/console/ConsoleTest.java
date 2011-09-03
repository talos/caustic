package net.microscraper.console;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.PrintStream;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;

import org.junit.Before;
import org.junit.Test;

public class ConsoleTest {
	
	@Mocked({"print", "println"}) PrintStream out;
	
	@Test
	public void testSimpleGoogle() throws Exception {
		new Expectations() {{
			out.print("\"scope\"	\"source\"	\"name\"	\"value\"" + ConsoleOptions.NEWLINE);
			out.println();
		}};
		Console.main(new String[] { "../fixtures/json/simple-google.json", "--input=query=hello" });
	}

}
