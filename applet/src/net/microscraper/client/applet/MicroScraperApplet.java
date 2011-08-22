package net.microscraper.client.applet;

import java.applet.Applet;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.microscraper.browser.JavaNetDecoder;
import net.microscraper.client.BasicMicroscraper;
import net.microscraper.client.Browser;
import net.microscraper.client.Microscraper;
import net.microscraper.util.HashtableUtils;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	
	private ThreadSafeDatabase database;
	private ThreadSafeLogger logger;
	private Thread current_thread;
	
	// thanks http://stackoverflow.com/questions/1272648/need-to-read-own-jars-manifest-and-not-root-classloaders-manifest
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
				
				
				database = new ThreadSafeDatabase();
				logger = new ThreadSafeLogger();
				Microscraper scraper = BasicMicroscraper.get(database, 100, 100);
				scraper.register(logger);
								
				Thread thread = new Thread(
						new ScrapeRunnable(
								scraper,
								instructionURI,
								HashtableUtils.fromFormEncoded(new JavaNetDecoder(), formEncodedDefaults, Browser.UTF_8)
							)
					);
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
