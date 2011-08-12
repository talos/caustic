package net.microscraper.executable;

import java.io.IOException;

import net.microscraper.Log;
import net.microscraper.MissingVariableException;
import net.microscraper.Utils;
import net.microscraper.Variables;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Instruction;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.regexp.RegexpException;

/**
 * {@link BasicExecutable} is a partial implementation of {@link Executable}.  It provides a framework
 * for implementing all of its interfaces except {@link Executable#hasName()},
 * {@link Executable#getName()}, {@link Executable#hasValue()},
 * and {@link Executable#getValue()}.
 * <p>Subclasses must provide implementations of {@link #generateResource}, {@link #generateResult},
 * and {@link #generateChildren}.
 * @author john
 *
 */
public final class BasicExecutable extends Log implements Executable {
	private final Instruction instruction;
	private final Result source;
	//private final Interfaces interfaces;
	private final Browser browser;
	private final RegexpCompiler compiler;
	private final Database database;
	private final Variables variables;
	
	private Result[] results = null;
	//private boolean generatedResults = false;
	
	private Executable[] children = null;
	
	private Throwable failure = null; // has to be Throwable because that's what #getCause returns.
	private String lastMissingVariable = null;
	private String missingVariable = null;
	
	private boolean isStuck = false;
	private boolean isComplete = false;
	
	/**
	 * Construct a new {@link BasicExecutable}.
	 * @param instruction The {@link Instruction} with instructions for execution.
	 * @param compiler the {@link RegexpCompiler} to use.
	 * @param browser the {@link Browser} to use.
	 * @param variables the {@link Variables} surrounding this {@link BasicExecutable}.
	 * @param source The {@link Result} which is the source of this {@link Executable}.  Can
	 * be <code>null</code> if there was none.
	 * @param database The {@link Database} to use when storing {@link Result}s.
	 * @see #run
	 */
	public BasicExecutable(Instruction instruction, RegexpCompiler compiler,
			Browser browser, Variables variables, Result source, Database database) {
		this.instruction = instruction;
		this.compiler = compiler;
		this.browser = browser;
		this.variables = variables;
		this.source = source;
		this.database = database;
	}
	
	public final void run() {
		isStuck = false; // always reset isStuck
		
		// Only allow #run if this is not yet complete or failed.
		if(!isComplete() && !hasFailed()) {
			try {
				// Only generate the result if we don't have one, and we have a resource.
				if(results == null) {
					results = instruction.execute(compiler, browser, this, source, database);
				}
				if(results != null) {
					children = instruction.generateChildren(compiler, browser, this, results, database);
					isComplete = true;
				}
			} catch(RegexpException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(MissingVariableException e) {
				handleMissingVariable(e);
			} catch(BrowserException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(IOException e) {
				handleFailure(new ExecutionFailure(e));				
			} catch(DeserializationException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(DatabaseException e) {
				handleFailure(new ExecutionFailure(e));
			}
		}
	}
	
	/**
	 * Catch-all failures.  Sets the state of the {@link Executable} to failed.
	 * @param e The {@link ExecutionFailure}.
	 */
	private void handleFailure(ExecutionFailure e) {
		failure = e.getCause();
		i("Failure in " + toString());
		e(failure);
	}
	
	/**
	 * Catch {@link MissingVariableException}.  If it's for the same tag as the last time the handler
	 * was called, change the state of the {@link BasicExecutable} to 'stuck'.
	 * @param e The {@link MissingVariableException}.
	 */
	private void handleMissingVariable(MissingVariableException e) {
		i("Missing " + Utils.quote(e.name) + " from " + toString());
		if(missingVariable != null) {
			lastMissingVariable = new String(missingVariable);
			missingVariable = e.name;
			if(lastMissingVariable.equals(missingVariable)) {
				isStuck = true;
				i("Stuck on " + Utils.quote(missingVariable) + " in " + toString());
			}
		} else {
			missingVariable = e.name;
		}
	}
	
	/**
	 * 
	 * @return <code>True</code> if the {@link Executable} has escaped its {@link GenerateResource}, {@link generateResult},
	 * or {@link generateChildren} twice because of the same variable.  <code>False</code> otherwise.
	 * @see #stuckOn()
	 */
	public final boolean isStuck() {
		return isStuck;
	}
	
	/**
	 * @see #isStuck()
	 */
	public final String stuckOn() throws IllegalStateException {
		if(isStuck()) {
			return missingVariable;
		} else {
			throw new IllegalStateException();
		}
	}
	
	public final boolean hasFailed() {
		if(failure != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public final Throwable failedBecause() throws IllegalStateException {
		if(hasFailed()) {
			return failure;
		} else {
			throw new IllegalStateException();
		}
	}

	public final boolean isComplete() {
		return isComplete;
	}
	
	public final String toString() {
		return instruction.toString();
	}
	
	public final Executable[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
	public String get(String key) throws MissingVariableException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	
/*
   // FindMany
	public final String get(String key) throws MissingVariableException {
		return variables.get(key);
	}
	
	public final boolean containsKey(String key) {
		return variables.containsKey(key);
	}
	*/
	/*
	 * FindOne
	 * 
	 * 
	 * 
	protected String localGet(String key) {
		if(isComplete()) {
			Result result = getResults()[0];
			if(result.getName().equals(key))
				return result.getValue();
			for(int i = 0 ; i < getFindOneExecutableChildren().length ; i ++) {
				String spawnedValue = getFindOneExecutableChildren()[i].localGet(key);
				if(spawnedValue != null)
					return spawnedValue;
			}
		}
		return null;
	}
	*/
	
	/*
	 * 
	 * Page
	 * 
	 * 
	public final String get(String key) throws MissingVariableException {
		if(isComplete()) {
			//Executable[] children = getChildren();
			for(int i = 0 ; i < getFindOneExecutableChildren().length ; i ++) {
				String localValue = getFindOneExecutableChildren()[i].localGet(key);
				if(localValue != null) {
					return localValue;
				}
			}
		}
		if(hasSource()) {
			if(getSource().getName().equals(key)) {
				return getSource().getValue();
			}
		}
		return variables.get(key);
	}
	
	public final boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
	}
	*/
}
