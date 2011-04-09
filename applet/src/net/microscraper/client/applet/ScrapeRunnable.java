package net.microscraper.client.applet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.microscraper.client.AbstractResult;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.database.schema.Default;

public class ScrapeRunnable implements Runnable {
	private final String url;
	private final Default[] defaults;
	
	
	private final Client client;
	
	public ScrapeRunnable(String _url, String params_string, ThreadSafeLogger log) {
		url = _url;
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
		//return "changed: " + Integer.toString(params.length);
		
		// test infinite looping
		public Void run() {
			boolean loop = true;
			int test = 0;
			while(loop == true) {
				test++;
				client.log.i(Integer.toString(test));
			}
			try {
				AbstractResult[] results = client.scrape(url, defaults);
				
				String response = "";
				for(int i = 0; i < results.length ; i ++ ) {
					response += results[i].variables().toString();
				}
				//return response;
			} catch(Throwable e) {
				//e.printStackTrace();
				StackTraceElement[] traces = e.getStackTrace();
				String traces_string = "";
				for(int i = 0 ; i < 10 && i < traces.length; i ++ ) {
					traces_string += traces[i].toString();
				}
				//return "Error: " + e.toString() + "Trace: " + traces_string;
			}
			return null;
		}
	}
}