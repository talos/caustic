package net.microscraper.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.Interfaces;
import net.microscraper.MissingVariableException;
import net.microscraper.Mustache;
import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.regexp.PatternInterface;

/**
 * When {@link #run}, {@link PageExecutable} makes an HTTP request according to
 * the instructions from {@link Page}.
 * @author john
 *
 */
public class PageExecutable extends BasicExecutable {
	
	private final Variables extendedVariables;
	private final Browser browser;
	
	public PageExecutable(Interfaces interfaces,
			Page page, Variables extendedVariables, Result source) {
		super(interfaces, page, source);
		browser = interfaces.getBrowser();
		this.extendedVariables = extendedVariables;
	}
	/**
	 * @param page The {@link Page} {@link Instruction} whose {@link Page#method} should be executed.
	 * @return The body of the page, if the {@link PageExecutable}'s {@link Page.method} is
	 * {@link Page.Method.GET} or {@link Page.Method.POST}; <code>Null</code> if it is
	 * {@link Page.Method.HEAD}.
	 */
	/*private String doMethod(Page page) throws MissingVariableException, ExecutionFailure {
		
	}*/
	
	protected String[] generateResultValues() 
			throws MissingVariableException, MustacheTemplateException,
			ExecutionFailure {
		Page page = (Page) getInstruction();
		browser.enableRateLimit();
		return new String[] { page.getResponse(browser, this) };
	}

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
		return extendedVariables.get(key);
	}
	
	public final boolean containsKey(String key) {
		try {
			get(key);
			return true;
		} catch(MissingVariableException e) {
			return false;
		}
	}

	protected boolean generatesManyResults() {
		return false;
	}
	
	protected String getDefaultName() throws MustacheTemplateException,
			MissingVariableException {
		return getURL();
	}
}
