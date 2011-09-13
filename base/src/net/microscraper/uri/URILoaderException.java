package net.microscraper.uri;

import java.io.IOException;

import net.microscraper.http.HttpException;

/**
 * This {@link Exception} is thrown when {@link URILoader} cannot load
 * its URI.
 * @author realest
 *
 */
public class URILoaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6528522046789038329L;

	protected URILoaderException(String message) {
		super(message);
	}

	/**
	 * Construct a {@link URILoaderException} for when a local file could
	 * not be loaded.
	 * @param e An {@link IOException}.
	 */
	public static URILoaderException fromLocal(IOException e) {
		return new URILoaderException(e.getMessage());
	}
	
	/**
	 * Construct a {@link URILoaderException} for when a remote file could
	 * not be loaded.
	 * @param e An {@link HttpException}.
	 */
	public static URILoaderException fromRemote(HttpException e) {
		return new URILoaderException(e.getMessage());
	}
}
