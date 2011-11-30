package net.caustic;

public class Resume implements Runnable {
	
	private final Executable executable;
	private final AbstractScraper scraper;
	
	Resume(AbstractScraper scraper, Executable executable) {
		this.scraper = scraper;
		this.executable = executable;
	}
	
	/**
	 * Confirm the instruction for scraping.
	 */
	public void run() {
		scraper.submit(executable);
	}

}
