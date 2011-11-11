package net.microscraper.concurrent;

import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;

public interface Executor {

	public abstract void execute(Instruction instruction, DatabaseView view, String source, 
			HttpBrowser browser) throws InterruptedException,
			DatabaseException;

}