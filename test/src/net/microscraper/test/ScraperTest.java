package net.microscraper.test;

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
import org.testng.annotations.*;

/**
 * Test a {@link net.microscraper.server.resources.Scraper}.
 * @author john
 *
 */
public class ScraperTest {
	private final URILoader uriLoader = new FileLoader();
	private final JSONInterface jsonInterface = new JSONME(uriLoader);
	private final Log log = new Log();
	private final NetInterface netInterface = new JavaNetInterface(new JavaNetBrowser(log, 10000));
	private Client client;
	
	@NonStrict Publisher publisher;
	
	@BeforeTest
	public void setupClient() throws ClientException {
		client = new Client(new JavaUtilRegexInterface(),
				log, netInterface, jsonInterface, Browser.UTF_8);
		//client.scrape(null, null, publisher);
	}
	
	@Test
	public void testSimpleGoogleScraper() throws Exception {
		URIInterface location = netInterface.getURI("file:///Users/john/microscraper/microscraper-client/test/fixtures/simple-google-scraper.json");
		
		new Expectations() {
			{
				publisher.publishResult(0, 0, 1, withSubstring("microscraper"), anyString); times = 1; 
				publisher.publishResult(1, 1, 2, "what do we say after hello?", anyString); minTimes = 1; 
				//publisher.publishResult(0, 1, 0, "what do we say after hello?", anyString); times = 1; 
			}
		};
		
		UnencodedNameValuePair[] extraVariables = new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("query", "hello")
		};
		
		client.scrape(location, extraVariables, publisher);
	}
	/**
	 * 
	 * 
	 * @param path The {@link URIInterface} path to the {@link Scraper}'s directory.
	 * @param name The {@link String} name of the {@link Scraper}.
	 * @return A {@link Scraper} from a {@link URIInterface URI}.
	 * @throws ClientException 
	 * @throws IllegalArgumentException 
	 */
	/*private void runScraper(String path, String name, UnencodedNameValuePair[] extraVariables, 
			Publisher publisher)
		throws IllegalArgumentException, ClientException {
		
		client.scrape(netInterface.getURI(path).resolve(name), extraVariables, publisher);
		//return new Scraper(jsonInterface.loadJSONObject(path.resolve(name)));
	}
	
	@Parameters({ "pathToScrapers" })
	@Test
	public void testSimpleGoogleScraper(String pathToScrapers)
			throws IllegalArgumentException, ClientException {
		
		// Recording
		Publisher publisher;
		new MockUp<Publisher>() {
			void publish(Executable executable) {
				
			}
		};
		String name = "simple-google-scraper.json";
		runScraper(pathToScrapers, name, new UnencodedNameValuePair[] {
				new UnencodedNameValuePair("query", "hello")
		}, publisher);
		//Scraper simpleGoogleScraper = newScraper(netInterface.getURI(pathToScrapers), name);
	}*/
}
