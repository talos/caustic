package net.microscraper.client;

import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;

public interface Publisher {
	public static final String ID = "id";
	public static final String SOURCE_ID = "source_id";
	public static final String STATUS_STRING = "status";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	
	public static final String SUCCESSFUL = "successful";
	public static final String DELAY = "delay";
	public static final String FAILURE = "failure";
	
	public void publish(Execution execution, String result) throws PublisherException;
	public void publish(Execution execution, ExecutionDelay delay) throws PublisherException;
	public void publish(Execution execution, ExecutionFailure failure) throws PublisherException;

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
