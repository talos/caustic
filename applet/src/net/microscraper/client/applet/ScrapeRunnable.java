package net.microscraper.client.applet;

import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.impl.ThreadSafePublisher;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Default;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final String model;
	private final Reference resource_ref;
	private final Default[] defaults;
	
	private final Client client;
	private final Publisher publisher;
	
	public ScrapeRunnable(String _url, String _model, Reference _resource_ref, String params_string, ThreadSafePublisher _publisher, Client _client) {
		url = _url;
		publisher = _publisher;
		model = _model;
		resource_ref = _resource_ref;
		client = _client;
		defaults = Default.fromFormParams(params_string, MicroScraperApplet.encoding);
	}
	@Override
	public void run() {
		AccessController.doPrivileged(new ScrapeAction());
	}
	
	private class ScrapeAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				client.scrape(url, model, resource_ref, defaults, publisher);
			} catch(Throwable e) {
				client.log.e(e);
			}
			return null;
		}
	}
}