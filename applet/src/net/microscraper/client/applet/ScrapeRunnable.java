package net.microscraper.client.applet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.database.schema.Default;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final Default[] defaults;
	
	private final Client client;
	private final Publisher publisher;
	
	public ScrapeRunnable(String _url, String params_string, ThreadSafePublisher _publisher, Client _client) {
		url = _url;
		publisher = _publisher;
		String[] params = Utils.split(params_string, "&");
		defaults = new Default[params.length];
		
		client = _client;
		
		try {
			for(int i = 0 ; i < params.length ; i ++ ) {
				String[] name_value = Utils.split(params[i], "=");
				defaults[i] =
					new Default(
								URLDecoder.decode(name_value[0], MicroScraperApplet.encoding),
								URLDecoder.decode(name_value[1], MicroScraperApplet.encoding)
								);
			}
		} catch(IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Parameters '" + params_string + "' should be serialized like HTTP Post data.");
		} catch(UnsupportedEncodingException e) {
			//throw new IllegalArgumentException("Unsupported encoding: " + e.getMessage());
			throw new Error(e);
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