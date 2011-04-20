package net.microscraper.client;

import net.microscraper.database.Execution;

public interface Publisher {
	public void publish(Execution execution) throws PublisherException;

	public class PublisherException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8677302071093634438L;
		
	}
}
