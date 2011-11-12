package net.caustic.template;

import net.caustic.regexp.StringTemplate;

/**
 * This {@link Exception} is thrown when the successful substitution of a tag in a {@link HashtableTemplate}
 * would cause the overwriting of a preexisting key.<p>
 * This would be thrown if a {@link HashtableTemplate} with this mapping:<p>
 * <code>
 * "key": "abc"<br>
 * "{{tag}}": "def"
 * </code><p>
 * Were substituted within a {@link Database} {@link Scope} where <code>tag</code> mapped to
 * "key".  The exception is necessary to prevent the value "def" from overwriting "abc".
 * @author talos
 *
 */
public class HashtableSubstitutionOverwriteException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2019155788174394914L;

	/**
	 * 
	 * @param overwrittenKey The {@link String} key that would be overwritten.
	 * @param overwritingTemplate The {@link StringTemplate} that would overwrite <code>overwrittenKey</code>.
	 */
	public HashtableSubstitutionOverwriteException(String overwrittenKey, StringTemplate overwritingTemplate) {
		super("Hashtable substitution of " + overwritingTemplate.toString()
				+ " caused key that ovewrites " + overwrittenKey);
	}
}
