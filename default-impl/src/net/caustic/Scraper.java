package net.caustic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.caustic.database.Database;
import net.caustic.database.MemoryDatabase;
import net.caustic.http.DefaultHttpBrowser;
import net.caustic.instruction.DefaultJSONDeserializer;
import net.caustic.scope.Scope;

/**
 * An implementation of {@link AbstractScraper} using {@link DefaultHttpBrowser},
 * {@link AsyncExecutor} with a specified number of threads, and
 * {@link DefaultJSONDeserializer} for deserialization.
 * @author realest
 *
 */
public class Scraper extends AbstractScraper {
	public static final int DEFAULT_THREADS = 10;

	private final ExecutorService executor;
	
	public Scraper(ScraperListener listener) {
		super(new MemoryDatabase(), new DefaultHttpBrowser(),
				new DefaultJSONDeserializer(), listener);
		
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(int nThreads, ScraperListener listener) {
		super(new MemoryDatabase(), new DefaultHttpBrowser(),
				new DefaultJSONDeserializer(), listener);
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	public Scraper(Database db, ScraperListener listener) {
		super(db, new DefaultHttpBrowser(),	
				new DefaultJSONDeserializer(), listener);
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(Database db, int nThreads, ScraperListener listener) {
		super(db, new DefaultHttpBrowser(),
				new DefaultJSONDeserializer(), listener);
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	/**
	 * Block the calling thread until {@link Scraper} is done, then shut
	 * down {@link Scraper}.
	 * @param seconds How many seconds to wait before interrupting the scraper.
	 */
	/*public void awaitDormancy(int seconds) throws InterruptedException {
		while(!isIdle()) {
			if(executor.isTerminated()) { // break if artificial termination
				break;
			}
			
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				interrupt();
			}
		}
		executor.shutdown();
		//executor.awaitTermination(60, TimeUnit.MINUTES);
		executor.awaitTermination(seconds, TimeUnit.SECONDS);
	}*/

	/**
	 * Block the calling thread until {@link Scraper} is done, then shut
	 * down {@link Scraper}.
	 * @param seconds How many seconds to wait before interrupting the scraper.
	 */
	/*
	public void awaitDormancy(int seconds) throws InterruptedException {
		while(!isDormant()) {
			if(executor.isTerminated()) { // break if artificial termination
				break;
			}
			
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				interrupt();
			}
		}
		executor.shutdown();
		//executor.awaitTermination(60, TimeUnit.MINUTES);
		executor.awaitTermination(seconds, TimeUnit.SECONDS);
	}
	*/
	
	
	public void interrupt(Throwable because) {
		because.printStackTrace();
		executor.shutdownNow();
	}
	
	public void interrupt() {
		executor.shutdownNow();
	}

	@Override
	protected void submit(Executable executable) {
		executor.submit(executable);
		//instruction.execute(source, database, scope, browser);
	}

}
