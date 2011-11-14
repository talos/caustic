package net.caustic.regexp;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.scope.Scope;
import net.caustic.template.StringSubstitution;

public interface StringTemplate {

	public static final String ENCODED_PATTERN = "\\{\\{([^\\{\\}]+)\\}\\}";
	public static final String UNENCODED_PATTERN = "\\{\\{\\{([^\\{\\}]+)\\}\\}\\}";

	/**
	 * Substitute the values from a {@link Variables} into the {@link StringTemplate}.
	 * @param db The {@link Database} to use when substituting.
	 * @param scope The {@link Scope} within <code>db</code>
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 * @throws DatabaseException if <code>input</code> could not be read from.
	 */
	public abstract StringSubstitution sub(Database db, Scope scope)
			throws DatabaseException;

}