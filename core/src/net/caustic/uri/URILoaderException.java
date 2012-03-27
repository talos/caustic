package net.caustic.uri;

import java.io.IOException;

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
	public static URILoaderException fromLocal(IOException e, String message) {
		return new URILoaderException("Could not load local URI: " + e.getMessage() + ", " + message);
	}
	
	/**
	 * Construct a {@link URILoaderException} for when a remote file could
	 * not be loaded.
	 * @param e An {@link IOException}.
	 */
	public static URILoaderException fromRemote(IOException e, String message) {
		return new URILoaderException("Could not load remote URI: " + e.getMessage() + ", " + message);
	}
}
