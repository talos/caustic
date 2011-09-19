package net.microscraper.uuid;

/**
 * Implementation of {@link UUIDFactory} using an incremented
 * integer.  Is synchronized.
 * @author realest
 *
 */
public final class IntUUIDFactory implements UUIDFactory {
	private int curId;
	
	@Override
	public UUID get() {
		synchronized(this) {
			curId++;
			return new IntUUID(curId);
		}
	}
	
	/**
	 * Construct {@link IntUUIDFactory} where the first {@link
	 * IntUUID} is <code>0</code>.
	 */
	public IntUUIDFactory() {
		curId = -1;
	}
	
	/**
	 * Construct {@link IntUUIDFactory} where the first {@link
	 * IntUUID} is <code>firstId</code>.
	 * @param the starting <code>int</code> in {@link IntUUID}.
	 */
	public IntUUIDFactory(int firstId) {
		curId = firstId -1;
	}
}
