package net.microscraper.impl.commandline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.microscraper.client.MicroscraperException;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.SQLConnectionException;

public class MicroScraperConsole {
	public static void main (String[] stringArgs) {
		Arguments arguments = getArguments(stringArgs);
		if(arguments != null) {
			ArgumentsMicroscraper scraper = getScraper(arguments);
			if(scraper != null) {
				scrape(scraper);
			}
		}
	}
	
	private static Arguments getArguments(String[] stringArgs) {
		try {
			return new Arguments(stringArgs);
		} catch(IllegalArgumentException e) {
			print(e.getMessage());
			print(Arguments.USAGE);
			return null;
		}
	}
	
	private static ArgumentsMicroscraper getScraper(Arguments arguments) {	
		try {
			return new ArgumentsMicroscraper(arguments);
		} catch(FileNotFoundException e) {
			print("Could not find the input file: " + e.getMessage());
		} catch(SQLConnectionException e) {
			print("Could not open connection to SQL: " + e.getMessage());
		} catch(DatabaseException e) {
			print("Could not set up database: " + e.getMessage());
		} catch(UnsupportedEncodingException e) {
			print("Unsupported encoding: " + e.getMessage());
		} catch(IOException e) {
			print("Could not open log file: " + e.getMessage());
		} catch(Throwable e) {
			print("Unhandled exception scraping: " + e.getClass().getName());
			print("Stack trace is in the log.");
		}
		return null;
	}
	
	private static void scrape(ArgumentsMicroscraper scraper) {
		try {
			scraper.scrape();
		} catch(MicroscraperException e) {
			print("Error scraping: " + e.getMessage());
		} catch(IOException e) {
			print("Error reading input file or writing to output file (log or output): " + e.getMessage());
		}
	}

	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}