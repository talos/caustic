package net.microscraper.database;

public class DatabaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4737299738008794427L;
	public DatabaseException(String message) { super(message); }
	
	public static class ResourceNotFoundException extends DatabaseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2178488029152395826L;
		public ResourceNotFoundException(Reference ref) {
			super("Could not find resource '" + ref.toString() + "'");
		}
	}
}
