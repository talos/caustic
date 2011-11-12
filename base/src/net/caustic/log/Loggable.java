package net.caustic.log;

/**
 * Interface for any class that can write to a {@link Logger}.
 * @author talos
 *
 */
public interface Loggable {
	public void register(Logger logger);
}
