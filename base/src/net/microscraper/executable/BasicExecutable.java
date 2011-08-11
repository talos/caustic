package net.microscraper.executable;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.Interfaces;
import net.microscraper.Log;
import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Utils;
import net.microscraper.Variables;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.regexp.RegexpCompiler;

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
public abstract class BasicExecutable extends Log implements Executable {
	private final Instruction instruction;
	private final Result source;
	//private final Interfaces interfaces;
	private final Browser browser;
	private final RegexpCompiler compiler;
	private final Database database;
	
	private Result[] results = null;
	private Executable[] children = null;
	private FindOneExecutable[] findOneExecutableChildren = null;
	
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
	 * @param source The {@link Result} which is the source of this {@link Executable}.  Can
	 * be <code>null</code> if there was none.
	 * @param database The {@link Database} to use when storing {@link Result}s.
	 * @see #run
	 */
	protected BasicExecutable(Instruction instruction, RegexpCompiler compiler,
			Browser browser, Result source, Database database) {
		this.instruction = instruction;
		this.compiler = compiler;
		this.browser = browser;
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
					String[] resultValues = generateResultValues();
					results = new Result[resultValues.length];
					//getInterfaces().getLog()
					//	.i(toString() + " has " + results.length + " results");
					for(int i = 0 ; i < resultValues.length ; i ++) {
						if(hasSource()) {
							results[i] = interfaces.getDatabase().store(
									getSource(),
									getName(),
									resultValues[i],
									i, getInstruction().shouldSaveValue());
						} else {
							results[i] = interfaces.getDatabase().store(
									getName(), resultValues[i], i,
									getInstruction().shouldSaveValue());							
						}
					}
				}
				if(results != null) {
					children = generateChildren(results);
					isComplete = true;
				}
			} catch(ExecutionFailure e) {
				handleFailure(e);
			} catch(MustacheTemplateException e) {
				handleFailure(new ExecutionFailure(e));
			} catch(MissingVariableException e) {
				handleMissingVariable(e);
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
	 * Must be overriden by {@link BasicExecutable} subclass.
	 * @return An array of {@link String}s from executing this particular {@link Executable}.  Will be passed to
	 * {@link generateChildren}.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws ExecutionFailure If there is some other exception that prevents this {@link Executable} from
	 * running successfully.  Use {@link ExecutionFailure#getCause} to determine why.
	 * @see #generateResource
	 * @see #generateChildren
	 */
	protected abstract String[] generateResultValues() throws
			MissingVariableException, MustacheTemplateException, ExecutionFailure;
	
	
	protected final FindOneExecutable[] getFindOneExecutableChildren() {
		return this.findOneExecutableChildren;
	}
	
	
	public final Instruction getInstruction() {
		return instruction;
	}

	
	public final boolean hasSource() {
		if(source != null) {
			return true;
		}
		return false;
	}
	
	
	public final Result getSource() {
		if(hasSource()) {
			return source;
		} else {
			throw new NullPointerException();
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
		return getInstruction().toString();
	}
	
	public final Executable[] getChildren() throws IllegalStateException {
		if(isComplete()) {
			return children;
		} else {
			throw new IllegalStateException();
		}
	}
	/*
	public Result[] getResults() throws IllegalStateException {
		if(isComplete()) {
			return results;
		}
		throw new IllegalStateException();
	}
	*/
	/**
	 * 
	 * @return The {@link Instruction#getName()}, compiled through
	 * {@link Mustache}, if {@link Instruction#hasName()} is <code>true</code>.
	 * Returns the {@link Instruction#getLocation()} as a {@link String} otherwise.
	 * @throws MustacheTemplateException If {@link Instruction#getName()} is an invalid {@link MustacheTemplate}.
	 * @throws MissingVariableException If the {@link Instruction#getName()}
	 * cannot be compiled with {@link #getVariables()}.
	 */
	public String getName() throws MissingVariableException,
			MustacheTemplateException {
		if(getInstruction().hasName()) {
			return getInstruction().getName().compile(this);
		} else {
			//return getInstruction().getLocation().toString();
			return getDefaultName();
		}
	}
	
	/**
	 * 
	 * @return The default {@link #getName()} for an {@link Executable}.
	 * @throws MustacheTemplateException If the default name has an invalid template.
	 * @throws MissingVariableException If the available {@link Variables} cannot
	 * compile the default name.
	 */
	protected abstract String getDefaultName() throws MustacheTemplateException, MissingVariableException;
}
