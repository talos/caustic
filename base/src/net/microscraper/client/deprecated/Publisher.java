package net.microscraper.client.deprecated;

/**
 * Interface for publishing information.
 */
public interface Publisher {
	//public abstract void publishProgress(Information information, int progressPart, int progressTotal);
	public abstract void publish(Information information);
}