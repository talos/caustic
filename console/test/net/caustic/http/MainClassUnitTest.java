package net.caustic.http;

import java.io.PrintStream;
import java.util.Hashtable;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import net.caustic.console.MainClass;
import net.caustic.console.Output;
import net.caustic.http.BrowserResponse;
import net.caustic.http.HttpBrowser;
import net.caustic.util.HashtableUtils;
import net.caustic.util.StringUtils;

import org.junit.After;
import org.junit.Test;

public class MainClassUnitTest {

	private static final String DEMOS = "../demos";
	private static final String FIXTURES = "../fixtures";
	private @Capturing HttpBrowser browser;
	
	//private @Mocked(inverse=true, methods={"print", "println"}) PrintStream out;
	//private @Mocked(methods={"lsdkjflsdkjf"}) PrintStream out;
	
	private @Mocked(methods={"print"}) Output out;
	
	/**
	 * 
	 * @param strings 
	 * @return A fake result string that would appear in console.
	 */
	/*private static String String... strings) {
		return StringUtils.quoteJoin(strings, "\t") + StringUtils.NEWLINE;
	}*/
	
	@After
	public void tearDown() throws Exception {
		// have to call this or else it is added repeatedly
		//Runtime.getRuntime().removeShutdownHook(Console.shutdownThread);
	}
	
	/*@Test
	public void testNoArgsMissingInstruction() throws Exception {
		new Expectations() {{
			out.print(ConsoleOptions.INSTRUCTION_MISSING_ERROR);
			out.println(); times = 2;
			out.print(ConsoleOptions.USAGE);
			out.println();
		}};
		//new Console();
		MainClass.main();
	}*/

	@Test
	public void testSimpleGoogleMissingInput() throws Exception {
		//console.execute();
		new VerificationsInOrder() {{
			out.print("scope", "source", "name", "value");
			//out.print(ScraperExecutor.formatStatusLine(0, 1, 0, 0, 0));
		}};
		
		MainClass.main(DEMOS + "/simple-google.json", "--log");
	}
	
	@Test
	public void testSimpleGoogleStringInput() throws Exception {
		new Expectations() {
			//@Mocked({"request"}) HttpBrowser browser;
			{
				browser.request("http://www.google.com/search?q=hello", "get", (Hashtable) any, (String[]) any, null);
					result = new BrowserResponse("hello world hello tree hello whee", new String[] {});
			}
		};
		
		MainClass.main(
				DEMOS + "/simple-google.json",
				"--input=query=hello",
				"--log"
					);
		
		//Console.shutdownThread.start();
		//Console.shutdownThread.join();
		
		new VerificationsInOrder() {{
			out.print("scope", "source", "name", "value");
			out.print("1", "0", "what do you say after 'hello'?", "I say 'world'!");
			out.print("2", "0", "what do you say after 'hello'?", "I say 'tree'!");
			out.print("3", "0", "what do you say after 'hello'?", "I say 'whee'!");
			//out.print(ScraperExecutor.formatStatusLine(2, 0, 0, 0, 0);
		}};
	}
	
	/**
	 * Record expectations and run the console for simple-google, with the queries input file.
	 * @param numThreads The number of threads to execute with.
	 * @throws Exception
	 */
	public void recordSimpleGoogleInputFile(int numThreads) throws Exception {
		new Expectations() {
			{
				browser.request("http://www.google.com/search?q=hello", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = new BrowserResponse("hello world", new String[] {});
				browser.request("http://www.google.com/search?q=meh", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = new BrowserResponse("unrelated words", new String[] {});
				browser.request("http://www.google.com/search?q=bleh", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
					result = new BrowserResponse("bleh this bleh that", new String[] {});
			}
		};
		MainClass.main(
				DEMOS + "/simple-google.json",
				"--input-file=" + FIXTURES + "/csv/queries.csv",
				"--threads=" + numThreads,
				"--log"
					);
	}
	
	@Test
	public void testSimpleGoogleInputFileSingleThread() throws Exception {
		
		recordSimpleGoogleInputFile(1);
		// In order, because this is synchronous
		new VerificationsInOrder() {{
			out.print("scope", "source", "name", "value");
			out.print("0", "0", "what do you say after 'hello'?", "I say 'world'!");
			out.print("3", "2", "what do you say after 'bleh'?", "I say 'this'!");
			out.print("4", "2", "what do you say after 'bleh'?", "I say 'that'!");
			//out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0);
		}};
	}

	@Test
	public void testSimpleGoogleInputFileMultiThread() throws Exception {
		recordSimpleGoogleInputFile(5);
		// Out of order, because this is asynchronous
		new Verifications() {{
			out.print("scope", "source", "name", "value");
			out.print(anyString, anyString, "what do you say after 'hello'?", "I say 'world'!");
			out.print(anyString, anyString, "what do you say after 'bleh'?", "I say 'this'!");
			out.print(anyString, anyString, "what do you say after 'bleh'?", "I say 'that'!");
			//out.print(ScraperExecutor.formatStatusLine(5, 0, 1, 0, 0);
		}};
	}
	
	public void testComplexGoogleGeneric(String pathToFixture) throws Exception {
		new Expectations() {
			{
			browser.request("http://www.google.com/search?q=hello", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = new BrowserResponse("hello world hello tree hello whee", new String[]{});
			browser.request("http://www.google.com/search?q=world", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = new BrowserResponse("world peace world domination", new String[]{});
			browser.request("http://www.google.com/search?q=tree", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = new BrowserResponse("tree planting", new String[]{});
			browser.request("http://www.google.com/search?q=whee", "get", HashtableUtils.EMPTY, (String[]) any, anyString);
				result = new BrowserResponse("", new String[]{});
		}};
		
		MainClass.main(pathToFixture, "--input=query=hello", "--threads=1", "--log");
		
		new Verifications() {{
			out.print("scope", "source", "name", "value");
			out.print("1", "0", "query", "world");
			out.print("2", "0", "query", "tree");
			out.print("3", "0", "query", "whee");
			out.print("4", "1", "what do you say after 'world'?", "I say 'peace'!");
			out.print("5", "1", "what do you say after 'world'?", "I say 'domination'!");
			out.print("2", "2", "what do you say after 'tree'?", "I say 'planting'!");
		}};
	}
	
	@Test
	public void testComplexGoogleNonreference() throws Exception {
		testComplexGoogleGeneric(DEMOS + "/complex-google.json");
	}
	
	@Test
	public void testComplexGoogleReference() throws Exception {
		testComplexGoogleGeneric(DEMOS + "/reference-google.json");		
	}
}
