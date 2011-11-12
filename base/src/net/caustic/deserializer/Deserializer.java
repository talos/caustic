package net.caustic.deserializer;

import net.caustic.database.DatabaseView;
import net.caustic.instruction.Instruction;

/**
 * Interface to generate {@link Instruction}s from {@link String}s.
 * @author talos
 *
 */
public interface Deserializer {
	
	/**
	 * Try to deserialize a {@link String} into an {@link Instruction}, which
	 * would be contained in {@link DeserializerResult}.
	 * @param serializedString The {@link String} to try to deserialize.
	 * @param input A {@link DatabaseView} to use for reference substitutions
	 * inside <code>serializedString</code>.
	 * @param uri A {@link String} URI to use when resolving references from
	 * <code>serializedString</code>.
	 * @return A {@link DeserializerResult} with the {@link Instruction} when 
	 * successful, or information about why the deserialization did not work.
	 * @throws InterruptedException if the user interrupted during deserialization.
	 */
	public abstract DeserializerResult deserialize(String serializedString,
			DatabaseView input, String uri) throws InterruptedException;
}
