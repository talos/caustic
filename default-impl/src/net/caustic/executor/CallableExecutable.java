package net.caustic.executor;

import java.util.concurrent.Callable;

import net.caustic.database.DatabaseException;
import net.caustic.executor.Executable;

/**
 * A {@link Callable} wrapper for {@link Executable}, because J2ME doesn't
 * have java.util.concurrent.  Submits children to {@link AsyncExecutor} and
 * returns itself as a {@link Future}.
 * @author talos
 *
 */
class CallableExecutable implements Callable<Executable> {

	private final Executable executable;
	private final AsyncExecutor executor;
	
	/**
	 * 
	 * @param executable The {@link Executable} to wrap.
	 */
	public CallableExecutable(Executable executable, AsyncExecutor executor) {
		this.executable = executable;
		this.executor = executor;
	}
	
	@Override
	public Executable call() throws DatabaseException, InterruptedException {
		Executable[] children = executable.execute();
		// run children
		if(children != null) {
			for(Executable child : children) {
				executor.execute(child);
			}
		}
		return executable;
	}
}
