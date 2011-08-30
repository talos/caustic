package net.microscraper.database;

import net.microscraper.util.UUID;

/**
 * {@link Scope} is used by {@link Database} to determine what is visible.
 * @author realest
 *
 */
public class Scope {
	/**
	 * Automatically assigned name for default scope.
	 */
	public static final String DEFAULT = "default";
	
	private final UUID id;
	private final String name;
	private final boolean isDefault;
	
	/**
	 * Private constructor for default scope.
	 * @param id
	 */
	private Scope(UUID id) {
		this.id = id;
		this.name = DEFAULT;
		this.isDefault = true;
	}
	
	/**
	 * 
	 * @param id A unique {@link UUID} for the {@link Scope}.
	 * @param name The {@link String} name of the scope.
	 */
	public Scope(UUID id, String name) {
		this.id = id;
		this.name = name;
		this.isDefault = false;
	}
	
	/**
	 * 
	 * @param id A unique {@link UUID} for the {@link Scope}.
	 * @return A scope with the {@link #DEFAULT} name.
	 */
	public static Scope getDefault(UUID id) {
		return new Scope(id);
	}
	
	/**
	 * @return The {@link String} name associated with {@link Scope}.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Alias for {@link #getName()}
	 */
	public String toString() {
		return getName();
	}
	
	/**
	 * 
	 * @return <code>True</code> if this {@link Scope} is default (has the
	 * {@link #DEFAULT} name), <code>false</code> otherwise.
	 */
	public boolean isDefault() {
		return isDefault;
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
