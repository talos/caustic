package net.caustic.console;

import java.io.PrintStream;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import net.caustic.console.ConsoleOptions;
import net.caustic.console.MainClass;
import net.caustic.http.HttpBrowser;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StringUtils;

import org.junit.After;
import org.junit.Test;

public class MainClassTest {

	//private @Mocked(inverse=true, methods={"print", "println"}) PrintStream out;
	private @Mocked(methods={"lsdkjflsdkjf"}) PrintStream out;
	
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
		//new Console();
		MainClass.main();
	}

	@Test
	public void testSimpleGoogleMissingInput() throws Exception {
		//final Console console = new Console("../fixtures/json/simple-google.json", "--log-stdout");
		//console.execute();
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			//out.print(ScraperExecutor.formatStatusLine(0, 1, 0, 0, 0));
		}};
		
		MainClass.main("../fixtures/json/simple-google.json", "--log-stdout");
	}
	
	@Test
	public void testSimpleGoogleStringInput() throws Exception {
		new Expectations() {
			@Mocked({"request"}) HttpBrowser browser;
			{
				browser.request("http://www.google.com/search?q=hello", "get", (Hashtable) any, (String[]) any, null);
					result = "hello world hello tree hello whee";
			}
		};
		
		MainClass.main(
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
			//out.print(ScraperExecutor.formatStatusLine(2, 0, 0, 0, 0));
		}};
	}
	
	/**
	 * Record expectations and run the console for simple-google, with the queries input file.
	 * @param numThreads The number of threads to execute with.
	 * @throws Exception
	 */
	public void recordSimpleGoogleInputFile(int numThreads) throws Exception {
		new Expectations() {
			@Mocked({"request"}) HttpBrowser browser;
			{
				browser.request("http://www.google.com/search?q=hello", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = "hello world";
				browser.request("http://www.google.com/search?q=meh", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = "unrelated words";
				browser.request("http://www.google.com/search?q=bleh", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = "bleh this bleh that";
			}
		};
		MainClass.main(
				"../fixtures/json/simple-google.json",
				"--input-file=../fixtures/csv/queries.csv",
				"--threads=" + numThreads
					);
	}
	
	@Test
	public void testSimpleGoogleInputFileSingleThread() throws Exception {
		
		recordSimpleGoogleInputFile(1);
		// In order, because this is synchronous
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("0", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(row("1", "1", "query", "meh"));
			out.print(row("2", "2", "query", "bleh"));
			out.print(row("3", "2", "what do you say after 'bleh'?", "I say 'this'!"));
			out.print(row("4", "2", "what do you say after 'bleh'?", "I say 'that'!"));
			//out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0));
		}};
	}

	@Test
	public void testSimpleGoogleInputFileMultiThread() throws Exception {
		recordSimpleGoogleInputFile(5);
		// Out of order, because this is asynchronous
		new Verifications() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(withSuffix(row("query", "hello")));
			out.print(withSuffix(row("what do you say after 'hello'?", "I say 'world'!")));
			out.print(withSuffix(row("query", "meh")));
			out.print(withSuffix(row("query", "bleh")));
			out.print(withSuffix(row("what do you say after 'bleh'?", "I say 'this'!")));
			out.print(withSuffix(row("what do you say after 'bleh'?", "I say 'that'!")));
			//out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0));
		}};
	}
	
	public void testComplexGoogleGeneric(String pathToFixture) throws Exception {
		new Expectations() {
			@Mocked({"request"}) HttpBrowser browser;
			{
			browser.request("http://www.google.com/search?q=hello", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = "hello world hello tree hello whee";
			browser.request("http://www.google.com/search?q=world", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = "world peace world domination";
			browser.request("http://www.google.com/search?q=tree", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = "tree planting";
			browser.request("http://www.google.com/search?q=whee", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = "";
		}};
		
		MainClass.main(
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
			//out.print(ScraperExecutor.formatStatusLine(7, 0, 1, 0, 0));
			//	$ = "The lack of matches for 'whee' should count as a failure.";
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
