package net.microscraper.client.utility;

import java.text.DateFormat;

import net.microscraper.client.Client;
import net.microscraper.client.Client.MicroScraperClientException;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Publisher;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JDBCSQLite;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.impl.SQLPublisher;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Default;

public class MicroScraperConsole {
	private static final String ENCODING = "UTF-8";
	private final SystemLogInterface log = new SystemLogInterface();
	private final Client client = Client.initialize(
			new ApacheBrowser(ApacheBrowser.DO_NOT_USE_CACHE),
			new JavaUtilRegexInterface(),
			new JSONME(),
			new Interfaces.Logger[] { log }
		);
	public static void main (String[] args) {
		new MicroScraperConsole(args);
	}
	public MicroScraperConsole(String[] args) {
		if(args.length < 4) {
			client.log.i("Proper use: microscraperconsole <url> <model> <resource> <defaults>");
		} else {
			try {
				String url = args[0];
				String model = args[1];
				Reference resource_ref = new Reference(args[2]);
				Default[] defaults = Default.fromFormParams(args[3], ENCODING);
				
				Publisher publisher = new SQLPublisher(new JDBCSQLite("./" + DateFormat.getTimeInstance() + ".sqlite", client.log));
				/*Default[] defaults = new Default[] {
					new Default("Borough_Number", "3"),
					new Default("House_Number", "373"),
					new Default("Street_Name", "Atlantic Ave"),
					new Default("Apartment_Number", "")
				};*/
				client.scrape(url, model, resource_ref, defaults, publisher);
				client.log.i("Finished execution.");
			} catch (MicroScraperClientException e) {
				client.log.e(e);
			} catch (SQLInterfaceException e) {
				client.log.e(e);
			}
		}
	}
}