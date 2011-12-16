package net.caustic.regexp;

import net.caustic.template.StringSubstitution;
import net.caustic.util.StringMap;

public abstract class StringTemplate {

	public static final String ENCODED_PATTERN = "\\{\\{([^\\{\\}]+)\\}\\}";
	public static final String UNENCODED_PATTERN = "\\{\\{\\{([^\\{\\}]+)\\}\\}\\}";

	/**
	 * Substitute the values from a {@link Variables} into the {@link StringTemplate}.
	 * @param db The {@link Database} to use when substituting.
	 * @param scope The {@link Scope} within <code>db</code>
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 */
	public abstract StringSubstitution sub(StringMap context);
	
	/**
	 * This method must be overriden.  It is used to ensure {@link StringTemplate}
	 * implementations have meaningful {@link #hashCode()} and {@link #equals(Object)}
	 * methods.
	 * @return a {@link String} representing this {@link StringTemplate}.
	 */
	protected abstract String asString();

	public final String toString() {
		return asString();
	}
	
	public final boolean equals(Object obj) {
		if(obj == this) {
			return true;
		} else if(obj instanceof StringTemplate) {
			StringTemplate other = (StringTemplate) obj;
			return other.asString().equals(this.asString());
		}
		return false;
	}
	
	public final int hashCode() {
		return asString().hashCode();
	}
}