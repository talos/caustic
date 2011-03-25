package net.microscraper.database;

public class DatabaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4737299738008794427L;

	public static class ModelNotFoundException extends DatabaseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4961650071060423898L;
		
	}
	
	public static class ResourceNotFoundException extends DatabaseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2178488029152395826L;
		
	}
	
	public static class PrematureRevivalException extends DatabaseException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4352576432005070821L;
		
	}
}
