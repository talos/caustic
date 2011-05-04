package net.microscraper.client.applet;

import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.client.Variables;
import net.microscraper.database.Model;
import net.microscraper.database.Reference;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final Reference resource_ref;
	private final Variables variables;
		
	public ScrapeRunnable(String url, String model, String full_name,
			String params_string) {
		this.url = url;
		this.resource_ref = new Reference(Model.get(model), full_name);
		this.variables = Variables.fromFormParams(params_string, MicroScraperApplet.encoding);
	}
	
	@Override
	public void run() {
		try {
			AccessController.doPrivileged(new ScrapeAction());
		} catch(Throwable e) {
			Client.log.e(e);
		}
	}
	
	private class ScrapeAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				Client.scrape(Client.browser.loadJSON(url, Client.json), resource_ref, variables);
			} catch(Throwable e) {
				Client.log.e(e);
			}
			return null;
		}
	}
}