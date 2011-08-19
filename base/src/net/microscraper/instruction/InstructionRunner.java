package net.microscraper.instruction;

import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.util.Variables;
import net.microscraper.util.VectorUtils;

public class InstructionRunner {
	
	private final Vector queue = new Vector();
	
	private int countStuckExecutables() {
		int numberOfStuckExecutables = 0;
		Enumeration executables = queue.elements();
		while(executables.hasMoreElements()) {
			if(((Executable) executables.nextElement()).isStuck()) {
				numberOfStuckExecutables ++;
			}
		}
		return numberOfStuckExecutables;
	}
	
	public void run(Instruction instruction, Variables variables, String source) {		
		queue.add(instruction.bind(variables, source));
		
		do {
			
			Executable executable = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			
			// Try to execute the executable.
			Execution execution = executable.execute();
			
			// Evaluate the execution's success.
			// It's successful -- add the resultant executables onto the queue.
			if(execution.isSuccessful()) {
				Executable[] children = execution.generateChildren();
				VectorUtils.arrayIntoVector(children, queue);
				
			// Try it again later.
			} else if(execution.isMissingVariables()) {
				queue.add(executable);
			} else { 
				// Log that execution has failed.
			}
			
			// End the loop when we run out of executables, or if they're all
			// stuck.
		} while(queue.size() > 0 && countStuckExecutables() < queue.size());
		
		queue.clear();
	}
}
