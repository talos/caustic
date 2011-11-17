package net.caustic.deserializer;

import net.caustic.instruction.Instruction;
import net.caustic.util.Result;

/**
 * This class is a {@link Result} that holds {@link Instruction}s when
 * {@link #isSuccess()} is <code>true</code>.
 * @author realest
 *
 */
public class DeserializerResult implements Result {
	private Instruction instruction;
	private String[] missingTags;
	private String failedBecause;
	
	private DeserializerResult(Instruction instruction) {
		this.instruction = instruction;
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
	public Instruction getInstruction() {
		return instruction;
	}
	
	/**
	 * @return A successful {@link DeserializerResult}.
	 */
	public static DeserializerResult success(Instruction instruction) {
		return new DeserializerResult(instruction);
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
