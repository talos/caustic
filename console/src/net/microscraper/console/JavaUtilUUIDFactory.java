package net.microscraper.console;


/**
 * {@link UUIDFactory} implementation that creates
 * {@link JavaUtilUUID}s.
 * @author talos
 *
 */
public class JavaUtilUUIDFactory implements UUIDFactory {

	@Override
	public UUID get() {
		synchronized(this) {
			return new JavaUtilUUID();
		}
	}
}
