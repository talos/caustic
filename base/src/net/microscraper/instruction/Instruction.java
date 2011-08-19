package net.microscraper.instruction;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.Logger;
import net.microscraper.database.Database;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonObject;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;
import net.microscraper.util.VectorUtils;

/**
 * {@link Instruction}s hold instructions for {@link Execution}s.
 * @author realest
 *
 */
public final class Instruction  {
	

	/**
	 * Key for {@link #minMatch} value when deserializing from JSON.
	 */
	public static final String MIN_MATCH = "min";
	
	/**
	 * Key for {@link #maxMatch} value when deserializing from JSON.
	 */
	public static final String MAX_MATCH = "max";
	
	/**
	 * The {@link MustacheTemplate} name for this {@link Instruction}.  Can be <code>null</code>.
	 * Name should therefore be retrieved through {@link #getName(Variables)}.
	 * @see #getName(Variables, Browser, RegexpCompiler)
	 */
	private final MustacheTemplate name;
	
	/**
	 * An array of {@link Find}s dependent upon this {@link Instruction}.
	 */
	private final Find[] finds;
	
	/**
	 * An array of {@link Load}s dependent upon this {@link Instruction}.
	 */
	private final Load[] loads;
	
	/**
	 * Whether or not this {@link Instruction} should save the values of its results.
	 */
	private final boolean shouldSaveValue;
	
	/**
	 * 
	 * @param shouldSaveValue Whether this {@link Instruction}'s results' values should be
	 * saved. <code>True</code> if they should be, <code>false</code> otherwise.
	 * @param name The {@link MustacheTemplate} that will be compiled and used as the name of this
	 * {@link Instruction}'s {@link Result}s. 
	 * @param finds An array of {@link Find}s launched by this {@link Instruction}.
	 * @param finds An array of {@link Load}s launched by this {@link Instruction}.
	 */
	public Instruction(boolean shouldSaveValue,
			MustacheTemplate name, Find[] finds, Load[] loads) {
		this.shouldSaveValue = shouldSaveValue;
		this.name = name;
		this.finds = finds;
		this.loads = loads;
	}
	
	/**
	 * @return The raw {@link MustacheTemplate} string of this {@link Instruction}'s {@link #name}.
	 */
	public String toString() {
		return name.toString();
	}
	
	public Executable bind(Variables variables, String source) {
		return new Executable();
	}
	
	/**
	 * Generate the array of {@link Result}s from executing this {@link Instruction}
	 * using a {@link Result} source.
	 * @param source The {@link Result} source for the execution.
	 * @return The array of {@link Result}s from executing this {@link Instruction}.
	 * @throws InterruptedException if the user interrupts execution.
	 */
	/*public Result[] execute(Result source) throws InterruptedException {
		
		Vector queue = new Vector();
		queue.add(executable.execute(source.getValue(), source));
		
		// Run queue.
		while(queue.size() > 0) {
			Execution exc = (Execution) queue.elementAt(0);
			queue.removeElementAt(0);
			
			if(exc.isComplete()) {
				String[] resultValues = exc.getResults();
				for(int i = 0; i < resultValues.length; i ++) {
					new Result(0, name, resultValues[i]);
					for(int j = 0 ; j < children.length ; j++) {
						
						children[j].execute(
					}
				}
			} else if(exc.isMissingVariables()) {
				
			} else if(exc.hasFailed()) {
				
			}
			
			/*if(log != null) {
				log.i("Running " + exc.toString());
			}
			exc.run();*/
			
			// If the execution is complete, add its children to the queue.
			/*if(exc.isComplete()) {
				VectorUtils.arrayIntoVector(exc.getChildren(), queue);
			} else if (exc.isStuck()) {
				if(log != null) {
					log.i(StringUtils.quote(exc.toString()) + " is stuck on " + StringUtils.quote(exc.stuckOn()));
				}
			} else if (exc.hasFailed()) {
				if(log != null) {
					log.w(exc.failedBecause());
				}
			// If the execution is not stuck and is not failed, return it to the end queue.
			} else {
				queue.addElement(exc);
			}
			
			if(!exc.isComplete() && !exc.isStuck() && !exc.hasFailed()) {
				queue.addElement(exc);
			}*/
		//}
	//}
	
	/**
	 * Execute this {@link Instruction}, including all its children.
	 * @param variables The {@link Variables} to use when compiling {@link MustacheTemplate}s.
	 * @param source The {@link Result} source for this execution.  Can be <code>null</code>.
	 */
	/*public void execute(RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database,
			Logger log) {
		// Create & initially stock queue.
	}*/
	
	/**
	 * Generate the children of this {@link Instruction} during execution.  There will be as many children
	 * as the product of <code>sources</code> and {@link #children}.
	 * @param sources The {@link Result} array from which to generate children.
	 * @return An array of {@link Executable[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws IOException If there was an error loading the {@link Instruction} for one of the children.
	 * @throws DeserializationException If there was an error deserializing the {@link Instruction} for one
	 * of the children.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	/*public Execution[] generateChildExecutables(RegexpCompiler compiler, Browser browser,
			Execution parent, Result[] sources, Database database)
				throws DeserializationException, IOException {
		Execution[] childExecutables = new Execution[sources.length * children.length];
		
		for(int i = 0; i < sources.length ; i++) {
			Result source = sources[i];
			for(int j = 0 ; j < children.length ; j++) {
				childExecutables[(i * children.length) + j] =
					new Execution(children[j], compiler, browser, parent, source, database);
			}
		}
		return childExecutables;
	}*/

	/**
	 * @param compiler The {@link RegexpCompiler} to parse with.
	 * @param browser The {@link Browser} to load with.
	 * @param variables The {@link Variables} to execute using.
	 * @param source The {@link String} to use as a source.  Can be <code>null</code>.
	 * @return An array of {@link String}s from executing this particular {@link Instruction}.  Will be passed to
	 * {@link generateChildren}.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Execution}'s {@link Variables}.
	 * @throws BrowserException If the {@link Browser} experienced an exception loading.
	 * @throws RegexpException If there was a problem matching with {@link RegexpCompiler}.
	 * @throws DatabaseException If there was a problem storing data in {@link Database}.
	 */
	/*public final Result[] generateResults(RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database) throws RegexpException {
		String[] resultValues;
		if(source == null) {
			resultValues = generateResultValues(compiler, browser, variables, null);
		} else {
			resultValues = generateResultValues(compiler, browser, variables, source.getValue());
		}
		Result[] results = new Result[resultValues.length];
		for(int i = 0 ; i < resultValues.length ; i ++) {
			//String name = getName(variables, browser, compiler);
			int id;
			if(source == null) {
				id = database.store(name, shouldSaveValue ? resultValues[i] : null, i);
			} else {
				id = database.store(source.getName(), source.getId(), name, shouldSaveValue ? resultValues[i] : null, i);
			}
			results[i] = new Result(id, name, resultValues[i]);
		}
		return results;
	}*/
}
