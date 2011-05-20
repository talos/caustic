package net.microscraper.client.utility;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Log;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.JDBCSQLite;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.impl.SQLPublisher;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.execution.Context;
import net.microscraper.model.URIMustBeAbsoluteException;

public class MicroScraperConsole {
	private static final SimpleDateFormat DATETIME_FORMAT =
		new SimpleDateFormat("yyyyMMddkkmmss");
	private static final String ENCODING = "UTF-8";
	private final Log log = new Log();
	private final Interfaces.JSON jsonInterface = new JSONME();
	public static void main (String[] args) {
		new MicroScraperConsole(args);
	}
	public MicroScraperConsole(String[] args) {
		if(args.length > 2 || args.length < 1) {
			log.i("Proper use: microscraperconsole <url> [<defaults>]");
		} else {
			Logger logger = new SystemLogInterface();
			log.register(logger);
			try {
				Client client = new Client(
						new Context(
							new LocalJSONResourceLoader(jsonInterface),
							new JavaUtilRegexInterface(),
							jsonInterface, new JavaNetBrowser(log),
							logger,
							ENCODING),
						new SQLPublisher(new JDBCSQLite("./" + DATETIME_FORMAT.format(new Date()) + ".sqlite", logger))
				);
				
				URI uri = new URI(args[0]);
				UnencodedNameValuePair[] extraVariables;
				if(args.length == 2) {
					extraVariables = Utils.formEncodedDataToNameValuePairs(args[1], ENCODING);
				} else {
					extraVariables = new UnencodedNameValuePair[0];
				}
				client.scrape(uri, extraVariables);
			} catch (URIMustBeAbsoluteException e) {
				log.e(e);
			} catch (URISyntaxException e) {
				log.e(e);
			} catch (UnsupportedEncodingException e) {
				log.e(e);
			} catch (SQLInterfaceException e ) {
				log.e(e);
			}
		}
	}
}