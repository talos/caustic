package net.microscraper.client.applet;

import java.applet.Applet;
import java.util.Hashtable;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.ResultSet.Result;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.ThreadSafePublisher;
import net.microscraper.database.Reference;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	private static final String version = ".01";
	
	private final ThreadSafePublisher publisher = new ThreadSafePublisher();
	private final JSON json = new JSONME();
	private final ThreadSafeLogger log = new ThreadSafeLogger(json);
	
	public static final String encoding = "UTF-8";
	private Thread thread;
	private final Client client = Client.initialize(
			new ApacheBrowser(false),
			new JavaUtilRegexInterface(), json,
			new Interfaces.Logger[] { log }
		);
	
	public String version() { return version; }
	
	/**
	 * Starts the ScrapeAction.
	 * @param url
	 * @param params_string
	 */
	public boolean start(String url, String model, String resource_ref, String params_string) {
		try {
			if(!isAlive()) {
				thread = new Thread(new ScrapeRunnable(url, model, new Reference(resource_ref), params_string, publisher, client));
				thread.start();
				return true;
			}
		} catch(Exception e) {
			log.e(e);
		}
		return false;
	}
	
	public boolean kill() {
		try {
			thread.interrupt();
			return true;
		} catch(Exception e) {
			log.e(e);
		}
		return false;
	}
	
	public boolean isAlive() {
		try {
			if(thread == null) {
				return false;
			} else {
				return thread.isAlive();
			}
		} catch(Exception e) {
			log.e(e);
		}
		return false;
	}
	
	public String results() {
		try {
			Result result = publisher.unshift();
			if(result != null) {
				Hashtable<String, String> result_table = new Hashtable<String, String>();
				result_table.put(result.ref.toString(), result.value);
				return json.toJSON(result_table);
			}
		} catch(Exception e) {
			log.e(e);
		}
		return null;
	}
	
	public String log() {
		return log.unshift();
	}
}
