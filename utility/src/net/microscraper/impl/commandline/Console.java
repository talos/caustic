package net.microscraper.impl.commandline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.microscraper.client.DeserializationException;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.uri.MalformedUriException;

public class Console {
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
		}
		return null;
	}
	
	private static ArgumentsMicroscraper getScraper(Arguments arguments) {	
		try {
			return new ArgumentsMicroscraper(arguments);
		} catch(IllegalArgumentException e) {
			print(e.getMessage());
			print(Arguments.USAGE);
		} catch(FileNotFoundException e) {
			print("Could not find the input file: " + e.getMessage());
		} catch(SQLConnectionException e) {
			print("Could not open connection to SQL: " + e.getMessage());
		} catch(UnsupportedEncodingException e) {
			print("Unsupported encoding: " + e.getMessage());
		} catch(IOException e) {
			print("Could not open log file: " + e.getMessage());
		} 
		return null;
	}
	
	private static void scrape(ArgumentsMicroscraper scraper) {
		try {
			scraper.scrape();
		} catch (MalformedUriException e) {
			print("Bad reference in Instruction: " + e.getMessage());
		} catch(IOException e) {
			print("IO problem: " + e.getMessage());
		} catch (InterruptedException e) {
			print("User interrupt.");
		} catch (DeserializationException e) {
			print("Could not deserialize Instruction: " + e.getMessage());
		}
	}

	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}