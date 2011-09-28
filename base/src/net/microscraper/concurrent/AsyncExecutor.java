package net.microscraper.concurrent;

import java.util.Vector;

import net.microscraper.client.Scraper;
import net.microscraper.util.VectorUtils;

/**
 * An executor that creates a new thread for each {@link Scraper}.
 * @author talos
 *
 */
public class AsyncExecutor extends Thread {	
	private Vector failedBecause = new Vector();
	private Vector queue = new Vector();
	private Vector toResubmit = new Vector();
	private final ExecutorThread[] threadPool;
	private final Executable initialExecutable;
	
	/**
	 * 
	 * @return A free {@link ExecutorThread}. This blocks until one is free.
	 */
	private ExecutorThread getFreeThread() throws InterruptedException {
		synchronized(this) {
			for(int i = 0 ; i < threadPool.length ; i ++) {
				// thread is free if it's asleep
				synchronized(threadPool[i]) {
					if(threadPool[i].isAsleep()) {
						return threadPool[i];
					}
				}
			}
			
			// if there were no free threads above, wait until we get a notification
			// that a thread opened to retry.
			this.wait();
		}
		return getFreeThread();
	}
	
	/**
	 * 
	 * @return <code>true</code> if all threads are asleep, <code>false</code>
	 * otherwise.
	 */
	private boolean areAllThreadsSleeping() {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			if(threadPool[i].isAsleep() == false) {
				return false;
			}
		}
		return true;
	}
	
	public void notifyFreeThread(ExecutorThread thread) {
		synchronized(this) {
			System.out.println("notifying of free thread!");
			this.notifyAll();
		}
	}
	
	/**
	 * Construct the {@link AsyncExecutor} and start its threads, but do not start it.
	 * @param nThreads how many threads to use.
	 * @param executable The initial executable.
	 */
	public AsyncExecutor(int nThreads, Executable executable) {
		this.initialExecutable = executable;
		this.threadPool = new ExecutorThread[nThreads];
		for(int i = 0 ; i < nThreads ; i ++) {
			this.threadPool[i] = new ExecutorThread(this);
			this.threadPool[i].start();
		}
	}

	public void run() {
		try {
			submit(initialExecutable);
			do {
				// synchronize on queue briefly to grab the next element, if one exists.
				Executable next = null;
				synchronized(queue) {
					// if there is an executable, run it 
					if(queue.size() > 0) {
						next = (Executable) queue.elementAt(0);
						queue.removeElementAt(0);
					} else {
						// otherwise, wait for a signal on the queue.
						queue.wait();
					}
				}
				
				if(next != null) {
					// this will block until a free thread is available
					ExecutorThread thread = getFreeThread();
					thread.execute(next);
				} else {
					// when the queue is empty, resubmit the stuck executables
					synchronized(toResubmit) {
						Executable[] toResubmitAry = new Executable[toResubmit.size()];
						toResubmit.copyInto(toResubmitAry);
						if(Executable.allAreStuck(toResubmitAry)) {
							// don't resubmit anything
						} else {
							toResubmit.clear();
							// refill the queue
							synchronized(queue) {
								VectorUtils.arrayIntoVector(toResubmitAry, queue);
							}
						}
					}
				}
				this.wait();
				
			// allow the executor to die only when all threads are sleeping
			// the queue could currently be empty, but one of the non-asleep threads will submit something.
			} while(areAllThreadsSleeping() == false);
		} catch(InterruptedException e) {
			e.printStackTrace();
			//TODO
		}
	}
	
	/**
	 * 
	 * @param executable An {@link Executable} that will be executed as soon
	 * as a thread becomes free.
	 */
	public void submit(Executable executable) {
		synchronized(queue) {
			queue.add(executable);
			queue.notifyAll();
		}
	}
	
	/**
	 * 
	 * @param executable An {@link Executable} that may be executed in a batch
	 * once this {@link AsyncExecutor}'s queue is empty, provided that not all the
	 * {@link Executable}s in the batch are stuck.
	 */
	public void resubmit(Executable executable)  {
		synchronized(toResubmit) {
			toResubmit.add(executable);
		}
	}
	
	public void interrupt() {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			threadPool[i].interrupt();
		}
		super.interrupt();
	}
	
	/**
	 * 
	 * @param failedBecause The reason an {@link Executable} failed.
	 */
	public void recordFailure(String failedBecause) {
		this.failedBecause.add(failedBecause);
	}
}
