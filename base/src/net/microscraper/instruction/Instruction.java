package net.microscraper.instruction;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheTemplate;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.executable.Executable;
import net.microscraper.executable.FindManyExecutable;
import net.microscraper.executable.FindOneExecutable;
import net.microscraper.executable.PageExecutable;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.regexp.RegexpException;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public abstract class Instruction  {
	
	/**
	 * Key for {@link #findManys} when deserializing from JSON.
	 */
	public static final String FINDS_MANY = "finds_many";

	/**
	 * Key for {@link #findOnes} when deserializing from JSON.
	 */
	public static final String FINDS_ONE = "finds_one";

	/**
	 * Key for {@link #spawnPages} when deserializing from JSON.
	 */
	public static final String THEN = "then";
	
	/**
	 * Key for {@link #name} value when deserializing from JSON.
	 */
	public static final String NAME = "name";
	
	/**
	 * Key for {@link #shouldSaveValue} value when deserializing from JSON.
	 */
	public static final String SAVE = "save";
	
	private final MustacheTemplate name;
	
	/**
	 * The {@link JSONInterfaceObject} this {@link Instruction} was deserialized from,
	 * as a formatted {@link String}.
	 */
	private final String formattedJSON;
	
	private final boolean shouldSaveValue;
	
	/**
	 * 
	 * @return Whether values resulting from the execution of this {@link Instruction}
	 * should be stored in the {@link Database}.
	 * @see #defaultShouldSaveValue()
	 */
	/*public boolean shouldSaveValue() {
		return shouldSaveValue;
	}*/
	
	/**
	 * 
	 * @return Whether {@link #shouldSaveValue()} should be <code>true</code>
	 * or <code>false</code> by default.
	 * @see #shouldSaveValue
	 */
	public abstract boolean defaultShouldSaveValue();
	
	/**
	 * @return A {@link MustacheTemplate} attached to this particular {@link Find} {@link Instruction}.
	 * Is <code>null</code> if it has none.
	 * @see {@link #hasName}
	 */
	/*public final MustacheTemplate getName() {
		return name;
	}*/

	/**
	 * Whether this {@link Find} {@link Instruction} has a {@link #name}.
	 * @see {@link #name}
	 */
	/*public final boolean hasName() {
		if(getName() == null)
			return false;
		return true;
	}*/

	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param jsonObject The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Instruction(JSONInterfaceObject jsonObject) throws DeserializationException, IOException {
		try {
			this.formattedJSON = jsonObject.toString();
			
			if(jsonObject.has(NAME)) {
				name = new MustacheTemplate(jsonObject.getString(NAME));
			} else {
				name = null;
			}
			if(jsonObject.has(SAVE)) {
				shouldSaveValue = jsonObject.getBoolean(SAVE);
			} else {
				shouldSaveValue = this.defaultShouldSaveValue();
			}
			
			if(jsonObject.has(FINDS_MANY)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(FINDS_MANY)) {
					findManys = new FindMany[] {
							new FindMany(jsonObject.getJSONObject(FINDS_MANY))
					};
				} else {
					JSONInterfaceArray array = jsonObject.getJSONArray(FINDS_MANY);
					findManys = new FindMany[array.length()];
					for(int i = 0 ; i < findManys.length ; i ++) {
						findManys[i] = new FindMany(array.getJSONObject(i));
					}
				}
			} else {
				findManys = new FindMany[] {};
			}
			
			if(jsonObject.has(FINDS_ONE)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(FINDS_ONE)) {
					findOnes = new FindOne[] {
							new FindOne(jsonObject.getJSONObject(FINDS_ONE))
					};
				} else {
					JSONInterfaceArray array = jsonObject.getJSONArray(FINDS_ONE);
					findOnes = new FindOne[array.length()];
					for(int i = 0 ; i < findOnes.length ; i ++) {
						findOnes[i] = new FindOne(array.getJSONObject(i));
					}
				}					
			} else {
				findOnes = new FindOne[0];
			}
			
			if(jsonObject.has(THEN)) {
				
				// If the key refers directly to an object, it is considered
				// an array of 1.
				if(jsonObject.isJSONObject(THEN)) {
					JSONInterfaceObject obj = jsonObject.getJSONObject(THEN);
					this.spawnPages = new Page[] { new Page(obj) };
				} else {
					final JSONInterfaceArray array = jsonObject.getJSONArray(THEN);
					
					Vector pages = new Vector();
					
					for(int i = 0 ; i < array.length() ; i ++) {
						//scrapers[i] = new Scraper(array.getJSONObject(i));
						JSONInterfaceObject obj = array.getJSONObject(i);
						pages.add(new Page(obj));
					}
					this.spawnPages = new Page[pages.size()];
					pages.copyInto(this.spawnPages);
				}						

			} else {
				this.spawnPages = new Page[] {};
			}
			
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch(MustacheTemplateException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}

	private final FindMany[] findManys;
	private final FindOne[] findOnes;
	private final Page[] spawnPages;
	
	/**
	 * @return {@link #formattedJSON}
	 */
	public String toString() {
		return formattedJSON;
	}
	

	/**
	 * @param sources The {@link Result} array from which to generate children.
	 * @return An array of {@link Executable[]}s whose parent is this execution.
	 * Later accessible through {@link #getChildren}.
	 * @throws MustacheTemplateException If a {@link MustacheTemplate} cannot be parsed.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws IOException If there was an error loading the {@link Instruction} for one of the children.
	 * @throws DeserializationException If there was an error deserializing the {@link Instruction} for one
	 * of the children.
	 * @see #generateResource
	 * @see #generateResult
	 * @see #getChildren
	 */
	public final Executable[] generateChildren(RegexpCompiler compiler, Browser browser,
			Variables variables, Result[] sources, Database database)
				throws MissingVariableException, DeserializationException, IOException {
		Vector children = new Vector();
		//Vector findOneExecutables = new Vector();
		
		for(int i = 0; i < sources.length ; i++) {
			Result source = sources[i];
			for(int j = 0 ; j < findOnes.length ; j ++) {
				FindOneExecutable findOneExecutable = new FindOneExecutable(
						findOnes[j], compiler, browser, variables, source, database);
				//findOneExecutables.add(findOneExecutable);
				children.add(findOneExecutable);
			}
			for(int j = 0 ; j < findManys.length ; j ++) {
				children.add(new FindManyExecutable(findManys[j], compiler,
						browser, variables, source, database));
			}
			for(int j = 0 ; j < spawnPages.length ; j ++) {
				children.add(new PageExecutable(spawnPages[j], compiler,
						browser, variables, source, database));
			}
		}
		
		//this.findOneExecutableChildren = new FindOneExecutable[findOneExecutables.size()];
		//findOneExecutables.copyInto(this.findOneExecutableChildren);
		
		Executable[] childrenAry = new Executable[children.size()];
		children.copyInto(childrenAry);
		return childrenAry;
	}

	/**
	 * @param compiler The {@link RegexpCompiler} to parse with.
	 * @param browser The {@link Browser} to load with.
	 * @param variables The {@link Variables} to execute using.
	 * @param source The {@link String} to use as a source.  Can be <code>null</code>.
	 * @return An array of {@link String}s from executing this particular {@link Instruction}.  Will be passed to
	 * {@link generateChildren}.
	 * @throws MissingVariableException If a tag needed for this execution is not accessible amongst the
	 * {@link Executable}'s {@link Variables}.
	 * @throws BrowserException
	 * @throws RegexpException
	 * @throws DatabaseException
	 */
	public Result[] execute(RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database) throws MissingVariableException,
			BrowserException, RegexpException, DatabaseException {
		String[] resultValues = generateResultValues(compiler, browser, variables, source.getValue());
		Result[] results = new Result[resultValues.length];
		for(int i = 0 ; i < resultValues.length ; i ++) {
			if(source == null) {
				results[i] = database.store(name.compile(variables), resultValues[i], i, shouldSaveValue);	
			} else {
				results[i] = database.store(source, name.compile(variables), resultValues[i], i, shouldSaveValue);
			}
		}
		return results;
	}
	
	protected abstract String[] generateResultValues(RegexpCompiler compiler, Browser browser,
			Variables variables, String source) throws MissingVariableException,
			BrowserException, RegexpException;
	
	protected abstract String getDefaultName(Variables variables, RegexpCompiler compiler,
			Browser browser) throws MissingVariableException, RegexpException;
}
