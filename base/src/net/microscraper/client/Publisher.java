package net.microscraper.client;

public interface Publisher {
	public void publish(AbstractResult[] result) throws PublisherException;
	
	public class PublisherException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8677302071093634438L;
		
	}
}
