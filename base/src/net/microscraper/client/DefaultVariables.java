package net.microscraper.client;

import java.util.Hashtable;

/**
 * An implementation of {@link Variables} optionally initialized with
 * several {@link NameValuePair}s.
 * @author realest
 *
 */
public class DefaultVariables implements Variables {
	
	private final Hashtable defaults = new Hashtable();
	//private Variables extendedVariables = null;
	
	/**
	 * Initialize {@link DefaultVariables} without {@link NameValuePairs}s.
	 */
	public DefaultVariables() { }
	
	/**
	 * 
	 * @param nameValuePairs an array of {@link NameValuePair}s that will be in the 
	 * {@link DefaultVariables} instance.
	 */
	public DefaultVariables(NameValuePair[] nameValuePairs) {
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			defaults.put(nameValuePairs[i].getName(), nameValuePairs[i].getValue());
		}
	}
	
	/**
	 * 
	 * @param extendedVariables A {@link Variables} instance that will be checked if there are no
	 * matches found in this one.
	 */
	/*public DefaultVariables(Variables extendedVariables) {
		this.extendedVariables = extendedVariables;
	}*/
	
	public String get(String key) throws MissingVariableException {
		Object value = defaults.get(key);
		/*if(extendedVariables != null) {
			return extendedVariables.get(key);
		}*/
		if(value == null) {
			throw new MissingVariableException(this, key);
		}
		return (String) value;
	}

	public boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
	}
}
