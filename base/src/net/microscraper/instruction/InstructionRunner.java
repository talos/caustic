package net.microscraper.instruction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.log.MultiLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
import net.microscraper.util.VectorUtils;

public class InstructionRunner implements Runnable, Loggable {
	
	private final InstructionPromise promise;
	private final Database database;
	private final Hashtable input;
	private final String source;
	
	private final Vector queue = new Vector();
	private final Vector failedExecutables = new Vector();
	private final MultiLog log = new MultiLog();
	
	/**
	 * 
	 * @param promise
	 * @param scope
	 * @param source
	 */
	public InstructionRunner(InstructionPromise promise, Database database, Hashtable input, String source) {
		this.promise = promise;		
		this.source = source;
		this.database = database;
		this.input = input;
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
			// Use a fresh database scope.
			Scope scope = database.getDefaultScope();
			
			// Store default values in database
			Enumeration keys = input.keys();
			while(keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				database.storeOneToOne(scope, key, (String) input.get(key));
			}
			
			Executable start = new Executable(source, scope, promise);
			queue.add(start);
			
			do {
				Executable executable = (Executable) queue.elementAt(0);
				queue.removeElementAt(0);
				
				// Try to execute the executable.
				log.i("Running " + StringUtils.quote(executable));
				Execution execution = executable.execute();
				
				// Evaluate the execution's success.
				if(execution.isSuccessful()) {
					// It's successful -- add the resultant executables onto the queue.
					Executable[] children = (Executable[]) execution.getExecuted();
					log.i(StringUtils.quote(executable) + " successful." + 
							" Adding its " + children.length + " children to the queue.");
					VectorUtils.arrayIntoVector(children, queue);
					
				} else if(execution.isMissingVariables()) {
					// Try it again later.
					String[] missingVariables = execution.getMissingVariables();
					log.i(StringUtils.quote(executable) + " is missing the " +
							missingVariables.length + " following variables: " +
							StringUtils.quoteJoin(missingVariables) + 
							". Placing at the end of the queue. It is " +
							(executable.isStuck() ? "stuck." : "not stuck."));
					queue.add(executable);
				} else {
					// Log that execution has failed.
					log.i(StringUtils.quote(executable) + " has failed because " +
							" of " + StringUtils.quoteJoin(execution.failedBecause()) +
							". Removing it from the queue.");
					failedExecutables.add(executable);
				}
				
				// End the loop when we run out of executables, or if they're all
				// stuck.
			} while(queue.size() > 0 && getStuckExecutables().size() < queue.size());
		
			log.i("Finished running " + StringUtils.quote(start));
		} catch(InterruptedException e) {
			log.i("Prematurely terminated execution because of user interrupt.");
		} catch(IOException e) {
			log.i("Prematurely terminated execution because the database could be saved: " + e.getMessage());
		}
		
		// Log information about stuck executables.
		/*Vector stuckExecutables = getStuckExecutables();
		if(stuckExecutables.size() > 0) {
			log.i("There were " + stuckExecutables.size() + " stuck executables: ");
			Enumeration e = stuckExecutables.elements();
			while(e.hasMoreElements()) {
				Executable stuck = (Executable) e.nextElement();
				log.i(stuck + " was stuck on " + 
						// TODO LOD violation
						StringUtils.quoteJoin(stuck.getLastExecution().getMissingVariables()));
			}
		}
		
		// Log inforamtion about failed executables.
		if(failedExecutables.size() > 0) {
			log.i("There were " + failedExecutables.size() + " failed executables: ");
			Enumeration e = failedExecutables.elements();
			while(e.hasMoreElements()) {
				Executable failed = (Executable) e.nextElement();
				log.i(failed + " failed because of " +
						// TODO LOD violation
						StringUtils.quoteJoin(failed.getLastExecution().failedBecause()));
			}			
		}*/
		
		queue.clear();
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
