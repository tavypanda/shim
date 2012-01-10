package com.google.code.shim.data;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
/**
 * Abstract base class for any kind of DAO.  Provides some convenience methods for 
 * class-level property files that can be used to store SQL, Javascript, configuration etc
 * for implementing your individual DAOs.
 * @author dgau
 *
 */
public abstract class BaseDao {

	static final Logger logger = LogManager.getLogger(BaseDao.class);
	private Properties props = null;
	 
	/**
	 * Protected constructor initializes the properties file to be associated with the DAO.  Properties file is
	 * useful for storing SQL or other data access commands, statements, or properties needed for data access.
	 * 
	 * @throws DataAccessException
	 */
	protected BaseDao() throws DataAccessException {
		try {
			InputStream propsIn = getClass().getResourceAsStream(getClass().getSimpleName() + ".properties");
			props = new Properties();
			props.load(propsIn);
			propsIn.close();
		} catch (Exception e) {
			logger.info("No properties file is being used for DAO: "+getClass().getSimpleName());
		}
	}
	
	/**
	 * Exception handling method that logs an exception, and then
	 * wraps the exception in a DataAccessException object for
	 * consistent exception handling.
	 * @param e
	 * @return a DataAccessException wrapping the specified exception.
	 */
	protected DataAccessException handleException(Exception e){
		logger.error(e.getMessage(),e);
		if(e instanceof DataAccessException){
			return (DataAccessException)e;
		} else {
			DataAccessException dae = new DataAccessException(e);
			return dae;
		}
	}
	
	/**
	 * Gets a property under the specified name. Usually used to load SQL
	 * statements stored in the corresponding property file for a DAO.
	 * 
	 * @param propertyName
	 * @return the property value
	 */
	public final String getStringProperty(String propertyName) {
		if (props == null) {
			logger.warn("No properties file has been defined.");
			return null;
		}
		return props.getProperty(propertyName);
	}

	/**
	 * Gets a property under the specified name. Usually used to load SQL
	 * statements stored in the corresponding property file for a DAO.
	 * 
	 * @param propertyName
	 * @param defaultPropertyValue
	 * @return the property value, or the default if not found
	 */
	public final String getStringProperty(String propertyName, String defaultPropertyValue) {
		if (props == null) {
			return defaultPropertyValue;
		}
		return props.getProperty(propertyName, defaultPropertyValue);
	}

	
	/**
	 * Convenience method used to determine calling method names.  This is useful for implementing method-based
	 * naming conventions without using annotations or other types of markup.
	 * 
	 * @param stackTraceLevel number of stack trace elements to iterate through to get to the method of interest.
	 * @return the name of the method in the stak trace.
	 */
	protected static final String deriveMethodNameFromStackTrace(int stackTraceLevel) {
		String callingMethodName = Thread.currentThread().getStackTrace()[stackTraceLevel].getMethodName();
		if (logger.isDebugEnabled()) {
			logger.debug("query property name: " + callingMethodName);
		}
		return callingMethodName;
	}
}
