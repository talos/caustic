package net.microscraper;

import java.util.Hashtable;

/**
 * An implementation of {@link Variables} optionally initialized with
 * several {@link NameValuePair}s.
 * @author realest
 *
 */
public class BasicVariables implements Variables {
	
	private final Hashtable defaults = new Hashtable();
	//private Variables extendedVariables = null;
	
	/**
	 * Initialize {@link BasicVariables} without {@link NameValuePairs}s.
	 */
	public BasicVariables() { }
	
	/**
	 * 
	 * @param nameValuePairs an array of {@link NameValuePair}s that will be in the 
	 * {@link BasicVariables} instance.
	 */
	public BasicVariables(NameValuePair[] nameValuePairs) {
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			defaults.put(nameValuePairs[i].getName(), nameValuePairs[i].getValue());
		}
	}
	
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
