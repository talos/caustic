package net.microscraper.client.utility;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.impl.ApacheBrowser;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SystemLogInterface;

//import java.io.*;

public class MicroScraperConsole {
	private static Client client = Client.initialize(
		new ApacheBrowser(),
		new JavaUtilRegexInterface(),
		new JSONME(),
		new Interfaces.Logger[] {
			new SystemLogInterface()
		}
	);
	public static void main (String[] args) {
		
	}
		/*
	   public static void main (String[] args) {
	
	      //  prompt the user to enter their name
	  //System.out.print("Enter your name: ");
	
	  //  open up standard input
	  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	  String userName = null;
	
	  //  read the username from the command-line; need to use try/catch with the
	  //  readLine() method
	  try {
	     userName = br.readLine();
	  } catch (IOException e) {
	     System.out.println("IO error trying to read your name!");
	     System.exit(1);
	  }
	
	  System.out.println("Thanks for the name, " + userName);
	}*/
}