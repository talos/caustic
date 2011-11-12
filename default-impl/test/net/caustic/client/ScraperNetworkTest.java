package net.caustic.client;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import mockit.Mocked;
import mockit.Verifications;
import net.caustic.client.Scraper;
import net.caustic.concurrent.SyncExecutor;
import net.caustic.database.DatabaseView;
import net.caustic.database.DatabaseViewListener;
import net.caustic.database.InMemoryDatabaseView;
import net.caustic.deserializer.DefaultJSONDeserializer;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.http.CookieManager;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequester;
import net.caustic.http.HttpUtils;
import net.caustic.http.JavaNetCookieManager;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.JavaNetHttpUtils;
import net.caustic.http.RateLimitManager;
import net.caustic.instruction.Find;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.json.JsonMEParser;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.uri.JavaNetUriResolver;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.xml.internal.ws.util.StringUtils;

/**
 * Test {@link Scraper} with calls to actual sites.
 * @author realest
 *
 */
@RunWith(Parameterized.class)
public class ScraperNetworkTest {
		
	private final HttpBrowser browser;
	private final RegexpCompiler c;

	private @Mocked DatabaseViewListener listener;
	private DatabaseView view;
	private Scraper scraper;
	private JSONDeserializer deserializer;
	
	public ScraperNetworkTest(HttpBrowser browser, RegexpCompiler compiler) {
		this.browser = browser;
		this.c = compiler;
	}
	
	@Parameters
	public static Collection<Object[]> implementations() throws Exception {
		return Arrays.asList(new Object[][] {
				{	new HttpBrowser(new JavaNetHttpRequester(),
						new RateLimitManager(new JavaNetHttpUtils()),
						new JavaNetCookieManager()), 
					new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8))
				}
		});
	}
	
	@Before
	public void setUp() {
		view = new InMemoryDatabaseView();
		view.addListener(listener);
		scraper = new Scraper(view, browser, new SyncExecutor());
		deserializer = new DefaultJSONDeserializer();
	}
	
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		/*Load loadGoogle = new Load(c.newTemplate("http://www.google.com/search?q={{query}}"));
				
		Find findWordAfter = new Find(c, c.newTemplate("{{query}}\\s+(\\w+)"));
		findWordAfter.setReplacement(c.newTemplate("I say $1"));
		findWordAfter.setName(c.newTemplate("what do you say after '{{query}}'?"));
		
		//Instruction instruction = new Instruction(loadGoogle);
		load.then(findWordAfter);
		*/
		
		view.put("query", "hello");
		//scraper.scrape(instruction);
		scraper.scrape(new SerializedInstruction("../", deserializer, ));
		
		new Verifications() {{
			listener.spawnChild("what do you say after 'hello'?", withPrefix("I say "),
					(DatabaseView) any); minTimes = 1;
		}};
	}
	/**
	 * Test a Scraper that would serialize as
	 * {
	"load" : "http://www.google.com/search?q={{query}}",
	"then"  : {
		"find"     : "{{query}}\\s+(\\w+)",
		"replace" : "$1",
		"name"   : "query",
		"then" : {
			"load" : "http://www.google.com/search?q={{query}}",
			"then" : {
				"find"     : "{{query}}\\s+(\\w+)",
				"replace" : "I say '$1'!",
				"name"   : "what do you say after '{{query}}'?"
			}
		}
	}
}
	 * @throws Exception
	 */
	public void testScrapeComplexGoogle() throws Exception {
		Load load = new Load(c.newTemplate("http://www.google.com/search?q={{query}}"));
		Find nextWord = new Find(c, c.newTemplate("{{query}}\\s+(\\w+)"));
		nextWord.setReplacement(c.newTemplate("$1"));
		
	}
	
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
