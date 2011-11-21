package net.caustic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.caustic.database.Database;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DefaultJSONDeserializer;
import net.caustic.http.DefaultHttpBrowser;

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
	
	public Scraper() {
		super(new InMemoryDatabase(), new DefaultHttpBrowser(),
				new DefaultJSONDeserializer());
		
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(int nThreads) {
		super(new InMemoryDatabase(), new DefaultHttpBrowser(),
				new DefaultJSONDeserializer());
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	public Scraper(Database db) {
		super(db, new DefaultHttpBrowser(),	
				new DefaultJSONDeserializer());
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(Database db, int nThreads) {
		super(db, new DefaultHttpBrowser(),
				new DefaultJSONDeserializer());
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	public void submit(Executable executable) {
		executor.submit(executable);
	}
	
	/**
	 * Block the calling thread until {@link Scraper} is dormant, then shut
	 * down {@link Scraper}.
	 */
	public void join() throws InterruptedException {
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
		executor.awaitTermination(3600, TimeUnit.SECONDS); // one hour
	}
	
	public void interrupt() {
		executor.shutdownNow();
	}
}
