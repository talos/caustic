package net.microscraper.uuid;

/**
 * Implementation of {@link UUIDFactory} using an incremented
 * integer.  Is synchronized.
 * @author realest
 *
 */
public final class IntUUIDFactory implements UUIDFactory {
	private int curId = -1;
	
	@Override
	public UUID get() {
		synchronized(this) {
			curId++;
			return new IntUUID(curId);
		}
	}
}
