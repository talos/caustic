package net.microscraper.client.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.microscraper.client.Browser;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Log;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.FileLogInterface;
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
	private static final int sqlBatchSize = 400;
	
	private final Log log = new Log();
	private final Interfaces.JSON jsonInterface = new JSONME();
	private SQLPublisher publisher;
	private Client client;
	
	public static void main (String[] args) {
		new MicroScraperConsole(args);
	}
	public MicroScraperConsole(String[] args) {
		Logger sysLogger = new SystemLogInterface();
		FileLogInterface fileLogger;
		String fileTimestamp = DATETIME_FORMAT.format(new Date());
		log.register(sysLogger);
		try {
			fileLogger = new FileLogInterface("./" + fileTimestamp + ".log");
			log.register(fileLogger);

			if(args.length > 2 || args.length < 1) {
				log.i("Proper use: microscraperconsole <url> [<defaults>]");
				return;
			}
			
			fileLogger.open();
			publisher = new SQLPublisher(
					new JDBCSQLite("./" + fileTimestamp + ".sqlite",
							log), sqlBatchSize);
			client = new Client(
						new Context(
							new LocalJSONResourceLoader(jsonInterface),
							new JavaUtilRegexInterface(),
							jsonInterface,
							new JavaNetBrowser(log, Browser.MAX_KBPS_FROM_HOST),
							log,
							ENCODING
						),
					publisher
				);
			
			URI uri = new URI(args[0]);
			UnencodedNameValuePair[] extraVariables;
			if(args.length == 2) {
				extraVariables = Utils.formEncodedDataToNameValuePairs(args[1], ENCODING);
			} else {
				extraVariables = new UnencodedNameValuePair[0];
			}
			client.scrape(uri, extraVariables);
			
			publisher.forceCommit();
			
			try {
				fileLogger.close();
			} catch (IOException e) {
				sysLogger.e(e);
			}
		} catch (URIMustBeAbsoluteException e) {
			log.e(e);
		} catch (URISyntaxException e) {
			log.e(e);
		} catch (UnsupportedEncodingException e) {
			log.e(e);
		} catch (SQLInterfaceException e ) {
			log.e(e);
		} catch (IOException e) {
			log.e(e);
		}
	}
}