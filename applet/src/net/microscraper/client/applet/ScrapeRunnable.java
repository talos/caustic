package net.microscraper.client.applet;

import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.database.Model;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Default;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final Reference resource_ref;
	private final Default[] defaults;
	
	private final Client client;
	
	public ScrapeRunnable(String url, String model, String full_name,
			String params_string, Client client) {
		this.url = url;
		this.resource_ref = new Reference(Model.get(model), full_name);
		this.client = client;
		this.defaults = Default.fromFormParams(params_string, MicroScraperApplet.encoding);
	}
	
	@Override
	public void run() {
		try {
			AccessController.doPrivileged(new ScrapeAction());
		} catch(Throwable e) {
			client.log.e(e);
		}
	}
	
	private class ScrapeAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				client.scrape(url, resource_ref, defaults);
			} catch(Throwable e) {
				client.log.e(e);
			}
			return null;
		}
	}
}