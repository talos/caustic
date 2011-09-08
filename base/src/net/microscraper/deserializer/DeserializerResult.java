package net.microscraper.deserializer;

import net.microscraper.instruction.Instruction;
import net.microscraper.util.Result;

/**
 * This class is a {@link Result} that holds {@link Instruction}s when
 * {@link #isSuccess()} is <code>true</code>.
 * @author realest
 *
 */
public class DeserializerResult extends Result {
	
	private DeserializerResult(Instruction deserializedInstruction) {
		super(deserializedInstruction);
	}
	
	private DeserializerResult(String[] missingTags) {
		super(missingTags);
	}
	
	private DeserializerResult(String failedBecause) {
		super(failedBecause);
	}
	
	/**
	 * 
	 * @return The successfully deserialized {@link Instruction}.
	 */
	public Instruction getInstruction() {
		return (Instruction) getSuccess();
	}
	
	/**
	 * Generate a successful {@link DeserializerResult}.
	 * @param deserializedInstruction The {@link Instruction} to return
	 * from {@link #getInstruction()}.
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
}
