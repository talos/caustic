package net.microscraper.deserializer;

import net.microscraper.instruction.DeserializedInstruction;
import net.microscraper.util.Result;

public class DeserializerResult extends Result {
	
	private DeserializerResult(DeserializedInstruction deserializedInstruction) {
		super(deserializedInstruction);
	}
	
	private DeserializerResult(String[] missingTags) {
		super(missingTags);
	}
	
	private DeserializerResult(String failedBecause) {
		super(failedBecause);
	}
	
	public DeserializedInstruction getInstruction() {
		return (DeserializedInstruction) getSuccess();
	}
	
	public static DeserializerResult success(DeserializedInstruction deserializedInstruction) {
		return new DeserializerResult(deserializedInstruction);
	}
	
	public static DeserializerResult missingTags(String[] missingTags) {
		return new DeserializerResult(missingTags);
	}
	
	public static DeserializerResult failure(String failedBecause) {
		return new DeserializerResult(failedBecause);
	}
}
