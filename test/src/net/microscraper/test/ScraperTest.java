package net.microscraper.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import mockit.*;
import mockit.external.hamcrest.BaseMatcher;
import mockit.external.hamcrest.Description;
import mockit.external.hamcrest.Matcher;
import net.microscraper.client.Client;
import net.microscraper.client.ClientException;
import net.microscraper.client.Log;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.Result;
import net.microscraper.client.impl.FileLoader;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaNetInterface;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URILoader;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.Resource;
import net.microscraper.server.resource.Scraper;
import net.microscraper.client.impl.JavaUtilRegexInterface;

import org.testng.Reporter;
import org.testng.TestException;
import org.testng.annotations.*;

/**
 * Test a {@link net.microscraper.server.resources.Scraper}.
 * @author john
 *
 */
public class ScraperTest {
	private static final URI fixturesFolder = new File(System.getProperty("user.dir")).toURI().resolve("fixtures/");
	
	private final URILoader uriLoader = new FileLoader();
	private final JSONInterface jsonInterface = new JSONME(uriLoader);
	private final Log log = new Log();
	private final NetInterface netInterface = new JavaNetInterface(new JavaNetBrowser(log, 10000));
	private Client client;
	
	@Mocked Publisher publisher;
	
	@BeforeTest
	public void setupClient() throws ClientException {
		client = new Client(new JavaUtilRegexInterface(), log, netInterface, jsonInterface, Browser.UTF_8);
	}
	
	/**
	 * Tests the simple-google-scraper.json fixture.
	 * @throws Exception
	 */
	@Test
	public void testSimpleGoogleScraper() throws Exception {
		URIInterface location = netInterface.getURI(fixturesFolder.resolve("simple-google-scraper.json").toString());
		final String expectedPhrase = "what do we say after hello?";
		
		new Expectations() {
			{
				publisher.publishResult(null,null,null,anyInt,null,null);
				forEachInvocation = new Object() {
					public void report(String name, String value, String uri, int number, String sourceUri, Integer sourceNumber) {
						
					}
				};
				/*publisher.publishResult(
						(String) withNull(),
						anyString,
						withSuffix("simple-google.json#"),
						0,
						(String) withNull(),
						(Integer) withNull());
				
				publisher.publishResult(
						expectedPhrase,
						anyString,
						withSuffix("simple-google.json#finds_many.0"),
						anyInt,
						withSuffix("simple-google.json#"),
						0);*/
			}
		};
		
		UnencodedNameValuePair[] extraVariables = new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("query", "hello")
		};
		try {
			client.scrape(location, extraVariables, publisher);
		} catch(BrowserException e) {
			throw new TestException("Error loading the page.", e);
		}
	}
}
