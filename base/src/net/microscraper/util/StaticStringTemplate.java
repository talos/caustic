package net.microscraper.util;

import net.microscraper.database.DatabaseView;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.template.StringSubstitution;

/**
 * A {@link StringTemplate} interface that will always return its initialized
 * {@link String} value within the {@link StringSubstitution} it returns from
 * the {@link #sub(DatabaseView)} method.
 * @author talos
 *
 */
public class StaticStringTemplate implements StringTemplate {
	private final String staticValue;
	
	/**
	 * 
	 * @param staticValue The {@link String} that will always be contained in
	 * the {@link StringSubstitution} returned by {@link #sub(DatabaseView)}.
	 */
	public StaticStringTemplate(String staticValue) {
		this.staticValue = staticValue;
	}
	
	/**
	 * Always returns a successful {@link StringSubstitution} containing 
	 * the {@link String} this {@link StaticStringTemplate} was
	 * initialized with.
	 */
	public StringSubstitution sub(DatabaseView input) {
		return StringSubstitution.success(staticValue);
	}

}