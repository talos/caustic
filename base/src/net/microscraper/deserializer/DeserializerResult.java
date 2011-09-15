package net.microscraper.deserializer;

import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.util.Result;

/**
 * This class is a {@link Result} that holds {@link Instruction}s when
 * {@link #isSuccess()} is <code>true</code>.
 * @author realest
 *
 */
public class DeserializerResult implements Result {
	private Find find;
	private Load load;
	private Instruction[] children;
	private String[] missingTags;
	private String failedBecause;
	
	private DeserializerResult(Find find, Load load, Instruction[] children) {
		this.find = find;
		this.load = load;
		this.children = children;
	}
	
	private DeserializerResult(String[] missingTags) {
		this.missingTags = missingTags;
	}
	
	private DeserializerResult(String failedBecause) {
		this.failedBecause = failedBecause;
	}
	
	/**
	 * 
	 * @return The successfully deserialized {@link Load}.
	 */
	public Load getLoad() {
		return load;
	}
	
	/**
	 * 
	 * @return The successfully deserialized {@link Find}.
	 */
	public Find getFind() {
		return find;
	}
	
	/**
	 * 
	 * @return The successfully deserialized {@link Instruction}.
	 */
	public Instruction[] getChildren() {
		return children;
	}
	
	/**
	 * @return A successful {@link DeserializerResult}.
	 */
	public static DeserializerResult find(Find find, Instruction[] children) {
		return new DeserializerResult(find, null, children);
	}

	/**
	 * @return A successful {@link DeserializerResult}.
	 */
	public static DeserializerResult load(Load load, Instruction[] children) {
		return new DeserializerResult(null, load, children);
	}


	/**
	 * Obtain a {@link DeserializerResult} with missing tag information.
	 * @param missingTags A {@link String} array of the tags that prevented
	 * a successful deserialization.
	 * @return A {@link DeserializerResult} with missing tag information.
	 */
	public static DeserializerResult missingTags(String[] missingTags) {
		return new DeserializerResult(missingTags);
	}

	/**
	 * Obtain a {@link DeserializerResult} with failure information.
	 * @param failure A {@link String} describing why the deserializer failed,
	 * and should not be tried again.
	 * @return A {@link DeserializerResult} with failure information.
	 */
	public static DeserializerResult failure(String failedBecause) {
		return new DeserializerResult(failedBecause);
	}

	public boolean isMissingTags() {
		return missingTags != null;
	}

	public String[] getMissingTags() {
		return missingTags;
	}

	public String getFailedBecause() {
		return failedBecause;
	}
}
