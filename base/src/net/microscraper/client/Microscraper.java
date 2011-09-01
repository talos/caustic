package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.instruction.InstructionRunner;
import net.microscraper.instruction.Load;
import net.microscraper.log.MultiLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.template.Template;
import net.microscraper.util.ThreadPool;

/**
 * A {@link Microscraper} can scrape an {@link Instruction}.
 * @author john
 *
 */
public class Microscraper implements Loggable {	
	private final String executionDir;
	private final MultiLog log = new MultiLog();
	private final Deserializer deserializer;
	private final Database database;
	private final ThreadPool threadPool;
	
	/**
	 * @param deserializer A {@link Deserializer} to use to instantiate {@link Instruction}s.
	 * @param database the {@link Database} to use for storage.  It should be opened beforehand,
	 * and closed after (if necessary).
	 * @param executionDir the {@link String} path to where {@link Microscraper}
	 * is being executed from. This is used to resolve the path to local instructions.
	 */
	public Microscraper(Deserializer deserializer, Database database, String executionDir) {
		this.deserializer = deserializer;
		this.database = database;
		this.executionDir = executionDir;
		this.deserializer.register(log); // receive Browser logs
		this.threadPool = null;
	}
	
	/**
	 * @param deserializer A {@link Deserializer} to use to instantiate {@link Instruction}s.
	 * @param database the {@link Database} to use for storage.  It should be opened beforehand,
	 * and closed after (if necessary).
	 * @param executionDir the {@link String} path to where {@link Microscraper}
	 * is being executed from. This is used to resolve the path to local instructions.
	 * @param threadPool the thread pool to use when executing.
	 */
	public Microscraper(Deserializer deserializer, Database database, String executionDir,
			ThreadPool threadPool) {
		this.deserializer = deserializer;
		this.database = database;
		this.executionDir = executionDir;
		this.deserializer.register(log); // receive Browser logs
		this.threadPool = threadPool;
	}
	
	private void scrape(String instructionString, Hashtable input, String source) {
		
		InstructionPromise promise = new InstructionPromise(deserializer, database, instructionString, executionDir);
		InstructionRunner runner = new InstructionRunner(promise, database, input, source);
		runner.register(log);
		
		if(threadPool != null) {
			threadPool.execute(runner); // use the thread pool if we have one.
		} else {
			runner.run();
		}
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 */
	public void scrape(String serializedString) {
		scrape(serializedString, new Hashtable(), null);
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 * @param input A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>serializedString</code> {@link Template} tags.
	 */
	public void scrape(String serializedString, Hashtable input)  {
		scrape(serializedString, input, null);
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
