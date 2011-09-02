package net.microscraper.console;

import java.util.HashMap;
import java.util.Map;

import net.microscraper.util.StringUtils;

public class Option  {
	private static final Map<String, Option> validOptions = new HashMap<String, Option>();

	private static final String PREPEND = "--";
	//private final String defaultValue;
	private final String name;
	private String value;
	private Option(String nonPrependedName, String defaultValue) {
		this.name = PREPEND + nonPrependedName;
		//this.defaultValue = defaultValue;
		this.value = defaultValue;
		validOptions.put(this.name, this);
	}
	private static boolean exists(String name) {
		return validOptions.containsKey(name);
	}
	public static Option withoutDefault(String nonPrependedName) {
		return new Option(nonPrependedName, null);
	}
	public static Option withDefault(String nonPrependedName, String defaultValue) {
		return new Option(nonPrependedName, defaultValue);
	}
	public static Option retrieve(String name) throws InvalidOptionException {
		if(exists(name)) {
			return validOptions.get(name);
		} else {
			throw new InvalidOptionException(name + " is not a valid option.");
		}
	}
	public void define(String value) {
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
}