package net.microscraper.client;

import net.microscraper.database.Database;
import net.microscraper.util.Execution;

/**
 * Interface to generate {@link Instruction}s from {@link String}s.
 * @author talos
 *
 */
public interface Deserializer {
	/**
	 * Generate an {@link Instruction} from a {@link String}.  The {@link Instruction}
	 * is contained in the {@link Execution#getExecuted()} of the result.
	 * @param serializedString {@link String} to which will be deserialized into an
	 * {@link Instruction}.
	 * @param database A {@link Database} to link deserialized {@link Instruction}s to.
	 * @param sourceId A {@link int} to use to determine the available values from a {@link Database}
	 * when doing substitutions.
	 * @param rootUri A {@link String} URI that will be used to resolve any references
	 * in <code>serializedString</code>.
	 * @return A {@link Execution} instance whose {@link Execution#getExecuted()} is
	 * an {@link Instruction}.
	 */
	public abstract Execution deserializeString(String serializedString, Database database, int sourceId, String rootUri);

}