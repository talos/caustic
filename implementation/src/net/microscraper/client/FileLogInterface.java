package net.microscraper.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import net.microscraper.client.LogInterface;


public class FileLogInterface implements LogInterface {
	
	private final String pathToLogFile;
	private File logFile;
	private PrintWriter logWriter;
	
	private final LogInterface subLogger = new SystemLogInterface();
	
	public FileLogInterface(String path) {
		pathToLogFile = path;
	}
	
	public void open() throws IOException {
		logFile = new File(pathToLogFile);
		//outputStream = new BufferedOutputStream(new FileOutputStream(logFile));
		 logWriter = new PrintWriter(logFile);
	}
	
	public void close() throws IOException {
		logWriter.close();
	}
	
	@Override
	public void e(String errorText, Throwable e) {
		Date now = new Date();
		errorText = now + ": " + errorText;
		subLogger.e(errorText, e);
		
		e.printStackTrace(logWriter);
		logWriter.print("Error: " + errorText);
		logWriter.println();
	}

	@Override
	public void i(String infoText) {
		Date now = new Date();
		infoText = now + ": " + infoText;
		
		subLogger.i(infoText);
		logWriter.print("Info: " + infoText);
		logWriter.println();
	}

}
