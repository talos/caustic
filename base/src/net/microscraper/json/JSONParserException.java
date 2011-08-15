package net.microscraper.json;

import net.microscraper.client.MicroscraperException;

/**
 * {@link JSONParserException} is thrown when something has gone wrong
 * with a {@link JSONParser}.
 * @see MicroscraperException
 * @see JSONParser
 * @author realest
 *
 */
public class JSONParserException extends MicroscraperException {
	private static final long serialVersionUID = 1L;
	public JSONParserException(String message ) {super(message); }
	public JSONParserException(Throwable e ) {super(e); }
}