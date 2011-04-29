package net.microscraper.client;

import net.microscraper.database.Execution;

public interface Publisher {
	public static final String ID = "id";
	public static final String SOURCE_ID = "source_id";
	public static final String STATUS_STRING = "status";
	public static final String STATUS_CODE = "status_code";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String MISSING_VARIABLES = "missing_variables";
	public static final String ERRORS = "errors";
	
	public void publish(Execution execution) throws PublisherException;

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
