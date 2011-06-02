package net.microscraper.client;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.client.impl.FileLoader;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaNetInterface;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URILoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScraperFixturesTest {
	private static final URI fixturesFolder = new File(System.getProperty("user.dir")).toURI().resolve("fixtures/");
	private static final String simpleGoogle = "simple-google.json";
	private static final String nycPropertyOwner = "nyc-property-owner.json";
	private static final String nycIncentives = "nyc-incentives.json";
	/**
	 * The test {@link Client} instance.
	 */
	private Client client;
	
	/**
	 * Utility {@link NetInterface} instance used to create paths to {@link Scraper}s.
	 */
	private NetInterface netInterface;
	
	/**
	 * The mock {@link Publisher}.
	 */
	@Mocked Publisher publisher;
	
	/**
	 * Set up the {@link #client} before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		Log log = new Log();
		log.register(new SystemLogInterface());
		netInterface = new JavaNetInterface(new JavaNetBrowser(log, 10000));
		client = new Client(
				new JavaUtilRegexInterface(),
				log,
				netInterface,
				new JSONME(new FileLoader()),
				Browser.UTF_8);
	}
	
	/**
	 * Destroy the {@link #client} and {@link #netInterface} after each test.
	 * @throws Exception
	 */
	@After
	public void TearDown() throws Exception {
		client = null;
		netInterface = null;
	}
	
	/**
	 * Test fixture {@link #simpleGoogle}.
	 * @throws Exception
	 */
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		final String expectedPhrase = "what do we say after hello?";

		UnencodedNameValuePair[] extraVariables = new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("query", "hello")
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
		UnencodedNameValuePair[] extraVariables = new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("House Number", "373"),
				new UnencodedNameValuePair("Street Name", "Atlantic Av"),
				new UnencodedNameValuePair("Borough Number", "3"),
				new UnencodedNameValuePair("Apartment Number", "")
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
		UnencodedNameValuePair[] extraVariables = new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("Borough Number", "1"),
				new UnencodedNameValuePair("Block", "1171"),
				new UnencodedNameValuePair("Lot", "63")
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
				
				// Viewstate.
				publisher.publishResult(
						"VIEWSTATE",
						anyString,
						withSuffix(nycIncentives + "#/finds_one/0"),
						0,
						withSuffix(nycIncentives + "#"),
						0);
				$ = "Problem with Viewstate"; times = 1;
				
				// Dependent page load.
				publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix(nycIncentives + "#/then/0"),
						0,
						withSuffix(nycIncentives + "#"),
						0);
				$ = "Problem with data page"; times = 1;
				
				// Find the Benefit name.
				publisher.publishResult(
						"Benefit Name",
						"421A-Newly constructed Multiple Dwelling Residential Property",
						withSuffix(nycIncentives + "#/then/0/finds_one/0"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
				// Find the Benefit amount.
				publisher.publishResult(
						"Benefit Amount",
						withPrefix("$"),
						withSuffix(nycIncentives + "#/then/0/finds_one/1"),
						0,
						withSuffix(nycIncentives + "#/then/0"),
						0);
				$ = "Problem with Benefit Name result"; times = 1;
				
			}
		};
		
		testScrape(getScraperURI(nycIncentives), extraVariables);
	}
	
	/**
	 * Convenience method to test one Scraper with our mock publisher.
	 * @param String {@link URIInterface} location of the {@link Scraper}
	 * instructions.
	 * @param extraVariables Array of {@link UnencodedNameValuePair}s to
	 * use as extra {@link Variables}.
	 * @throws Exception If the test failed.
	 */
	private void testScrape(URIInterface location,
			UnencodedNameValuePair[] extraVariables) throws Exception {
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
	 * @return The {@link URIInterface} to the {@link Scraper} file.
	 * @throws NetInterfaceException If there was an {@link Exception} resolving the
	 * path to the {@link Scraper}.
	 */
	private URIInterface getScraperURI(String scraperName) throws NetInterfaceException {
		return netInterface.getURI(fixturesFolder.resolve(scraperName).toString());
	}
}
