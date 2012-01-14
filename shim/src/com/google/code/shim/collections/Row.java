package com.google.code.shim.collections;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LinkedHashMap subclass that represents a labelled list of items returned from a database call.  This class also provides
 * accessor methods that handle type casting, where needed.
 * @author dgau
 *
 */
public class Row extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public Row() {
		super();
	}

	public Row(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public Row(int initialCapacity) {
		super(initialCapacity);
	}

	public Row(Map<? extends String, ? extends Object> anotherMap) {
		super(anotherMap);
	}

	/**
	 * Gets a String value from the row for the given key.
	 * @param key
	 * @return
	 */
	public String getString(String key){
		return (String) this.get(key);
	}
	
	/**
	 * Gets an Integer value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Integer getInteger(String key){
		return (Integer) this.get(key);
	}
	
	/**
	 * Gets a Boolean value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key){
		return (Boolean) this.get(key);
	}
	
	/**
	 * Gets a Double value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Double getDouble(String key){
		return (Double) this.get(key);
	}
	
	/**
	 * Gets a Double value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Float getFloat(String key){
		return (Float) this.get(key);
	}
	
	/**
	 * Gets a Date value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Date getDate(String key){
		return (Date) this.get(key);
	}
	
}
