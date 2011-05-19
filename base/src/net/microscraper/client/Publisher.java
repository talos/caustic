package net.microscraper.client;

import net.microscraper.execution.Execution;

public interface Publisher {
	public static final String RESOURCE_LOCATION = "resource_location";
	public static final String ID = "id";
	public static final String SOURCE_ID = "source_id";
	//public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String VALUE = "value";
	
	public static final String COMPLETE = "complete";
	public static final String STUCK = "stuck";
	public static final String FAILURE = "failure";
	
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
