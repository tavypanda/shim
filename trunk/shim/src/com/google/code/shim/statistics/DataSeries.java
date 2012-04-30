package com.google.code.shim.statistics;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Convenience class that wraps a numeric data series, providing normalization, minimum, maximum and averaging 
 * capabilities over it.
 * @author dgau
 *
 */
public class DataSeries {
	static Logger logger = LogManager.getLogger(DataSeries.class);
	
	private int pctMin;
	private int pctMax;
	private int pctAvg;
	
	private Number min;
	private Number max;
	private Number avg;
	
	private final String name;
	private final Number[] series;
	private Integer[] pctSeries;
	
	public DataSeries(String n, Number[] s){
		name = n;
		series = s;
		
		if(series!=null && series.length!=0){
			Number[] minMaxAvg = findMinMaxAvg(series);
			min=minMaxAvg[0];
			max=minMaxAvg[1];
			avg=minMaxAvg[2];
			
			pctSeries=normalizeToPercentages(series);
			
			Number[] pctMinMaxAvg = findMinMaxAvg(pctSeries);
			pctMin=Math.round(pctMinMaxAvg[0].floatValue());
			pctMax=Math.round(pctMinMaxAvg[1].floatValue());
			pctAvg=Math.round(pctMinMaxAvg[2].floatValue());
		}
	}
	
	
	/**
	 * @return the pctMin
	 */
	public int getPctMin() {
		return pctMin;
	}


	/**
	 * @return the pctMax
	 */
	public int getPctMax() {
		return pctMax;
	}


	/**
	 * @return the pctAvg
	 */
	public int getPctAvg() {
		return pctAvg;
	}


	/**
	 * @return the min
	 */
	public Number getMin() {
		return min;
	}


	/**
	 * @return the max
	 */
	public Number getMax() {
		return max;
	}


	/**
	 * @return the avg
	 */
	public Number getAvg() {
		return avg;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the raw numeric series
	 */
	public Number[] getSeries() {
		return series;
	}


	/**
	 * @return the series normalized to whole integer percentages (for chart display)
	 */
	public Integer[] getPctSeries() {
		return pctSeries;
	}

	

	/**
	 * For the given number series, this method returns an array containing the minimum, maximum and average value.  Only non-null elements are counted in the tally.
	 * @param s
	 * @return
	 */
	public static Number[] findMinMaxAvg( Number[] s) {
		Double min = Double.MAX_VALUE;
		Double max = -1*Double.MAX_VALUE;
		Double sum = 0.0;
		int points = 0;
		for (int i = 0; i < s.length; i++) {
			if (s[i] == null)
				continue;
			points++;
			if (min == null || s[i].doubleValue() < min ) {
				min = s[i].doubleValue();
			}
			if (max == null || s[i].doubleValue() > max ) {
				max = s[i].doubleValue();
			}
			sum = sum + s[i].doubleValue();
		}
		double avg = points == 0 ? 0 : sum / points;
		return new Number[]{min,max,avg};
	}
	/**
	 * Produces an integer series of by normalizing the incoming series into rounded percentages (no decimals)
	 * @param series
	 * @return
	 */
	public static Integer[] normalizeToPercentages(Number[] series) {
		Number[] minMaxAvg = findMinMaxAvg(series);
		
		if (minMaxAvg[1].doubleValue() == 0.0) {
			logger.warn("Data series has a max value = 0, cannot convert to percentages.");
			return new Integer[0];
		}

		Integer[] results = new Integer[series.length];
		for (int i = 0; i < series.length; i++) {
			if (series[i] == null) {
				results[i]=null;
			} else {
				results[i]=(int) Math.round(series[i].floatValue()/minMaxAvg[1].floatValue()*100);
			}
		}
		
		return results;
		
	}
	 
	
	
}
