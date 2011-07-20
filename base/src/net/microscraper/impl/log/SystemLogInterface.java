package net.microscraper.impl.log;

import net.microscraper.Utils;
import net.microscraper.interfaces.log.Logger;

public class SystemLogInterface implements Logger {
	
	public void i(String infoText) {
		System.out.print("Info: " + Utils.truncate(infoText, MAX_ENTRY_LENGTH));
		System.out.println();
	}

	public void e(Throwable e) {
		System.out.print("Error: " +  Utils.truncate(e.getMessage(), MAX_ENTRY_LENGTH));
		System.out.println();
	}
	
	public void w(Throwable w) {
		System.out.print("Warning: " +  Utils.truncate(w.getMessage(), MAX_ENTRY_LENGTH));
		System.out.println();
	}
}
