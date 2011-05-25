package net.microscraper.client.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.microscraper.client.Client;
import net.microscraper.client.ClientException;
import net.microscraper.client.Log;
import net.microscraper.client.NameValuePair;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.FileLogInterface;
import net.microscraper.client.impl.JDBCSQLite;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaNetInterface;
import net.microscraper.client.impl.JavaNetURI;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.impl.SQLPublisher;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.Logger;
import net.microscraper.client.interfaces.NetInterfaceException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.client.interfaces.URILoader;

public class MicroScraperConsole {
	private static final SimpleDateFormat DATETIME_FORMAT =
		new SimpleDateFormat("yyyyMMddkkmmss");
	private static final String ENCODING = "UTF-8";
	private static final int sqlBatchSize = 400;
	
	private final Log log = new Log();
	private final URILoader uriLoader = new LocalURILoader();
	private final JSONInterface jsonInterface = new JSONME(uriLoader);
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
			client = new Client(new JavaUtilRegexInterface(),
					new JavaNetBrowser(log, Browser.MAX_KBPS_FROM_HOST),
					log,
					new JavaNetInterface(),
					jsonInterface, ENCODING);
					
			URIInterface uri = new JavaNetURI(args[0]);
			NameValuePair[] extraVariables;
			if(args.length == 2) {
				extraVariables = Utils.formEncodedDataToNameValuePairs(args[1], ENCODING);
			} else {
				extraVariables = new NameValuePair[0];
			}
			client.scrape(uri, extraVariables, publisher);
			
			publisher.forceCommit();
			
			try {
				fileLogger.close();
			} catch (IOException e) {
				sysLogger.e(e);
			}
		} catch (UnsupportedEncodingException e) {
			log.e(e);
		} catch (SQLInterfaceException e ) {
			log.e(e);
		} catch (IOException e) {
			log.e(e);
		} catch (ClientException e) {
			log.e(e);
		} catch (NetInterfaceException e) {
			log.e(e);
		}
	}
}