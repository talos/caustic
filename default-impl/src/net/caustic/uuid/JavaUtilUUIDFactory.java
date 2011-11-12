package net.caustic.uuid;

import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;


/**
 * {@link ScopeFactory} implementation that creates
 * {@link JavaUtilUUID}s.
 * @author talos
 *
 */
public final class JavaUtilUUIDFactory implements ScopeFactory {

	@Override
	public Scope get() {
		synchronized(this) {
			return new JavaUtilUUID();
		}
	}
}
