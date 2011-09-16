package net.microscraper.database;

/**
 * This {@link Exception} is the superclass for errors reading or writing
 * to {@link DatabaseView}.
 * @author realest
 *
 */
public class DatabaseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4737299738008794427L;
	
	protected DatabaseException(String message) {
		super(message);
	}
	
	protected DatabaseException(String message, Throwable e) {
		super(message);
		e.printStackTrace();
	}
}
