package net.microscraper.client;

import static org.junit.Assert.*;

import java.util.Hashtable;

import net.microscraper.client.Scraper;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.HttpUtils;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;

import org.junit.Before;
import org.junit.Test;

/**
 * @author realest
 *
 */
public class ScraperTest {
	private HttpBrowser browser;
	private Encoder encoder;
	private RegexpCompiler compiler;
	
	/**
	 * Override this method to test a specific {@link HttpRequester}.
	 * @return An {@link HttpRequester}
	 * @throws Exception
	 */
	protected HttpRequester getHttpRequester() throws Exception {
		return new JavaNetHttpRequester();
	}

	/**
	 * Override this method to test a specific {@link CookieManager}.
	 * @return An {@link CookieManager}
	 * @throws Exception
	 */
	protected CookieManager getCookieManager() throws Exception {
		return new JavaNetCookieManager();
	}

	/**
	 * Override this method to test a specific {@link HttpUtils}.
	 * @return An {@link HttpUtils}
	 * @throws Exception
	 */
	protected HttpUtils getHttpUtils() throws Exception {
		return new JavaNetHttpUtils();
	}

	/**
	 * Override this method to test a specific {@link Encoder}.
	 * @return An {@link Encoder}
	 * @throws Exception
	 */
	protected Encoder getEncoder() throws Exception {
		return new JavaNetEncoder(Encoder.UTF_8);
	}

	/**
	 * Override this method to test a specific {@link RegexpCompiler}.
	 * @return An {@link RegexpCompiler}
	 * @throws Exception
	 */
	protected RegexpCompiler getRegexpCompiler() throws Exception {
		return new JavaUtilRegexpCompiler();
	}
	
	/**
	 * Set up the {@link #client} before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		browser = new HttpBrowser(new JavaNetHttpRequester(),
				new RateLimitManager(getHttpUtils()),
				getCookieManager());
		encoder = getEncoder();
		compiler = getRegexpCompiler();
	}
	
	/**
	 * Test fixture {@link #simpleGoogle}.
	 * @throws Exception
	 */
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		StringTemplate googleTemplate = new StringTemplate("http://www.google.com/search?q={{query}}", "{{", "}}");
		Load loadGoogle = new Load(browser, encoder, googleTemplate);
		
		StringTemplate whatDoYouSayTemplate = new StringTemplate("what do you say after '{{query}}'?", "{{", "}}");
		Find findWordAfter = new Find(compiler,
				new StringTemplate("{{query}}\\s+(\\w+)", "{{", "}}"));
		findWordAfter.setReplacement(new StringTemplate("I say $1", "{{", "}}"));
		findWordAfter.setName(whatDoYouSayTemplate);
		
		loadGoogle.setChildren(new Instruction[] { findWordAfter } );

		Hashtable<String, String> inputTable = new Hashtable<String, String>();
		inputTable.put("query", "hello");
		DatabaseView input = new InMemoryDatabaseView(inputTable);
		
		Scraper scraper = new Scraper(loadGoogle, input);
		
		ScraperResult result;
		
		result = scraper.scrape();
		assertTrue(result.isSuccess());
		assertEquals(googleTemplate.subEncoded(input, encoder).getSubstituted(), result.getName());
		
		Scraper[] children = result.getChildren();
		assertEquals("Should have one child", 1, children.length);
		
		result = children[0].scrape();
		assertTrue(result.isSuccess());
		assertEquals(whatDoYouSayTemplate.sub(input).getSubstituted(), result.getName());
		assertEquals("Should not have children", 0, result.getChildren().length);
		
		String[] results = result.getValues();
		assertTrue(results.length > 0);
		for(int i = 0 ; i < results.length ; i ++) {
			assertTrue(results[i].startsWith("I say "));
		}
	}
	/*
	public void testScrapeComplexGoogle(String pathToFixture) throws Exception {
		final ScopeGenerator defaults = new ScopeGenerator();
		final ScopeGenerator afterHello = new ScopeGenerator();
		final ScopeGenerator recordGoogleAfters = new ScopeGenerator();
		final ScopeGenerator afterSomethingElse = new ScopeGenerator();
		
		new NonStrictExpectations() {{
			database.get((Scope) any, "query"); result = "hello";
			database.get((Scope) any, anyString); result = "something else";
		}};
		
		new Expectations() {{
			database.getDefaultScope(); result = defaults; times = 1;
			database.storeOneToOne((Scope) with(defaults.matchFirst()), "query", "hello");
			database.storeOneToOne((Scope) with(defaults.matchFirst()), withPrefix("http://www.google.com/"));
			database.storeOneToMany((Scope) with(defaults.matchFirst()), "after", withPrefix(anyString));
					result = afterHello;
			database.storeOneToOne((Scope) with(afterHello.matchWithin()), withPrefix("http://www.google.com/")); result = recordGoogleAfters;
			database.storeOneToMany((Scope) with(afterHello.matchWithin()), withPrefix("what do you say after"), withPrefix("I say "));
					result = afterSomethingElse;
		}};
		
		Hashtable<String, String> defaultHash = new Hashtable<String, String>();
		defaultHash.put("query", "hello");

		scraper.scrape(pathToFixture, defaultHash);
		
		assertEquals(1, defaults.count());
		assertTrue(afterHello.count() > 1);
		assertEquals("Google should have been requested for each 'after hello'.", afterHello.count(), recordGoogleAfters.count());
		assertTrue(afterSomethingElse.count() > afterHello.count());
	}
	*/
	/*
	@Test
	public void testScrapeComplexGoogleNonReference() throws Exception {
		testScrapeComplexGoogle(complexGoogle);
	}
	
	@Test
	public void testScrapeComplexGoogleReference() throws Exception {
		testScrapeComplexGoogle(referenceGoogle);
	}*/
	/*
	@Test
	public void testScrapeNYCPropertyOwners() throws Exception {
		
		final ScopeGenerator defaults = new ScopeGenerator();
		final Hashtable<String, String> propertyDefaults = new Hashtable<String, String>();
		propertyDefaults.put("Number", "373");
		propertyDefaults.put("Street", "Atlantic Ave");
		propertyDefaults.put("Borough", "3");
		propertyDefaults.put("Apt", "");

		new Expectations() {{
			database.getDefaultScope(); result = defaults;
		}};
		
		new NonStrictExpectations() {{
			Enumeration<String> keys = propertyDefaults.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				database.get((Scope) any, key); result = propertyDefaults.get(key);
				database.storeOneToOne((Scope) any, key, propertyDefaults.get(key));
			}
		}};
				
		scraper.scrape(nycPropertyOwner, propertyDefaults);
		
		new VerificationsInOrder() {{
			database.storeOneToOne((Scope) with(defaults.matchFirst()), "http://webapps.nyc.gov:8084/CICS/fin1/find001I");
			database.storeOneToMany((Scope) with(defaults.matchFirst()),
					"Owner of 373 Atlantic Ave, Borough 3", "373 ATLANTIC AVENUE C"); times = 1;
			database.storeOneToMany((Scope) with(defaults.matchFirst()),
					"Owner of 373 Atlantic Ave, Borough 3", "373 ATLANTIC AVENUE CORPORATION"); times = 1;
			
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
}
