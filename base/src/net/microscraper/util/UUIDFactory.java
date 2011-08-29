package net.microscraper.util;


/**
 * Factory interface to generate {@link UUID}s.
 * @author realest
 *
 */
public interface UUIDFactory {
	
	/**
	 * 
	 * @return A new {@link UUID}.
	 */
	public UUID get();
}
