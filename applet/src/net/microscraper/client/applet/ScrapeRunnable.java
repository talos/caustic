package net.microscraper.client.applet;

import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.Client;
import net.microscraper.execution.HasVariableExecutions;
import net.microscraper.model.Reference;

public class ScrapeRunnable implements Runnable {
	private final Client client;
	private final String url;
	private final Reference resource_ref;
	private final HasVariableExecutions variables;
		
	public ScrapeRunnable(Client client, String url, String model, String full_name,
			String params_string) {
		this.url = url;
		this.resource_ref = new Reference(Model.get(model), full_name);
		this.variables = HasVariableExecutions.fromFormParams(params_string, MicroScraperApplet.encoding);
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
				client.scrape(client.browser.loadJSON(url, client.json), resource_ref, variables);
			} catch(Throwable e) {
				client.log.e(e);
			}
			return null;
		}
	}
}