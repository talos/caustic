package net.microscraper.uuid;


/**
 * {@link UUIDFactory} implementation that creates
 * {@link JavaUtilUUID}s.
 * @author talos
 *
 */
public final class JavaUtilUUIDFactory implements UUIDFactory {

	@Override
	public UUID get() {
		synchronized(this) {
			return new JavaUtilUUID();
		}
	}
}
