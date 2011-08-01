package net.microscraper;

import static org.junit.Assert.*;

import java.io.File;

import mockit.Capturing;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.microscraper.Client;
import net.microscraper.BasicNameValuePair;
import net.microscraper.Log;
import net.microscraper.Variables;
import net.microscraper.executable.Result;
import net.microscraper.impl.browser.JavaNetBrowser;
import net.microscraper.impl.file.JavaIOFileLoader;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.json.JavaNetJSONLocation;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageExecutableTest {
	private JSONLocation fixturesFolder, simpleGoogle, nycPropertyOwner, nycIncentives,
						nycIncentivesSimple, eventValidation, simpleGoogleSplit1, 
						simpleGoogleSplit2;
	
	/**
	 * The test {@link Client} instance.
	 */
	//private Client client;
	
	/**
	 * The mock {@link Database}.
	 */
	//@Mocked Database publisher;
	
	/**
	 * The {@link Browser}.
	 */
	private Browser browser;
	
	
	
	/**
	 * Set up the {@link #client} before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		fixturesFolder = new JavaNetJSONLocation(
				new File(System.getProperty("user.dir")).toURI().resolve("fixtures/"));
		
		simpleGoogle = fixturesFolder.resolve("simple-google.json");
		simpleGoogleSplit1 = fixturesFolder.resolve("simple-google-split-1.json");
		simpleGoogleSplit2 = fixturesFolder.resolve("simple-google-split-2.json");
		nycPropertyOwner = fixturesFolder.resolve("nyc-property-owner.json");
		nycIncentives = fixturesFolder.resolve("nyc-incentives.json");
		eventValidation = fixturesFolder.resolve("event-validation.json");
		nycIncentivesSimple = fixturesFolder.resolve("nyc-incentives-simple.json");
		
		Log log = new Log();
		log.register(new SystemOutLogger());

		browser = new JavaNetBrowser(log, 500, 1000);
		/*client = new Client(
				new JakartaRegexpCompiler(),
				log, browser,
				new JSONME(new JavaIOFileLoader(), browser),
				publisher);*/
	}
	
	/**
	 * Test fixture {@link #simpleGoogle}.
	 * @throws Exception
	 */
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		final ConstructorDelegate delegate = new ConstructorDelegate();
		
		final String expectedPhrase = "what do we say after hello?";

		BasicNameValuePair[] extraVariables = new BasicNameValuePair[] {
				new BasicNameValuePair("query", "hello")
		};

		new Expectations() {
			@Mocked Result result1, result2;
			
			{
				// mockResult1
				// Download Google HTML.
				/*publisher.publishResult(
						(String) withNull(),
						anyString,
						withEqual(simpleGoogle),
						0,
						(JSONLocation) withNull(),
						(Integer) withNull()); times = 1;
				*/
				
				// Pull out the words.
				publisher.publishResult(
						expectedPhrase,
						anyString,
						withEqual(simpleGoogle.resolve("#/finds_many")),
						anyInt,
						withEqual(simpleGoogle),
						0); minTimes = 1;
			}
		};
		
		testScrape(simpleGoogle, extraVariables);
	}

	/**
	 * Test fixture {@link #simpleGoogleSplit1} and {@link #simpleGoogleSplit2}.
	 * @throws Exception
	 */
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
	
	/**
	 * Convenience method to test one Scraper with our mock publisher.
	 * @param String {@link JSONLocation} location of the {@link Scraper}
	 * instructions.
	 * @param extraVariables Array of {@link BasicNameValuePair}s to
	 * use as extra {@link Variables}.
	 * @throws Exception If the test failed.
	 */
	private void testScrape(JSONLocation location,
			BasicNameValuePair[] extraVariables) throws Exception {
		try {
			client.scrape(location, extraVariables, publisher);
		} catch(BrowserException e) {
			throw new Exception("Error loading the page.", e);
		}
	}
}
