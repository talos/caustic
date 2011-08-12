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

import org.junit.Test;

public class PageTest {

	@Mocked JSONInterfaceObject obj;
	@Mocked Database database;
	@Mocked Browser browser;
	@Mocked RegexpCompiler compiler;
	//@Tested Page page;
	

	@Test
	public void testByDefaultDoesntSaveValue() throws Exception {
		final String google = "http://www.google.com";
		new NonStrictExpectations() {
			{
				obj.getString(Page.URL); result = google;
			}
		};
		Page page = new Page(obj);
		page.execute(compiler, browser, new BasicVariables(), null, database);
		
		new Verifications() {
			{
				database.store(anyString, anyString, 0, false);
			}
		};
	}
	

	@Test
	public void testCanSaveValue() throws Exception {
		final String google = "http://www.google.com";
		final String content = "Google!";
		new NonStrictExpectations() {
			{
				obj.getString(Page.URL); result = google;
				obj.has(Instruction.SAVE); result = true; 
				obj.getBoolean(Instruction.SAVE); result = true;
				browser.get(google, (NameValuePair[]) any, (NameValuePair[]) any, (PatternInterface[]) any); result = content;
			}
		};
		Page page = new Page(obj);
		page.execute(compiler, browser, new BasicVariables(), null, database);
		
		new Verifications() {
			{
				database.store(anyString, anyString, 0, true);
			}
		};
	}
}
