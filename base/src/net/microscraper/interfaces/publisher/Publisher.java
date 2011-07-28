package net.microscraper.interfaces.publisher;

import net.microscraper.Client;
import net.microscraper.executable.Executable;
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

}
