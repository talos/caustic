package net.microscraper.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.microscraper.Microscraper;
import net.microscraper.MicroscraperException;
import net.microscraper.impl.database.SQLConnectionException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.uri.URIInterfaceException;

public class MicroScraperConsole {
	public static void main (String[] args) {
		try {
			Map<String, String> arguments = CommandLine.getArguments(args);
			Database database = CommandLine.getDatabase(arguments);
			Microscraper scraper = CommandLine.getScraper(arguments, database);
			
			try {
				CommandLine.runScraper(arguments, scraper);
			} catch(MicroscraperException e) {
				print("Error scraping: " + e.getMessage());
			} catch(IOException e) {
				print("Error reading input file or writing to output file (log or output): " + e.getMessage());
			}
		} catch(IllegalArgumentException e) {
			// Error with args provided
			print(e.getMessage());
			print(CommandLine.USAGE);
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
			
			//client.e(e);
			StackTraceElement[] trace = e.getStackTrace();
			for(int i = 0 ; i < trace.length ; i ++) {
				//client.i(trace[i].toString());
			}
		}
		/*(try {
			//finish();
		} catch(IOException e) {
			print("Could not close file: " + e.getMessage());
		}*/
	}

	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}