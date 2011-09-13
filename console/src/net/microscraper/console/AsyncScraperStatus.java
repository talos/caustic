package net.microscraper.console;

public class AsyncScraperStatus {

	private final int numSuccess;
	private final int numMissingTags;
	private final int numFailed;
	private final int numCrashed;
	private final int numWaiting;
	
	public AsyncScraperStatus(int numSuccess, int numMissingTags,
			int numFailed, int numCrashed, int numWaiting) {
		this.numSuccess = numSuccess;
		this.numMissingTags = numMissingTags;
		this.numFailed = numFailed;
		this.numCrashed = numCrashed;
		this.numWaiting = numWaiting;
	}
	
	public String toString() {
		return numSuccess + " successful, " + numMissingTags + " missing tags, " +
			numFailed + " failed, " + numCrashed + " crashed, " +
			numWaiting + " not yet executed.";
	}
}
