package net.microscraper.client;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.instruction.InstructionRunner;
import net.microscraper.instruction.Load;
import net.microscraper.log.BasicLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.template.Template;

/**
 * A {@link Microscraper} can scrape an {@link Instruction}.
 * @author john
 *
 */
public class Microscraper implements Loggable {	
	private final String executionDir;
	private final BasicLog log = new BasicLog();
	private final Deserializer deserializer;
	private final Database database;
	
	/**
	 * @param deserializer A {@link Deserializer} to use to instantiate {@link Instruction}s.
	 * @param database the {@link Database} to use for storage.
	 * @param executionDir the {@link String} path to where {@link Microscraper}
	 * is being executed from. This is used to resolve the path to local instructions.
	 */
	public Microscraper(Deserializer deserializer, Database database, String executionDir) {
		this.deserializer = deserializer;
		this.database = database;
		this.executionDir = executionDir;
	}
	
	private void scrape(String instructionString, Hashtable[] defaultsHashes, String source) throws IOException {
		InstructionPromise promise = new InstructionPromise(deserializer, database, instructionString, executionDir);
		for(int i = 0 ; i < defaultsHashes.length ; i ++) {
			Hashtable defaults = defaultsHashes[i];
			
			// Use a fresh database scope.
			Scope scope = database.getScope();
			
			// Store default values in database
			Enumeration keys = defaults.keys();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				database.storeOneToOne(scope, key, (String) defaults.get(key));
			}
			
			InstructionRunner runner = new InstructionRunner(promise, scope, source);
			runner.register(log);
			runner.run();
		}
		database.close();
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 */
	public void scrape(String serializedString) throws IOException {
		scrape(serializedString, new Hashtable[] { new Hashtable() }, null);
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaults A {@link Hashtable} mapping {@link String}s to {@link String}s to substitute in 
	 * <code>serializedString</code> {@link Template} tags.
	 */
	public void scrape(String serializedString, Hashtable defaults) throws IOException  {
		scrape(serializedString, new Hashtable[] { defaults }, null );
	}
	
	/**
	 * Scrape from a {@link Load} in a serialized String.
	 * @param serializedString A {@link String} with a {@link Load} serialized in JSON.
	 * @param defaultsArray An array of {@link Hashtable}s.  Each maps {@link String}s to {@link String}s to substitute in 
	 * <code>serializedString</code> {@link Template} tags.
	 */
	public void scrape(String serializedString, Hashtable[] defaultsArray) throws IOException  {
		scrape(serializedString, defaultsArray, null);
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
