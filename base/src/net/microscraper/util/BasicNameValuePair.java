package net.microscraper.util;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * Both {@link #getName()} and {@link #getValue()} can be <code>null</code>.
 * @author john
 *
 */
public class BasicNameValuePair implements NameValuePair {
	private final String name;
	private final String value;
	
	/**
	 * Create a new {@link BasicNameValuePair}.
	 * @param name The {@link String} value for {@link #getName()}.
	 * Can be <code>null</code>.
	 * @param valueThe {@link String} value for {@link #getValue()}.
	 * Can be <code>null</code>.
	 */
	public BasicNameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	/**
	 * Preview an array of {@link NameValuePair}s as a {@link String}.
	 * @param nameValuePairs The {@link NameValuePair}s to preview.
	 * @return A {@link String} previewing <code>nameValuePairs</code>.
	 */
	public static String preview(NameValuePair[] nameValuePairs) {
		String[] joined = new String[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			joined[i] = nameValuePairs[i].getName() + ": " + nameValuePairs[i].getValue();
		}
		return StringUtils.join(joined, ", ");
	}
}
