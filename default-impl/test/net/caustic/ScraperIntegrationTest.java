package net.caustic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Expectations;
import mockit.NonStrict;
import net.caustic.Response.DoneFind;
import net.caustic.Response.DoneLoad;
import net.caustic.Response.MissingTags;
import net.caustic.Scraper;
import net.caustic.http.Cookies;
import net.caustic.http.HashtableCookies;
import net.caustic.log.Logger;
import net.caustic.log.SystemErrLogger;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link ScraperInterface} with calls to actual sites.
 * @author realest
 *
 */
public class ScraperIntegrationTest {
	
	private @NonStrict StringMap tags;
	private Cookies cookies;
	private static final String URI = StringUtils.USER_DIR;
	
	private static final String demosDir = "../demos/";
	private Scraper scraper;
	private Logger logger = new SystemErrLogger();
	
	@Before
	public void setUp() throws Exception {
		scraper = new DefaultScraper();
		cookies = new HashtableCookies();
	}
	
	@Test
	public void testScrapeStuck() throws Exception {
		Response resp = scraper.scrape(
				new Request("id", demosDir + "simple-google.json", URI, null, tags, cookies, true));
		
		assertEquals(Response.MISSING_TAGS, resp.getStatus());
		MissingTags missingTags = (MissingTags) resp;
		assertArrayEquals(new String[] { "query" }, missingTags.getMissingTags());
	}

	@Test
	public void testScrapeFail() throws Exception {	
		Response resp = scraper.scrape(
				new Request("id", "/path/to/nothing", URI, null, tags, cookies, true));
		assertEquals(Response.FAILED, resp.getStatus());
	}
	
	@Test
	public void testScrapeSimpleGoogle() throws Exception {
		new Expectations() {{
			tags.get("query"); result = "hello";
		}};
		
		DoneLoad doneLoad = (DoneLoad) scraper.scrape(
				new Request("id", demosDir + "simple-google.json", URI, null, tags, cookies, true));

		assertEquals(1, doneLoad.getChildren().length);
		DoneFind doneFind = (DoneFind) scraper.scrape(new Request("id", doneLoad.getChildren()[0], doneLoad.uri, doneLoad.getContent(), tags, cookies, false));
		
		assertEquals("what do you say after 'hello'?", doneFind.getName());
		for(String value : doneFind.getValues()) {
			assertTrue(value.startsWith("I say"));
		}
	}
	
	@Test
	public void testScrapeSimpleGooglePointer() throws Exception {
		new Expectations() {{
			tags.get("query"); result = "hello";
		}};
		
		DoneLoad doneLoad = (DoneLoad) scraper.scrape(
				new Request("id", demosDir + "pointer.json", URI, null, tags, cookies, true));
		
		assertEquals(1, doneLoad.getChildren().length);
		DoneFind doneFind = (DoneFind) scraper.scrape(
				new Request("id", doneLoad.getChildren()[0], doneLoad.uri, doneLoad.getContent(), tags, cookies, false));
		
		assertEquals("what do you say after 'hello'?", doneFind.getName());
		for(String value : doneFind.getValues()) {
			assertTrue(value.startsWith("I say"));
		}
	}
	
	@Test
	public void testArrayOfScrapes() throws Exception {
		new Expectations() {{
			tags.get("query"); result = "hello";
			tags.get("Number"); result = "373";
			tags.get("Street"); result = "Atlantic Ave";
			tags.get("Borough"); result = "3";
			tags.get("Apt"); result = "";
		}};
		
		// not forced
		Response.Reference resp = (Response.Reference) scraper.scrape(
				new Request("id", demosDir + "array.json", URI, null, tags, cookies, false));
		
		List<String> values = new ArrayList<String>();
		
		assertEquals(2, resp.getReferenced().length);
		for(String child : resp.getReferenced()) {
			DoneLoad childResp = (DoneLoad) scraper.scrape(
					new Request("id", child, resp.uri, null, tags, cookies, true));
			for(String grandchild : childResp.getChildren()) {
				DoneFind grandchildResp = (DoneFind) scraper.scrape(new Request("id", grandchild,
						childResp.uri, childResp.getContent(), tags,
						cookies, false));
				values.addAll(Arrays.asList(grandchildResp.getValues()));
			}
		}
	
		boolean containsISay = false;
		for(String value : values) {
			if(value.startsWith("I say ")) {
				containsISay = true;
				break;
			}
		}
		assertTrue(containsISay);

		boolean containsOwner = false;
		for(String value : values) {
			if(value.startsWith("373 ATLANTIC AVE")) {
				containsOwner = true;
				break;
			}
		}
		assertTrue(containsOwner);

	}
	
	/**
	 * Test several calls to scrape before joining.
	 * @throws Exception
	 */
	/*
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
	*/
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
	
	
}
