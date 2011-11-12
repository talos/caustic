package net.caustic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseListener;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DefaultJSONDeserializer;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.executor.AsyncExecutor;
import net.caustic.http.DefaultHttpBrowser;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.util.StringUtils;

/**
 * An implementation of {@link Scraper} using {@link DefaultHttpBrowser}
 * and {@link AsyncExecutor} with a specified number of threads.
 * @author realest
 *
 */
public class Scraper {
	public static final int DEFAULT_THREADS = 10;

	private final Database db;
	private final AsyncExecutor executor;
	private final JSONDeserializer deserializer = new DefaultJSONDeserializer();
	private final HttpBrowser browser = new DefaultHttpBrowser();
	
	public Scraper() {
		this.db = new InMemoryDatabase();
		executor = new AsyncExecutor(DEFAULT_THREADS, db);
	}
	
	public Scraper(int nThreads) {
		this.db = new InMemoryDatabase();
		executor = new AsyncExecutor(nThreads, db);
	}
	
	public Scraper(Database db) {
		this.db = db;
		executor = new AsyncExecutor(DEFAULT_THREADS, db);
	}
	
	public Scraper(Database db, int nThreads) {
		this.db = db;
		executor = new AsyncExecutor(nThreads, db);
	}

	public void addListener(DatabaseListener listener) {
		db.addListener(listener);
	}

	public void scrape(String uriOrJSON) throws DatabaseException {	
		Map<String, String> empty = Collections.emptyMap();
		scrape(uriOrJSON, empty);
	}
	
	public void scrape(String uriOrJSON, Map<String, String> input) throws DatabaseException {
		
		executor.execute(
				new SerializedInstruction(uriOrJSON, deserializer, StringUtils.USER_DIR),
				input, null, browser);
	}
}
