package net.microscraper;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.executable.Executable;
import net.microscraper.executable.Executable;
import net.microscraper.instruction.Page;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * A microscraper {@link Client} can scrape a {@link Instruction}.
 * @author john
 *
 */
public final class Client extends Log {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle this warning differently
	
	private final RegexpCompiler compiler;
	private final Browser browser;
	private final Database database;
	
	/**
	 * @param compiler The {@link RegexpCompiler} to use when compiling regular
	 * expressions.
	 * @param browser A {@link Browser} to use for HTTP requests.
	 * @param database the {@link Database} to use for storage.
	 */
	public Client(RegexpCompiler compiler, Browser browser, Database database) {
		this.compiler = compiler;
		this.browser = browser;
		this.database = database;
	}
	
	private void scrape(Page page, NameValuePair[] extraVariables)
			throws ClientException {
		Executable pageExecutable = new Executable(
				page, compiler, browser, new BasicVariables(extraVariables),
				null, database);
		execute(pageExecutable);
	}
	
	/**
	 * 
	 * @param pageLocation A {@link JSONLocation} with the {@link Page}'s instructions.
	 * @param extraVariables An array of {@link NameValuePair}s to use initially.
	 * @throws ClientException If the {@link Page} could not be run.
	 */
	/*public void scrape(JSONLocation pageLocation, NameValuePair[] extraVariables)
			throws ClientException {
		try {
			Page page = new Page(interfaces.getJSONInterface().load(pageLocation));
			scrape(page, extraVariables);
		} catch(IOException e) {
			throw new ClientException(e);
		}
	}*/

	/**
	 * 
	 * @param pageInstructionJSON A {@link JSONInterfaceObject} with the {@link Page}'s instructions.
	 * @param extraVariables An array of {@link NameValuePair}s to use initially.
	 * @throws ClientException If the {@link Page} could not be run.
	 */
	public void scrape(JSONInterfaceObject pageInstructionJSON, NameValuePair[] extraVariables)
			throws ClientException {
		try {
			scrape(new Page(pageInstructionJSON), extraVariables);
		} catch (IOException e) {
			throw new ClientException(e);
		}
	}
	
	/**
	 * {@link #execute} runs an {@link Executable} and its children, publishing them after each run.
	 * @param rootExecutable The {@link Executable} to start with.
	 */
	private void execute(Executable rootExecutable) {
		Vector queue = new Vector();
		queue.add(rootExecutable);
		while(queue.size() > 0) {
			if(queue.size() > LARGE_QUEUE) {
				i("Large execution queue: " + Utils.quote(queue.size()));
			}
			Executable exc = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			i("Running " + exc.toString());
			exc.run();
			// If the execution is complete, add its children to the queue.
			if(exc.isComplete()) {
				Executable[] children = exc.getChildren();
				Utils.arrayIntoVector(children, queue);
			} else if (exc.isStuck()) {
				i(exc.toString() + " is stuck on " + exc.stuckOn());
			} else if (exc.hasFailed()) {
				w(exc.failedBecause());
			// If the execution is not stuck and is not failed, add it back to the queue.
			} else {
				queue.addElement(exc);
			}
		}
	}
}
