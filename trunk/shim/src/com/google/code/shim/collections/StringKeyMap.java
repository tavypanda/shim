package com.google.code.shim.collections;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a LinkedHashMap subclass that is simply a map with String keys, which is a very, very common use case.
 * As such, this class is syntactic sugar and can be used for any map that represents a labelled list of items.  
 * This class also provides get methods that handle common type casting, where needed.
 * @author dgau
 *
 */
public class StringKeyMap extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public StringKeyMap() {
		super();
	}

	public StringKeyMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public StringKeyMap(int initialCapacity) {
		super(initialCapacity);
	}

	public StringKeyMap(Map<? extends String, ? extends Object> anotherMap) {
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
	public String getString(String key, String defaultWhenNull){
		String s = getString(key);
		if(s==null) return defaultWhenNull;
		return s;
	}
	
	/**
	 * Gets an Integer value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Integer getInteger(String key){
		return (Integer) this.get(key);
	}
	public Integer getInteger(String key, Integer defaultWhenNull){
		Integer v = (Integer) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	/**
	 * Gets a Boolean value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key){
		return (Boolean) this.get(key);
	}
	public Boolean getBoolean(String key, Boolean defaultWhenNull){
		Boolean v = (Boolean) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	/**
	 * Gets a Number  from the row for the given key.
	 * @param key
	 * @return
	 */
	public Number getNumber(String key){
		return (Number) this.get(key);
	}
	public Number getNumber(String key, Number defaultWhenNull){
		Number v = (Number) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	
	
	/**
	 * Gets a Double value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Double getDouble(String key){
		return (Double) this.get(key);
	}
	public Double getDouble(String key, Double defaultWhenNull){
		Double v = (Double) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	/**
	 * Gets a Double value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Float getFloat(String key){
		return (Float) this.get(key);
	}
	public Float getFloat(String key, Float defaultWhenNull){
		Float v = (Float) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	/**
	 * Gets a Date value from the row for the given key.
	 * @param key
	 * @return
	 */
	public Date getDate(String key){
		return (Date) this.get(key);
	}
	public Date getDate(String key, Date defaultWhenNull){
		Date v = (Date) this.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	
}
