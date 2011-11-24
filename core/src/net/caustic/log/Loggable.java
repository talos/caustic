package net.caustic.log;

/**
 * Interface for any class that can write to a {@link Logger}.
 * @author talos
 *
 */
public interface Loggable {
	
	/**
	 * Any calls to this {@link Loggable} will be sent to the registered <code>
	 * logger</code> as well.
	 * @param logger The {@link Loggable} to register.
	 */
	public void register(Logger logger);
}
