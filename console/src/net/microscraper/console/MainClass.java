package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
			
			console.open();
			console.execute();
		} catch(InvalidOptionException e) {
			println(e.getMessage());
			println();
			println(ConsoleOptions.USAGE);
		} catch(UnsupportedEncodingException e) {
			println("Your computer does not support the required encoding: " + e.getMessage());
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			println("Interrupt");
		}
	}
}
