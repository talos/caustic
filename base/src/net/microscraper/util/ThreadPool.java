package net.microscraper.util;

/**
 * Interface for a very simple thread pool.
 * @author realest
 *
 */
public interface ThreadPool {
	
	/**
	 * Add a {@link runnable} to this {@link ThreadPool}'s queue.
	 * @param runnable
	 */
	public void execute(Runnable runnable);
	
	/**
	 * Interrupt all this {@link ThreadPool}'s threads, and clear
	 * its queue.
	 */
	public void empty();
}
