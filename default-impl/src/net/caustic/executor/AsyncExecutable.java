package net.caustic.executor;

import net.caustic.database.DatabaseException;
import net.caustic.executor.Executable;

/**
 * A {@link Runnable} wrapper for {@link Executable}.
 * Submits children to {@link AsyncExecutor}.
 * @author talos
 *
 */
class AsyncExecutable implements Runnable {

	private final Executable executable;
	private final AsyncExecutor executor;
	
	/**
	 * 
	 * @param executable The {@link Executable} to wrap.
	 */
	public AsyncExecutable(Executable executable, AsyncExecutor executor) {
		this.executable = executable;
		this.executor = executor;
	}
	
	@Override
	public void run() {
		try {
			Executable[] children = executable.execute();
			executor.finished(executable, children);
			// run children
		} catch(DatabaseException e) {
			executor.notifyCrash(e);
		} catch(InterruptedException e) {
			executor.notifyCrash(e);
		} catch(Throwable e) {
			executor.notifyCrash(e);
		}
	}
}
