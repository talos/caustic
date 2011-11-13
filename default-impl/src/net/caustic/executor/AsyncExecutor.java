package net.caustic.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseReadException;
import net.caustic.database.DatabaseView;
import net.caustic.executor.Executable;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.scope.Scope;

/**
 * An implementation of {@link Executor} that does not block on {@link #execute(Executable)}.
 * @author talos
 *
 */
public class AsyncExecutor {

	private final ExecutorService service;
	private List<Executable> stuckExecutables = Collections.synchronizedList(new ArrayList<Executable>());
	private List<String> failMessages = Collections.synchronizedList(new ArrayList<String>());
	//private List<Future<Executable>> futures = Collections.synchronizedList(new ArrayList<Future<Executable>>());
	private final Database db;
	
	private int submitted = 0;
	private int finished = 0;
	
	public AsyncExecutor(int nThreads, Database db) {
		service = Executors.newFixedThreadPool(nThreads);
		this.db = db;
		db.addListener(new AsyncExecutorListener(this));
	}
	
	/**
	 * Doesn't block!
	 */
	public void execute(Instruction instruction, Map<String, String> input,
			String source, HttpBrowser browser) throws DatabaseException {
		DatabaseView view = new DatabaseView(db);
		for(String key : input.keySet()) {
			view.put(key, input.get(key));
		}
		submit(new Executable(instruction, view, source, browser));
	}
	

	/**
	 * Wait for {@link AsyncExecutor} to wrap up.
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		while(submitted > finished + stuckExecutables.size()) {
			/*System.out.println(submitted);
			System.out.println(finished);
			System.out.println(stuckExecutables.size());*/
			if(service.isTerminated()) { // break if artificial termination
				break;
			}
			Thread.sleep(100);
		}
		//service.awaitTermination(100, TimeUnit.MINUTES);
	}
	
	/**
	 * Check the {@link stuckExecutables} for an {@link Executable} that is stuck on that key,
	 * and re-submit it. Thread-safe.
	 * @param key The {@link String} key for a variable that was just found.
	 * @param view The {@link DatabaseView} for which the variable was found.
	 */
	void kick(Scope scope, String key) throws DatabaseException {
		// this currently ignores scope, because it doesn't keep track of the database's
		// inheritance.
		synchronized(stuckExecutables) {			
			Iterator<Executable> iter = stuckExecutables.iterator();
			while(iter.hasNext()) {
				Executable executable = iter.next();
				List<String> missingTags = Arrays.asList(executable.getMissingTags());
				
				// If one of the missing tags was found, then check to see if they are all present in view.
				if(missingTags.contains(key)) {
					// reexecute and remove from list if we're no longer missing tags.
					if(!executable.isStuck()) {
						submit(executable);
						iter.remove();					
					}
				}
			}
		}
	}
		
	void finished(Executable executable, Executable[] children) {
		finished++;
		if(children != null) {
			for(Executable child : children) {
				submit(child);
			}
		} else if(executable.isMissingTags()) {
			synchronized(stuckExecutables) {
				stuckExecutables.add(executable);
			}
		} else {
			synchronized(failMessages) {
				failMessages.add(executable.getFailedBecause());
			}
		}
	}
	
	void notifyCrash(Throwable e) {
		e.printStackTrace();
		service.shutdownNow();
	}
	
	private void submit(Executable executable) {
		submitted++;
		service.submit(new AsyncExecutable(executable, this));
	}
}
