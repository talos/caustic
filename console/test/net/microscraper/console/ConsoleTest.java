package net.microscraper.console;

import java.io.PrintStream;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import net.microscraper.http.HttpBrowser;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.StringUtils;

import org.junit.After;
import org.junit.Test;

public class ConsoleTest {

	private @Mocked({"print", "println"}) PrintStream out;
	
	/**
	 * 
	 * @param strings 
	 * @return A fake result string that would appear in console.
	 */
	private static String row(String... strings) {
		return StringUtils.quoteJoin(strings, "\t") + StringUtils.NEWLINE;
	}
	
	@After
	public void tearDown() throws Exception {
		// have to call this or else it is added repeatedly
		//Runtime.getRuntime().removeShutdownHook(Console.shutdownThread);
	}
	
	@Test
	public void testNoArgsMissingInstruction() throws Exception {
		new Expectations() {{
			out.print(ConsoleOptions.INSTRUCTION_MISSING_ERROR);
			out.println(); times = 2;
			out.print(ConsoleOptions.USAGE);
			out.println();
		}};
		new Console();
	}

	@Test
	public void testSimpleGoogleMissingInput() throws Exception {
		final Console console = new Console("../fixtures/json/simple-google.json", "--log-stdout");
		console.execute();
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(ScraperExecutor.formatStatusLine(0, 1, 0, 0, 0));
		}};
	}
	
	@Test
	public void testSimpleGoogleStringInput() throws Exception {
		new Expectations() {
			@Mocked({"head", "get", "post"}) HttpBrowser browser;
			{
				browser.get("http://www.google.com/search?q=hello", (Hashtable) any, (Pattern[]) any);
					result = "hello world hello tree hello whee";
			}
		};
		
		final Console console = new Console(
				"../fixtures/json/simple-google.json",
				"--input=query=hello"
					);
		
		//Console.shutdownThread.start();
		//Console.shutdownThread.join();
		
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("1", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(row("2", "0", "what do you say after 'hello'?", "I say 'tree'!"));
			out.print(row("3", "0", "what do you say after 'hello'?", "I say 'whee'!"));
			out.print(ScraperExecutor.formatStatusLine(2, 0, 0, 0, 0));
		}};
	}
	
	/**
	 * Record expectations and run the console for simple-google, with the queries input file.
	 * @param numThreads The number of threads to execute with.
	 * @throws Exception
	 */
	public Console recordSimpleGoogleInputFile(int numThreads) throws Exception {
		new Expectations() {
			@Mocked({"head", "get", "post"}) HttpBrowser browser;
			{
				browser.get("http://www.google.com/search?q=hello", (Hashtable) any, (Pattern[]) any);
					result = "hello world";
				browser.get("http://www.google.com/search?q=meh", (Hashtable) any, (Pattern[]) any);
					result = "unrelated words";
				browser.get("http://www.google.com/search?q=bleh", (Hashtable) any, (Pattern[]) any);
					result = "bleh this bleh that";
			}
		};
		return new Console(
				"../fixtures/json/simple-google.json",
				"--input-file=../fixtures/csv/queries.csv",
				"--threads=" + numThreads
					);
	}
	
	@Test
	public void testSimpleGoogleInputFileSingleThread() throws Exception {
		
		final Console console = recordSimpleGoogleInputFile(1);
		// In order, because this is synchronous
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("0", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(row("1", "1", "query", "meh"));
			out.print(row("2", "2", "query", "bleh"));
			out.print(row("3", "2", "what do you say after 'bleh'?", "I say 'this'!"));
			out.print(row("4", "2", "what do you say after 'bleh'?", "I say 'that'!"));
			out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0));
		}};
	}

	@Test
	public void testSimpleGoogleInputFileMultiThread() throws Exception {
		final Console console = recordSimpleGoogleInputFile(5);
		// Out of order, because this is asynchronous
		new Verifications() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(withSuffix(row("query", "hello")));
			out.print(withSuffix(row("what do you say after 'hello'?", "I say 'world'!")));
			out.print(withSuffix(row("query", "meh")));
			out.print(withSuffix(row("query", "bleh")));
			out.print(withSuffix(row("what do you say after 'bleh'?", "I say 'this'!")));
			out.print(withSuffix(row("what do you say after 'bleh'?", "I say 'that'!")));
			out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0));
		}};
	}
	
	public void testComplexGoogleGeneric(String pathToFixture) throws Exception {
		new Expectations() {
			@Mocked({"head", "get", "post"}) HttpBrowser browser;
			{
			browser.get("http://www.google.com/search?q=hello", (Hashtable) any, (Pattern[]) any);
				result = "hello world hello tree hello whee";
			browser.get("http://www.google.com/search?q=world", (Hashtable) any, (Pattern[]) any);
				result = "world peace world domination";
			browser.get("http://www.google.com/search?q=tree", (Hashtable) any, (Pattern[]) any);
				result = "tree planting";
			browser.get("http://www.google.com/search?q=whee", (Hashtable) any, (Pattern[]) any);
				result = "";
		}};
		
		final Console console = new Console(
				pathToFixture,
				"--input=query=hello"
					);
		
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("1", "0", "query", "world"));
			out.print(row("2", "0", "query", "tree"));
			out.print(row("3", "0", "query", "whee"));
			out.print(row("4", "1", "what do you say after 'world'?", "I say 'peace'!"));
			out.print(row("5", "1", "what do you say after 'world'?", "I say 'domination'!"));
			out.print(row("2", "2", "what do you say after 'tree'?", "I say 'planting'!"));
				$ = "Single match, should share scope.";
			out.print(ScraperExecutor.formatStatusLine(7, 0, 1, 0, 0));
				$ = "The lack of matches for 'whee' should count as a failure.";
		}};
	}
	
	@Test
	public void testComplexGoogleNonreference() throws Exception {
		testComplexGoogleGeneric("../fixtures/json/complex-google.json");
	}
	
	@Test
	public void testComplexGoogleReference() throws Exception {
		testComplexGoogleGeneric("../fixtures/json/reference-google.json");		
	}
}
