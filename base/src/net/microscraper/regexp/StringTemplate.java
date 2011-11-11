package net.microscraper.regexp;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringSubstitution;

public interface StringTemplate {

	public static final String ENCODED_PATTERN = "\\{\\{([^\\{\\}]+)\\}\\}";
	public static final String UNENCODED_PATTERN = "\\{\\{\\{([^\\{\\}]+)\\}\\}\\}";

	/**
	 * Substitute the values from a {@link Variables} into the {@link StringTemplate}.
	 * @param input The {@link DatabaseView} to use when substituting.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 * @throws DatabaseReadException if <code>input</code> could not be read from.
	 */
	public abstract StringSubstitution sub(DatabaseView input)
			throws DatabaseReadException;

}