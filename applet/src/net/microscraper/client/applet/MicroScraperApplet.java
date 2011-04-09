package net.microscraper.client.applet;

import java.applet.Applet;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class MicroScraperApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	private final ThreadSafeLogger log = new ThreadSafeLogger();

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
				thread = new Thread(new ScrapeRunnable(url, params_string, log));
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
	
	public String log() {
		return log.unshift();
	}
}
