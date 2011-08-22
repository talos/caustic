package net.microscraper.client;

import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

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
	 * @param variables A {@link Variables} instance.
	 * @param rootUri A {@link String} URI that will be used to resolve any references
	 * in <code>serializedString</code>.
	 * @return A {@link Execution} instance whose {@link Execution#getExecuted()} is
	 * an {@link Instruction}.
	 */
	public abstract Execution deserializeString(String serializedString, Variables variables, String rootUri);

}