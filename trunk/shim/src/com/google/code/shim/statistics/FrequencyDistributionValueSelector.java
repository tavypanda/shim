package com.google.code.shim.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that provides the ability to select values according to a frequency distribution in a randomized fashion.  
 * 
 * @author dgau
 * 
 */
public class FrequencyDistributionValueSelector<T extends Object> {

	private int totalFrequency;
	private final ArrayList<CumuFrequency> frequencies;
	private final Random random = new Random();

	/**
	 * Create with a JSONObject. The object should contain a JSONArray under the "values" key. This
	 * JSONArray should have object elements with the following keys:
	 * <ul>
	 * <li><code>"value": <i>the value to assign</i></code></li>
	 * <li><code>"frequency": <i>the relative frequency, given as an integer</i></code></li>
	 * </ul>
	 * 
	 * @param objectWithValuesAndFrequencies
	 * @throws JSONException
	 */
	public FrequencyDistributionValueSelector(JSONObject objectWithValuesAndFrequencies) throws JSONException {
		JSONArray valuesArray = objectWithValuesAndFrequencies.getJSONArray("values");
		this.totalFrequency = 0;
		frequencies = new ArrayList<CumuFrequency>();
		for (int i = 0; i < valuesArray.length(); i++) {
			JSONObject valueFrequency = valuesArray.getJSONObject(i);
			@SuppressWarnings("unchecked")
			T valueId = (T) valueFrequency.get("value");
			int frequency = valueFrequency.getInt("frequency");
			totalFrequency = totalFrequency + frequency;
			frequencies.add(new CumuFrequency(totalFrequency, valueId));
		}
	}

	/**
	 * Create using value and relative frequency arrays.
	 * 
	 * @param values
	 *            you want to generate over
	 * @param relativeFrequencies
	 *            relative frequency corresponding to each value in the value map.
	 */
	public FrequencyDistributionValueSelector(T[] values, Integer[] relativeFrequencies) {
		this.totalFrequency = 0;
		frequencies = new ArrayList<CumuFrequency>();
		for (int i = 0; i < values.length; i++) {
			T valueId = values[i];
			int frequency = relativeFrequencies[i];
			totalFrequency = totalFrequency + frequency;
			frequencies.add(new CumuFrequency(totalFrequency, valueId));
		}
	}

	/**
	 * Create with a Map. The map must be of the form:
	 * <ul>
	 * <li>Map key: <i>the value to assign</i></li>
	 * <li>Map value: <i>the relative frequency, given as an integer</i></li>
	 * </ul>
	 * 
	 * @param valueAndFrequencyMap
	 */
	public FrequencyDistributionValueSelector(Map<T, Integer> valueAndFrequencyMap) {
		this.totalFrequency = 0;
		frequencies = new ArrayList<CumuFrequency>();
		for (T valueKey : valueAndFrequencyMap.keySet()) {
			Integer frequency = valueAndFrequencyMap.get(valueKey);
			totalFrequency = totalFrequency + frequency;
			frequencies.add(new CumuFrequency(totalFrequency, valueKey));
		}
	}

	/**
	 * Using a randomized frequency distribution, select the value.
	 * 
	 * @return
	 * @throws Exception
	 */
	public T select() {
		int rndValue = random.nextInt(totalFrequency+1);

		// Pick the cumu frequency nearest, but greater than the number.
		for (CumuFrequency f : frequencies) {
			if (rndValue <= f.cumulativeFrequency) {
				return f.theValue;
			}
		}
		//should never happen...
		throw new RuntimeException("Unexpected value outside frequency range: " + rndValue);

	}

	/**
	 * Holds the cumulative frequncy-to-value mapping.
	 * 
	 * @author dgau
	 * 
	 */
	private class CumuFrequency {
		int cumulativeFrequency;
		T theValue;

		public CumuFrequency(int cumulativeFreq, T value) {
			this.cumulativeFrequency = cumulativeFreq;
			this.theValue = value;
		}
		
		public String toString(){
			return "frequency" + " : " +cumulativeFrequency + ", value" + " : " + theValue.toString();
		}

	}

	/**
	 * 
	 * @param args
	 */
	public static final void main(String[] args){
		if(args.length==0){
			test();
		}
		System.exit(0);
	}
	
	public static final void test(){
		try{
			String[] values = new String[]{"dogs","cats","fish","hamsters","chickens","pigs"};
			Integer[] freqs = new Integer[]{50,30,10,5,4,1};
			Integer[] valueCounter = new Integer[values.length]; 
			Arrays.fill(valueCounter, 0);
			
			FrequencyDistributionValueSelector<String> g = new FrequencyDistributionValueSelector<String>(values, freqs);
			int howMany = 100;
			System.out.println("Generating "+ howMany +" values:");
			
			for(int i=0; i<howMany; i++){
				
				String v = g.select();
				System.out.println( v );
				for(int j=0; j<values.length; j++){
					if(v.equals(values[j])){
						//count it.
						valueCounter[j]++;
					}
				}
			}
			
			System.out.println("Distribution was: ");
			for(int i=0;i<values.length; i++){
				System.out.println(values[i] + " = " + valueCounter[i]);
			}
			
		} catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			
		}
	}
}
