package net.microscraper.client.impl;

import net.microscraper.client.Interfaces.Logger;

public class SystemLogInterface implements Logger {
	@Override
	public void i(String infoText) {
		System.out.print("Info: " + infoText);
		System.out.println();
	}

	@Override
	public void e(Throwable e) {
		System.out.print("Error: " +  e.getMessage());
		System.out.println();
	}
	
	@Override
	public void w(Throwable w) {
		System.out.print("Warning: " +  w.getMessage());
		System.out.println();
	}
}
