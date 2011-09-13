package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.microscraper.client.Scraper;

public class MainClass {

	/**
	 * 
	 * @param objToPrint The object to print to the console.
	 */
	private static void println(Object objToPrint) {
		System.out.print(objToPrint.toString());
		println();
	}
	
	/**
	 * Print blank line to console.
	 */
	private static void println() {
		System.out.println();
	}
	
	public static void main(String... args) {
		try {
			Console console = new Console(args);
			Runtime.getRuntime().addShutdownHook(console.getShutdownThread());
			/*
			String statusLine = console.execute();
			println(statusLine);*/
			console.execute();
		} catch(InvalidOptionException e) {
			println(e.getMessage());
			println();
			println(ConsoleOptions.USAGE);
		} catch(UnsupportedEncodingException e) {
			println("Your computer does not support the required encoding: " + e.getMessage());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate a readout of the relative success of the {@link Scraper}s' executions.
	 * @param successful the number of successful executions.
	 * @param stuck the number of executions that got stuck.
	 * @param failed the number of failed executions.
	 * @return A {@link String} summary.
	 */
	public String statusLine(int successful, int stuck, int failed) {
		return successful + " successful instructions, " + stuck + " stuck instructions, " + failed +
				" failed instructions.";
	}
}
