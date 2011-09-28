package net.microscraper.concurrent;

import net.microscraper.database.DatabaseException;

final class ExecutorThread extends Thread {
	
	private final AsyncExecutor executor;
	
	//private final Object lock = new Object();
	private volatile Executable executable = null;
	private volatile boolean isShutdown = false;
	
	public ExecutorThread(AsyncExecutor executor) {
		this.executor = executor;
	}
	
	public void run() {
		try {
			do {
				synchronized(this) {
					executable = null;
					executor.notifyFreeThread(this); // let executor know that this thread is now free.
					wait(); // wait for notification that we have an executable or got shut down.
					
					if(isShutdown) {
						break;
					}
				}
				
				if(executable != null) {
					Executable[] children = executable.execute();
					if(children != null) { // success
						for(int i = 0 ; i < children.length ; i ++) {
							executor.submit(children[i]);
						}
					} else if(executable.isMissingTags()) {
						executor.resubmit(executable);
					} else {
						executor.recordFailure(executable.getFailedBecause());
					}
				}
			} while(executable != null && !isShutdown);
		} catch(InterruptedException e) {
			// end the executor too
			executor.interrupt();
		} catch(DatabaseException e) {
			e.printStackTrace(); // TODO
			executor.interrupt();
		}
	}
	
	/**
	 * 
	 * @param executable An {@link Executable} to execute in this {@link ExecutorThread}.
	 */
	public void execute(Executable next) {
		synchronized(this) {
			if(executable == null) {
				executable = next;
				this.notifyAll();
			} else {
				throw new IllegalStateException("Already executing an executable " + executable);
			}
		}
	}
	
	/**
	 * 
	 * @return <code>true</code> if this {@link ExecutorThread} is not currently
	 * executing anything.
	 */
	public boolean isAsleep() {
		synchronized(this) {
			return executable == null;
		}
	}
	
	/**
	 * Shut down this {@link ExecutorThread}, allowing it to die naturally.
	 */
	public void shutdown() {
		synchronized(this) {
			this.isShutdown = true;
			this.notifyAll();
		}
	}
}
