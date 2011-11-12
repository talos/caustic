package net.caustic.console;

import java.util.HashMap;
import java.util.Map;

public class Option  {
	private static final Map<String, Option> validOptions = new HashMap<String, Option>();
	
	private final String defaultValue;
	private final String name;
	private String value;
	private Option(String name, String defaultValue) {
		this.name = name;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		validOptions.put(this.name, this);
	}
	public static Option withoutDefault(String prependedName) {
		return new Option(prependedName, null);
	}
	public static Option withDefault(String prependedName, String defaultValue) {
		return new Option(prependedName, defaultValue);
	}
	public static Option retrieve(String name) throws InvalidOptionException {
		if(validOptions.containsKey(name)) {
			return validOptions.get(name);
		} else {
			throw new InvalidOptionException(name + " is not a valid option.");
		}
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * 
	 * @return The {@link String} value assigned to this {@link Option}.
	 * Is <code>null</code> if none has been assigned and there is no
	 * default.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * 
	 * @return The {@link String} name of this {@link Option}.
	 */
	public Object getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The {@link String} default value for this {@link Option},
	 * or <code>null</code> if none was defined.
	 */
	public String getDefault() {
		return defaultValue;
	}
}