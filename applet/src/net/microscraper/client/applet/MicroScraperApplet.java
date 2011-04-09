package net.microscraper.client.applet;

import java.applet.Applet;
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

public class MicroScraperApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2768937336583253219L;
	/*
	public static void main(String[] args)
	{
		return;
	}
	*/
	
	
	private final Client client = Client.initialize(new ApacheBrowser(),
			new JavaUtilRegexInterface(), new JSONME(),
			new Interfaces.Logger[] {}
			);
	
	public String scrape(String url, String params_string) {
		return AccessController.doPrivileged(new ScrapeAction(url, params_string));
	}
	
	private class ScrapeAction implements PrivilegedAction<String> {
		private final String url;
		private final Default[] defaults;
		public ScrapeAction(String _url, String params_string) {
			url = _url;
			String[] params = Utils.split(params_string, "&");
			defaults = new Default[params.length];
			
			try {
				for(int i = 0 ; i < params.length ; i ++ ) {
					String[] name_value = Utils.split(params[i], "=");
					defaults[i] = new Default(name_value[0], name_value[1]);
				}
				
				String response = "";
				for(int i = 0; i < defaults.length ; i ++ ) {
					response += defaults[i].toString();
				}
			} catch(IndexOutOfBoundsException e) {
				throw new IllegalArgumentException("Invalid parameters.");
			}
		}
		@Override
		public String run() {
			//return "changed: " + Integer.toString(params.length);
			try {
				AbstractResult[] results = client.scrape(url, defaults);
				
				String response = "";
				for(int i = 0; i < results.length ; i ++ ) {
					response += results[i].variables().toString();
				}
				return response;
			} catch(Throwable e) {
				e.printStackTrace();
				return "TERROR: " + e.toString();
			}
		}
	}
		/*try {
			//Publisher publisher = new SQLPublisher(new JDBCSQLite("./" + DateFormat.getTimeInstance() + ".sqlite", client.log));
			Default[] defaults = new Default[] {
				new Default("Borough_Number", "3"),
				new Default("House_Number", "373"),
				new Default("Street_Name", "Atlantic Ave"),
				new Default("Apartment_Number", "")
			};
			client.scrape(url, defaults);
			//publisher.publish(client.scrape(args[0], defaults));
			//client.log.i("Finished execution.");
		} catch (MicroScraperClientException e) {
			client.log.e(e);
		} catch (PublisherException e) {
			client.log.e(e);
		}*/
}
