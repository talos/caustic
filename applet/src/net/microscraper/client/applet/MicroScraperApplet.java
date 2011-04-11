package net.microscraper.client.applet;

import java.applet.Applet;

import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.ResultSet.Result;
import net.microscraper.client.impl.JSONME;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	private final ThreadSafeLogger log = new ThreadSafeLogger();
	private final ThreadSafePublisher publisher = new ThreadSafePublisher();
	private final JSON json = new JSONME();
	
	public static final String encoding = "UTF-8";
	private Thread thread;
	
	/**
	 * Starts the ScrapeAction.
	 * @param url
	 * @param params_string
	 */
	public boolean start(String url, String params_string) {
		try {
			if(!isAlive()) {
				thread = new Thread(new ScrapeRunnable(url, params_string, publisher, log));
				thread.run();
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
			return "<result><ref>" + result.ref.toString() + "</ref><value>" + result.value + "</value></div>";
		} catch(Exception e) {
			log.e(e);
		}
		return null;
	}
	
	public String log() {
		return log.unshift();
	}
}
