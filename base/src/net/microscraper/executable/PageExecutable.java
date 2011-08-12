package net.microscraper.executable;

import java.io.UnsupportedEncodingException;

import net.microscraper.MissingVariableException;
import net.microscraper.MustacheNameValuePair;
import net.microscraper.MustacheTemplateException;
import net.microscraper.Variables;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * When {@link #run}, {@link PageExecutable} makes an HTTP request according to
 * the instructions from {@link Page}.
 * @author john
 *
 */
public class PageExecutable extends BasicExecutable {
	
	private final Variables variables;
	
	public PageExecutable(Page page, RegexpCompiler compiler, Browser browser,
			Variables variables, Result source, Database database) {
		super(page, compiler, browser, source, database);
		this.variables = variables;
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
	
}
