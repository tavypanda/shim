package com.google.code.shim.data.sql.handler;

import java.sql.SQLException;

/**
 * Maps types from the ResultSet into other types (for example Clobs to Strings, BigDecimal to Double) where convenient.
 * 
 * @author dgau
 * 
 */
public interface ResultSetTypeMapper {
	/**
	 * Maps values
	 * 
	 * @param sqlType
	 *            - specified sql type of the object. Typically provided by the ResultSetMetadata. See
	 *            {@link java.sql.Types} for more info.
	 * @param fromValue
	 * @return a new object of (possibly) a different type.
	 */
	public Object mapValue(int sqlType, Object fromValue) throws SQLException;

}
