package net.microscraper.instruction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.database.Database;
import net.microscraper.database.Variables;
import net.microscraper.log.BasicLog;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
import net.microscraper.util.VectorUtils;

public class InstructionRunner implements Runnable, Loggable {
	
	private final InstructionPromise promise;
	private final Variables variables;
	private final String source;
	
	private final Vector queue = new Vector();
	private final Vector failedExecutables = new Vector();
	private final BasicLog log = new BasicLog();
	
	/**
	 * 
	 * @param promise
	 * @param database
	 * @param defaults
	 * @param source
	 * @throws IOException If there was a problem writing to the database.
	 */
	public InstructionRunner(InstructionPromise promise, Database database, Hashtable defaults, String source)
			throws IOException {
		this.promise = promise;
		//this.variables = Variables.fromHashtable(database, defaults);
		
		this.source = source;
		variables = database.open();
		Enumeration keys = defaults.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			variables.storeOneToOne(key, (String) defaults.get(key));
		}
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
		Executable start = new Executable(source, variables, promise);
		queue.add(start);
		
		//log.i("Starting to execute with " + StringUtils.quote(start));
		
		// try - catch the entire loop for user interrupt.
		try {
			do {
				Executable executable = (Executable) queue.elementAt(0);
				queue.removeElementAt(0);
				
				// Try to execute the executable.
				log.i("Executing " + StringUtils.quote(executable));
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
		
			log.i("Finished execution starting with " + StringUtils.quote(start));
		} catch(InterruptedException e) {
			log.i("Prematurely terminated execution starting with " + StringUtils.quote(start) 
					+ " because of user interrupt.");
		} catch(IOException e) {
			log.i("Prematurely terminated execution starting with " + StringUtils.quote(start)
					+ " because the database could not be saved to: " + e.getMessage());
		}
		
		// Log information about stuck executables.
		Vector stuckExecutables = getStuckExecutables();
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
		}
		
		queue.clear();
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
