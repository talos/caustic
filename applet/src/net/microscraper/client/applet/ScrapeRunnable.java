package net.microscraper.client.applet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.database.schema.Default;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final Default[] defaults;
	
	private final Client client;
	private final Publisher publisher;
	
	public ScrapeRunnable(String _url, String params_string, ThreadSafePublisher _publisher, ThreadSafeLogger log) {
		url = _url;
		publisher = _publisher;
		String[] params = Utils.split(params_string, "&");
		defaults = new Default[params.length];
		
		client = Client.initialize(new ApacheBrowser(),
					new JavaUtilRegexInterface(), new JSONME(),
					new Interfaces.Logger[] { log }
				);
		
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				defaults[i] =
					new Default(
								URLDecoder.decode(name_value[0], MicroScraperApplet.encoding),
								URLDecoder.decode(name_value[1], MicroScraperApplet.encoding)
								);
			}
			
			String response = "";
			for(int i = 0; i < defaults.length ; i ++ ) {
				response += defaults[i].toString();
			}
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported encoding: " + e);
		}
	}
	@Override
	public void run() {
		AccessController.doPrivileged(new ScrapeAction());
	}
	
	private class ScrapeAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				client.scrape(url, defaults, publisher);
			} catch(Throwable e) {
				client.log.e(e);
			}
			return null;
		}
	}
}