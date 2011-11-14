package net.caustic.client;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import mockit.NonStrict;
import mockit.VerificationsInOrder;
import net.caustic.Scraper;
import net.caustic.ScraperListener;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.log.Logger;
import net.caustic.log.SystemOutLogger;
import net.caustic.scope.Scope;
import net.caustic.scope.SerializedScope;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link ScraperInterface} with calls to actual sites.
 * @author realest
 *
 */
public class ScraperIntegrationTest {
		
	private @NonStrict ScraperListener listener;
	private Map<String, String> input;
	private Scraper scraper;
	private Logger logger = new SystemOutLogger();
	
	@Before
	public void setUp() throws Exception {
		logger.open();
		
		scraper = new Scraper();
		scraper.addListener(listener);
		scraper.register(logger);
		
		input = new HashMap<String, String>();
	}


	@Test
	public void testScrapeStuck() throws Exception {		
		scraper.scrape("fixtures/json/simple-google.json", input);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.newScope(scope(0));
			listener.missing((Instruction) any, scope(0), null, (HttpBrowser) any, (String[]) any);
			listener.terminated(0, 1, 0);
		}};
	}
	
	@Test
	public void testScrapeFail() throws Exception {		
		scraper.scrape("path/to/nothing.json", input);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.newScope(scope(0));
			listener.failed((Instruction) any, scope(0), null, anyString);
			listener.terminated(0, 0, 1);
		}};
	}
	
	@Test
	public void testScrapeSimpleGoogle() throws Exception {		
		input.put("query", "hello");
		scraper.scrape("fixtures/json/simple-google.json", input);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.newScope(scope(0));
			listener.put(scope(0), "query", "hello");
			listener.newScope(scope(0), "what do you say after 'hello'?", withPrefix("I say "), (Scope) any);
				minTimes = 1;
		}};
	}
	
	@Test
	public void testScrapeComplexGoogle() throws Exception {

		input.put("query", "hello");
		scraper.scrape("fixtures/json/complex-google.json", input);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.newScope(scope(0));
			listener.put(scope(0), "query", "hello");
			listener.newScope(scope(0), "query", anyString, (Scope) any); minTimes = 1;
			listener.newScope((Scope) any, withPrefix("what do you say after"), withPrefix("I say "), (Scope) any);
				minTimes = 1;
		}};
	}
	
	@Test
	public void testScrapeReferenceGoogle() throws Exception {
		
		input.put("query", "hello");
		scraper.scrape("fixtures/json/reference-google.json", input);
		scraper.join();
		new VerificationsInOrder() {{
			listener.newScope(scope(0));
			listener.put(scope(0), "query", "hello");
			listener.newScope(scope(0), "query", anyString, (Scope) any); minTimes = 1;
			listener.newScope((Scope) any, withPrefix("what do you say after"), withPrefix("I say "), (Scope) any);
				minTimes = 1;
		}};
	}
	
	@Test
	public void testScrapeNYCPropertyOwners() throws Exception {
		input.put("Number", "373");
		input.put("Street", "Atlantic Ave");
		input.put("Borough", "3");
		input.put("Apt", "");
		
		scraper.scrape("fixtures/json/nyc/nyc-property-owner.json", input);
		scraper.join();
		
		new VerificationsInOrder() {{
			listener.newScope(scope(0), "Owner of 373 Atlantic Ave, Borough 3", "373 ATLANTIC AVENUE C", scope(1));
			listener.newScope(scope(0), "Owner of 373 Atlantic Ave, Borough 3", "373 ATLANTIC AVENUE CORPORATION", scope(2));
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
			client.scrape(location, extraVariables);
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
		return new SerializedScope(Integer.toString(scopeNumber));
	}
}
