package net.microscraper.client;

import net.microscraper.resources.Scraper;
import net.microscraper.resources.Status;


public interface Publisher {
	public static final String ID = "id";
	public static final String SOURCE_ID = "source_id";
	public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String VALUE = "value";
	
	public static final String SUCCESS = "success";
	public static final String DELAY = "delay";
	public static final String FAILURE = "failure";
	
	public void publish(Scraper execution, Status status) throws PublisherException;
	
	public class PublisherException extends Exception {

		public PublisherException(Throwable e) {
			super(e);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -8677302071093634438L;
		
	}
}
