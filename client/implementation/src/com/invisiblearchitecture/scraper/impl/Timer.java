package com.invisiblearchitecture.scraper.impl;

import java.util.Calendar;

public abstract class Timer {
	public boolean isActive() {
		return isActive(Calendar.getInstance());
	}
	protected abstract boolean isActive(Calendar now);
	
	public static Timer alwaysOn() {
		return new Timer() {
			@Override
			protected boolean isActive(Calendar now) { return true; }
		};
	}
}
