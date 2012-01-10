package com.google.code.shim;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Properties helper class that extends the basic properties capabilites and 
 * provides methods that return data types instead of simple strings.
 * @author dgau
 *
 */
public class PropertiesHelper {

	static final Logger logger = LogManager.getLogger(PropertiesHelper.class);
	protected final Properties props;
	/**
	 * Creates a PropertiesHelper for the specified Properties collection.
	 * @param propertiesToUse
	 */
	public PropertiesHelper(Properties propertiesToUse){
		props = propertiesToUse;
	}
	/**
	 * Creates a PropertiesHelper by locating a properties resource on the classpath.
	 * @param resourceName
	 * @throws IOException
	 */
	public PropertiesHelper(String resourceName) throws IOException{
		props = loadProperties(resourceName);
	}
	/**
	 * Loads and returns a properties instance from a resource (file) name.
	 * @param propertiesResourceName
	 * @return a loaded Properties reference
	 * @throws IOException
	 */
	public static Properties loadProperties(String propertiesResourceName) throws IOException {
		
		InputStream propsIn = PropertiesHelper.class.getResourceAsStream(propertiesResourceName);
		Properties properties = new Properties();
		properties.load(propsIn);
		propsIn.close();
		return properties;
	}
	/**
	 * Returns a string property
	 * @param propName
	 * @return the property
	 * @throws IllegalArgumentException when the property is not found.
	 */
	public String getStringProperty(String propName ){
		String s = props.getProperty(propName, null);
		if(s==null) throw new IllegalArgumentException("property not found: " + propName);
		return s;
	}
	/**
	 * Returns a string property
	 * @param propName
	 * @param defaultValue
	 * @return the property
	 * 
	 */
	public String getStringProperty(String propName, String defaultValue){
		return props.getProperty(propName, defaultValue);
	}
	/**
	 * Returns an int property
	 * @param propName
	 * @param defaultValue
	 * @return  the property
	 */
	public int getIntProperty(String propName, int defaultValue){
		String s = getStringProperty(propName, null);
		if(s==null){
			return defaultValue;
		}
		return Integer.parseInt(s);
	}
	/**
	 * Return an int property
	 * @param propName
	 * @return  the property
	 * @throws IllegalArgumentException when the property is not found.
	 */
	public int getIntProperty(String propName ){
		String s = props.getProperty( propName );
		if(s==null) throw new IllegalArgumentException("property not found: " + propName);
		return Integer.parseInt(s);
	}
	/**
	 * Returns a double property
	 * @param propName
	 * @param defaultValue
	 * @return the property
	 */
	public double getDoubleProperty(String propName, double defaultValue){
		String s = getStringProperty(propName, null);
		if(s==null){
			return defaultValue;
		}
		return Double.parseDouble(s);
	}
	/**
	 * Return a double property
	 * @param propName
	 * @return  the property
	 * @throws IllegalArgumentException when the property is not found.
	 */
	public double getDoubleProperty(String propName){
		String s = props.getProperty( propName );
		if(s==null) throw new IllegalArgumentException("property not found: " + propName);
		return Double.parseDouble(s);
	}
	/**
	 * Returns a boolean property
	 * @param propName
	 * @param defaultValue
	 * @return the property
	 */
	public boolean getBooleanProperty(String propName, boolean defaultValue){
		String s = getStringProperty(propName, null);
		if(s==null){
			return defaultValue;
		}
		return Boolean.parseBoolean(s);
	}
	/**
	 * Return a boolean property
	 * @param propName
	 * @return  the property
	 * @throws IllegalArgumentException when the property is not found.
	 */
	public boolean getBooleanProperty(String propName){
		String s = props.getProperty( propName );
		if(s==null) throw new IllegalArgumentException("property not found: " + propName);
		return Boolean.parseBoolean(s);
	}
}
