package net.caustic.deserializer;

import net.caustic.database.Database;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

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
	 * @param db A {@link Database} to use for reference substitutions
	 * inside <code>serializedString</code>.
	 * @param scope {@link Scope} within <code>db</code>
	 * @param uri A {@link String} URI to use when resolving references from
	 * <code>serializedString</code>.
	 * @return A {@link DeserializerResult} with the {@link Instruction} when 
	 * successful, or information about why the deserialization did not work.
	 * @throws InterruptedException if the user interrupted during deserialization.
	 */
	public abstract DeserializerResult deserialize(String serializedString,
			Database db, Scope scope, String uri) throws InterruptedException;
}
