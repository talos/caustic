package net.microscraper.client.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.microscraper.client.Browser;
import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Model;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Variables;
import net.microscraper.client.impl.JDBCSQLite;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.impl.SQLPublisher;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.model.Reference;

public class MicroScraperConsole {
	private static final SimpleDateFormat DATETIME_FORMAT =
		new SimpleDateFormat("yyyyMMddkkmmss");
	private static final String ENCODING = "UTF-8";
	private final Interfaces.Logger log = new SystemLogInterface();
	public static void main (String[] args) {
		new MicroScraperConsole(args);
	}
	public MicroScraperConsole(String[] args) {
		if(args.length > 2 || args.length < 1) {
			log.i("Proper use: microscraperconsole <url> [<defaults>]");
		} else {
			try {
				Browser browser = new JavaNetBrowser();
				Interfaces.JSON jsonInterface = new JSONME();
				Client client = new Client(
					browser,
					new JavaUtilRegexInterface(),
					jsonInterface,
					new Interfaces.Logger[] { log },
					new SQLPublisher(
						new JDBCSQLite("./" + DATETIME_FORMAT.format(new Date()) + ".sqlite", log))
				);
				String url = args[0];
				
				String[] urlSplit = url.split("/");
				Reference resource_ref = new Reference(
						Model.get(urlSplit[urlSplit.length - 3]),
						urlSplit[urlSplit.length - 2],
						urlSplit[urlSplit.length - 1]);
				
				Variables variables;
				if(args.length > 1) {
					variables = Variables.fromFormParams(args[1], ENCODING);
				} else {
					variables = new Variables();
				}
				Interfaces.JSON.Object jsonObject = browser.loadJSON(url, jsonInterface);
				client.scrape(jsonObject, resource_ref, variables);
				log.i("Finished execution.");
			} catch (SQLInterfaceException e) {
				log.e(e);
			} catch (InterruptedException e) {
				log.e(e);
			} catch (BrowserException e) {
				log.e(e.getCause());
			} catch (JSONInterfaceException e) {
				log.e(e);
			}
		}
	}
}