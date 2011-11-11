package net.microscraper.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;

/**
 * An implementation of {@link Executor} that does not block on {@link #execute(Executable)}.
 * @author talos
 *
 */
public class AsyncExecutor implements Executor {

	private final ExecutorService service;
	private List<Executable> stuckExecutables = Collections.synchronizedList(new ArrayList<Executable>());
	private List<Future<Executable>> futures = new ArrayList<Future<Executable>>();
	
	public AsyncExecutor(int nThreads) {
		service = Executors.newFixedThreadPool(nThreads);
	}
	
	/**
	 * Doesn't block!
	 */
	@Override
	public void execute(Instruction instruction, DatabaseView view, String source, HttpBrowser browser)
			throws InterruptedException, DatabaseException {
		view.addListener(new AsyncExecutorListener(this));
		execute(new Executable(instruction, view, source, browser));
	}
	
	void execute(Executable executable) throws DatabaseReadException {
		if(executable.isStuck()) {
			stuckExecutables.add(executable);
		} else {
			futures.add(service.submit(new CallableExecutable(executable, this)));
		}
	}
	
	/**
	 * Check the {@link stuckExecutables} for an {@link Executable} that is stuck on that key,
	 * and re-submit it.
	 * @param key The {@link String} key for a variable that was just found.
	 * @param view The {@link DatabaseView} for which the variable was found.
	 */
	void kick(String key) throws DatabaseReadException {
		synchronized(stuckExecutables) {			
			Iterator<Executable> iter = stuckExecutables.iterator();
			while(iter.hasNext()) {
				Executable executable = iter.next();
				List<String> missingTags = Arrays.asList(executable.getMissingTags());
				
				// If one of the missing tags was found, then check to see if they are all present in view.
				if(missingTags.contains(key)) {
					// reexecute and remove from list if we're no longer missing tags.
					if(!executable.isStuck()) {
						execute(executable);
						iter.remove();					
					}
				}
			}
		}
	}
}
