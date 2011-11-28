package net.caustic.client;

import static org.junit.Assert.*;
import static net.caustic.util.StringUtils.quote;

import java.util.Hashtable;

import mockit.NonStrict;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import net.caustic.LogScraperListener;
import net.caustic.Scraper;
import net.caustic.ScraperListener;
import net.caustic.database.Database;
import net.caustic.instruction.Instruction;
import net.caustic.log.Logger;
import net.caustic.log.SystemErrLogger;
import net.caustic.scope.Scope;
import net.caustic.scope.SerializedScope;
import net.caustic.util.StringUtils;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link ScraperInterface} with calls to actual sites.
 * @author realest
 *
 */
public class ScraperIntegrationTest {
		
	private static final String demosDir = "../demos/";
	private @NonStrict ScraperListener listener, listener2, listener3;
	private Hashtable<String, String> input;
	private Scraper scraper;
	private Logger logger = new SystemErrLogger();
	
	@Before
	public void setUp() throws Exception {		
		scraper = new Scraper();
		scraper.register(logger);
		
		input = new Hashtable<String, String>();
	}
	
	@Test
	public void testScrapeStuck() throws Exception {
		scraper.scrapeAll(demosDir + "simple-google.json", input, listener);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.onFinish(1, 1, 0);
		}};
	}
	
	@Test
	public void testScrapeFail() throws Exception {	
		scraper.scrapeAll("path/to/nothing.json", input, listener);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.onFinish(0, 0, 1);
		}};
	}
	
	@Test
	public void testScrapeSimpleGoogle() throws Exception {		
		input.put("query", "hello");
		scraper.scrapeAll(demosDir + "simple-google.json", input, listener);
		scraper.join();

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"what do you say after 'hello'?", (String[]) any);
			listener.onFinish(4, 0, 0);
		}};
	}

	@Test
	public void testScrapeSimpleGoogleQuoted() throws Exception {		
		input.put("query", "hello");
		// it shouldn't make a difference if we quote a string.
		scraper.scrapeAll(quote(demosDir + "simple-google.json"), input, listener);
		scraper.join();

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"what do you say after 'hello'?", (String[]) any);
			listener.onFinish(4, 0, 0);
		}};
	}
	
	@Test
	public void testScrapeSimpleGooglePointer() throws Exception {
		input.put("query", "hello");
		scraper.scrapeAll(demosDir + "pointer.json", input, listener);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"what do you say after 'hello'?", (String[]) any);
			listener.onFinish(4, 0, 0);
		}};
	}
	
	@Test
	public void testArrayOfScrapes() throws Exception {		
		input.put("query", "hello");
		input.put("Number", "373");
		input.put("Street", "Atlantic Ave");
		input.put("Borough", "3");
		input.put("Apt", "");
		
		final ScraperListener listener = new LogScraperListener(logger);
		
		scraper.scrapeAll(demosDir + "array.json", input, listener);
		scraper.join();
		
		new Verifications() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"what do you say after 'hello'?", (String[]) any);
			listener.onSuccess((Instruction) any, (Database) any, scope(0), (Scope) any, anyString,
					"Owner of 373 Atlantic Ave",
					(String[]) any);
			listener.onFinish(withNotEqual(0), 0, 0);
		}};
	}
	
	/**
	 * Test several calls to scrape before joining.
	 * @throws Exception
	 */
	@Test
	public void testMultipleSimpleScrapes() throws Exception {
		scraper.scrapeAll("path/to/nothing.json", new Hashtable(), listener); // should fail
		scraper.scrapeAll(demosDir + "simple-google.json", new Hashtable(), listener2); // should get stuck
		
		input.put("query", "hello");
		scraper.scrapeAll(demosDir + "simple-google.json", input, listener3); // should succeed
		
		scraper.join();
		
		new Verifications() {{
			listener.onFinish(0, 0, 1);
			listener2.onFinish(1, 1, 0);
			listener3.onFinish(4, 0, 0);
		}};
	}
	
	@Test
	public void testScrapeAllComplexGoogle() throws Exception {
		input.put("query", "hello");
		scraper.scrapeAll(demosDir + "complex-google.json", input, listener);
		scraper.join();
		

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"query", (String[]) any);
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(1), null,
					withSubstring("what do you say after '"), (String[]) any);
			listener.onFinish(withNotEqual(0), 0, 0);
		}};
	}
	
	@Test
	public void testScrapeAllReferenceGoogle() throws Exception {
		input.put("query", "hello");
		scraper.scrapeAll(demosDir + "reference-google.json", input, listener);
		scraper.join();
		

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(0), null,
					"query", (String[]) any);
			listener.onSuccess((Instruction) any, (Database) any, (Scope) any, scope(1), null,
					withSubstring("what do you say after '"), (String[]) any);
			listener.onFinish(withNotEqual(0), 0, 0);
		}};
	}
	
	@Test
	public void testScrapeAllNYCPropertyOwners() throws Exception {
		input.put("Number", "373");
		input.put("Street", "Atlantic Ave");
		input.put("Borough", "3");
		input.put("Apt", "");
		
		scraper.scrapeAll(demosDir + "nyc/nyc-property-owner.json", input, listener);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, scope(0), (Scope) any, anyString,
					"Owner of 373 Atlantic Ave",
					(String[]) any
					//withEqual(new String[] { "373 ATLANTIC AVENUE C", "373 ATLANTIC AVENUE CORPORATION" })
					);
		}};
	}
	
	@Test
	public void testScrapeAllBKPropertyOwners() throws Exception {
		input.put("Number", "373");
		input.put("Street", "Atlantic Ave");
		
		scraper.scrapeAll(demosDir + "nyc/BK-property.json", input, listener);
		scraper.join();

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, scope(0), (Scope) any, anyString,
					"Owner of 373 Atlantic Ave",
					(String[]) any
					//withEqual(new String[] { "373 ATLANTIC AVENUE C", "373 ATLANTIC AVENUE CORPORATION" })
					);
		}};
	}
	

	@Test
	public void testScrapePauseBKPropertyOwners() throws Exception {
		input.put("Number", "373");
		input.put("Street", "Atlantic Ave");
		
		scraper.scrape(demosDir + "nyc/BK-property.json", input, listener);
		
		//scraper.join();

		new VerificationsInOrder() {{
			listener.onSuccess((Instruction) any, (Database) any, scope(0), (Scope) any, anyString,
					"Owner of 373 Atlantic Ave",
					(String[]) any
					//withEqual(new String[] { "373 ATLANTIC AVENUE C", "373 ATLANTIC AVENUE CORPORATION" })
					);
			//listener.onReady((Instruction) any, (Database) any, scope(0), , parent, source, browser, start)
		}};
	}
	
		/*
	@Test
	public void testScrapeNYCIncentives() throws Exception {
		BasicNameValuePair[] extraVariables = new BasicNameValuePair[] {
				new BasicNameValuePair("Borough Number", "1"),
				new BasicNameValuePair("Block", "1171"),
				new BasicNameValuePair("Lot", "63")
		};
		new Expectations() {
			
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(nycIncentives),
						0,
						(JSONLocation) withNull(),
						(Integer) withNull());
				$ = "Problem with initial page"; times = 1;
				
				publisher.publishResult(
						"VIEWSTATE",
						anyString,
						withEqual(nycIncentives.resolve("#/finds_one/0")),
						0,
						withEqual(nycIncentives),
						0);
				$ = "Problem with Viewstate"; times = 1;

				publisher.publishResult(
						"EVENTVALIDATION",
						anyString,
						withEqual(eventValidation),
						0,
						withEqual(nycIncentives),
						0);
				$ = "Problem with EventValidation"; times = 1;
				
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(nycIncentives.resolve("#/then")),
						0,
						withEqual(nycIncentives),
						0);
				$ = "Problem with data page"; times = 1;
				
				publisher.publishResult(
						"Benefit Name",
						"421A-Newly constructed Multiple Dwelling Residential Property",
						withEqual(nycIncentives.resolve("#/then/finds_one/0")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
				publisher.publishResult(
						"Benefit Amount",
						withPrefix("$"),
						withEqual(nycIncentives.resolve("#/then/finds_one/1")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				

				publisher.publishResult(
						"Current Benefit Year",
						"16",
						withEqual(nycIncentives.resolve("#/then/finds_one/2")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Year result"; times = 1;

				publisher.publishResult(
						"Number of Benefit Years",
						"20",
						withEqual(nycIncentives.resolve("#/then/finds_one/3")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Number of Benefit Years result"; times = 1;
				
				publisher.publishResult(
						"Benefit Type",
						"Completion",
						withEqual(nycIncentives.resolve("#/then/finds_one/4")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Type result"; times = 1;

				publisher.publishResult(
						"Benefit Start Date",
						"July 01, 1996",
						withEqual(nycIncentives.resolve("#/then/finds_one/5")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Benefit End Date",
						"June 30, 2016",
						withEqual(nycIncentives.resolve("#/then/finds_one/6")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Ineligible Commercial %",
						"00.0000%",
						withEqual(nycIncentives.resolve("#/then/finds_one/7")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Ineligible Commercial % result"; times = 1;
				

				publisher.publishResult(
						"Base Year",
						"Year ending June 30, 1993",
						withEqual(nycIncentives.resolve("#/then/finds_one/8")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Base Year result"; times = 1;

				publisher.publishResult(
						"Base Year Assessed Value",
						"$5,500,000",
						withEqual(nycIncentives.resolve("#/then/finds_one/9")),
						0,
						withEqual(nycIncentives.resolve("#/then")),
						0);
				$ = "Problem with Base Year Assessed Value result"; times = 1;
				
			}
		};
		
		testScrape(nycIncentives, extraVariables);
		
	}
	

	@Test
	public void testScrapeNYCIncentivesSimple() throws Exception {
		BasicNameValuePair[] extraVariables = new BasicNameValuePair[] {
				new BasicNameValuePair("Borough Number", "1"),
				new BasicNameValuePair("Block", "1171"),
				new BasicNameValuePair("Lot", "63")
		};
		
		new Expectations() {
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(nycIncentivesSimple),
						0,
						(JSONLocation) withNull(),
						(Integer) withNull());
				$ = "Problem with initial page"; times = 1;
				
				// Dependent page load.
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0,
						withEqual(nycIncentivesSimple),
						0);
				$ = "Problem with data page"; times = 1;
				
				publisher.publishResult(
						"Benefit Name",
						"421A-Newly constructed Multiple Dwelling Residential Property",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/0")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
				publisher.publishResult(
						"Benefit Amount",
						withPrefix("$"),
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/1")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Amount result"; times = 1;

				publisher.publishResult(
						"Current Benefit Year",
						"16",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/2")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Year result"; times = 1;

				publisher.publishResult(
						"Number of Benefit Years",
						"20",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/3")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Number of Benefit Years result"; times = 1;
				
				publisher.publishResult(
						"Benefit Type",
						"Completion",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/4")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Type result"; times = 1;

				publisher.publishResult(
						"Benefit Start Date",
						"July 01, 1996",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/5")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Benefit End Date",
						"June 30, 2016",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/6")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Ineligible Commercial %",
						"00.0000%",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/7")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Ineligible Commercial % result"; times = 1;
				

				publisher.publishResult(
						"Base Year",
						"Year ending June 30, 1993",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/8")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Base Year result"; times = 1;

				publisher.publishResult(
						"Base Year Assessed Value",
						"$5,500,000",
						withEqual(nycIncentivesSimple.resolve("#/then/finds_one/9")),
						0,
						withEqual(nycIncentivesSimple.resolve("#/then")),
						0);
				$ = "Problem with Base Year Assessed Value result"; times = 1;
			}
		};
		
		testScrape(nycIncentivesSimple, extraVariables);
	}
	*/
	/**
	 * Convenience method to test one Scraper with our mock publisher.
	 * @param String {@link URIInterface} location of the {@link Scraper}
	 * instructions.
	 * @param extraVariables Array of {@link BasicNameValuePair}s to
	 * use as extra {@link Variables}.
	 * @throws Exception If the test failed.
	 */
	/*private void testScrape(URIInterface location,
			BasicNameValuePair[] extraVariables) throws Exception {
		try {
			client.scrapeAll(location, extraVariables);
		} catch(BrowserException e) {
			throw new Exception("Error loading the page.", e);
		}
	}*/
	
	/**
	 * Convenience method to generate a matching scope from an int.
	 * @param scopeNumber The <code>int</code> number of the scope.
	 * @return A {@link Scope}
	 */
	private Scope scope(int scopeNumber) {
		return new SerializedScope(Integer.toString(scopeNumber), "");
	}
}
