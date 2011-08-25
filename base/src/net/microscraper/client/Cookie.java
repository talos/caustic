package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Class for simple creation of cookies, including only a URL,
 * name, and value.
 * @see #getUrl()
 * @see #getName()
 * @see #getValue()
 * @author realest
 *
 */
public class Cookie {
	private final String url;
	private final String name;
	private final String value;
	public Cookie(String url, String name, String value) {
		this.url = url;
		this.name = name;
		this.value = value;
	}
	
	/**
	 * 
	 * @return This {@link Cookie}'s URL as a {@link String}.
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * 
	 * @return This {@link Cookie}'s name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return This {@link Cookie}'s value.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Create an array of {@link Cookie}s from a {@link Hashtable}.  The
	 * <code>table</code> maps cookie names to cookie values.
	 * @param url The {@link String} url to use for all the resulting
	 * {@link Cookie}s.
	 * @param table A {@link Hashtable} mapping cookie names to cookie values.
	 * @return A {@link Cookie} array the same length as the size of <code>
	 * table</code>.
	 */
	public static Cookie[] fromHashtable(String url, Hashtable table) {
		Cookie[] result = new Cookie[table.size()];
		int i = 0;
		Enumeration keys = table.keys();
		try {
			while(keys.hasMoreElements()) {
				String name = (String) keys.nextElement();
				String value = (String) table.get(name);
				result[i] = new Cookie(url, name, value);
				i++;
			}
		} catch(ClassCastException e) {
			throw new IllegalArgumentException("Hashtable used to create cookies" +
					" must only contain String keys and values.");
		}
		return result;
	}
}
