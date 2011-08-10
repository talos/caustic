package net.microscraper;

/**
 * Class to hold and retrieve unencoded name value pairs.
 * Both {@link #getName()} and {@link #getValue()} can be <code>null</code>.
 * @author john
 *
 */
public final class BasicNameValuePair implements NameValuePair {
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
	/*
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(!(obj instanceof NameValuePair))
			return false;
		NameValuePair that = (NameValuePair) obj;
		try {
			return this.getValue().equals(that.getValue()) &&
					this.getValue().equals(that.getValue());
		} catch(NullPointerException e) {
			return false;
		}
	}*/
}
