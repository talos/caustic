package net.microscraper.deserializer;

import net.microscraper.instruction.Instruction;
import net.microscraper.util.StringMap;

/**
 * Interface to generate {@link Instruction}s from {@link String}s.
 * @author talos
 *
 */
public interface Deserializer {
	
	public abstract DeserializerResult deserialize(String serializedString, StringMap input, String uri)
			throws InterruptedException;
}
