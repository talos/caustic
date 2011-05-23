package net.microscraper.client.executable;

import net.microscraper.client.ExecutionContext;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.server.resource.Ref;

/**
 * {@link ScraperExecutableChild} is a {@link ScraperExecutable} subclass spawned
 * by a {@link ScraperExecutable} (including other {@link ScraperExecutableChild}ren
 *  or a {@link LeafExecutable}.
 * @see ScraperExecutable
 * @see LeafExecutable
 * @author john
 *
 */
public final class ScraperExecutableChild extends ScraperExecutable {
	private final String extraName;
	private final String extraValue;
	private final Variables parentVariables;
	
	public ScraperExecutableChild(ExecutionContext context, 
			Ref pipe, Executable parent, Variables parentVariables) {
		super(context, pipe, new UnencodedNameValuePair[] { }, parent);
		//super(log,resourceLoader,browser, regexpInterface, pipe, , parent );
		this.extraName = null;
		this.extraValue = null;
		this.parentVariables = parentVariables;
	}
	public ScraperExecutableChild(ExecutionContext context, 
			Ref pipe, Executable parent, Variables parentVariables,
			String extraName, String extraValue) {
		super(context, pipe,
				new UnencodedNameValuePair[] { 
					new UnencodedNameValuePair(extraName, extraValue)
				}, parent);
		//super(pipe, context, new UnencodedNameValuePair[] { new UnencodedNameValuePair(extraName, extraValue) }, parent);
		this.extraName = extraName;
		this.extraValue = extraValue;
		this.parentVariables = parentVariables;
	}
	
	public String get(String key) throws MissingVariableException {
		if(super.containsKey(key) == false) {
			return parentVariables.get(key);
		} else{
			return super.get(key);
		}
	}
	
	public boolean containsKey(String key) {
		if(super.containsKey(key) == false) {
			return parentVariables.containsKey(key);
		}
		return true;
	}

	public boolean hasName() {
		if(extraName != null)
			return true;
		return false;
	}

	public String getName() {
		if(hasName())
			return extraName;
		return null;
	}

	public boolean hasValue() {
		if(extraValue != null)
			return true;
		return false;
	}

	public String getValue() {
		if(hasValue())
			return extraValue;
		return null;
	}
}
