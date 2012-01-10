package com.google.code.shim.data.sql;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates SQL dialect-specific settings needed by the framework.
 * @author dgau
 *
 */
public final class DialectInfo extends Properties{

	private static final long serialVersionUID = 1L;
	public static final String GENERIC_DIALECT = "generic";

	private String dialectName;
	
	public DialectInfo()throws IOException{
		super();
		this.dialectName=GENERIC_DIALECT;
		InputStream in = getClass().getResourceAsStream(getClass().getSimpleName() + ".properties");
		load(in);
		in.close();
		
	}
	
	public DialectInfo(String dialectName) throws IOException{
		super();
		this.dialectName = dialectName;
		InputStream in = getClass().getResourceAsStream(getClass().getSimpleName() + ".properties");
		load(in);
		in.close();
	}
	
	/**
	 * Gets readable dialect-specific description of the SQLState.
	 * @param sqlState
	 * @return a description of the SQL state
	 */
	public String getSQLStateDescription(String sqlState){
		String key = "sqlstate."+ dialectName + "."+sqlState;
		return getProperty(key, null);
	}
	
	/**
	 * Tests whether the given SQLState matches a state where the database is not available
	 * for some reason.
	 * @param sqlState
	 * @return a boolean indicating whether the database is unavailable based on the sql state value.
	 */
	public boolean isDatabaseUnavailableSQLState(String sqlState){
		if(sqlState==null) return false;
		
		String key = "unavailable.sqlstates."+ dialectName;
		String states = getProperty(key);
		String[] statesArray = states.split(",");
		for(String s: statesArray){
			if(sqlState.equalsIgnoreCase(s)) return true;
		}
		
		//Check standard class codes
		if(sqlState.startsWith("08"))	{
			return true;
		}
		return false;
	}
	
}
