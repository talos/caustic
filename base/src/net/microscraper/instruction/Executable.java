package net.microscraper.instruction;

/**
 * The {@link Executable} interface is implemented by classes
 * that can generate an {@link Execution} from a {@link Variables}.
 * @see Load
 * @see Find
 * @author talos
 *
 */
public interface Executable {
	
	/**
	 * Generate an {@link Execution} bound to {@link Variables}.
	 * @param variables The {@link Variables} to bind the {@link Execution} to.
	 * @return The bound {@link Execution}.
	 */
	public void Execution execute(Variables variables);
}
