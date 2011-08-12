package net.microscraper.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.Microscraper;
import net.microscraper.MicroscraperException;
import net.microscraper.Utils;
import net.microscraper.database.impl.DelimitedConnection;
import net.microscraper.impl.database.JDBCSqliteConnection;
import net.microscraper.impl.database.MultiTableDatabase;
import net.microscraper.impl.database.SQLConnectionException;
import net.microscraper.impl.database.SingleTableDatabase;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.IOConnection;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.uri.URIInterfaceException;

public class MicroScraperConsole {
	public static void main (String[] args) {
		try {
			initialize(args);
			try {
				scrape();
			} catch(MicroscraperException e) {
				print("Error scraping: " + e.getMessage());
			} catch(IOException e) {
				print("Error reading input file or writing to output file (log or output): " + e.getMessage());
			}
		} catch(IllegalArgumentException e) {
			// Error with args provided
			print(e.getMessage());
			print(usage);
		} catch(FileNotFoundException e) {
			print("Could not find the input file: " + e.getMessage());
		} catch(SQLConnectionException e) {
			print("Could not open connection to SQL: " + e.getMessage());
		} catch(DatabaseException e) {
			print("Could not set up database: " + e.getMessage());
		} catch(UnsupportedEncodingException e) {
			print("Unsupported encoding: " + e.getMessage());
		} catch(URIInterfaceException e) {
			print("Could not locate instructions: " + e.getMessage());
		} catch(IOException e) {
			print("Could not open log file: " + e.getMessage());
		} catch(Throwable e) {
			print("Unhandled exception scraping: " + e.getClass().getName());
			print("Stack trace is in the log.");
			
			client.e(e);
			StackTraceElement[] trace = e.getStackTrace();
			for(int i = 0 ; i < trace.length ; i ++) {
				client.i(trace[i].toString());
			}
		}
		try {
			finish();
		} catch(IOException e) {
			print("Could not close file: " + e.getMessage());
		}
	}
	
}