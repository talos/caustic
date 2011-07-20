package net.microscraper.impl.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import net.microscraper.Utils;
import net.microscraper.interfaces.log.Logger;

public class FileLogInterface implements Logger {
	
	//private final String pathToLogFile;
	private final File logFile;
	private PrintWriter logWriter;
	
	public FileLogInterface(File logFile) {
		this.logFile = logFile;
	}
	
	public void open() throws IOException {
		//logFile = new File(pathToLogFile);
		logWriter = new PrintWriter(logFile);
	}
	
	public void close() throws IOException {
		logWriter.close();
	}
	
	public void e(Throwable e) {
		Date now = new Date();
		
		e.printStackTrace(logWriter);
		logWriter.print(now + " Error: " + Utils.truncate(e.getMessage(), MAX_ENTRY_LENGTH));
		logWriter.println();
	}

	public void i(String infoText) {
		Date now = new Date();
		logWriter.print(now + " Info: " + Utils.truncate(infoText, MAX_ENTRY_LENGTH));
		logWriter.println();
	}

	public void w(Throwable w) {
		Date now = new Date();
		logWriter.print(now + " Warning: " + Utils.truncate(w.getMessage(), MAX_ENTRY_LENGTH));
		logWriter.println();
	}
}
