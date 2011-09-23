package net.microscraper.concurrent;

import java.util.Vector;

import net.microscraper.client.Scraper;
import net.microscraper.database.DatabaseException;
import net.microscraper.util.VectorUtils;

/**
 * An executor that creates a new thread for each {@link Scraper}.
 * @author talos
 *
 */
public class AsyncExecutor extends Thread {	
	private Vector queue = new Vector();
	private Vector toResubmit = new Vector();
	private final ExecutorThread[] threadPool;
	
	private void retryResubmits() {
		synchronized(toResubmit) {
			for(int i = 0 ; i < toResubmit.size() ; i ++) {
				Executable executable = (Executable) toResubmit.elementAt(i);
				findLeastOccupiedThread().add(executable);
			}
			toResubmit.clear();
		}
	}
	
	/**
	 * 
	 * @return A free {@link ExecutorThread}, blocks until one is free.
	 */
	private ExecutorThread getFreeThread() throws InterruptedException {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			if(threadPool[i].isAsleep() == false) {
				return threadPool[i];
			}
		}
		
		// if there were no free threads above, wait until we get a notification
		// that a thread opened to retry.
		synchronized(this) {
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
	
	/**
	 * Construct the {@link AsyncExecutor} and start it.
	 * @param nThreads
	 * @param executable The initial executable.
	 */
	public AsyncExecutor(int nThreads, Executable executable) {
		threadPool = new ExecutorThread[nThreads];
		for(int i = 0 ; i < nThreads ; i ++) {
			threadPool[i] = new ExecutorThread(this);
			threadPool[i].start();
		}
		queue.add(executable);
		this.start();
	}

	public void run() {
		try {
			// allow the executor to die only when all threads are sleeping.
			while(areAllThreadsSleeping() == false) {
				// run through the queue, but do not hard-synch on it while we're running through
				synchronized(queue) {
					if(queue.size() > 0) {
						Executable executable = (Executable) queue.elementAt(0);
						queue.removeElementAt(0);
						getFreeThread().execute(executable);
					} else {
						// when the queue is empty, resubmit the stuck executables
						synchronized(toResubmit) {
							Executable[] toResubmitAry = new Executable[toResubmit.size()];
							toResubmit.copyInto(toResubmitAry);
							if(Executable.allAreStuck(toResubmitAry)) {
								// don't resubmit anything
							} else {
								toResubmit.clear();
								VectorUtils.arrayIntoVector(toResubmitAry, queue);
							}
						}
					}
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
			//TODO
		}
	}
	
	public void submit(Executable executable) {
		synchronized(queue) {
			queue.add(executable);
		}
	}

	public void resubmit(Executable executable)  {
		synchronized(toResubmit) {
			toResubmit.add(executable);
		}
	}
	
	public void interrupt() {
		super.interrupt();
		for(int i = 0 ; i < threadPool.length ; i ++) {
			threadPool[i].interrupt();
		}
	}
}
