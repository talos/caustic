package net.microscraper.database;

import net.microscraper.util.UUID;

/**
 * {@link Scope} is used by {@link Database} to determine what is visible.
 * @author realest
 *
 */
public class Scope {
	private final UUID id;
	//private final String name;
	
	/**
	 * 
	 * @param id A unique {@link UUID} for the {@link Scope}.
	 * @param name The {@link String} name of the scope.
	 */
	/*public Scope(UUID id, String name) {
		this.id = id;
		this.name = name;
	}*/
	
	/**
	 * @return The {@link String} name associated with {@link Scope}.
	 */
	/*public String getName() {
		return name;
	}*/
	
	/**
	 * Alias for {@link #getName()}
	 */
	/*public String toString() {
		return getName();
	}*/
	
	
	/**
	 * 
	 * @param id A unique {@link UUID} for the {@link Scope}.
	 */
	public Scope(UUID id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return The {@link UUID} associated with {@link Scope}.
	 */
	public UUID getID() {
		return id;
	}
	
	/**
	 * {@link Scope} uses its {@link #getID}'s {@link UUID#asInt()} method to
	 * provide a unique hash code.
	 */
	public int hashCode() {
		return getID().asInt();
	}
	
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Scope) {
			Scope other = (Scope) obj;
			return other.hashCode() == this.hashCode();
		}
		return false;
	}
}
