package net.microscraper.mustache;

/**
 * {@link MustacheSubstitution} results from an attempt to substitute
 * {@link Variables} in a {@link MustacheTemplate}.<p>
 * If the substitution was successful, the substituted text can be retrieved
 * by {@link #getSubbed()}.  Otherwise, the tag that was missing can be 
 * retrieved by {@link #getMissingTag()}.
 * @author talos
 *
 */
public class MustacheSubstitution {
	
	private final String text;
	private final boolean isSuccessful;
	private MustacheSubstitution(boolean isSuccessful, String text) {
		this.text = text;
		this.isSuccessful = isSuccessful;
	}
	
	/**
	 * @param substitutedText The successfully substituted text.
	 * @return A successful {@link MustacheSubstitution}.
	 */
	public static MustacheSubstitution success(String substitutedText) {
		return new MustacheSubstitution(true, substitutedText);
	}

	/**
	 * @param missingTag the {@link String} tag that prevented the 
	 * substitution from succeeding.
	 * @return A failed {@link MustacheSubstitution}.
	 */
	public static MustacheSubstitution fail(String missingTag) {
		return new MustacheSubstitution(false, missingTag);
	}
	
	/**
	 * 
	 * @return Whether this {@link MustacheSubstitution} is from a
	 * successful substitution in {@link MustacheTemplate}.
	 */
	public boolean isSuccessful() {
		return isSuccessful;
	}
	
	/**
	 * 
	 * @return The tag that caused an unsuccessful {@link MustacheSubstitution}.
	 * Should only be called if {@link #isSuccessful()} is <code>false</code>.
	 */
	public String getMissingTag() {
		if(isSuccessful == true) {
			throw new IllegalStateException("Template was successful.");
		}
		return text;
	}

	/**
	 * 
	 * @return The successfully substituted text of a {@link MustacheSubstitution}.
	 * Should only be called if {@link #isSuccessful()} is <code>true</code>.
	 */
	public String getSubbed() {
		if(isSuccessful == false) {
			throw new IllegalStateException("Template was not successful.");
		}
		return text;
	}
}
