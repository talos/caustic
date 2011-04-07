package net.microscraper.client.impl;

import net.microscraper.client.Interfaces.Logger;

public class SystemLogInterface implements Logger {
	public void i(String infoText) {
		System.out.print("Info: " + infoText);
		System.out.println();
	}

	public void e(Throwable e) {
		System.out.print("Error: " +  e.getMessage());
		System.out.println();
	}
	
	public void w(Throwable w) {
		System.out.print("Warning: " +  w.getMessage());
		System.out.println();
	}
}
