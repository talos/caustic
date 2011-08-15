package net.microscraper.instruction;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;


/**
 * {@link Result}s are {@link NameValuePair}s implementations with a {@link #getId()}.
 * @see NameValuePair
 * @author john
 *
 */
public class Result extends BasicNameValuePair {

	private final int id;
	
	public Result(int id, String name, String value)  {
		super(name, value);
		this.id = id;
	}
	
	/**
	 * 
	 * @return A unique <code>int</code> identifier for this {@link Result}.
	 */
	public int getId() {
		return id;
	}
}
