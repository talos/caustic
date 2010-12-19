package com.invisiblearchitecture.scraper.impl;

import com.invisiblearchitecture.scraper.LogInterface;

public class SystemLogInterface implements LogInterface {
	@Override
	public void e(String errorText, Throwable e) {
		e.printStackTrace();
		System.out.print("Error: " +  errorText);
		System.out.println();
	}

	@Override
	public void i(String infoText) {
		System.out.print("Info: " + infoText);
		System.out.println();
	}
}
