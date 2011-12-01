package net.caustic.database;

class FailedExecution {

	private final String failedBecause;
	
	FailedExecution(String source, String instruction, String uri, String failedBecause) {
		this.failedBecause = failedBecause;
	}

}
