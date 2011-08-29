package net.microscraper.client;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.database.HashtableDatabase;
import net.microscraper.file.FileLoader;
import net.microscraper.http.HttpBrowser;
import net.microscraper.json.JsonParser;
import net.microscraper.regexp.RegexpCompiler;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link Microscraper} using fixtures, with a live {@link HttpBrowser}, {@link
 * RegexpCompiler}, {@link JsonParser}, and {@link FileLoader}.
 * @author realest
 *
 */
public abstract class MicroscraperImplementationTest {
	private String simpleGoogle, complexGoogle, 
	
		nycPropertyOwner, nycIncentives, eventValidation;
	
	private static final String PATH_TO_FIXTURES = "../fixtures/json/";
	
	/**
	 * The mocked {@link Database}.
	 */
	private Database database;
	
	/**
	 * The tested {@link Microscraper} instance.
	 */
	private Microscraper scraper;
	
	protected abstract Microscraper getScraperToTest(Database database) throws Exception;
	
	/**
	 * Set up the {@link #client} before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		URI fixtures = new File(System.getProperty("user.dir")).toURI().resolve(PATH_TO_FIXTURES);
		
		simpleGoogle =       fixtures.resolve("simple-google.json").toString();
		complexGoogle =      fixtures.resolve("complex-google.json").toString();
		
		nycPropertyOwner =   fixtures.resolve("nyc-property-owner.json").toString();
		nycIncentives =      fixtures.resolve("nyc-incentives.json").toString();
		eventValidation =     fixtures.resolve("event-validation.json").toString();
		
		database = new HashtableDatabase();
		scraper = getScraperToTest(database);
	}
	
	/**
	 * Test fixture {@link #simpleGoogle}.
	 * @throws Exception
	 */
	@Test
	public void testScrapeSimpleGoogle() throws Exception {		
		/*new Expectations() {
			@Mocked Database database;
			{
			database.open(); result = variables;
			variables.storeOneToOne("query", "hello"); result = variables;
			database.storeOneToMany(0, "what do you say after 'hello'?", withPrefix("I say ")); result = 2;
			database.storeOneToMany(0, "what do you say after 'hello'?", withPrefix("I say ")); result = 3;
			database.storeOneToMany(0, "what do you say after 'hello'?", withPrefix("I say ")); result = 4;
			//database.store(0, 0, "query", (String) withNull(), "hello"); result = 0;
			// etc.
			database.storeOneToMany(0, "what do you say after 'hello'?", withPrefix("I say ")); minTimes = 1;
			
			database.close();
		}};*/
		
		Hashtable<String, String> defaults = new Hashtable<String, String>();
		defaults.put("query", "hello");
		//scraper.register(new SystemOutLogger());
		scraper.scrape(simpleGoogle, defaults);
		
		new Verifications() {
			@Mocked Database database;
			{
				database.storeOneToOne(0, "");
		}};
	}
	
	@Test
	public void testScrapeComplexGoogle() throws Exception {
		new Expectations() {{
			database.storeOneToOne(0, "query", "hello"); result = 1;

			database.storeOneToMany(0, "after", anyString); result = 2;
			database.storeOneToMany(0, "after", anyString); result = 3;
			database.storeOneToMany(0, "after", anyString); result = 4;
			database.storeOneToMany(0, "after", anyString); minTimes = 1;
			
			database.storeOneToMany(anyInt, withPrefix("what do you say after"), withPrefix("I say")); minTimes = 1;
		
			database.close();
		}};
		
		Hashtable<String, String> defaults = new Hashtable<String, String>();
		defaults.put("query", "hello");

		scraper.scrape(complexGoogle, defaults);
	}
	
	/**
	 * Test fixture {@link #simpleGoogleSplit1} and {@link #simpleGoogleSplit2}.
	 * @throws Exception
	 */
	/*
	@Test
	public void testScrapeSimpleGoogleSplit() throws Exception {
		final String expectedPhrase = "what do we say after hello?";

		BasicNameValuePair[] extraVariables = new BasicNameValuePair[] {
				new BasicNameValuePair("query", "hello")
		};
		
		new Expectations() {
			{
				// Download Google HTML.
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(simpleGoogleSplit1),
						0,
						(JSONLocation) withNull(),
						(Integer) withNull()); times = 1;
				
				// Pull out the words.
				publisher.publishResult(
						expectedPhrase,
						anyString,
						withEqual(simpleGoogleSplit2),
						anyInt,
						withEqual(simpleGoogleSplit1),
						0); minTimes = 1;
			}
		};
		
		testScrape(simpleGoogleSplit1, extraVariables);
	}
	
	@Test
	public void testScrapeNYCPropertyOwners() throws Exception {
		final String ownerName = "Owner Name";
		final String expectedOwner0 = "373 ATLANTIC AVENUE C";
		final String expectedOwner1 = "373 ATLANTIC AVENUE CORPORATION";
		BasicNameValuePair[] extraVariables = new BasicNameValuePair[] {
				new BasicNameValuePair("House Number", "373"),
				new BasicNameValuePair("Street Name", "Atlantic Av"),
				new BasicNameValuePair("Borough Number", "3"),
				new BasicNameValuePair("Apartment Number", "")
		};
		
		new Expectations() {
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(nycPropertyOwner),
						0,
						(JSONLocation) withNull(),
						(Integer) withNull());
				$ = "Problem with page"; times = 1;
				
				// Find the first owner.
				publisher.publishResult(
						ownerName,
						expectedOwner0,
						withEqual(nycPropertyOwner.resolve("#/finds_many/0")),
						0,
						withEqual(nycPropertyOwner),
						0);
				$ = "Problem with first owner result"; times = 1;
				
				// Find the second owner.
				publisher.publishResult(
						ownerName,
						expectedOwner1,
						withEqual(nycPropertyOwner.resolve("#/finds_many/0")),
						1,
						withEqual(nycPropertyOwner),
						0);
				$ = "Problem with second owner result"; times = 1; 
			}
		};
		
		testScrape(nycPropertyOwner, extraVariables);
	}
		
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
}
