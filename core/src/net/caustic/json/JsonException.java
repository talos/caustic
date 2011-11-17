package net.caustic.json;

/**
 * {@link JsonException} is thrown when something has gone wrong
 * with a {@link JsonParser}.
 * @see JsonParser
 * @author realest
 *
 */
public class JsonException extends Exception {
	private static final long serialVersionUID = 1L;
	public JsonException(String message ) {super(message); }
	public JsonException(Throwable e ) {super(e); }
}