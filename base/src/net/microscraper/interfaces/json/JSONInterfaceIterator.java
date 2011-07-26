package net.microscraper.interfaces.json;

/**
 * An iterator interface for the keys of a {@link JSONInterfaceObject}.
 * @author realest
 * @see JSONInterfaceObject
 * @see JSONInterfaceObject#keys()
 *
 */
public interface JSONInterfaceIterator {
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