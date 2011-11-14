package net.caustic;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DefaultJSONDeserializer;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.http.DefaultHttpBrowser;
import net.caustic.instruction.Executable;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.util.StringUtils;

/**
 * An implementation of {@link ScraperInterface} using {@link DefaultHttpBrowser}
 * and {@link AsyncExecutor} with a specified number of threads.
 * @author realest
 *
 */
public class Scraper extends DefaultScraper {
	public static final int DEFAULT_THREADS = 10;

	private final ExecutorService executor;
	private final JSONDeserializer deserializer = new DefaultJSONDeserializer();
	
	public Scraper() {
		super(new InMemoryDatabase());
		
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(int nThreads) {
		super(new InMemoryDatabase());
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	public Scraper(Database db) {
		super(db);
		executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	public Scraper(Database db, int nThreads) {
		super(db);
		executor = Executors.newFixedThreadPool(nThreads);
	}

	public void scrape(String uriOrJSON) throws DatabaseException {	
		Map<String, String> empty = Collections.emptyMap();
		scrape(uriOrJSON, empty);
	}
	
	public void scrape(String uriOrJSON, Map<String, String> input) throws DatabaseException {
		Instruction instruction = new SerializedInstruction(uriOrJSON, deserializer, StringUtils.USER_DIR);
		scrape(instruction, new Hashtable<String, String>(input), new DefaultHttpBrowser());
	}

	public void submit(Executable executable) {
		executor.submit(executable);
	}
	
	/**
	 * Wait for this to wrap up.
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		while(!isDone()) {
			if(executor.isTerminated()) { // break if artificial termination
				break;
			}
			Thread.sleep(100);
		}
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.MINUTES);
	}
	
	public void interrupt() {
		executor.shutdownNow();
	}
}
