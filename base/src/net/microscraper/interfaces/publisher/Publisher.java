package net.microscraper.interfaces.publisher;

import net.microscraper.Client;
import net.microscraper.executable.Executable;
import net.microscraper.executable.FindManyExecutable;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * Implementations of {@link Publisher} receive updates of {@link Executable}s 
 * as the {@link Client} runs.
 * @see Executable
 * @see Client
 * @author john
 *
 */
public interface Publisher {


	/*
	 * This can be called multiple times on a single {@link Executable}.
	 * {@link Executable}s <b>should not</b> be accessed outside of this method.
	 * @param executable The {@link Executable} instance that may, or may not,
	 * have changed.
	 * @throws PublisherException If the publisher has experienced an exception.
	 * @see FindManyExecutable
	 */
	//public void publish(Executable executable) throws PublisherException;
	
	
	/*
	 * @param sourceResourceLocation A String describing where the source {@link Resource} is.
	 * @param sourceResultNumber The <b>resultNumber</b> of the source {@link Result}.
	 * @param resourceLocation A String describing where the {@link Resource} is.
	 * @param stuckOn The name of the tag that could not be found in {@link Variables}.
	 * @throws PublisherException If the publisher has experienced an {@link Exception}.
	 */
	//public void publishStuck(String sourceResourceLocation, int sourceTryNumber,
	//		String resourceLocation, String stuckOn) throws PublisherException;
	
	/*
	 * @param sourceResultId The unique <code>int</code> ID of the {@link Result} that was the source.
	 * @param executableId The <code>int</code> ID of the {@link Executable} that is being published.
	 * @param failureBecause The {@link Throwable} that caused the {@link Executable} to fail.
	 * @throws PublisherException If the publisher has experienced an {@link Exception}.
	 */
	//public void publishFailure(String sourceResourceLocation, int sourceTryNumber,
	//		String resourceLocation, Throwable failureBecause) throws PublisherException;
	
	/*
	 * @param sourceResultId The unique <code>int</code> ID of the {@link Result} that was the source.
	 * @param executableId The <code>int</code> ID of the {@link Executable} that is being published.
	 * @param results An array of {@link Result}s.
	 * @throws PublisherException If the publisher has experienced an {@link Exception}.
	 */
	
	/**
	 * Publish a {@link Result} or equivalent.
	 * @param name The {@link Result}'s name.  Can be <code>null</code>.
	 * @param value The {@link Result}'s value.  Cannot be <code>null</code>.
	 * @param uri The {@link JSONLocation} where instructions
	 * for {@link Result} are located.  Cannot be <code>null</code>.
	 * @param number How many times thus far a {@link Result} has been generated from the <b>uri</b>.
	 * Provides a unique identifier when combined with <b>uri</b>.  Cannot be <code>null</code>
	 * @param sourceUri The {@link JSONLocation} where instructions for
	 * the {@link Result} that was the source for this
	 * {@link Result} are located.  Is <code>null</code> if there was no source.
	 * @param sourceNumber The <b>number</b> of the source {@link Result}.  Is <code>null</code> if
	 * there was no source.  Can identify the source along with <b>sourceUri</b>.
	 * @throws PublisherException If the publisher has experienced an {@link Exception}.
	 * @see {@link Result#publishTo}.
	 */
	public void publishResult(String name, String value,
			JSONLocation uri, int number, JSONLocation sourceUri, Integer sourceNumber)
					throws PublisherException;

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
