package net.microscraper;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.Client;
import net.microscraper.DefaultNameValuePair;
import net.microscraper.Log;
import net.microscraper.Variables;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.log.IOFileLoader;
import net.microscraper.impl.log.SystemLogInterface;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.publisher.Publisher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScraperFixturesTest {
	private static final URI fixturesFolder = new File(System.getProperty("user.dir")).toURI().resolve("fixtures/");
	private static final String simpleGoogle = "simple-google.json";
	private static final String nycPropertyOwner = "nyc-property-owner.json";
	private static final String nycIncentives = "nyc-incentives.json";
	private static final String nycIncentivesSimple = "nyc-incentives-simple.json";
	/**
	 * The test {@link Client} instance.
	 */
	private Client client;
		
	/**
	 * The mock {@link Publisher}.
	 */
	@Mocked Publisher publisher;
	
	/**
	 * The mock {@link Browser}.
	 */
	@Mocked Browser browser;
	
	/**
	 * Set up the {@link #client} before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		Log log = new Log();
		log.register(new SystemLogInterface());
		client = new Client(
				new JakartaRegexpCompiler(),
				log, browser,
				new JSONME(new IOFileLoader()));
	}
	
	/**
	 * Destroy the {@link #client} and {@link #netInterface} after each test.
	 * @throws Exception
	 */
	@After
	public void TearDown() throws Exception {
		client = null;
	}
	
	/**
	 * Test fixture {@link #simpleGoogle}.
	 * @throws Exception
	 */
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		final String expectedPhrase = "what do we say after hello?";

		DefaultNameValuePair[] extraVariables = new DefaultNameValuePair[] {
				new DefaultNameValuePair("query", "hello")
		};
		
		new Expectations() {
			{
				// Download Google HTML.
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(simpleGoogle + "#"),
						0,
						(String) withNull(),
						(Integer) withNull()); times = 1;
				
				// Pull out the words.
				publisher.publishResult(
						expectedPhrase,
						anyString,
						withSuffix(simpleGoogle + "#/finds_many/0"),
						anyInt,
						withSuffix(simpleGoogle + "#"),
						0); minTimes = 1;
			}
		};
		
		testScrape(getScraperURI(simpleGoogle), extraVariables);
	}
	
	@Test
	public void testScrapeNYCPropertyOwners() throws Exception {
		final String ownerName = "Owner Name";
		final String expectedOwner0 = "373 ATLANTIC AVENUE C";
		final String expectedOwner1 = "373 ATLANTIC AVENUE CORPORATION";
		DefaultNameValuePair[] extraVariables = new DefaultNameValuePair[] {
				new DefaultNameValuePair("House Number", "373"),
				new DefaultNameValuePair("Street Name", "Atlantic Av"),
				new DefaultNameValuePair("Borough Number", "3"),
				new DefaultNameValuePair("Apartment Number", "")
		};
		
		new Expectations() {
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycPropertyOwner + "#"),
						0,
						(String) withNull(),
						(Integer) withNull());
				$ = "Problem with page"; times = 1;
				
				// Find the first owner.
				publisher.publishResult(
						ownerName,
						expectedOwner0,
						withSuffix(nycPropertyOwner + "#/finds_many/0"),
						0,
						withSuffix(nycPropertyOwner + "#"),
						0);
				$ = "Problem with first owner result"; times = 1;
				
				// Find the second owner.
				publisher.publishResult(
						ownerName,
						expectedOwner1,
						withSuffix(nycPropertyOwner + "#/finds_many/0"),
						1,
						withSuffix(nycPropertyOwner + "#"),
						0);
				$ = "Problem with second owner result"; times = 1; 
			}
		};
		
		testScrape(getScraperURI(nycPropertyOwner), extraVariables);
	}
	
	@Test
	public void testScrapeNYCIncentives() throws Exception {
		DefaultNameValuePair[] extraVariables = new DefaultNameValuePair[] {
				new DefaultNameValuePair("Borough Number", "1"),
				new DefaultNameValuePair("Block", "1171"),
				new DefaultNameValuePair("Lot", "63")
		};
		
		new Expectations() {
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycIncentives + "#"),
						0,
						(String) withNull(),
						(Integer) withNull());
				$ = "Problem with initial page"; times = 1;
				
				publisher.publishResult(
						"VIEWSTATE",
						anyString,
						withSuffix(nycIncentives + "#/finds_one/0"),
						0,
						withSuffix(nycIncentives + "#"),
						0);
				$ = "Problem with Viewstate"; times = 1;

				publisher.publishResult(
						"EVENTVALIDATION",
						anyString,
						withSuffix(nycIncentives + "#/finds_one/1"),
						0,
						withSuffix(nycIncentives + "#"),
						0);
				$ = "Problem with EventValidation"; times = 1;
				
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycIncentives + "#/then/0"),
						0,
						withSuffix(nycIncentives + "#"),
						0);
				$ = "Problem with data page"; times = 1;
				
				publisher.publishResult(
						"Benefit Name",
						"421A-Newly constructed Multiple Dwelling Residential Property",
						withSuffix(nycIncentives + "#/then/0/finds_one/0"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
				publisher.publishResult(
						"Benefit Amount",
						withPrefix("$"),
						withSuffix(nycIncentives + "#/then/0/finds_one/1"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				

				publisher.publishResult(
						"Current Benefit Year",
						"16",
						withSuffix(nycIncentives + "#/then/0/finds_one/2"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Year result"; times = 1;

				publisher.publishResult(
						"Number of Benefit Years",
						"20",
						withSuffix(nycIncentives + "#/then/0/finds_one/3"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Number of Benefit Years result"; times = 1;
				
				publisher.publishResult(
						"Benefit Type",
						"Completion",
						withSuffix(nycIncentives + "#/then/0/finds_one/4"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Type result"; times = 1;

				publisher.publishResult(
						"Benefit Start Date",
						"July 01, 1996",
						withSuffix(nycIncentives + "#/then/0/finds_one/5"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Benefit End Date",
						"June 30, 2016",
						withSuffix(nycIncentives + "#/then/0/finds_one/6"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Ineligible Commercial %",
						"00.0000%",
						withSuffix(nycIncentives + "#/then/0/finds_one/7"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Ineligible Commercial % result"; times = 1;
				

				publisher.publishResult(
						"Base Year",
						"Year ending June 30, 1993",
						withSuffix(nycIncentives + "#/then/0/finds_one/8"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Base Year result"; times = 1;

				publisher.publishResult(
						"Base Year Assessed Value",
						"$5,500,000",
						withSuffix(nycIncentives + "#/then/0/finds_one/9"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Base Year Assessed Value result"; times = 1;
				
			}
		};
		
		testScrape(getScraperURI(nycIncentives), extraVariables);
	}
	

	@Test
	public void testScrapeNYCIncentivesSimple() throws Exception {
		DefaultNameValuePair[] extraVariables = new DefaultNameValuePair[] {
				new DefaultNameValuePair("Borough Number", "1"),
				new DefaultNameValuePair("Block", "1171"),
				new DefaultNameValuePair("Lot", "63")
		};
		
		new Expectations() {
			{
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycIncentivesSimple + "#"),
						0,
						(String) withNull(),
						(Integer) withNull());
				$ = "Problem with initial page"; times = 1;
				
				// Dependent page load.
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0,
						withSuffix(nycIncentivesSimple + "#"),
						0);
				$ = "Problem with data page"; times = 1;
				
				publisher.publishResult(
						"Benefit Name",
						"421A-Newly constructed Multiple Dwelling Residential Property",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/0"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
				publisher.publishResult(
						"Benefit Amount",
						withPrefix("$"),
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/1"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Amount result"; times = 1;

				publisher.publishResult(
						"Current Benefit Year",
						"16",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/2"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Year result"; times = 1;

				publisher.publishResult(
						"Number of Benefit Years",
						"20",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/3"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Number of Benefit Years result"; times = 1;
				
				publisher.publishResult(
						"Benefit Type",
						"Completion",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/4"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Type result"; times = 1;

				publisher.publishResult(
						"Benefit Start Date",
						"July 01, 1996",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/5"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Benefit End Date",
						"June 30, 2016",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/6"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Benefit Start Date result"; times = 1;

				publisher.publishResult(
						"Ineligible Commercial %",
						"00.0000%",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/7"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Ineligible Commercial % result"; times = 1;
				

				publisher.publishResult(
						"Base Year",
						"Year ending June 30, 1993",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/8"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Base Year result"; times = 1;

				publisher.publishResult(
						"Base Year Assessed Value",
						"$5,500,000",
						withSuffix(nycIncentivesSimple + "#/then/0/finds_one/9"),
						0,
						withSuffix(nycIncentivesSimple + "#/then/0"),
						0);
				$ = "Problem with Base Year Assessed Value result"; times = 1;
			}
		};
		
		testScrape(getScraperURI(nycIncentivesSimple), extraVariables);
	}
	
	/**
	 * Convenience method to test one Scraper with our mock publisher.
	 * @param String {@link URIInterface} location of the {@link Scraper}
	 * instructions.
	 * @param extraVariables Array of {@link DefaultNameValuePair}s to
	 * use as extra {@link Variables}.
	 * @throws Exception If the test failed.
	 */
	private void testScrape(String location,
			DefaultNameValuePair[] extraVariables) throws Exception {
		try {
			client.scrape(location, extraVariables, publisher);
		} catch(BrowserException e) {
			throw new Exception("Error loading the page.", e);
		}
	}
	
	/**
	 * Obtain the {@link URIInterface} path to a {@link Scraper} fixture based
	 * off its name.
	 * Uses {@link #fixturesFolder}.
	 * @param scraperName The file name of the {@link Scraper}.
	 * @return The URI to the {@link Scraper} file.
	 */
	private String getScraperURI(String scraperName) {
		return fixturesFolder.resolve(scraperName).toString();
	}
}
