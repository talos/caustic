package net.microscraper.client;

import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.instruction.Instruction;
import net.microscraper.log.Loggable;
import net.microscraper.template.DependsOnTemplate;

/**
 * Interface to generate {@link Instruction}s from {@link String}s.
 * @author talos
 *
 */
public interface SerializedInstruction extends Loggable, DependsOnTemplate {
	/**
	 * Generate an {@link Instruction} from a {@link String}.  The {@link Instruction}
	 * is contained in the {@link Execution#getExecuted()} of the result.
	 * @param serializedString {@link String} to which will be deserialized into an
	 * {@link Instruction}.
	 * @param database A {@link Database} to link deserialized {@link Instruction}s to.
	 * @param scope A {@link Scope} to determine the available values from a {@link Database}
	 * when doing substitutions.
	 * @param rootUri A {@link String} URI that will be used to resolve any references
	 * in <code>serializedString</code>.
	 * @return <code>True</code> if deserialization was successful, <code>false</code> otherwise.
	 */
	public abstract boolean deserialize(String serializedString, Database database, Scope scope, String rootUri);

	public abstract Instruction getInstruction();
	
}