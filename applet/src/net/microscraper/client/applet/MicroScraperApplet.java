package net.microscraper.client.applet;

import java.applet.Applet;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.ThreadSafeJSONPublisher;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	
	private static final ThreadSafeJSONPublisher publisher = new ThreadSafeJSONPublisher();
	private static final JSON json = new JSONME();
	private static final ThreadSafeLogger log = new ThreadSafeLogger(json);
	
	public static final String encoding = "UTF-8";
	private Thread current_thread;
	private final Client client = Client.initialize(
			new JavaNetBrowser(),
			new JavaUtilRegexInterface(), json,
			new Interfaces.Logger[] { log },
			publisher
		);
		
	// thx http://stackoverflow.com/questions/1272648/need-to-read-own-jars-manifest-and-not-root-classloaders-manifest
	public String manifest(String key) {
		try {
			String className = MicroScraperApplet.class.getSimpleName() + ".class";
			String classPath = MicroScraperApplet.class.getResource(className).toString();
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
				"/META-INF/MANIFEST.MF";
		
			Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			Attributes attr = manifest.getMainAttributes();
			
			return (String) attr.getValue(key);
		} catch (Throwable e) {
			return "Error obtaining " + key + " from manifest: " + e.toString();
		}
	}
	
	/**
	 * Starts the ScrapeAction.
	 * @param url
	 * @param params_string
	 */
	public void start(String url, String model, String full_name, String params_string) {
		try {
			if(!isAlive()) {
				Thread thread = new Thread(new ScrapeRunnable(url, model, full_name, params_string, client));
				thread.start();
				current_thread = thread;
			} else {
				client.log.i("Not starting test, another test has yet to complete.  Stop it manually to test this now.");
			}
		} catch(Throwable e) {
			client.log.e(e);
		}
	}
	
	public void stop() {
		try {
			current_thread.interrupt();
			client.log.i("Killed test.");
		} catch(Throwable e) {
			client.log.e(e);
		}
	}
	
	public boolean isAlive() {
		try {
			if(current_thread == null) {
				return false;
			} else {
				return current_thread.isAlive();
			}
		} catch(Throwable e) {
			client.log.e(e);
		}
		return false;
	}
	
	public String results() {
		try {
			//Result result = publisher.shift();
			/*if(result != null) {
				Hashtable<String, String> result_table = new Hashtable<String, String>();
				result_table.put(result.key, result.value);
				return json.toJSON(result_table);
			}*/
			return publisher.toJSON();
		} catch(Throwable e) {
			client.log.e(e);
		}
		return null;
	}
	
	public String log() {
		try {
			return log.shift();
		} catch(Throwable e) {
			return "Error pulling from log: " + e.toString();
		}
	}
}
