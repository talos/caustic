package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.BasicVariables;
import net.microscraper.NameValuePair;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.test.TestUtils;

import org.junit.Test;

public class PageTest {
	
	private final static int LENGTH = 10;
	
	@Mocked JSONInterfaceObject obj;
	@Mocked Database database;
	@Mocked Browser browser;
	@Mocked RegexpCompiler compiler;

	@Test
	public void testByDefaultDoesntSaveValue() throws Exception {
		final String url = TestUtils.makeRandomString(LENGTH);
		new NonStrictExpectations() {
			{
				obj.getString(Page.URL); result = url;
			}
		};
		Page page = new Page(obj);
		page.execute(compiler, browser, new BasicVariables(), null, database);
		
		new Verifications() {
			{
				database.store(anyString, (String) withNull(), 0); $ = "Stored page response in database by default.";
			}
		};
	}
	
	@Test
	public void testSavesValue() throws Exception {
		final String url = TestUtils.makeRandomString(LENGTH);
		final String content = TestUtils.makeRandomString(LENGTH);
		new NonStrictExpectations() {
			{
				obj.getString(Page.URL); result = url;
				obj.has(Instruction.SAVE); result = true; 
				obj.getBoolean(Instruction.SAVE); result = true;
				browser.get(url, (NameValuePair[]) any, (NameValuePair[]) any, (PatternInterface[]) any); result = content;
			}
		};
		Page page = new Page(obj);
		page.execute(compiler, browser, new BasicVariables(), null, database);
		
		new Verifications() {
			{
				database.store(anyString, content, 0); $ = "Did not store page response in database.";
			}
		};
	}
}
