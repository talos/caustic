package net.microscraper.client.applet;

import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.Microscraper;

public class ScrapeRunnable implements Runnable {
	private final Microscraper client;
	private final String instructionURI;
	private final String formEncodedDefaults;
		
	public ScrapeRunnable(Microscraper client, String instructionURI, String formEncodedDefaults) {
		this.client = client;
		this.instructionURI = instructionURI;
		this.formEncodedDefaults = formEncodedDefaults;
	}
	
	@Override
	public void run() {
		try {
			AccessController.doPrivileged(new ScrapeAction());
		} catch(Throwable e) {
			client.e(e);
		}
	}
	
	private class ScrapeAction implements PrivilegedAction<Void> {
		public Void run() {
			try {
				client.scrapeWithURI(instructionURI, formEncodedDefaults);
			} catch(Throwable e) {
				client.e(e);
			}
			return null;
		}
	}
}