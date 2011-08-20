package net.microscraper.instruction;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Loggable;
import net.microscraper.client.Logger;
import net.microscraper.impl.log.BasicLog;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;
import net.microscraper.util.VectorUtils;

public class InstructionRunner implements Runnable, Loggable {
	
	
	private final Instruction instruction;
	private final Hashtable defaults;
	private final String source;
	
	private final Vector queue = new Vector();
	private final Vector failedExecutables = new Vector();
	private final BasicLog log = new BasicLog();
	
	public InstructionRunner(Instruction instruction, Hashtable defaults, String source) {
		this.instruction = instruction;
		this.defaults = defaults;
		this.source = source;
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
		Variables variables = Variables.fromHashtable(defaults);
		Executable start = instruction.bind(variables, source);
		queue.add(start);
		
		log.i("Starting to execute with " + StringUtils.quote(start) + " and variables " +
				StringUtils.quote(variables));
		
		do {
			
			Executable executable = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			
			// Try to execute the executable.
			log.i("Trying to execute " + StringUtils.quote(executable));
			Execution execution = executable.execute();
			
			// Evaluate the execution's success.
			if(execution.isSuccessful()) {
				// It's successful -- add the resultant executables onto the queue.
				Executable[] children = (Executable[]) execution.getExecuted();
				log.i("Executable " + StringUtils.quote(executable) + " successful." + 
						" Adding its " + children.length + " children to the queue.");
				VectorUtils.arrayIntoVector(children, queue);
				
			} else if(execution.isMissingVariables()) {
				// Try it again later.
				String[] missingVariables = execution.getMissingVariables();
				log.i("Executable " + StringUtils.quote(executable) + " is missing the " +
						" following variables: " +
						StringUtils.quoteJoin(missingVariables) + 
						" Placing at to the end of the queue. It is " +
						(executable.isStuck() ? "stuck." : "not stuck."));
				queue.add(executable);
			} else {
				// Log that execution has failed.
				log.i("Executable " + StringUtils.quote(executable) + " has failed because " +
						" of " + StringUtils.quote(execution.failedBecause()) + ". Removing" +
						" it from the queue.");
				failedExecutables.add(executable);
			}
			
			// End the loop when we run out of executables, or if they're all
			// stuck.
		} while(queue.size() > 0 && getStuckExecutables().size() < queue.size());
		
		log.i("Finished execution starting with " + StringUtils.quote(start) + " and " +
				"variables " + StringUtils.quote(variables));
		
		Vector stuckExecutables = getStuckExecutables();
		if(stuckExecutables.size() > 0) {
			log.i("There were " + stuckExecutables.size() + " stuck executables: ");
			Enumeration e = stuckExecutables.elements();
			while(e.hasMoreElements()) {
				Executable stuckExecutable = (Executable) e.nextElement();
				log.i("Executable " + stuckExecutable + " was stuck on " + 
						StringUtils.quoteJoin(stuckExecution.getMissingVariables()));
			}
		}
		
		if(failedExecutables.size() > 0) {
			log.i("There were " + failedExecutables.size() + " failed executables: ");
			Enumeration e = stuckExecutables.elements();
			while(e.hasMoreElements()) {
				Executable failedExecutable = (Executable) e.nextElement();
				log.i("Executable " + failedExecutable + " failed because of " +
						StringUtils.quote(failedExecution.failedBecause()));
			}			
		}
		
		queue.clear();
	}

	public void register(Logger logger) {
		log.register(logger);
	}
}
