package net.microscraper.client;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.instruction.Executable;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.log.MultiLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.util.Execution;
import net.microscraper.util.VectorUtils;

public class Scraper implements Runnable, Loggable {
	
	private final Executable start;
	private final Database database;
	private final Hashtable input;
	private final Scope defaultScope;
	
	/**
	 * Queue of {@link Executable}s.
	 */
	private final Vector queue = new Vector();

	//private final Vector failedExecutables = new Vector();
	private final MultiLog log = new MultiLog();
	
	/**
	 * {@link Vector} to hold all {@link Execution}s whose
	 * {@link Executable}s won't be executed again.
	 */
	private final Vector finishedExecutions = new Vector();
	
	/**
	 * Whether this {@link Scraper} has been run, should
	 * be synchronized.
	 */
	private Boolean hasBeenRun = Boolean.FALSE;
	
	/**
	 * 
	 * @param promise
	 * @param scope
	 * @param source
	 */
	public Scraper(Instruction instruction, Database database, Hashtable input, String source) 
			throws IOException {
		this.database = database;
		this.input = input;
		this.defaultScope = database.getDefaultScope();
		this.start = new Executable(source, defaultScope, instruction);
	}

	public Scraper(String serializedInstruction, Deserializer deserializer,
			String executionDir, Database database, Hashtable input, String source) 
			throws IOException {
		this.database = database;
		this.input = input;
		this.defaultScope = database.getDefaultScope();
		
		InstructionPromise promise = new InstructionPromise(deserializer, database, serializedInstruction, executionDir);
		this.start = new Executable(source, defaultScope, promise);
	}
	
	private Vector getStuckExecutables() {
		Vector stuckExecutables = new Vector();
		Enumeration e = queue.elements();
		while(e.hasMoreElements()) {
			Executable executable = (Executable) e.nextElement();
			if(executable.isStuck()) {
				stuckExecutables.add(executable);
			}
		}
		return stuckExecutables;
	}
	
	public void run() {
		// try - catch the entire loop for user interrupt and database IO problems.
		try {
			// Store default values in database
			Enumeration keys = input.keys();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				database.storeOneToOne(defaultScope, key, (String) input.get(key));
			}
			
			queue.add(start);
			
			do {
				Executable executable = (Executable) queue.elementAt(0);
				queue.removeElementAt(0);
				
				// Try to execute the executable.
				//log.i("Running " + StringUtils.quote(executable));
				Execution execution = executable.execute();
				
				// Evaluate the execution's success.
				if(execution.isSuccessful()) {
					// It's successful -- add the resultant executables onto the queue.
					Executable[] children = (Executable[]) execution.getExecuted();
					/*log.i(StringUtils.quote(executable) + " successful." + 
							" Adding its " + children.length + " children to the queue.");*/
					VectorUtils.arrayIntoVector(children, queue);
					
					finishedExecutions.add(execution);
				} else if(execution.isMissingVariables()) {
					// Try it again later.
				//	String[] missingVariables = execution.getMissingVariables();
					/*log.i(StringUtils.quote(executable) + " is missing the " +
							missingVariables.length + " following variables: " +
							StringUtils.quoteJoin(missingVariables) + 
							". Placing at the end of the queue. It is " +
							(executable.isStuck() ? "stuck." : "not stuck."));*/
					queue.add(executable);
					
				} else {
					// Log that execution has failed.
					/*log.i(StringUtils.quote(executable) + " has failed because " +
							" of " + StringUtils.quoteJoin(execution.failedBecause()) +
							". Removing it from the queue.");*/
					//failedExecutables.add(executable);
					
					finishedExecutions.add(execution);
				}
				
				// End the loop when we run out of executables, or if they're all
				// stuck.
			} while(queue.size() > 0 && getStuckExecutables().size() < queue.size());
		
			//log.i("Finished running " + StringUtils.quote(start));
		} catch(InterruptedException e) {
			log.i("Prematurely terminated execution because of user interrupt.");
		} catch(IOException e) {
			log.i("Prematurely terminated execution because the database could be saved: " + e.getMessage());
		}
		
		// Copy the stuck executions into finished executions, as we now know they're
		// definitely stuck.
		for(int i = 0 ; i < queue.size() ; i++) {
			Executable executable = (Executable) queue.elementAt(i);
			finishedExecutions.add(executable.getLastExecution());
		}
		
//		synchronized(hasBeenRun) {
			hasBeenRun = Boolean.TRUE;
//		}
		//queue.clear();
	}
	
	/**
	 * @return <code>true</code> if this {@link Scraper} has been run, <code>
	 * false</code> otherwise.
	 */
	public boolean hasBeenRun() {
//		synchronized(hasBeenRun) {
			return hasBeenRun.booleanValue();
//		}
	}

	/**
	 * 
	 * @return An array of the {@link Execution}s this {@link Scraper} generated
	 * while running.  Should only be called if {@link #hasBeenRun()} is <code>true</code>.
	 */
	public Execution[] getExecutions() {
		if(hasBeenRun() != true) {
			throw new IllegalStateException("Still running this instruction.");
		} else {
			Execution[] result = new Execution[finishedExecutions.size()];
			finishedExecutions.copyInto(result);
			return result;
		}
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
}
