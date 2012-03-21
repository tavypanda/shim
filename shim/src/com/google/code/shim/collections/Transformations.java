package com.google.code.shim.collections;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.code.shim.collections.StringKeyMap;

public class Transformations {
	public enum AggregateFunction {
		SUM,
		MAX,
		MIN,
		AVG
	}
	public static String EMPTY_PIVOT_VALUE="(empty)";
	
	public static void sort(List<StringKeyMap> toBeSorted, final String... sortKeys){
		if(toBeSorted==null || toBeSorted.isEmpty()) return;
		Collections.sort(toBeSorted, new Comparator<StringKeyMap>(){

			@Override
			public int compare(StringKeyMap m1, StringKeyMap m2) {
				for(String key: sortKeys){
					Object v1 = m1.get(key);
					Object v2 = m2.get(key);
					int result = objCompare(v1,v2);
					if(result != 0){
						return result;
					}
				}
				return 0;
			}
			
			private final int objCompare(Object v1, Object v2){
				if(v1!=null && v2!=null){
					if(v1 instanceof Number && v2 instanceof Number){
						Number x1 = (Number) v1;
						Number x2 = (Number) v2;
						return new Double(x1.doubleValue()).compareTo(new Double(x2.doubleValue()));
					} else if (v1 instanceof Boolean && v2 instanceof Boolean){
						Boolean x1 = (Boolean) v1;
						Boolean x2 = (Boolean) v2;
						return x1.compareTo(x2);
					} else if (v1 instanceof Date && v2 instanceof Date){
						Date x1 = (Date) v1;
						Date x2 = (Date) v2;
						return x1.compareTo(x2);
					} else {
						//Otherwise do a string comparison.
						return v1.toString().compareTo(v2.toString());
					}
				} else if(v1==null && v2!=null){
					//v1 is "less"
					return -1;
				} else if(v1!=null && v2==null){
					//v2 is "less"
					return 1;
				}
				return 0;
			}
		});
	}
	
	/**
	 * Creates a map of key/values where the key is given by the keyKey attribute name and the value is given by
	 * the valueKey attribute name.  This is useful for creating cross-reference maps from returned data tables.
	 * Note that the map will contain the last value associated with the given key
	 * according to the iteration order of the data table.
	 * @param dataTable containing the data from which to build the map
	 * @param keyKey
	 * @param valueKey
	 * @return a map of the keys and values found in the table
	 */
	public static <T, V> Map<T,V> createMap(List<StringKeyMap> dataTable, String keyKey, String valueKey){
		LinkedHashMap<T,V> map = new LinkedHashMap<T,V>();
		for(StringKeyMap row: dataTable){
			
			@SuppressWarnings("unchecked")
			T  theKey = (T) row.get(keyKey);
			
			@SuppressWarnings("unchecked")
			V  theValue = (V) row.get(valueKey);
			
			map.put(theKey, theValue);
		}
		return map;
	}
	
	/**
	 * Performs "group by" functionality on a data table containing various keys and one numeric key to which an aggregate function is to be applied.
	 * The result table is a "flattened" version of the original table containing just the group by keys/values and the numericKey/aggregateValue.
	 * @param dataTable MUST be sorted in the order of the groupByKeys
	 * @param numericKey key in the data table that locates numeric data to be aggregated
	 * @param fc aggregate function to be applied
	 * @param groupByKeys the keys that you want to use to group the data.
	 * @return a modified data table containing the same keys as the original for each row, however the key specified by the numericKey will now 
	 * return a number that represents an aggregated value.
	 */
	public static List<StringKeyMap> groupBy(List<StringKeyMap> dataTable, String numericKey, AggregateFunction fc, String... groupByKeys ){
		if(dataTable==null || dataTable.isEmpty()) return Collections.emptyList();
		sort(dataTable, groupByKeys); 
		
		Object[] lastValues = null;
		Object[] currentValues = new Object[groupByKeys.length];
		Number aggregate=null;
		int countInGroup=0;
		List<StringKeyMap> result = new ArrayList<StringKeyMap>();
		
		Iterator<StringKeyMap> rowIter = dataTable.iterator();
		while(rowIter.hasNext()){
			StringKeyMap row = rowIter.next();
			
			//Populate the current values of the keys.
			for(int i=0; i<currentValues.length; i++){
				currentValues[i] = row.get(groupByKeys[i]);
			}
			if(lastValues==null){
				//Initialize on first loop
				lastValues = Arrays.copyOf(currentValues, currentValues.length);
			}
			
			boolean doBreak = !Arrays.equals(currentValues, lastValues);
			if(doBreak){
			
				StringKeyMap resultEntry = buildGroupByRecord( groupByKeys, lastValues, countInGroup, numericKey, aggregate, fc);
				 
				result.add(resultEntry);
				
				countInGroup=0;
				aggregate=null;
				
				
			}
			
			
			Number n = row.getNumber(numericKey);
			countInGroup++;
			switch(fc){
			case AVG:
			case SUM:
				if(aggregate==null){
					aggregate = n;
				} else {
					aggregate = aggregate.doubleValue() + n.doubleValue();
				}
				break;
			case MIN:
				if(aggregate==null){
					aggregate = n;
				} else if (n.doubleValue() < aggregate.doubleValue() ){
					aggregate = n;
				}
				break;
			case MAX:
				if(aggregate==null){
					aggregate = n;
				}  else if (n.doubleValue() > aggregate.doubleValue() ){
					aggregate = n;
				}
				break;
			}
			
			
			
			
			lastValues = Arrays.copyOf(currentValues, currentValues.length);
			

		}
		
		StringKeyMap resultEntry = buildGroupByRecord( groupByKeys, lastValues, countInGroup, numericKey, aggregate, fc);
		 
		result.add(resultEntry);
		
		
		return result;
	}
	
	
	private static StringKeyMap buildGroupByRecord(String[] groupByKeys, Object[] groupByValues, int countInGroup, String numericKey, Number aggregate,  AggregateFunction fc){
		StringKeyMap resultEntry = new StringKeyMap();
		Number value=null;
		switch(fc){
		case AVG:
			if(countInGroup==0){
				value = null;
			} else {
				value = aggregate.doubleValue() / countInGroup;
			}
			break;
		case SUM:
		case MIN:
		case MAX:
			value = aggregate;
			break;
		}
		
		for(int i=0; i<groupByValues.length; i++){
			resultEntry.put(groupByKeys[i], groupByValues[i]);
		}
		resultEntry.put(numericKey, value);
		
		return resultEntry;
	}
	/**
	 *  Flattens the dataTable, producing another data table having one row per each groupByKey.  Each row will then be "re-keyed"
	 * with one key each from the distinct values found in by the pivotKey parameter.   The values of each of these new keys will be calculated
	 * by performing the specified aggregate function on each value specified by the valueKey parameter.
	 * 
	 * Consider the following input table.
	 * <table>
	 * <tr><th>state</th><th>pet_type</th><th>city</th><th>owner_count</th></tr>
	 * <tr><td>IL</td><td>dog</td><td>Chicago</td><td>100</td></tr>
	 * <tr><td>IL</td><td>cat</td><td>Chicago</td><td>50</td></tr>
	 * <tr><td>IL</td><td>fish</td><td>Chicago</td><td>20</td></tr>
	 * <tr><td>IL</td><td>snake</td><td>Chicago</td><td>10</td></tr>
	 * <tr><td>IL</td><td>dog</td><td>Rockford</td><td>100</td></tr>
	 * <tr><td>IL</td><td>cat</td><td>Rockford</td><td>50</td></tr>
	 * <tr><td>IL</td><td>fish</td><td>Rockford</td><td>20</td></tr>
	 * <tr><td>IL</td><td>snake</td><td>Rockford</td><td>10</td></tr>
	 * <tr><td>IL</td><td>null</td><td>null</td><td>7000</td></tr>		  
	 * <tr><td>WI</td><td>dog</td><td>Madison</td><td>105</td></tr>
	 * <tr><td>WI</td><td>cat</td><td>Madison</td><td>30</td></tr>
	 * <tr><td>WI</td><td>fish</td><td>Madison</td><td>15</td></tr>
	 * <tr><td>WI</td><td>&nbsp;</td><td>null</td><td>2000</td></tr>
	 * <tr><td>MN</td><td>dog</td><td>St. Paul</td><td>110</td></tr>
	 * <tr><td>MN</td><td>fish</td><td>St. Paul</td><td>60</td></tr>
	 * <tr><td>MN</td><td>snake</td><td>St. Paul</td><td>5</td></tr>
	 * <tr><td>MN</td><td>null</td><td>null</td><td>1000</td></tr>
	 * </table>
	 * After the pivot function is called using <code>groupByKey=state, pivotKey=pet_type, valueKey=owner_count, fc=SUM</code>
	 * <table>
	 * <tr><th>state</th><th>dog</th><th>cat</th><th>fish</th><th>snake</th><th>(empty)</th><th>&nbsp;</th></tr>
	 * <tr><td>IL</td><td>200</td><td>100</td><td>40</td><td>10</td><td>7000</td><td>null</td></tr>
	 * <tr><td>WI</td><td>200</td><td>30</td><td>15</td><td>0</td><td>null</td><td>2000</td></tr>
	 * <tr><td>MN</td><td>110</td><td>0</td><td>60</td><td>5</td><td>1000</td><td>null</td></tr>
	 * </table>
	 * 
	 * @param dataTable
	 * @param groupByKey
	 * @param pivotKey
	 * @param valueKey
	 * @param fc
	 * @param emptyValue the value to be used when the aggregation is null or empty.  Can be null, but zero is commonly used.
	 * @return
	 */
	public static List<StringKeyMap> pivot(List<StringKeyMap> dataTable, String groupByKey, String pivotKey, String valueKey,  AggregateFunction fc, Number emptyValue  ){
		if(dataTable==null || dataTable.isEmpty()) return Collections.emptyList();
		sort(dataTable, groupByKey, pivotKey);
		//First pass, find the set of values (e.g. dog, cat, fish, snake)
		LinkedHashSet<Object> distinctValues = new LinkedHashSet<Object>();

		for(StringKeyMap row: dataTable){
			distinctValues.add( row.get(pivotKey,EMPTY_PIVOT_VALUE) );
		}
		
		Object[] pivotValueArray = new Object[distinctValues.size()];
		distinctValues.toArray(pivotValueArray);
		
		//Second pass, perform group-by and aggregate.
		List<StringKeyMap> results = new ArrayList<StringKeyMap>();
		String currentKey=null;
		String lastKey=null;
		Number[] currentValuesPerPivot = new Number[ pivotValueArray.length];
		Integer[] countsPerPivot = new Integer[ pivotValueArray.length];
		Arrays.fill(countsPerPivot, 0);
		Number[] aggValuesPerPivot = new Number[ pivotValueArray.length];
		Iterator<StringKeyMap> rowIter = dataTable.iterator();
		while(rowIter.hasNext()){
			StringKeyMap row = rowIter.next();
			currentKey = row.getString(groupByKey,"");
			
			
			if(lastKey!=null && !currentKey.equals(lastKey)){
				//Build the pivot record.
				StringKeyMap record =  buildPivotRecord(groupByKey, lastKey, pivotValueArray, aggValuesPerPivot, countsPerPivot, fc, emptyValue);
				 
				//Add to the result table.
				results.add(record);
				
				//Reset counts and aggregate values
				Arrays.fill(countsPerPivot, 0);
				Arrays.fill(aggValuesPerPivot, null);
				
			}
			
			
			Object pivotVal = row.get(pivotKey);
			for(int i=0; i<pivotValueArray.length; i++){
				Number n = row.getNumber( valueKey );
				if(pivotVal==null){
					pivotVal = EMPTY_PIVOT_VALUE;
				}
				if(pivotVal==null|| !pivotVal.equals(pivotValueArray[i])){
					
					continue;
				}
				currentValuesPerPivot[i] = n;
				if(n!=null){
					countsPerPivot[i]++;
				}
				
				switch(fc){
				case AVG:
					if(aggValuesPerPivot[i] == null){
						aggValuesPerPivot[i] = 0;
					}
					if( currentValuesPerPivot[i]!=null){
						aggValuesPerPivot[i] = aggValuesPerPivot[i].doubleValue() + currentValuesPerPivot[i].doubleValue();
					}
					
					break;
				case SUM:
					if(aggValuesPerPivot[i] == null){
						aggValuesPerPivot[i] = 0;
					}
					if( currentValuesPerPivot[i]!=null){
						aggValuesPerPivot[i] = aggValuesPerPivot[i].doubleValue() + currentValuesPerPivot[i].doubleValue();
					}
					break;
				case MIN:
					if(currentValuesPerPivot[i]!=null && aggValuesPerPivot[i] == null ){
						aggValuesPerPivot[i] = currentValuesPerPivot[i];	
					} else 	if( currentValuesPerPivot[i]!=null && currentValuesPerPivot[i].doubleValue() < aggValuesPerPivot[i].doubleValue()){
						aggValuesPerPivot[i] = currentValuesPerPivot[i];
					}
					System.out.println(pivotValueArray[i] + " min is " + aggValuesPerPivot[i]);
					
					break;
				case MAX:
					if(currentValuesPerPivot[i]!=null && aggValuesPerPivot[i] == null ){
						aggValuesPerPivot[i] = currentValuesPerPivot[i];
					} else 	if( currentValuesPerPivot[i]!=null && currentValuesPerPivot[i].doubleValue() > aggValuesPerPivot[i].doubleValue()){
						aggValuesPerPivot[i] = currentValuesPerPivot[i];
					}
					break;
				}
			}
			
			
			lastKey = currentKey;
			
		}
		
		//Build the last pivot record.
		StringKeyMap lastRecord = buildPivotRecord(groupByKey, currentKey, pivotValueArray, aggValuesPerPivot, countsPerPivot, fc, emptyValue);
		 
		//Add to the result table.
		results.add(lastRecord);
		
		return results;
	}
	
	/**
	 * Internal method for outputting pivot rows when the groupByKey changes.
	 * @param groupByKey
	 * @param keyValueToUse
	 * @param pivotValueArray
	 * @param aggValuesPerPivot
	 * @param countsPerPivot
	 * @param fc
	 * @return
	 */
	private static StringKeyMap buildPivotRecord(String groupByKey, String keyValueToUse, Object[] pivotValueArray, Number[] aggValuesPerPivot, Integer[] countsPerPivot, AggregateFunction fc, Number emptyValue){
		StringKeyMap record = new StringKeyMap();
		record.put(groupByKey, keyValueToUse);
		
		for(int i=0; i<pivotValueArray.length; i++){
			
			if(aggValuesPerPivot[i] == null){
				record.put(pivotValueArray[i].toString(), emptyValue);
			} else {
				Number value = null;
				switch(fc){
				case AVG:
					if(aggValuesPerPivot[i] != null && countsPerPivot[i] > 0){
						value = aggValuesPerPivot[i].doubleValue()/countsPerPivot[i].doubleValue();
						record.put(pivotValueArray[i].toString(), value);
					}
					break;
				case SUM:
				case MIN:	
				case MAX:
					if(aggValuesPerPivot[i] != null){
						value = aggValuesPerPivot[i].doubleValue();
						record.put(pivotValueArray[i].toString(), value);
					} 
				}
			}
			
			
		} 
		return record;
		
	}
	/**
	 * Returns the distinct values across the whole data table for the given table key.
	 * @param <T>
	 * @param dataTable
	 * @param tableKey
	 * @return the array, sorted in natural ascending order.
	 */
	public static Object[] distinctValuesFrom(List<StringKeyMap> dataTable, String tableKey ){
		LinkedHashSet<Object> vals = new LinkedHashSet<Object>();
		for(StringKeyMap row: dataTable){
			vals.add( row.get(tableKey,EMPTY_PIVOT_VALUE));
		}
	
		Object[] result = vals.toArray(new Object[vals.size()]);
		Arrays.sort(result);
		return result;
	}
	
	
	public static final void main(String[] args) {
		try {
			String filename = null;
			for (String arg : args) {
				if (arg.startsWith("-filename=")) {
					filename = arg.substring(arg.indexOf("=") + 1);
				}
			}
			
			Object[][] tbl = new Object[][]{
				  new Object[]{"IL","dog","Chicago",100},
				  new Object[]{"IL","cat","Chicago",50},
				  new Object[]{"IL","fish","Chicago",20},
				  new Object[]{"IL","snake","Chicago",10},
				  new Object[]{"IL","dog","Rockford",100},
				  new Object[]{"IL","cat","Rockford",50},
				  new Object[]{"IL","fish","Rockford",20},
				  new Object[]{"IL","snake","Rockford",10},
				  new Object[]{"IL",null,null,7000},
				  new Object[]{"WI","dog","Madison",105},
				  new Object[]{"WI","cat","Madison",30},
				  new Object[]{"WI","fish","Madison",15},
				  new Object[]{"WI","",null,2000},
				  new Object[]{"MN","dog","St Paul",110},
				  new Object[]{"MN","fish","St Paul",60},
				  new Object[]{"MN","snake","St Paul",5},
				  new Object[]{"MN",null,null,1000},
				  
				  
				};
			
			
			List<StringKeyMap> table = new ArrayList<StringKeyMap>();
			for(Object[] r: tbl){
				StringKeyMap map = new StringKeyMap();
				map.put("state", r[0]);
				map.put("pet_type",  r[1]);
				map.put("city",  r[2]);
				map.put("owner_count", r[3]);
				
				table.add(map);
			}
			
			System.out.println("Group By Results:");
			List<StringKeyMap> gbResults = Transformations.groupBy(table, "owner_count", AggregateFunction.SUM, "state");
			boolean first = true;
			for(StringKeyMap row: gbResults){
				if(first){
					for(String k: row.keySet()){
						System.out.print(k+"\t");
					}
					System.out.print("\n");
					first=false;
				}
				for(String k: row.keySet()){
					System.out.print(row.get(k)+"\t");
				}
				System.out.print("\n");
			}
			
			
			
			//Do the pivot.
			System.out.println("Pivot Results:");
			List<StringKeyMap> results = Transformations.pivot(table, "state", "pet_type", "owner_count", AggregateFunction.SUM, 0);
			first = true;
			for(StringKeyMap row: results){
				if(first){
					for(String k: row.keySet()){
						System.out.print(k+"\t");
					}
					System.out.print("\n");
					first=false;
				}
				for(String k: row.keySet()){
					System.out.print(row.get(k)+"\t");
				}
				System.out.print("\n");
			}
			
			System.exit(0);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	
}
