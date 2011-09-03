package net.microscraper.console;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.http.BadURLException;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.HttpResponse;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.StringUtils;

import org.junit.Before;
import org.junit.Test;

public class ConsoleTest {
	
	/**
	 * 
	 * @param strings 
	 * @return A fake result string that would appear in console.
	 */
	private static String result(String... strings) {
		return StringUtils.quoteJoin(strings, "\t") + StringUtils.NEWLINE;
	}
	
	@Mocked({"print", "println"}) PrintStream out;
	
	@Test
	public void testNoArgsMissingInstruction() throws Exception {
		new Expectations() {{
			out.print(ConsoleOptions.INSTRUCTION_MISSING_ERROR);
			out.print(ConsoleOptions.USAGE);
		}};
		Console.main(new String[] { });
	}
	
	@Test
	public void testSimpleGoogle(@Mocked({"head", "get", "post"}) final HttpBrowser browser) throws Exception {
		new Expectations() {{
			out.print(result("scope", "source", "name", "value"));
			out.print(result("0", "0", "query", "hello"));
			out.print(result("1", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(result("2", "0", "what do you say after 'hello'?", "I say 'tree'!"));
			out.print(result("3", "0", "what do you say after 'hello'?", "I say 'whee'!"));
			browser.get("http://www.google.com/search?q=hello", (Hashtable) any, (Pattern[]) any);
				result = "hello world hello tree hello whee";
			
			
			
		}};
		
		Console.main(new String[] { "../fixtures/json/simple-google.json", "--input=query=hello" });
	}

}
