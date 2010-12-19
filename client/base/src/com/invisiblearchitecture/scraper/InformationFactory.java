package com.invisiblearchitecture.scraper;

/**
 * Creates new Information objects ready for collection from a namespace & type.
 * @author john
 *
 */
public interface InformationFactory {
	public abstract Information get(String namespace, String type);
}
