package net.microscraper.concurrent;

import java.util.Date;
import java.util.Enumeration;
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
	private Vector freeThreads = new Vector();
	
	private final ExecutorThread[] threadPool;
	private final Executable initialExecutable;
	
	/**
	 * Shutdown and join all threads.
	 */
	private void shutdownAllAndJoin() throws InterruptedException {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			threadPool[i].shutdown();
			threadPool[i].join();
		}
	}
	
	/**
	 * 
	 * @return <code>true</code> if all threads are free, <code>false</code>
	 * otherwise.
	 */
	private boolean areAllThreadsFree() {
		synchronized(freeThreads) {
			return freeThreads.size() < threadPool.length;
		}
	}
	
	/**
	 * Construct the {@link AsyncExecutor} and its threads.
	 * @param nThreads how many threads to use.
	 * @param executable The initial executable.
	 */
	public AsyncExecutor(int nThreads, Executable executable) {
		this.initialExecutable = executable;
		this.threadPool = new ExecutorThread[nThreads];
		for(int i = 0 ; i < nThreads ; i ++) {
			this.threadPool[i] = new ExecutorThread(this);
		}
	}

	public void run() {
		try {
			for(int i = 0 ; i < threadPool.length ; i ++) {
				this.threadPool[i].start();
			}
			submit(initialExecutable);
			do {
				// synchronize on queue briefly to grab the next element, if one exists.
				Executable next = null;
				synchronized(queue) {
					
					// exit from queue if it gains an element
					while(queue.size() == 0) {
						queue.wait();
					}
					
					// grab the next element from queue if there is one
					next = (Executable) queue.elementAt(0);
					if(queue.size() > 0) {
						next = (Executable) queue.elementAt(0);
						queue.removeElementAt(0);
					}
				}
				
				if(next != null) {
					// this will block until a free thread is available
					ExecutorThread freeThread;
					synchronized(freeThreads) {
						while(freeThreads.size() == 0) {
							freeThreads.wait();
						}
						freeThread = (ExecutorThread) freeThreads.elementAt(0);
						freeThreads.removeElementAt(0);
					}
					freeThread.execute(next);
				} else {
					// when the queue is empty, resubmit the stuck executables
					synchronized(toResubmit) {
						Executable[] toResubmitAry = new Executable[toResubmit.size()];
						toResubmit.copyInto(toResubmitAry);
						if(Executable.allAreStuck(toResubmitAry)) {
							// if everything is stuck, don't do anything
						} else {
							toResubmit.clear();
							// refill the queue
							synchronized(queue) {
								VectorUtils.arrayIntoVector(toResubmitAry, queue);
							}
						}
					}
				}
				
			// allow the executor to die only when all threads are sleeping and queue is empty
			// the queue could currently be empty, but one of the non-asleep threads will submit something.
			} while(areAllThreadsFree() == false || queue.size() > 0);
			
			shutdownAllAndJoin();
		} catch(InterruptedException e) {
			e.printStackTrace();
			//TODO
		}
	}
	
	public void notifyFreeThread(ExecutorThread thread) {
		synchronized(freeThreads) {
			freeThreads.addElement(thread);
			freeThreads.notifyAll();
		}
	}
	
	/**
	 * 
	 * @param executable An {@link Executable} that will be executed as soon
	 * as a thread becomes free.
	 */
	public void submit(Executable executable) {
		synchronized(queue) {
			queue.addElement(executable);
			queue.notifyAll();
		}
	}
	
	/**
	 * 
	 * @param executable An {@link Executable} that may be executed in a batch
	 * once this {@link AsyncExecutor}'s queue is empty, provided that not all the
	 * {@link Executable}s in the batch are stuck.
	 */
	/*public void resubmit(Executable executable)  {
		synchronized(toResubmit) {
			toResubmit.addElement(executable);
		}
	}*/
	
	public void interrupt() {
		for(int i = 0 ; i < threadPool.length ; i ++) {
			threadPool[i].interrupt();
		}
		super.interrupt();
	}
	
	/**
	 * 
	 * @param failedBecauseStr The reason an {@link Executable} failed.
	 */
	public void recordFailure(String failedBecauseStr) {
		synchronized(failedBecause) {
			failedBecause.addElement(failedBecauseStr);
		}
	}
}
