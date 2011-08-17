package net.microscraper.json;

/**
 * An iterator interface for the {@link String} keys of a {@link JsonObject}.
 * @author realest
 * @see JsonObject
 * @see JsonObject#keys()
 *
 */
public interface JsonIterator {
	/**
	 * 
	 * @return <code>true</code> if there is another key, <code>false</code> otherwise.
	 */
	public abstract boolean hasNext();
	
	/**
	 * 
	 * @return The next {@link String} key.
	 */
	public abstract String next();
}