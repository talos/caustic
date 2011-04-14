package net.microscraper.client;

import net.microscraper.database.Result;

public interface Publisher {
	public void publish(Result result) throws PublisherException;
	
	public class PublisherException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8677302071093634438L;
		
	}
}
