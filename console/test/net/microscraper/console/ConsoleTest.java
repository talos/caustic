package net.microscraper.console;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.VerificationsInOrder;
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

	private @Mocked({"print", "println"}) PrintStream out;
	
	/**
	 * 
	 * @param strings 
	 * @return A fake result string that would appear in console.
	 */
	private static String row(String... strings) {
		return StringUtils.quoteJoin(strings, "\t") + StringUtils.NEWLINE;
	}
	
	@Test
	public void testNoArgsMissingInstruction() throws Exception {
		new Expectations() {{
			out.print(ConsoleOptions.INSTRUCTION_MISSING_ERROR);
			out.println(); times = 2;
			out.print(ConsoleOptions.USAGE);
			out.println();
		}};
		Console.main(new String[] { });
	}

	@Test
	public void testSimpleGoogleMissingInput() throws Exception {
		Console.main("../fixtures/json/simple-google.json");
		
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(Console.statusLine(0, 1, 0));
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
		
		Console.main(
				"../fixtures/json/simple-google.json",
				"--input=query=hello"
					);
		
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("1", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(row("2", "0", "what do you say after 'hello'?", "I say 'tree'!"));
			out.print(row("3", "0", "what do you say after 'hello'?", "I say 'whee'!"));
			out.print(Console.statusLine(2, 0, 0));
		}};
	}


	@Test
	public void testSimpleGoogleInputFile() throws Exception {
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
		Console.main(
				"../fixtures/json/simple-google.json",
				"--input-file=../fixtures/csv/queries.csv"
					);
		
		new VerificationsInOrder() {{
			out.print(row("scope", "source", "name", "value"));
			out.print(row("0", "0", "query", "hello"));
			out.print(row("1", "1", "query", "meh"));
			out.print(row("2", "2", "query", "bleh"));
			out.print(row("3", "0", "what do you say after 'hello'?", "I say 'world'!"));
			out.print(row("4", "2", "what do you say after 'bleh'?", "I say 'this'!"));
			out.print(row("4", "2", "what do you say after 'bleh'?", "I say 'that'!"));
			out.print(Console.statusLine(5, 0, 1));
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
		
		Console.main(
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
			out.print(Console.statusLine(7, 0, 1));
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
