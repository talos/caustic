package net.microscraper;

/**
 * A simple interface for name-value pairs.
 * @author john
 * @see #getName
 * @see #getValue
 */
public interface NameValuePair {
	
	/**
	 * 
	 * @return The {@link String} <b>name</b> of this {@link NameValuePair}.
	 */
	public String getName();
	

	/**
	 * 
	 * @return The {@link String} <b>value</b> of this {@link NameValuePair}.
	 */
	public String getValue();
}
