package com.google.code.shim.collections;
 
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * Convenience class for working with maps.
 * @author dgau
 *
 */
public class MapUtil {
	
	/**
	 * Convenience method for handle typed retrieval of map values.  This method returns a default value when a value is not found.
	 * @param map, must have String-based keys.
	 * @param key the key for which data is being retrieved.
	 * @param defaultWhenNull a default value to return when the value is not found.
	 * @return
	 */
	public static <T> T getValue(Map<? extends String, ? extends Object>map,  String key, T defaultWhenNull){
		@SuppressWarnings("unchecked")
		T v = (T) map.get(key);
		if(v==null) return defaultWhenNull;
		return v;
	}
	/**
	 * Convenience method for handle typed retrieval of map values.  This method returns null when a value is not found.
	 * @param map
	 * @param key
	 * @return
	 */
	public static <T> T getValue(Map<? extends String, ? extends Object>map, String key){
		return getValue(map, key, null); 
	}

	/**
	 * Builds an intersection of the two maps.  Matches the keys in each map. When matching keys are found
	 * the first map's value and the second map's value are added to an object array.  The resultant map 
	 * contains the intersection of the two maps.
	 * @param map1
	 * @param map2
	 * @return a map containing the intersection of the two maps - the values from each map are provided in the value array for
	 * each intersecting key.
	 */
	public static Map<String, Object[]> intersection(Map<String,Object> map1, Map<String,Object> map2){
		Map<String,Object[]> intersectionMap = new LinkedHashMap<String,Object[]>();
		for(String key1: map1.keySet()){
			if(map2.containsKey(key1)){
				Object val1 = map1.get(key1);
				Object val2 = map2.get(key1);
				intersectionMap.put(key1, new Object[]{val1, val2} );
			}
		}
		return intersectionMap;
	}
}
