package net.microscraper.client.applet;

import java.applet.Applet;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.microscraper.client.BasicMicroscraper;
import net.microscraper.client.Logger;
import net.microscraper.client.Microscraper;
import net.microscraper.json.JSONParser;
import net.microscraper.json.JSONMEParser;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	
	private static final JSONParser json = new JSONMEParser();
	
	public static final String encoding = "UTF-8";
	
	//private ThreadSafeJSONPublisher database;
	private ThreadSafeJSONLogger logger = new ThreadSafeJSONLogger(json);
	private Thread current_thread;
	//private Iterator<Stringer> publisherIterator;
	//private Iterator<Stringer> logIterator;
	
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
			e.printStackTrace();
			return "Error obtaining " + key + " from manifest: " + e.toString();
		}
	}
	
	/**
	 * Starts the ScrapeAction.
	 * @param url
	 * @param params_string
	 */
	public void start(String instructionURI, String formEncodedDefaults) {
		try {
			if(!isAlive()) {
				// Reset the log and publisher with each execution.
				database = new ThreadSafeJSONDatabase(json);
				logger = new ThreadSafeJSONLogger(json);
				//logIterator = logger.getIterator();
				//publisherIterator = publisher.getIterator();
				Thread thread = new Thread(new ScrapeRunnable());
				thread.start();
				current_thread = thread;
			} else {
				logger.i("Not starting test, another test has yet to complete.  Stop it manually to test this now.");
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			current_thread.interrupt();
			Microscraper.reset();
			logger.i("Killed test.");
		} catch(Throwable e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return false;
	}
	
	public void resetExecutionsIterator() {
		try {
			database.resetIterator();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasMoreExecutions() {
		try {
			return database.hasNext();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getNextExecution() {
		try {
			return database.next().toString();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean hasMoreLogEntries() {
		try {
			return logger.hasNext();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getNextLogEntry() {
		try {
			return logger.next().toString();
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
