package net.microscraper.client.interfaces;

import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.FindManyExecutable;

/**
 * Implementations of {@link Publisher} receive updates of {@link Executable}s 
 * as the {@link Client} runs.
 * @see Executable
 * @see Client
 * @author john
 *
 */
public interface Publisher {
	public static final String RESOURCE_LOCATION = "resource_location";
	
	public static final String ID = "id";
	public static final String SOURCE_RESULT_ID = "source_result_id";
	public static final String EXECUTABLE_ID = "executable_id";
	
	public static final String STUCK_ON = "stuck_on";
	public static final String FAILURE_BECAUSE = "failure_because";
	
	public static final String NAME = "name";
	public static final String VALUE = "value";
	

	/**
	 * This can be called multiple times on a single {@link Executable}.
	 * {@link Executable}s <b>should not</b> be accessed outside of this method.
	 * @param executable The {@link Executable} instance that may, or may not,
	 * have changed.
	 * @throws PublisherException If the publisher has experienced an exception.
	 * @see FindManyExecutable
	 */
	public void publish(Executable executable) throws PublisherException;
	
	
	/**
	 * This can be called multiple times on a single {@link FindManyExecutable}.
	 * {@link FindManyExecutable}s <b>should not</b> be accessed outside of this method.
	 * @param executable The {@link FindManyExecutable} instance that may, or may not,
	 * have changed.
	 * @throws PublisherException If the publisher has experienced an exception.
	 * @see FindManyExecutable
	 */
	//public void publish(FindManyExecutable executable) throws PublisherException;
	
	/**
	 * This can be called multiple times on a single {@link FindOneExecutable}.
	 * {@link FindOneExecutable}s <b>should not</b> be accessed outside of this method.
	 * @param executable The {@link FindOneExecutable} instance that may, or may not,
	 * have changed.
	 * @throws PublisherException If the publisher has experienced an exception.
	 * @see FindManyExecutable
	 */
	//public void publish(FindOneExecutable findOneExecutable) throws PublisherException;
}
