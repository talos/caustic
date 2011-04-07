package net.microscraper.client.applet;
/*
import java.text.DateFormat;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Publisher;
import net.microscraper.client.Client.MicroScraperClientException;
import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.database.schema.Default;
*/

import java.applet.Applet;

public class MicroScraperApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2768937336583253219L;
	/*public static void main(String[] args)
	{
	}*/
	/*private final SystemLogInterface log = new SystemLogInterface();
	private final Client client = Client.initialize(
			new ApacheBrowser(),
			new JavaUtilRegexInterface(),
			new JSONME(),
			new Interfaces.Logger[] { log }
		);*/
	
	public String scrape(String url) {
		return "different hello " + url;
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
