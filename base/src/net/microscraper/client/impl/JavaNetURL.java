package net.microscraper.client.impl;

import java.net.URL;

import net.microscraper.client.interfaces.URLInterface;

public class JavaNetURL implements URLInterface {
	private final URL url;
	public JavaNetURL(URL url) {
		this.url = url;
	}
	public String toString() {
		return this.url.toString();
	}
}
