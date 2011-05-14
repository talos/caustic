package net.microscraper.client;

/**
 * Class to hold name-value pairs.  URLEncodes upon instantiation.
 * @author john
 *
 */
public interface NameValuePair {
	public abstract String getName();
	public abstract String getValue();
}
