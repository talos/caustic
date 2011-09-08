package net.microscraper.instruction;

import net.microscraper.client.ScraperResult;
import net.microscraper.deserializer.Deserializer;
import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.util.StringMap;

/**
 * An implementation of {@link Instruction} that wraps the deserialization of
 * a String into an {@link DeserializedInstruction} within
 * {@link Instruction#execute(String, StringMap)}.
 * @author realest
 *
 */
public class SerializedInstruction implements Instruction {
	private final String serializedString;
	private final Deserializer deserializer;
	private final String uri;
	
	private Instruction instruction;
	
	private ScraperResult executeWithDeserialized(String source, StringMap input) throws InterruptedException {
		return instruction.execute(source, input);
	}
	
	public SerializedInstruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
		this.uri = uri;
	}

	public ScraperResult execute(String source, StringMap input) throws InterruptedException {
		
		final ScraperResult result;
		
		// Attempt to deserialize the string if this has not yet been done.
		if(instruction == null) {
			DeserializerResult deserializerResult = deserializer.deserialize(serializedString, input, uri);
			
			if(deserializerResult.isSuccess()) {
				instruction = deserializerResult.getInstruction();
				result = executeWithDeserialized(source, input);
			} else if(deserializerResult.isMissingTags()) {
				result = ScraperResult.missingTags(deserializerResult.getMissingTags());
			} else {
				result = ScraperResult.failure(deserializerResult.getFailedBecause());
			}
		} else { // don't duplicate effort in deserializing
			result = executeWithDeserialized(source, input);
		}
		
		return result;
	}
}