package net.microscraper.instruction;

import java.io.IOException;

import net.microscraper.MustacheTemplate;
import net.microscraper.instruction.mixin.CanFindMany;
import net.microscraper.instruction.mixin.CanFindOne;
import net.microscraper.instruction.mixin.CanSpawnPages;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONLocation;

/**
 * {@link Instruction}s hold instructions for {@link Executable}s.
 * @author realest
 *
 */
public abstract class Instruction implements CanFindOne, CanFindMany, CanSpawnPages {
	
	/**
	 * Key for {@link #getName()} value when deserializing from JSON.
	 */
	public static final String NAME = "name";
	
	/**
	 * Key for {@link #shouldSaveValue()} value when deserializing from JSON.
	 */
	public static final String SAVE = "save";
	
	private final MustacheTemplate name;
	
	private final boolean shouldSaveValue;
	
	/**
	 * 
	 * @return Whether values resulting from the execution of this {@link Instruction}
	 * should be stored in the {@link Database}.
	 * @see #defaultShouldSaveValue()
	 */
	public boolean shouldSaveValue() {
		return shouldSaveValue;
	}
	/**
	 * 
	 * @return Whether {@link #shouldSaveValue()} should be <code>true</code>
	 * or <code>false</code> by default.
	 * @see #shouldSaveValue
	 */
	public abstract boolean defaultShouldSaveValue();
	
	private final JSONLocation location;
	
	/**
	 * 
	 * @return The {@link JSONLocation} where this {@link Instruction} is located.
	 */
	public final JSONLocation getLocation() {
		return location;
	}
	
	/**
	 * @return A {@link MustacheTemplate} attached to this particular {@link Find} {@link Instruction}.
	 * Is <code>null</code> if it has none.
	 * @see {@link #hasName}
	 */
	public final MustacheTemplate getName() {
		return name;
	}

	/**
	 * Whether this {@link Find} {@link Instruction} has a {@link #name}.
	 * @see {@link #name}
	 */
	public final boolean hasName() {
		if(getName() == null)
			return false;
		return true;
	}

	/**
	 *
	 * @param location A {@link JSONLocation} where this {@link Instruction} is located.
	 * @param name The {@link MustacheTemplate} to use as a name for this {@link Instruction}.
	 * Can be <code>null</code>.
	 */
	public Instruction(JSONLocation location, MustacheTemplate name, boolean shouldSaveValue,
			FindOne[] findOnes, FindMany[] findManys, Page[] spawnPages) {
		this.location = location;
		this.name = name;
		this.shouldSaveValue = shouldSaveValue;
		this.findOnes = findOnes;
		this.findManys = findManys;
		this.spawnPages = spawnPages;
	}

	/**
	 * {@link Instruction} can be initialized with a {@link JSONInterfaceObject}, which has a location.
	 * @param obj The {@link JSONInterfaceObject} object to deserialize.
	 * @throws DeserializationException If there is a problem deserializing <code>obj</code>
	 * @throws IOException If there is an error loading one of the references.
	 */
	public Instruction(JSONInterfaceObject obj) throws DeserializationException, IOException {
		this.location = obj.getLocation();
		try {
			if(obj.has(NAME)) {
				name = new MustacheTemplate(obj.getString(NAME));
			} else {
				name = null;
			}
			if(obj.has(SAVE)) {
				shouldSaveValue = obj.getBoolean(SAVE);
			} else {
				shouldSaveValue = this.defaultShouldSaveValue();
			}
			CanFindMany canFindMany = CanFindMany.Deserializer.deserialize(obj);
			CanFindOne  canFindOne = CanFindOne.Deserializer.deserialize(obj);
			CanSpawnPages canSpawnPages = CanSpawnPages.Deserializer.deserialize(obj);
			
			this.spawnPages = canSpawnPages.getPages();
			this.findManys = canFindMany.getFindManys();
			this.findOnes  = canFindOne.getFindOnes();
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, obj);
		}
	}

	private final FindMany[] findManys;
	public FindMany[] getFindManys() {
		return findManys;
	}

	private final FindOne[] findOnes;
	public FindOne[] getFindOnes() {
		return findOnes;
	}

	private final Page[] spawnPages;
	public Page[] getPages() throws DeserializationException, IOException {
		return spawnPages;
	}
}
