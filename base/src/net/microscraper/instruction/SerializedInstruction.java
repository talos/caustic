package net.microscraper.instruction;

import net.microscraper.client.ScraperResult;
import net.microscraper.deserializer.Deserializer;
import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.util.StringMap;

public class SerializedInstruction implements Instruction {
	private final String serializedString;
	private final Deserializer deserializer;
	private final String uri;
	
	private DeserializedInstruction deserializedInstruction;
	
	private ScraperResult executeWithDeserialized(String source, StringMap input) throws InterruptedException {
		return deserializedInstruction.execute(source, input);
	}
	
	public SerializedInstruction(String serializedString, Deserializer deserializer, String uri) {
		this.serializedString = serializedString;
		this.deserializer = deserializer;
		this.uri = uri;
	}

	public ScraperResult execute(String source, StringMap input) throws InterruptedException {
		
		final ScraperResult result;
		
		// Attempt to deserialize the string if this has not yet been done.
		if(deserializedInstruction == null) {
			DeserializerResult deserializerResult = deserializer.deserialize(serializedString, input, uri);
			
			if(deserializerResult.isSuccess()) {
				deserializedInstruction = deserializerResult.getInstruction();
				result = executeWithDeserialized(source, input);
			} else if(deserializerResult.isMissingTags()) {
				result = ScraperResult.missingTags(deserializerResult.getMissingTags());
			} else {
				result = ScraperResult.failure(deserializerResult.getFailedBecause());
			}
		} else {
			result = executeWithDeserialized(source, input);
		}
		
		return result;
	}
}