package net.microscraper.util;

/**
 * An interface for a unique identifier.
 * @author realest
 *
 */
public interface UUID {
	
	/**
	 * 
	 * @return The {@link UUID} expressed as an integer.
	 */
	public int asInt();
	
	/**
	 * 
	 * @return The {@link UUID} expressed as a {@link String}.
	 */
	public String asString();
}
